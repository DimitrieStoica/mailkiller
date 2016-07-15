package org.telaside.mailkiller.service;

import static org.telaside.mailkiller.domain.EmailCheckerStatus.CLEAR;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.PROBABLY_SPAM;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.UNKNOWN;
import static org.telaside.mailkiller.domain.EmailStatus.DIAGNOSED;
import static org.telaside.mailkiller.domain.EmailStatus.TO_BE_DELETED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailAccountStatus;
import org.telaside.mailkiller.domain.EmailCheckerResult;
import org.telaside.mailkiller.domain.EmailCheckerStatus;
import org.telaside.mailkiller.domain.EmailKillerUser;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.EmailStatus;
import org.telaside.mailkiller.domain.EmailSummary;
import org.telaside.mailkiller.domain.filter.EmailUserBaseObjectFilterEntry;
import org.telaside.mailkiller.domain.filter.EmailUserDomainBlackListEntry;
import org.telaside.mailkiller.domain.repository.EmailAccountRepository;
import org.telaside.mailkiller.domain.repository.EmailReceivedRepository;
import org.telaside.mailkiller.domain.repository.EmailUserObjectFilterRepository;

@Service
@Transactional
public class EmailReceivedService {
	
	static private final Logger LOG = LoggerFactory.getLogger(EmailReceivedService.class);
	
	@Autowired
	private EmailReceivedRepository emailReceivedRepository;
	
	@Autowired
	private EmailAccountRepository emailAccountRepository;
	
	@Autowired
	private EmailUserObjectFilterRepository emailUserObjectFilterRepository;

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

	public void save(List<EmailReceived> receivedEmails) {
		emailReceivedRepository.save(receivedEmails);
	}

	public boolean emailExist(EmailReceived receivedMessage) {
		EmailReceived check = emailReceivedRepository.getReceivedEmailByMessageId(receivedMessage.getMessageId());
		return check != null;
	}

	public List<EmailAccountStatus> getEmailsPerStatus(EmailAccount account, List<EmailCheckerStatus> statuses) {
		return emailReceivedRepository.getEmailsPerStatus(account, 
					Arrays.asList(DIAGNOSED),
					statuses); //Arrays.asList(CLEAR, PROBABLY_SPAM, UNKNOWN));
	}

	public List<EmailSummary> emailSummaries(EmailKillerUser user, String emailAddress, EmailStatus status, EmailCheckerStatus diagnostic) {
		EmailAccount account = emailAccountRepository.findByEmailAddress(emailAddress);
		return emailReceivedRepository.findSummariesFor(user, status, account, diagnostic);
	}

	public void clearEmails(EmailKillerUser user, List<String> messageIds) {
		emailReceivedRepository.updateEmailCheckerStatus(CLEAR.toString(), /*user, */ messageIds);
	}

	public void deleteEmails(EmailKillerUser user, List<String> messageIds) {
		emailReceivedRepository.updateEmailStatus(TO_BE_DELETED.toString(), /*user, */ messageIds);
	}
	
	public void deleteEmailsAndBanDomains(EmailKillerUser user, List<String> messageIds) {
		banDomainsForUser(user, messageIds);
		deleteEmails(user, messageIds);
		//banDomainsForUser(user, messageIds);
	}
	
	private void banDomainsForUser(EmailKillerUser user, List<String> messageIds) {
		List<EmailUserDomainBlackListEntry> domainBlackList = emailUserObjectFilterRepository.getDomainBlackListFor(user);
		List<String> domains = emailReceivedRepository.getDomainFromMessages(messageIds);
		
		List<EmailUserDomainBlackListEntry> newEntries = new ArrayList<EmailUserDomainBlackListEntry>();
		
		for(String domain : domains) {
			if(domain == null) {
				continue;
			}
			if(isInEntryList(domain, domainBlackList)) {
				continue;
			}
			newEntries.add(new EmailUserDomainBlackListEntry(user, domain));
		}
		
		if(newEntries.size() > 0) {
			LOG.info("Banning {} new domain(s) for {}", newEntries.size(), user.getPrincipalEmailAddress());
			emailUserObjectFilterRepository.save(newEntries);
		}
	}

	@SuppressWarnings("rawtypes")
	private boolean isInEntryList(String domain, List entries) {
		for(int index = 0; index < entries.size(); index++) {
			EmailUserBaseObjectFilterEntry entry = (EmailUserBaseObjectFilterEntry) entries.get(index);
			if(domain.equalsIgnoreCase(entry.getFilter())) {
				return true;
			}
		}
		return false;
	}

	public List<EmailCheckerResult> getEmailCheckerResult(EmailKillerUser user, String internalMessageId) {
		return emailReceivedRepository.getEmailCheckerResult(user, internalMessageId);
	}
}
