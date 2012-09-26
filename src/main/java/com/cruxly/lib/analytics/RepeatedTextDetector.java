package com.cruxly.lib.analytics;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import com.cruxly.lib.model.EmailMessage;


public class RepeatedTextDetector {
	public static int MIN_DOCUMENTS_IN_MODEL = 10000;
	public static long MAX_DOCUMENT_IN_MODEL_AGE_DAYS = 90; 

	public RepeatedTextDetector() {
	}
	
	
	public  RepeatedTextModel trainModel (EmailMessage[] inbox, EmailMessage [] outbox) {
		RepeatedTextModel rt = new RepeatedTextModel();
		
		for (int i=0; i<inbox.length; i++)
			updateModel (rt, inbox[i], true);


		return rt;
	}
	
	public void updateModel (RepeatedTextModel model, EmailMessage newMessage, boolean inbox) {
		if (!inbox)
			return;
		
		model.trimModel();

		
		model.updateModel(newMessage);
	}

	public static TextSegment[] getIgnoreRegions(EmailMessage email, String text, RepeatedTextModel rtm) {
		return rtm.getIgnoreRegions(email, text);
	}

	public static class DocumentMetadata {
		public String id;
		public long timestamp;
		public boolean inbox;
	
		public DocumentMetadata (String id, Date d, boolean in) {
			this.id = id;
			this.timestamp = d.getTime();
			this.inbox = in;
		}
		
		public DocumentMetadata (java.io.DataInputStream in) throws java.io.IOException {
			this.id = in.readUTF();
			this.timestamp = in.readLong();
			this.inbox = in.readBoolean();
		}
		
		
		public void serialize(java.io.DataOutputStream out) throws java.io.IOException {
			out.writeUTF(id);
			out.writeLong(timestamp);
			out.writeBoolean(inbox);		
		}
		
		public int hashCode() {
			return id.hashCode();
		}
		
		public boolean equals (Object o) {
			return ((o instanceof DocumentMetadata) && ((DocumentMetadata)o).id.equals(this.id));
		}
	}
	
	public static class RepeatedTextModel {
		protected Hashtable mapEmailToRepeatedText;
		public static final int MIN_LINE_LENGTH = 20;
		private static final int serializedVersion = 6;
		private Hashtable messageIdsTrainedOn = new Hashtable();
		protected static final char CONCATENATE_TOKENS_SYMBOL = ' ';
		private long lastTrimTimestamp = 0;


		protected RepeatedTextModel() {
			mapEmailToRepeatedText = new Hashtable();
		}

		public RepeatedTextModel (java.io.DataInputStream in) throws java.io.IOException {
			mapEmailToRepeatedText = new Hashtable();

			int version = in.readInt();
			if (version != serializedVersion)
				throw new java.io.IOException ("Incorrect version "+version+", expectiong "+serializedVersion);

			int numIds = in.readInt();
			String [] docIds = new String[numIds];
			
			for (int i=0; i<numIds; i++) {
				DocumentMetadata dm = new DocumentMetadata(in);
				docIds[i] = dm.id;
				messageIdsTrainedOn.put(dm.id, dm);
			}

			int num = in.readInt();
			for (int i=0; i<num; i++) {
				String key = in.readUTF();
				RepeatedText t = new RepeatedText(in, docIds);
				mapEmailToRepeatedText.put(key, t);
			}
		}


		
		protected void removeDocument(DocumentMetadata doc) {
			messageIdsTrainedOn.remove(doc.id);
		}
		
		// Convert common token groups (e.g. 1234) into a generic mask (e.g. "__NUMBER__")
		protected static String maskTokens(String src) {

			String [] tokens = StringUtils.splitBySeparator(src, ' ');

			for (int j=0; j<tokens.length; j++) {
				String token = tokens[j];
				token = trimPunctuation(token);
				
				if (tokens[j].length() == 0)
					continue;
				else if (TokenCategories.isDOW(token)) {
					tokens[j] = "__DOW__";
				} else if (TokenCategories.isMonth(token)) {
					tokens[j] = "__MONTH__";
				} else if (TokenCategories.isURL(token)) {
					tokens[j] = "__URL__";
				} else if (TokenCategories.isEmail(token)) {
					tokens[j] = "__EMAIL__";
				} else if (TokenCategories.isNumber(token)) {
					tokens[j] = "__NUMBER__";
				} else if (TokenCategories.isAlphaNum(token)) {
					tokens[j] = "__ALPHANUM__";
				}
			}
			
			return StringUtils.join(tokens, CONCATENATE_TOKENS_SYMBOL);
		}
		
