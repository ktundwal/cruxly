package com.cruxly.lib.analytics;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class IntentRule {
	public String intent;
	public String rule;
	public IntentRule(String intent, String rule) {
		super();
		this.intent = intent;
		this.rule = rule;
	}
	
	public static IntentRule getWantInstanceWithNoRule() {
		return new IntentRule("intent", "");
	}
	
	@Override
	public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(intent).
            //append(rule).
            toHashCode();
    }

	@Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        IntentRule rhs = (IntentRule) obj;
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
            append(intent, rhs.intent).
            //append(rule, rhs.rule).
            isEquals();
    }
}