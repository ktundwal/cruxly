/* Copyright (c) 2010 Cruxly Inc.
 */

package com.cruxly.lib.analytics;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import com.cruxly.lib.model.EmailMessage;


public class ConversationAnalysis {

	public static long MAX_DOCUMENT_IN_MODEL_AGE_DAYS = 90; 
	public static int MIN_DOCUMENTS_IN_MODEL = 10000;
	private static final long INVALID_TIME = -1;
	
	public ConversationAnalysis() {
	}

	public ConversationModel trainModel (EmailMessage[] inbox, EmailMessage [] outbox) {

		ConversationModel model = new ConversationModel();

		// Go through the inbox
		for (int i=0; i<inbox.length; i++) {
			updateModel (model, inbox[i], true);
		}

		// Go through the outbox
		for (int i=0; i<outbox.length; i++) {
			updateModel (model, outbox[i], false);
		}
		
		
		return model;
	}
	
	
	public void updateModel(ConversationModel model, EmailMessage newMessage, boolean incoming)  {

		model.trimModel();
		
		DocumentMetadata docMetadata = new DocumentMetadata(newMessage.messageId, newMessage.date, incoming);
		model.addDocument(docMetadata);

		if (incoming) {
			String[] emails = StringUtils.extractEmailAddresses(newMessage.from);
			String fromAddress = null;
			if (emails.length >= 1) {
				fromAddress = emails[0];
			} else {
				throw new RuntimeException ("Invalid form address "+newMessage.from);
			}

			model.updateContactStats(fromAddress, docMetadata, incoming);

		} else {
			for (int j=0; j<newMessage.to.length; j++) {
				String[] emails = StringUtils.extractEmailAddresses(newMessage.to[j]);
				String toAddress = null;
				if (emails.length >= 1) {
					toAddress = emails[0];
				} else {
					throw new RuntimeException ("Invalid to address "+newMessage.to[j]);
				}
	
				model.updateContactStats(toAddress, docMetadata, incoming);
			}			
		}
		
	}