		protected static String trimPunctuation(String s) {
			int startIdx = 0;
			int endIdx = s.length() - 1;
			if (endIdx < 0)
				return "";
			
			while ((startIdx < endIdx) && (StringUtils.isPunc(s.charAt(startIdx))))
				startIdx++;
			while ((endIdx > startIdx) && (StringUtils.isPunc(s.charAt(endIdx))))
				endIdx--;
			
			return s.substring(startIdx, endIdx);
		}
		
		public void serialize(java.io.DataOutputStream out) throws java.io.IOException {
			
			out.writeInt (serializedVersion);

			out.writeInt(messageIdsTrainedOn.size());
			Hashtable id2idx = new Hashtable();
			int docCount = 0;
			Enumeration enumVar = messageIdsTrainedOn.elements();
			while (enumVar.hasMoreElements()) {
				DocumentMetadata doc = (DocumentMetadata)enumVar.nextElement();
				int idx = docCount++;
				id2idx.put(doc.id, new Integer(idx));
				doc.serialize(out);
			}

			out.writeInt (mapEmailToRepeatedText.size());
			enumVar = mapEmailToRepeatedText.keys();
			while (enumVar.hasMoreElements()) {
				String fromAddr = (String)enumVar.nextElement();
				RepeatedText t = (RepeatedText)mapEmailToRepeatedText.get(fromAddr);
				out.writeUTF(fromAddr);
				t.serialize(out, id2idx);
			}
		}
		

		protected void updateModel (EmailMessage email) {
			if (email.messageId != null) {
				if (messageIdsTrainedOn.containsKey(email.messageId))
					return;
				messageIdsTrainedOn.put(email.messageId, new DocumentMetadata(email.messageId, email.date, true));
			}

			// Ignore emails that are re: or fwd:
			if (email.subject != null) {
				String slc = email.subject.toLowerCase().trim();
				if (slc.startsWith("re:") || slc.startsWith("fwd:"))
					return;
			}

			RepeatedText rt = null;
			String [] fromAddr = StringUtils.extractEmailAddresses(email.from);
			if (fromAddr.length < 1)
				return;

			if (mapEmailToRepeatedText.containsKey(fromAddr[0])) {
				rt = (RepeatedText)mapEmailToRepeatedText.get(fromAddr[0]);
			} else {
				rt = new RepeatedText();
				mapEmailToRepeatedText.put(fromAddr[0], rt);
			}

			String emailContent = StringUtils.getEmailContent(email);
			rt.processText(emailContent, email.messageId);
		}

		public void trimModel() {
			long nowTimestamp = System.currentTimeMillis();
			if ((nowTimestamp - lastTrimTimestamp) < 1000L * 3600L)
				return;
			
			Set toDelete = new Set();
			
			Enumeration enumVar = this.messageIdsTrainedOn.elements();
			while (enumVar.hasMoreElements()) {
				DocumentMetadata doc = (DocumentMetadata)enumVar.nextElement();
				if ((nowTimestamp - doc.timestamp) > 1000L * 3600L * MAX_DOCUMENT_IN_MODEL_AGE_DAYS )
					toDelete.addElement(doc);
			}
			
			enumVar = toDelete.elements();
			while (enumVar.hasMoreElements()) {
				DocumentMetadata doc = (DocumentMetadata)enumVar.nextElement();
				messageIdsTrainedOn.remove(doc);
			}
			
			lastTrimTimestamp = nowTimestamp;
		}

