/* Copyright (c) 2010 Cruxly Inc.
 */

package com.cruxly.lib.analytics;


import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.cruxly.lib.model.EmailMessage;



/**
 * @author Kirill
 *
 */
public class ContentAnalysis {

	protected float epsilon = 0.000001f;
	protected Hashtable trainCounts = null;
	protected Hashtable stopWords = null;
	protected static Object dummy = new Object();
	protected Hashtable stemTable = null;
	public static long MAX_DOCUMENT_IN_MODEL_AGE_DAYS = 90; 
	public static int MIN_DOCUMENTS_IN_MODEL = 100;
	public static int TERM_MIN_NUM_OCCURRENCE = 5;

	
	public ContentAnalysis() {
	}

	
	public void init(DataInputStream stemTableStream, InputStream stopWordsStream) {
		this.stemTable = loadStemTable(stemTableStream);
		this.stopWords = loadStopWords(stopWordsStream);
	}
	

	protected static Hashtable loadStemTable(DataInputStream stemTableStream) {
		Hashtable _stemTable = new Hashtable();
		
		try {
			int size = stemTableStream.readInt();
			
			for (int i=0; i<size; i++) {
				String k = stemTableStream.readUTF();
				String v = stemTableStream.readUTF();
				_stemTable.put(k, v);
			}
			stemTableStream.close();
			
		} catch (java.io.IOException e) {
			throw new RuntimeException("Unable to load stem table");
		}
		
		return _stemTable;
	}
	
	protected static Hashtable loadStopWords (InputStream inputStream) {
		Hashtable _stopWords = new Hashtable();
		StringBuffer buffer = new StringBuffer();
		
		try {
			// Read it a text file
			int read = -1;
			byte[] bytes = new byte[50000];
			while ((read = inputStream.read(bytes)) > 0) {
				buffer.append(new String(bytes,0,read));
			}

			inputStream.close();
		} catch (java.io.IOException e) {
			throw new RuntimeException("Unable to load stopwords");
		}
		
		String [] lines = StringUtils.splitByNewlines(buffer.toString());
		for (int i=0; i<lines.length; i++) {
			String line = lines[i].trim();
			if ((line.length() > 0) && (line.charAt(0) != '#')) {
				String[] words = StringUtils.splitBySeparator(line, ',');
				for (int j=0; j<words.length; j++) {
					String sw = words[j].trim();
					if (sw.length() > 0) {
						_stopWords.put(sw, dummy);
					}
				}
			}
		}
		
		return _stopWords;
	}


	/**
	 * Counts the number of unique, case-insensitive tokens
	 * that occur in the text
	 * 
	 * @param text - the text to process
	 * @param counts - a hashtable of "word" -> MutableInteger to hold the counts, can be empty
	 * @return - total number of tokens encountered
	 */
	public static int countTokens (String text, Hashtable counts, Set exclude) {
		int totalCount = 0;

		String [] tokens = StringUtils.splitIntoTokens(text, true, false);

		int numTokens = tokens.length;
		for (int ti=0; ti<numTokens; ti++) {
			String token = tokens[ti];
			
			if (exclude.contains(token))
				continue;

			if ((token.length() < 2) || (token.length() > 20))
				continue;

			if (token.indexOf('.') > -1)
				continue;  // ignore urls/emails

			totalCount++;

			if (counts.containsKey(token)) {
				((MutableInteger)counts.get(token)).increment();
			} else {
				counts.put(token, new MutableInteger(1));
			}

		}

		return totalCount;
	}


	/**
	 * Train the content model
	 * 
	 * @param inbox
	 * @param outbox
	 * @return
	 * @throws Exception
	 */

