package org.telaside.mailkiller.domain;

public interface MailHeaderPrefix {
	public static final String H_FROM = "From: ";
	public static final int H_FROM_LENGTH = H_FROM.length();
	
	public static final String H_ID = "message-id: ";
	public static final int H_ID_LENGTH = H_ID.length();
	
	public static final String H_SUBJECT = "Subject: ";
	public static final int H_SUBJECT_LENGTH = H_SUBJECT.length();

	public static final String H_DATE = "Date: ";
	public static final int H_DATE_LENGTH = H_DATE.length();
}