		protected TextSegment[] getIgnoreRegions(EmailMessage email, String text) {
			String [] fromAddr = StringUtils.extractEmailAddresses(email.from);
			if (fromAddr.length < 1)
				return null;

			RepeatedTextModel.RepeatedText rt = (RepeatedTextModel.RepeatedText)mapEmailToRepeatedText.get(fromAddr[0]);
			if (rt == null)
				return null;

			return rt.getIgnoreRegions(email, text);

		}
		
		protected class RepeatedText {
			protected int numEncountered = 0;
			protected Hashtable line2DocIds = new Hashtable();
			protected long lastProcessedTime = 0L;

			protected RepeatedText() {
			}

			protected RepeatedText (java.io.DataInputStream in, String[] docIds) throws java.io.IOException {
				numEncountered = in.readInt();
				lastProcessedTime = in.readLong();

				int num = in.readInt();
				for (int i=0; i<num; i++) {
					String key = in.readUTF();
					int count = in.readInt();
					
					Set s = new Set();
					for (int j=0; j<count; j++) {
						int idx = in.readInt();
						s.addElement(docIds[idx]);
					}
					line2DocIds.put(key, s);
				}
			}

			protected void processText(String text, String docId) {
				String [] linesNew = StringUtils.splitByNewlines(text);

				for (int i=0; i<linesNew.length; i++) {
					String line = linesNew[i];

					if (line.length() < MIN_LINE_LENGTH)
						continue;

					line = maskTokens(line);
					if (line.length() < MIN_LINE_LENGTH) // The length may have shrunk after masking
						continue;

					if (line2DocIds.containsKey(line)) {
						Set s = (Set)line2DocIds.get(line);
						s.addElement(docId);
					} else {
						Set s = new Set();
						s.addElement(docId);
						line2DocIds.put(line, s);
					}
				}


				numEncountered ++;
				lastProcessedTime = System.currentTimeMillis();
			}

			protected boolean containsLine(String line) {
				line = maskTokens(line);

				if (!line2DocIds.containsKey(line))
					return false;
				
				Set s = (Set)line2DocIds.get(line);
				int count = 0;
				
				Enumeration enumVar = s.elements();
				while (enumVar.hasMoreElements()) {
					String id = (String)enumVar.nextElement();
					if (messageIdsTrainedOn.containsKey(id))
						if (++count > 2)
							return true;
				}
				return false;
				
			}



			protected TextSegment[] getIgnoreRegions(EmailMessage email,
					String text) {
				String[] lines = StringUtils.splitByNewlines(text);

				Vector ignoreRegions = new Vector();
				int startIdx = 0;
				for (int i=0; i<lines.length; i++) {
					String line = lines[i];

					if ((line.length() >= MIN_LINE_LENGTH) && (containsLine(line))) { 
						ignoreRegions.addElement(new TextSegment(text, startIdx, startIdx + line.length()));
					}

					startIdx += line.length()+1;

				}

				if (ignoreRegions.size() == 0)
					return null;

				TextSegment []result = new TextSegment[ignoreRegions.size()];
				ignoreRegions.copyInto(result);
				return result;
			}


			

			protected void serialize(java.io.DataOutputStream out, Hashtable docId2idx) throws java.io.IOException {
				out.writeInt(this.numEncountered);
				out.writeLong(this.lastProcessedTime);

				out.writeInt(line2DocIds.size());
				Enumeration enumVar = line2DocIds.keys();
				while (enumVar.hasMoreElements()) {
					String line = (String)enumVar.nextElement();

					// Convert email ids to integer indicies
					// This also removes outdated document ids, that are no longer in the model
					Set s = (Set)line2DocIds.get(line);
					Set s2 = new Set();
					Enumeration enumDocs = s.elements();
					while (enumDocs.hasMoreElements()) {
						String id = (String)enumDocs.nextElement();
						Integer idx = (Integer)docId2idx.get(id);
						if (idx != null)
							s2.addElement(idx);
					}
					
					// Output line and the list of integers
					int count = s2.size();
					out.writeUTF(line);
					out.writeInt(count);
					Enumeration enumIdxs = s2.elements();
					while (enumIdxs.hasMoreElements()) {
						Integer idx = (Integer)enumIdxs.nextElement();
						out.writeInt(idx.intValue());
					}
				}
			}

		}	

	}

}
