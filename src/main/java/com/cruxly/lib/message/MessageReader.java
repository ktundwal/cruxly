package com.cruxly.lib.message;

import java.util.Date;

import com.cruxly.lib.exceptions.FolderNotFoundException;
import com.cruxly.lib.exceptions.InboxEmptyException;
import com.cruxly.lib.exceptions.MessagingException;
import com.cruxly.lib.model.EmailMessage;

public interface MessageReader {
	public EmailMessage[] getReceivedMessages(Date from, Date to) throws FolderNotFoundException, InboxEmptyException, MessagingException, Exception;
	public EmailMessage[] getSentMessages(Date from, Date to) throws FolderNotFoundException, InboxEmptyException, MessagingException, Exception;
}