	public ImportantTermsModel trainModel (EmailMessage[] inbox, EmailMessage [] outbox, String[] _blacklist) throws Exception {
		if (!isInitialized())
			throw new RuntimeException ("ContentAnalysis not initialized");
		
		Set blacklist = new Set();
		if (_blacklist != null)
			blacklist.addAll(_blacklist);


		ImportantTermsModel model = new ImportantTermsModel();

		// First count all the tokens
		for (int di=0; di<outbox.length; di++) {
			EmailMessage outboxEmailMessage = outbox[di];
			validateMessage (outboxEmailMessage, di);
			updateModel (model, outboxEmailMessage, false, blacklist);
		}

		for (int di=0; di<inbox.length; di++) {
			EmailMessage inboxEmailMessage = inbox[di];
			validateMessage (inboxEmailMessage, di);
			updateModel (model, inboxEmailMessage, true, blacklist);
		}
		
		model.trimModel2();
		
		return  model;
	}
	
	protected static void validateMessage (EmailMessage email, int di) throws Exception  {
		if (email == null) throw new Exception("Null inbox message found at " + di);

		String from = email.from;
		if (from == null) throw new Exception("Null 'from' inbox message found at " + di);

		String[] to = email.to;
		if (to == null) throw new Exception("Null 'to[]' inbox message found at " + di);

		String subject = email.subject;
		//if (subject == null) throw new Exception("Null 'subject' inbox message found at " + di);
		if (subject == null)
			email.subject = "";

		String body = email.text;
		if (body == null) throw new Exception("Null 'body' inbox message found at " + di);
	}
	
	
	public void updateModel(ImportantTermsModel model, EmailMessage newMessage, boolean incoming, Set blacklist) throws Exception {

		model.trimModel();

		// Exclude the elements from to, from fields
		Set exclude = new Set();
		for (int j=0; j<newMessage.to.length; j++) {
			String[] toComponents = StringUtils.splitBySeparator(newMessage.to[j].toLowerCase(), ' ');
			for (int k=0; k<toComponents.length; k++)
				exclude.addElement(toComponents[k]);
		}
		String[] toComponents = StringUtils.splitBySeparator(newMessage.from.toLowerCase(), ' ');
		for (int k=0; k<toComponents.length; k++)
			exclude.addElement(toComponents[k]);

		// Count the tokens
		Hashtable counts = new Hashtable();
		String text = StringUtils.getEmailContent(newMessage);
		int count = countTokens(text, counts, exclude);
		
		// Add doc to the model
		DocumentMetadata docId = new DocumentMetadata(newMessage.messageId, newMessage.date, count, incoming);
		model.addDcoument(docId);
		
		// Add/update all the tokens to the model
		Enumeration enumVar = counts.keys();
		while (enumVar.hasMoreElements()) {
			String key = (String)enumVar.nextElement();
			int termCount = ((MutableInteger)counts.get(key)).val;
			
			// Ignore if stopword
			if (stopWords.containsKey(key) || blacklist.contains(key) || (!isAllLetters(key)))
				continue;

			// stem
			if (stemTable.containsKey(key.toLowerCase()))
				key = stemTable.get(key).toString();
			
			model.updateTermStats (key, docId, incoming, termCount);
			
			
		}
	}
	
	protected static boolean isAllLetters(String key) {
		for (int j=0; j<key.length(); j++) {
			if (!StringUtils.isAlpha(key.charAt(j))) {
				return false;
			}
		}
		return true;
	}

	public boolean isInitialized() {
		return ((this.stemTable != null) && (this.stopWords != null));
	}
	
	protected float computeTermScore (String key, ImportantTermsModel model) {
		
		if (stemTable.containsKey(key))
			key = stemTable.get(key).toString();
		
		if (!model.termStats.containsKey(key))
			return 0.0f;
		
		float score = 0.0f;
		int valIn = model.getTermCount(key, true);
		int valOut = model.getTermCount(key, false);
		if (valIn == 0)
			valIn = 1;
		if (valOut == 0)
			valOut = 1;

		long totalInboxCount = (model.inTokenCount > 0)?model.inTokenCount:1;
		long totalOutboxCount = (model.outTokenCount > 0)?model.outTokenCount:1;
	
		if ((valIn + valOut) > TERM_MIN_NUM_OCCURRENCE)
			score = (((float)valOut) / ((float)totalOutboxCount)) - (((float)valIn) / ((float)totalInboxCount));
	
		return score;
	}
	
