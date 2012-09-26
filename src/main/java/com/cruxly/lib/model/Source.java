package com.cruxly.lib.model;

/* Copyright (c) 2010 Cruxly Inc.
 */

public class Source {
	private String application;
	private String mailBoxName;
	private String folder;
	private String owner;

	public Source(String application, String mailboxName, String folder,
			String owner) {
		setApplication(application);
		setMailBoxName(mailboxName);
		setFolder(folder);
		setOwner(owner);
	}

	public void setApplication(String application) {
		this.application = application;
	}
	public String getApplication() {
		return application;
	}
	public void setMailBoxName(String mailBoxName) {
		this.mailBoxName = mailBoxName;
	}
	public String getMailBoxName() {
		return mailBoxName;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	public String getFolder() {
		return folder;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getOwner() {
		return owner;
	}
}
