
package com.neodem.pop.data.beans;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.commons.lang.StringUtils;

public class MessageVO {
	private static final char NL = '\n';
	private static final String TEXT_TYPE = "text/plain";
	private static final DateFormat DF = DateFormat.getDateTimeInstance();
	
	
	private String fromName;

	private String fromEmailAddress;

	private AddressVO fromAddress;

	private List<AddressVO> tos = new ArrayList<AddressVO>();

	private List<ContentVO> contents = new ArrayList<ContentVO>();

	private Date sentDate;

	private String subject;

	private String uid = "<unknown>";

	private Long id;

	public MessageVO() {
	}

	public MessageVO(Message message) throws MessagingException, IOException {
		parseHeader(message);
		parseBody(message);
	}

	// Get the header information
	private void parseHeader(Message message) throws MessagingException {
		fromAddress = new AddressVO(message.getFrom()[0]);
		fromName = fromAddress.getPersonal();
		fromEmailAddress = fromAddress.getAddress();

		subject = message.getSubject();
		sentDate = message.getSentDate();
		Address[] recipients = message.getAllRecipients();
		if (recipients != null) {
			for (int i = 0; i < recipients.length; i++) {
				tos.add(new AddressVO(recipients[i]));
			}
		}
	}

	private void parseBody(Part messagePart) throws MessagingException, IOException {
		Object content = messagePart.getContent();
		if (content instanceof Multipart) {
			parseMultipart((Multipart) content);
		} else {
			contents.add(parseContent(messagePart));
		}
	}

	private void parseMultipart(Multipart multipart) throws MessagingException, IOException {
		int size = multipart.getCount();
		for (int i = 0; i < size; i++) {
			contents.add(parseContent(multipart.getBodyPart(i)));
		}
	}

	private ContentVO parseContent(Part part) throws MessagingException, IOException {
		return new ContentVO(part);
	}

	/**
	 * 
	 * @param removeExtras if true will skip any content starting with '>'
	 * @return
	 */
	public String makePrintableMessage(boolean removeExtras) {
		StringBuffer b = new StringBuffer();
		
		b.append("date:");
		b.append(getPrintableDate());
		b.append(NL);
		b.append("from:");
		b.append(getPrintableFrom());
		b.append(NL);
		b.append("to:");
		b.append(getFirstPrintableTo());
		b.append(NL);
		b.append("subject:");
		b.append(getPrintableSubject());
		b.append(NL);
		b.append("message:");
		b.append(NL);
		b.append(getFirstPrintableContent(removeExtras));
		b.append(NL);
		
		return b.toString();
	}
	
	public String getPrintableDate() {
		return DF.format(getSentDate());
	}

	/**
	 * just return the first text content
	 * 
	 * @param clean if true will skip any content starting with '>'
	 * @return
	 */
	public String getFirstPrintableContent(boolean clean) {
		String content = "<unknown>";

		List<ContentVO> contents = getContents();
		if (contents != null) {
			for (int i = 0; i < contents.size(); i++) {
				ContentVO contentVO = contents.get(i);
				if (contentVO != null) {
					String contentType = contentVO.getContentType();
					if (!StringUtils.isBlank(contentType)) {
						if (contentType.startsWith(TEXT_TYPE)) {
							content = contentVO.getBody();
							break;
						}
					}
				}
			}
		}
		
		if(clean) {
			content = clean(content);
		}

		return content;
	}

	private String clean(String content) {
		if(StringUtils.isBlank(content)) {
			return content;
		}
		StringBuffer b = new StringBuffer(content.length());
		String[] lines = StringUtils.split(content, '\n');
		for (String line : lines) {
			if(line.startsWith(">")) {
				continue;
			}
			b.append(line);
			b.append('\n');
		}
		return b.toString();
	}

	public String getPrintableFrom() {
		String from = "<unknown>";
		
		String personal = fromAddress.getPersonal();
		String address = fromAddress.getAddress();

		if (StringUtils.isNotBlank(personal)) {
			from = personal;
		} else {
			if(StringUtils.isNotBlank(address)) {
				from = address;
			} 
		}

		return from;
	}

	public String getPrintableSubject() {
		String subject = getSubject();

		if (StringUtils.isBlank(subject)) {
			return "<unknown>";
		}

		return subject;
	}

	public long getSentTime() {
		Date sent = getSentDate();
		if (sent != null) {
			return sent.getTime();
		}
		return 0;
	}

	/**
	 * just return the first to
	 * 
	 * @param message
	 * @return
	 */
	public String getFirstPrintableTo() {
		String to = "<unknown>";

		List<AddressVO> tos = getTos();
		if (tos != null) {
			if (tos.size() > 0) {
				AddressVO address = tos.get(0);
				if (address != null) {
					to = address.getAddress();
				}
			}
		}

		return to;
	}
	
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();

		b.append("ID:" + id).append('\n');

		b.append("FROM: " + fromAddress).append('\n');

		for (AddressVO address : tos) {
			b.append("TO: " + address).append('\n');
		}
		b.append("SENT: " + sentDate).append('\n');

		b.append("SUBJECT: " + subject).append('\n');
		for (ContentVO content : contents) {
			b.append(content).append('\n');
		}

		b.append("-----------------------------").append('\n');

		return b.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the sentDate
	 */
	public Date getSentDate() {
		return sentDate;
	}

	/**
	 * @param sentDate
	 *            the sentDate to set
	 */
	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the tos
	 */
	public List<AddressVO> getTos() {
		return tos;
	}

	/**
	 * @param tos
	 *            the tos to set
	 */
	public void setTos(List<AddressVO> tos) {
		this.tos = tos;
	}

	/**
	 * @return the contents
	 */
	public List<ContentVO> getContents() {
		return contents;
	}

	/**
	 * @param contents
	 *            the contents to set
	 */
	public void setContents(List<ContentVO> contents) {
		this.contents = contents;
	}

	/**
	 * @return the fromAddress
	 */
	public AddressVO getFromAddress() {
		return fromAddress;
	}

	/**
	 * @param fromAddress
	 *            the fromAddress to set
	 */
	public void setFromAddress(AddressVO fromAddress) {
		this.fromAddress = fromAddress;
	}

	/**
	 * @return the fromName
	 */
	public String getFromName() {
		return fromName;
	}

	/**
	 * @param fromName
	 *            the fromName to set
	 */
	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	/**
	 * @return the fromEmailAddress
	 */
	public String getFromEmailAddress() {
		return fromEmailAddress;
	}

	/**
	 * @param fromEmailAddress
	 *            the fromEmailAddress to set
	 */
	public void setFromEmailAddress(String fromEmailAddress) {
		this.fromEmailAddress = fromEmailAddress;
	}

	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	public void addTo(AddressVO addressVO) {
		tos.add(addressVO);
	}

	public void addContent(ContentVO c) {
		contents.add(c);
	}

}
