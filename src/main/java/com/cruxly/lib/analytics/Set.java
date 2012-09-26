package com.cruxly.lib.analytics;
import java.util.Enumeration;
import java.util.Hashtable;

import com.cruxly.lib.analytics.FiniteStateMachine.State;

public class Set {
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		Enumeration enumVar = this.elements();
		while (enumVar.hasMoreElements()) {
			Object o = enumVar.nextElement();
			if (o instanceof String) {
				b.append((String)o + ", ");
			} else if (o instanceof Hashtable) {
				b.append("Hashtable serialization not implemented");
			} else if (o instanceof State) {
				b.append((State)o);
			} else if (o instanceof String[]) {
				for (String s : (String[])o) {
					b.append(s + ", ");
				}
			}
		}
		return "Set [hash=" + b.toString() + "]";
	}

	protected Hashtable hash = null;
	protected static final Object dummy = new Object();
	
	public Set() {
		hash = new Hashtable();
	}
	
	public Set (int size) {
		hash = new Hashtable(size);
	}
	
	public int size() {
		return hash.size();
	}
	
	public boolean addElement(Object o) {
		if (hash.containsKey(o))
			return false;
		
		hash.put(o, dummy);
		return true;
	}
	
	public void addAll(Set s) {
		Enumeration e = s.hash.keys();
		while (e.hasMoreElements()) {
			Object o = e.nextElement();
			this.addElement(o);
		}
	}

	public void addAll(Object[] objs) {
		if (objs == null)
			return;
		
		for (int i=0; i<objs.length; i++)
			this.addElement(objs[i]);
	}
	
	public void remove(Object o) {
		hash.remove(o);
	}
	
	
	public Enumeration elements() {
		return hash.keys();
	}
	
	public boolean contains(Object o) {
		return hash.containsKey(o);
	}
	
	
	public Set minus(Set v) {
		Set result = new Set();
		
		Enumeration enumVar = this.elements();
		while (enumVar.hasMoreElements()) {
			Object o = enumVar.nextElement();
			if (!v.contains(o))
				result.addElement(o);
		}
		
		return result;
	}
	
	public Set intersection(Set v) {
		return intersection(v.elements());
	}
	
	public Set intersection(Enumeration enumVar) {
		Set result = new Set();
		
		while (enumVar.hasMoreElements()) {
			Object o = enumVar.nextElement();
			if (this.contains(o))
				result.addElement(o);
		}
		return result;
	}
	
	public void copyInto(Object[] array) {
		if ((array == null) || (array.length < this.size()))
			throw new RuntimeException ("Invalid size");
		
		Enumeration enumVar = hash.keys();
		int i=0;
		while (enumVar.hasMoreElements()) {
			Object o = enumVar.nextElement();
			array[i++] = o;
		}
	}
	

}
