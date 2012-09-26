package com.cruxly.lib.model;
/* Copyright (c) 2010 Cruxly Inc.
 */

public class Repository {

	private final InboxMessages inboxMessages;
	private final OutboxMessages outboxMessages;
	private final ScoredMessages scoredMessages;

	public InboxMessages getInboxMessages() {
		return inboxMessages;
	}

	public OutboxMessages getOutboxMessages() {
		return outboxMessages;
	}

	public ScoredMessages getScoredMessages() {
		return scoredMessages;
	}

	private static Repository instance = null;

	protected Repository() {
		inboxMessages = new InboxMessages();
		outboxMessages = new OutboxMessages();
		scoredMessages = new ScoredMessages();
	}

	public static Repository getInstance() {
		if (instance == null) {
			instance = new Repository();
		}
		return instance;
	}
}
