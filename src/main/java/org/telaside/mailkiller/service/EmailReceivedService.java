package org.telaside.mailkiller.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailAccountRepository;
import org.telaside.mailkiller.domain.EmailAccountStatus;
import org.telaside.mailkiller.domain.EmailCheckerStatus;
import org.telaside.mailkiller.domain.EmailKillerUser;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.EmailReceivedRepository;
import org.telaside.mailkiller.domain.EmailStatus;
import org.telaside.mailkiller.domain.EmailSummary;

@Service
@Transactional
public class EmailReceivedService {
	
	static private final Logger LOG = LoggerFactory.getLogger(EmailReceivedService.class);
	
	@Autowired
	private EmailReceivedRepository emailReceivedRepository;
	
	@Autowired
	private EmailAccountRepository emailAccountRepository;

	public void markToBeDeleted(EmailReceived receivedMessage) {
		receivedMessage.setStatus(EmailStatus.TO_BE_DELETED);
		emailReceivedRepository.save(receivedMessage);
	}
	
	public List<EmailReceived> getClearMessagesFor(String user) {
		return emailReceivedRepository.getReceivedEmailsFor(user, EmailStatus.DIAGNOSED, EmailCheckerStatus.CLEAR);
	}

	public boolean saveIfNew(EmailReceived receivedMessage) {
		if(emailReceivedRepository.getReceivedEmailByMessageId(receivedMessage.getMessageId()) == null) {
    		LOG.debug("Saving new received message {}", receivedMessage);
    		emailReceivedRepository.save(receivedMessage);
    		return true;
		}
		return false;
	}

	public List<EmailReceived> getReceivedEmailByStatus(EmailStatus status) {
		return emailReceivedRepository.getReceivedEmailByStatus(status);
	}

	public void save(EmailReceived receivedEmail) {
		emailReceivedRepository.save(receivedEmail);
	}

	public boolean emailExist(EmailReceived receivedMessage) {
		EmailReceived check = emailReceivedRepository.getReceivedEmailByMessageId(receivedMessage.getMessageId());
		return check != null;
	}

	public List<EmailAccountStatus> getEmailsPerStatus(EmailAccount account) {
		return emailReceivedRepository.getEmailsPerStatus(account);
	}

	public List<EmailSummary> emailSummaries(EmailKillerUser user, String emailAddress, EmailCheckerStatus status) {
		EmailAccount account = emailAccountRepository.findByEmailAddress(emailAddress);
		return emailReceivedRepository.findSummariesFor(user, account, status);
	}

	public void clearEmails(EmailKillerUser user, List<String> messageIds) {
		emailReceivedRepository.updateEmailCheckerStatus(EmailCheckerStatus.CLEAR.toString(), /*user, */ messageIds);
	}
}
