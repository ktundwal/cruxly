/* Copyright (c) 2010 Cruxly Inc.
 */

package com.cruxly.lib.model;

import java.util.Vector;

public abstract class EnhancedVector extends Vector {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void addElementsFromArray(Object[] objects) throws Exception {
		int sizeBeforeAddition = this.size();
		for (int i = 0; i < objects.length; i++) {
			addElement(objects[i]);
		}
		int sizeAfterAddition = this.size();
		if (sizeAfterAddition != sizeBeforeAddition + objects.length) {
			throw new Exception("Error adding array elements to vector");
		}
	}
}
