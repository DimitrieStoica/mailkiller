package org.telaside.mailkiller.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telaside.mailkiller.checker.EmailCheckerStatus;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.EmailReceivedRepository;
import org.telaside.mailkiller.domain.EmailStatus;

@Service
@Transactional
public class EmailReceivedService {
	
	static private final Logger LOG = LoggerFactory.getLogger(EmailReceivedService.class);
	
	@Autowired
	private EmailReceivedRepository emailRepository;
	
	public void markToBeDeleted(EmailReceived receivedMessage) {
		receivedMessage.setStatus(EmailStatus.TO_BE_DELETED);
		emailRepository.save(receivedMessage);
	}
	
	public List<EmailReceived> getClearMessagesFor(String user) {
		return emailRepository.getReceivedEmailsFor(user, EmailStatus.DIAGNOSED, EmailCheckerStatus.CLEAR);
	}

	public void saveIfNew(EmailReceived receivedMessage) {
		if(emailRepository.getReceivedEmailByMessageId(receivedMessage.getMessageId()) == null) {
    		LOG.debug("Saving new received message {}", receivedMessage);
    		emailRepository.save(receivedMessage);
		}
	}

	public List<EmailReceived> getReceivedEmailByStatus(EmailStatus status) {
		return emailRepository.getReceivedEmailByStatus(status);
	}

	public void save(EmailReceived receivedEmail) {
		emailRepository.save(receivedEmail);
	}

	public boolean emailExist(EmailReceived receivedMessage) {
		EmailReceived check = emailRepository.getReceivedEmailByMessageId(receivedMessage.getMessageId());
		return check != null;
	}
}
