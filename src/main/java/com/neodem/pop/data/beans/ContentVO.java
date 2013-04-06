
package com.neodem.pop.data.beans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.mail.MessagingException;
import javax.mail.Part;

/**
 * currently can only display/store text
 * 
 * @author Vince
 * 
 */
public class ContentVO {

	private Long id;

	private String body;

	private String contentType;

	public ContentVO() {
	}

	public ContentVO(Part part) throws MessagingException, IOException {
		// -- Get the content type --
		contentType = part.getContentType();
		// -- If the content is plain text, we save it --
		if (contentType.startsWith("text/plain") || contentType.startsWith("text/html")) {
			InputStream is = part.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String thisLine = reader.readLine();
			StringBuffer b = new StringBuffer();
			while (thisLine != null) {
				b.append(thisLine);
				b.append('\n');
				thisLine = reader.readLine();
			}
			body = b.toString();
		} else {
			body = "<can't parse to text>";
		}
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("CONTENT TYPE: " + contentType).append('\n');
		b.append("BODY: " + body);
		return b.toString();
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

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
