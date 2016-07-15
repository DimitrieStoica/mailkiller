package org.telaside.mailkiller.integration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.EmailStatus;
import org.telaside.mailkiller.service.EmailReceivedService;

public class NewEmailsMessageSource implements MessageSource<List<EmailReceived>> {
	
	static private final Logger LOG = LoggerFactory.getLogger(NewEmailsMessageSource.class);
	
	@Autowired
	private EmailReceivedService emailService;


	@Override
	public Message<List<EmailReceived>> receive() {
		List<EmailReceived> receivedEmails = emailService.getReceivedEmailByStatus(EmailStatus.RECEIVED);
		LOG.info("{} newly received emails", receivedEmails.size());
		if(receivedEmails.size() == 0) {
			return null;
		}
		return MessageBuilder.withPayload(receivedEmails).build();
	}
}