	public DocumentContentScore rateDocumentImportance (EmailMessage document, String text, ImportantTermsModel model) {
		if (!isInitialized())
			throw new RuntimeException ("ContentAnalysis not initialized");
		
		DocumentContentScore result = new DocumentContentScore();
		
		TextSegment [] tokens = StringUtils.splitIntoTextSegments (text, true, false);
		Vector annotations = new Vector();
		float sum = 0.0f;
		int count = 0;

		for (int i=0; i<tokens.length; i++) {
			TextSegment seg = tokens[i];
			if (seg instanceof TextSegment.SpecialSymbolSegment)
				continue;
			String key = seg.toString();

			float termScore = computeTermScore (key, model);
			if (termScore > 0.0f) {
				sum += termScore;
				count ++;
				annotations.addElement(seg);
			}
		}

		result.documentScore = ((float)count) / 10.0f;
		if (result.documentScore > 1.0f)
			result.documentScore = 1.0f;
		
		result.annotations = new TextSegment[annotations.size()];
		annotations.copyInto(result.annotations);

		return result;
	}

	

	public static class ImportantTermsModel {
		private Hashtable termStats = new Hashtable();;
		private Hashtable documentsIds = new Hashtable();;
		private final int serializedVersion = 4;
		private long inTokenCount = 0;
		private long outTokenCount = 0;
		private long lastTrimTimestamp = 0;

		protected ImportantTermsModel() {
		}
		
		public void addDcoument(DocumentMetadata doc) {
			documentsIds.put(doc.id, doc);
			if (doc.inbox) 
				inTokenCount += doc.tokenCount;
			else
				outTokenCount += doc.tokenCount;
		}
		
		public void removeDocument(DocumentMetadata doc) {
			documentsIds.remove(doc.id);
			if (doc.inbox)
				inTokenCount -= doc.tokenCount;
			else
				outTokenCount -= doc.tokenCount;
		}
		
		public void updateTermStats (String key, DocumentMetadata docId, boolean inbox, int count) {
			TermStatistics tokenStats = null;
			if (termStats.containsKey(key)) {
				tokenStats = (TermStatistics)termStats.get(key);
			} else {
				tokenStats = new TermStatistics();
				termStats.put(key, tokenStats);
;			}
		
			if (inbox)
				tokenStats.addInDoc (docId.id);
			else
				tokenStats.addOutDoc(docId.id);
		}
		
