/* Copyright (c) 2010 Cruxly Inc.
 */

package com.cruxly.lib.model;

public class Address {
	private String firstName;
	private String lastName;
	private String email;

	public Address(String firstName, String lastName, String email) {
		setFirstName(firstName);
		setLastName(lastName);
		setEmail(email);
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLastName() {
		return lastName;
	}
}
