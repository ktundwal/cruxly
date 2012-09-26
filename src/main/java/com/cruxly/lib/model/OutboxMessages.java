/* Copyright (c) 2010 Cruxly Inc.
 */

package com.cruxly.lib.model;



public class OutboxMessages extends EnhancedVector {
	public Object[] toArray() {
		EmailMessage[] objects = new EmailMessage[this.size()];
		for (int i = 0; i < this.size(); i++) {
			objects[i] = (EmailMessage) this.elementAt(i);
		}
		return objects;
	}
}
