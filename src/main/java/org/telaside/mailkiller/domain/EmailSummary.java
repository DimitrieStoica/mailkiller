package org.telaside.mailkiller.domain;

import static java.nio.charset.Charset.forName;

import java.nio.charset.Charset;
import java.util.Date;

public class EmailSummary {
	
	static private final String UTF_8_MARKER = "=?UTF-8?"; 
	private String headerFrom;
	private String subject;
	private String internalMessageId;
	private Date messageDate;
	private EmailCheckerStatus diagnostic;
	private EmailStatus status;
	
	public EmailSummary() {}
	
	public EmailSummary(String headerFrom, String subject, String internalMessageId, Date messageDate, EmailCheckerStatus diagnostic, EmailStatus status) {
		this.headerFrom = utf8(headerFrom);
		this.subject = utf8(subject);
		this.internalMessageId = internalMessageId;
		this.messageDate = messageDate;
		this.diagnostic = diagnostic;
		this.status = status;
	}
	
	private String utf8(String header) {
		if(header != null && header.toUpperCase().startsWith(UTF_8_MARKER)) {
			String utf8 = header.substring(UTF_8_MARKER.length());
			String newHeader = new String(utf8.getBytes(), forName("UTF-8"));
			return newHeader;
		}
		return header;
	}

	public String getHeaderFrom() {
		return headerFrom;
	}

	public void setHeaderFrom(String headerFrom) {
		this.headerFrom = headerFrom;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getInternalMessageId() {
		return internalMessageId;
	}

	public void setInternalMessageId(String internalMessageId) {
		this.internalMessageId = internalMessageId;
	}

	public Date getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(Date messageDate) {
		this.messageDate = messageDate;
	}

	public EmailCheckerStatus getDiagnostic() {
		return diagnostic;
	}

	public void setDiagnostic(EmailCheckerStatus diagnostic) {
		this.diagnostic = diagnostic;
	}

	public EmailStatus getStatus() {
		return status;
	}

	public void setStatus(EmailStatus status) {
		this.status = status;
	}
}