		public int getTermCount (String term, boolean in) {
			TermStatistics ts = (TermStatistics) this.termStats.get(term);
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

		public ImportantTermsModel (java.io.DataInputStream in) throws java.io.IOException {
			int version = in.readInt();
			if (version != serializedVersion)
				throw new java.io.IOException ("Incorrect version "+version+", expectiong "+serializedVersion);

				this.inTokenCount = in.readLong();
				this.outTokenCount = in.readLong();
				
				
				int numDocIds = in.readInt();
				String [] docIds = new String[numDocIds];
				for (int i=0; i<numDocIds; i++) {
					DocumentMetadata doc = new DocumentMetadata(in);
					this.documentsIds.put(doc.id, doc);
					docIds[i] = doc.id;
				}
				
				int numTokenStats = in.readInt();
				this.termStats = new Hashtable();
				for (int i=0; i<numTokenStats; i++) {
					String key = in.readUTF();
					TermStatistics ts = new TermStatistics();
					
					int numIn = in.readInt();
					for (int j=0; j<numIn; j++) {
						ts.addInDoc(docIds[in.readInt()]);
					}
					
					int numOut = in.readInt();
					for (int j=0; j<numOut; j++) {
						ts.addOutDoc(docIds[in.readInt()]);
					}
					
					this.termStats.put(key, ts);
				}
		}

		public void serialize(java.io.DataOutputStream out) throws java.io.IOException {
			out.writeInt (serializedVersion);
			
			out.writeLong(this.inTokenCount);
			out.writeLong(this.outTokenCount);

			out.writeInt(documentsIds.size());
			Hashtable id2idx = new Hashtable();
			int docCount = 0;
			Enumeration enumVar = documentsIds.elements();
			while (enumVar.hasMoreElements()) {
				DocumentMetadata doc = (DocumentMetadata)enumVar.nextElement();
				int idx = docCount++;
				id2idx.put(doc.id, new Integer(idx));
				doc.serialize(out);
			}
			
			int numTokenStats = this.termStats.size();
			out.writeInt(numTokenStats);
			
			enumVar = this.termStats.keys();
			while (enumVar.hasMoreElements()) {
				String key = (String)enumVar.nextElement();
				out.writeUTF(key);
				
				TermStatistics ts = (TermStatistics)this.termStats.get(key);
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
		
		public void trimModel2() {
			Set toDelete = new Set();

			Enumeration enumVar = this.termStats.keys();
			while (enumVar.hasMoreElements()) {
				String key = (String)enumVar.nextElement();
				TermStatistics ts = (TermStatistics)this.termStats.get(key);
				if (ts.outDocIds.size() == 0)
					toDelete.addElement(key);
			}
			
			enumVar = toDelete.elements();
			while (enumVar.hasMoreElements()) {
				this.termStats.remove((String)enumVar.nextElement());
			}
		}
		
		public String toString() {
			StringBuffer buff = new StringBuffer();
			buff.append(" ");
			Enumeration enumVar = this.termStats.keys();
			while (enumVar.hasMoreElements()) {
				String key = (String)enumVar.nextElement();
				buff.append(',').append(key);
			}
			
			
			return buff.toString();
		}

		private static class TermStatistics {
			public Set inDocIds;
			public Set outDocIds;
			
			public TermStatistics () {
				inDocIds = new Set();
				outDocIds = new Set();
			}
			
			public void addInDoc (String id) {
				inDocIds.addElement(id);
			}
	
			public void addOutDoc (String id) {
				outDocIds.addElement(id);
			}
		}
	}
	
	
	public static class DocumentMetadata {
		public String id;
		public long timestamp;
		public boolean deleted;
		public int tokenCount;
		public boolean inbox;
		
		public DocumentMetadata (String id, Date d, int tc, boolean in) {
			this.tokenCount = tc;
			this.id = id;
			this.timestamp = d.getTime();
			deleted = false;
			this.inbox = in;
		}
		
		public DocumentMetadata (java.io.DataInputStream in) throws java.io.IOException {
			this.id = in.readUTF();
			this.timestamp = in.readLong();
			this.deleted = in.readBoolean();
			this.tokenCount = in.readInt();
			this.inbox = in.readBoolean();
		}
		
		
		public void serialize(java.io.DataOutputStream out) throws java.io.IOException {
			out.writeUTF(id);
			out.writeLong(timestamp);
			out.writeBoolean(deleted);
			out.writeInt(tokenCount);
			out.writeBoolean(inbox);		
		}
		
		public int hashCode() {
			return id.hashCode();
		}
		
		public boolean equals (Object o) {
			return ((o instanceof DocumentMetadata) && ((DocumentMetadata)o).id.equals(this.id));
		}
	}
	


	public static class MutableInteger {
		public int val = 0;
		public MutableInteger () {

		}
		public MutableInteger (int seed) {
			val = seed;
		}

		public int increment() {
			val ++;
			return val;
		}

		public int intValue() {
			return val;
		}

		public String toString() {
			return ""+val;
		}
	}

	public static class DocumentContentScore extends DocumentScore {
	}


}
