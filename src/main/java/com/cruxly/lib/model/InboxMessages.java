/* Copyright (c) 2010 Cruxly Inc.
 */

package com.cruxly.lib.model;



public class InboxMessages extends EnhancedVector {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Object[] toArray() {
		EmailMessage[] objects = new EmailMessage[this.size()];
		for (int i = 0; i < this.size(); i++) {
			objects[i] = (EmailMessage) this.elementAt(i);
		}
		return objects;
	}
}
