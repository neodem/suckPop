
package com.neodem.pop.data.beans;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class AddressVO {

	private String personal;

	private String address;

	private Long id;

	public AddressVO() {
	}

	public AddressVO(Address emailAddress) {
		this.personal = ((InternetAddress) emailAddress).getPersonal();
		this.address = ((InternetAddress) emailAddress).getAddress();
	}
	
	public AddressVO(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		if (personal == null) {
			b.append(address);
		} else {
			b.append(personal);
			b.append(' ');
			b.append('<');
			b.append(address);
			b.append('>');
		}

		return b.toString();
	}

	/**
	 * @return the personal
	 */
	public String getPersonal() {
		return personal;
	}

	/**
	 * @param personal
	 *            the personal to set
	 */
	public void setPersonal(String personal) {
		this.personal = personal;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	private void setId(Long id) {
		this.id = id;
	}
}
