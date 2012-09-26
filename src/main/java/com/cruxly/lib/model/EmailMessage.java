/* Copyright (c) 2010 Cruxly Inc.
 */

package com.cruxly.lib.model;
import java.util.Date;

public class EmailMessage {

	public final static int INBOX = 1;
	public final static int OUTBOX = 2;

	public String messageId;
	public Source source;
	public int messageType;

	public String from = null;
	public String[] to = null;
	public String subject = null;
	public Date date = null;
	public String text = null;
	private final Object nativeMessage;

	/*public EmailMessage() {
	}

	public EmailMessage (String _text) {
		text = _text;
	}*/

	public EmailMessage (String _messageId, String _from, String[] _to, Date _date, String _subject, String _text, Object _nativeMessage) {
		messageId = _messageId;
		from = _from;
		to = _to;
		subject = _subject;
		text = _text;
		date = _date;
		nativeMessage = _nativeMessage;
	}

	public int hashCode() {
		int hc = 0;
		hc += (subject != null)?subject.hashCode():-1;
		hc += (text != null)?text.hashCode():-1;
		hc += (from != null)?from.hashCode():-1;
		hc += (to[0] != null)?to[0].hashCode():-1;
		hc += (date != null)?date.hashCode():-1;
		return hc;
	}

	public String getSender() {
		return from;
	}

	public Date getDate() {
		// TODO Auto-generated method stub
		return date;
	}

	public String getSubject() {
		return subject;
	}

	public String getPlainText() {
		return text;
	}

	public Object getNativeMessage() {
		return nativeMessage;
	}
}