	public float rateDocumentImportance(EmailMessage doc, ConversationModel model) {
		if ((model == null) || (model.totalCountOut == 0))
			return 0.0f;

		String[] emails = StringUtils.extractEmailAddresses(doc.from);
		String fromAddress = null;
		if (emails.length >= 1) {
			fromAddress = emails[0];
		} else {
			throw new RuntimeException ("Invalid email address "+ doc.from);
		}

		
		if (!model.contains(fromAddress))
			return 0.0f;
		
		long lastMessageTimestamp = model.getLastTimestamp(fromAddress);
		long oldestTimestamp  = model.getOldestTimestamp();
		if (lastMessageTimestamp == INVALID_TIME)
			return 0.0f;
		
		// The score is higher the more recently the user has written to the send of this email
		float result = ((float)lastMessageTimestamp - oldestTimestamp)  /  ((float)System.currentTimeMillis() - oldestTimestamp);
		if (result > 1.0f)
			result = 1.0f;
		if (result < 0.0f)
			result = 0.0f;
	
		
		return result;
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

	public static class ConversationModel {
		protected int totalCountIn = 0;
		protected int totalCountOut = 0;
		private Hashtable contactStats = new Hashtable();
		private Hashtable documentsIds = new Hashtable();
		private static final int serializedVersion = 8;
		private long lastTrimTimestamp = 0;
		
		protected ConversationModel() {
		}
		
		public long getOldestTimestamp() {
			long timestamp = System.currentTimeMillis();
			
			Enumeration enumVar = documentsIds.elements();
			while (enumVar.hasMoreElements()) {
				DocumentMetadata dm = (DocumentMetadata)enumVar.nextElement();
				if (dm.timestamp < timestamp)
					timestamp = dm.timestamp;
			}
			return timestamp;
		}	

		public boolean contains(String fromAddress) {
			return contactStats.containsKey(fromAddress);
		}

		public void addDocument(DocumentMetadata doc) {
			documentsIds.put(doc.id, doc);
			if (doc.inbox) 
				totalCountIn ++;
			else
				totalCountOut++;
		}
		
		public void removeDocument(DocumentMetadata doc) {
			documentsIds.remove(doc.id);
			if (doc.inbox)
				totalCountIn --;
			else
				totalCountOut --;
			
		}
		
		public void updateContactStats (String key, DocumentMetadata docMetadata, boolean inbox) {
			ConversationStatistics convStats = null;
			if (contactStats.containsKey(key)) {
				convStats = (ConversationStatistics)contactStats.get(key);
			} else {
				convStats = new ConversationStatistics();
				contactStats.put(key, convStats);
			}
		
			if (inbox)
				convStats.inDocIds.addElement(docMetadata.id);
			else
				convStats.outDocIds.addElement(docMetadata.id);
			
		}
		
		public int getEmailCount (String email, boolean in) {
			ConversationStatistics ts = (ConversationStatistics) this.contactStats.get(email);
			if (ts == null)
				return 0;
			
			Set v = in?ts.inDocIds:ts.outDocIds;
			int count = 0;

			Enumeration enumVar = v.elements();
			while (enumVar.hasMoreElements()) {
				String key = (String)enumVar.nextElement();
				if (this.documentsIds.containsKey(key)) {
					count ++;
				} 
			}
			
			
			return count;
		}

		public ConversationModel (java.io.DataInputStream in) throws java.io.IOException {
			int version = in.readInt();
			if (version != serializedVersion)
				throw new java.io.IOException ("Incorrect version "+version+", expectiong "+serializedVersion);

			this.totalCountIn = in.readInt();
			this.totalCountOut = in.readInt();
			
			int numDocIds = in.readInt();
			
			String [] docIds = new String[numDocIds];
			for (int i=0; i<numDocIds; i++) {
				DocumentMetadata doc = new DocumentMetadata(in);
				this.documentsIds.put(doc.id, doc);
				docIds[i] = doc.id;
			}
			
			int numConvStats = in.readInt();
			this.contactStats = new Hashtable();
			for (int i=0; i<numConvStats; i++) {
				String key = in.readUTF();
				ConversationStatistics ts = new ConversationStatistics();
				
				int numIn = in.readInt();
				for (int j=0; j<numIn; j++) {
					ts.inDocIds.addElement(docIds[in.readInt()]);
				}
				
				int numOut = in.readInt();
				for (int j=0; j<numOut; j++) {
					ts.outDocIds.addElement(docIds[in.readInt()]);
				}
				
				this.contactStats.put(key, ts);
			}
		}

		public void serialize(java.io.DataOutputStream out) throws java.io.IOException {
			out.writeInt (serializedVersion);
			
			out.writeInt(this.totalCountIn);
			out.writeInt(this.totalCountOut);
			
			Hashtable id2idx = new Hashtable();
			int docCount = 0;
			out.writeInt(this.documentsIds.size());
			Enumeration enumVar = this.documentsIds.elements();
			while (enumVar.hasMoreElements()) {
				DocumentMetadata doc = (DocumentMetadata)enumVar.nextElement();
				int idx = docCount++;
				id2idx.put(doc.id, new Integer(idx));
				doc.serialize(out);
			}	
			
			int numConvStats = this.contactStats.size();
			out.writeInt(numConvStats);
			
			enumVar = this.contactStats.keys();
			while (enumVar.hasMoreElements()) {
				String key = (String)enumVar.nextElement();
				out.writeUTF(key);
				
				ConversationStatistics ts = (ConversationStatistics)this.contactStats.get(key);
				
				// Clean up docs that are deleted
				Set inIdxs = mapIds2Idxs(ts.inDocIds, id2idx);
				Set outIdxs = mapIds2Idxs(ts.outDocIds, id2idx);
							
				out.writeInt(inIdxs.size());
				Enumeration enum1 = inIdxs.elements();
				while (enum1.hasMoreElements()) {
					out.writeInt(((Integer)enum1.nextElement()).intValue());
				}
				
				out.writeInt(outIdxs.size());
				enum1 = outIdxs.elements();
				while (enum1.hasMoreElements()) {
					out.writeInt(((Integer)enum1.nextElement()).intValue());
				}
			}
		}
		
		private static Set mapIds2Idxs(Set ids, Hashtable id2idx) {
			Set s2 = new Set();
			Enumeration enumIds = ids.elements();
			while (enumIds.hasMoreElements()) {
				String id = (String)enumIds.nextElement();
				Integer idx = (Integer)id2idx.get(id);
				if (idx != null)
					s2.addElement(idx);
			}
			
			return s2;
		}
		
		public void trimModel() {
			long nowTimestamp = System.currentTimeMillis();
			if ((nowTimestamp - lastTrimTimestamp) < 1000L * 3600L)
				return;
			
			Set toDelete = new Set();
			
			Enumeration enumVar = this.documentsIds.elements();
			while (enumVar.hasMoreElements()) {
				DocumentMetadata doc = (DocumentMetadata)enumVar.nextElement();
				if ((nowTimestamp - doc.timestamp) > 1000L * 3600L * MAX_DOCUMENT_IN_MODEL_AGE_DAYS)
					toDelete.addElement(doc);
			}
			
			enumVar = toDelete.elements();
			while (enumVar.hasMoreElements()) {
				DocumentMetadata doc = (DocumentMetadata)enumVar.nextElement();
				documentsIds.remove(doc);
			}
			
			lastTrimTimestamp = nowTimestamp;
		}
		
		public long getLastTimestamp(String email) {
			ConversationStatistics ts = (ConversationStatistics) this.contactStats.get(email);
			if (ts == null)
				return 0;

			long lastTimestamp = INVALID_TIME;
			Enumeration enumVar = ts.outDocIds.elements();
			while (enumVar.hasMoreElements()) {
				String key = (String)enumVar.nextElement();
				if (this.documentsIds.containsKey(key)) {
					DocumentMetadata dm = (DocumentMetadata)documentsIds.get(key);
					if ((lastTimestamp == INVALID_TIME) || (lastTimestamp < dm.timestamp))
						lastTimestamp = dm.timestamp;
				}
			}
			
			if (lastTimestamp == INVALID_TIME)
				return INVALID_TIME;
			
			
			enumVar = ts.inDocIds.elements();
			while (enumVar.hasMoreElements()) {
				String key = (String)enumVar.nextElement();
				if (this.documentsIds.containsKey(key)) {
					DocumentMetadata dm = (DocumentMetadata)documentsIds.get(key);
					if ((lastTimestamp == INVALID_TIME) || (lastTimestamp < dm.timestamp))
						lastTimestamp = dm.timestamp;
				}
			}
			
			return lastTimestamp;
		}


		private static class ConversationStatistics {
			public Set inDocIds = new Set();
			public Set outDocIds = new Set();
		}

	}
}
