package org.telaside.mailkiller.domain;

import java.util.Date;

import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailSummary {
	
	private String headerFrom;
	private String subject;
	private String internalMessageId;
	private Date messageDate;
	private EmailCheckerStatus diagnostic;
	private EmailStatus status;
	
	private static final Logger LOG = LoggerFactory.getLogger(EmailSummary.class);
	
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
		if(header != null) {
			try {
				return MimeUtility.decodeText(header);
			} catch(Exception e) {
				LOG.error("Cannot decode {}", header);
			}
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
