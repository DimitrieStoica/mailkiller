package org.telaside.mailkiller.domain;

import static org.junit.Assert.assertNotEquals;

import java.util.Date;

import org.junit.Test;


public class CanParseAllDatesTest {
	
	@Test
	public void canParseDates() {
		String date = "Sun, 10 Jul 2016 12:06:42 00200."; //.";
		EmailReceived emailReceived = new EmailReceived();
		emailReceived.parseMailDate(date);
		Date receivedDate = emailReceived.getMessageDate();
		assertNotEquals(receivedDate, new Date(0));
	}
}
