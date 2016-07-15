package org.telaside.mailkiller.checker.impl;

import static org.telaside.mailkiller.checker.EmailCheckerPriority.SPAM_BLOCKER_HIGH;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.CERTAINLY_SPAM;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telaside.mailkiller.checker.EmailChecker;
import org.telaside.mailkiller.checker.EmailCheckerDiagnostic;
import org.telaside.mailkiller.checker.EmailCheckerPriority;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.filter.EmailUserDomainBlackListEntry;
import org.telaside.mailkiller.domain.repository.EmailUserObjectFilterRepository;

@Service
public class DomainBlackListChecker implements EmailChecker {
	
	static private final Logger LOG = LoggerFactory.getLogger(DomainBlackListChecker.class);
	
	@Autowired
	private EmailUserObjectFilterRepository emailUserObjectFilterRepository;

	@Override
	@Transactional
	public void checkEmail(EmailReceived email, EmailCheckerDiagnostic diagnostic) throws Exception {
		List<EmailUserDomainBlackListEntry> domainBlackList = 
				emailUserObjectFilterRepository.getDomainBlackListFor(email.getEmailAccount().getUser());
		String domain = email.getDomain();
		LOG.debug("Checking {} is not in {}", domain, domainBlackList);
		if(domainBlackList.contains(domain)) {
			LOG.info("Excluded email by domain");
			diagnostic.status(CERTAINLY_SPAM);
			diagnostic.append(String.format("Domain [%] is part of the user domain black list", domain));
		}
	}

	@Override
	public String name() {
		return "Domain black list";
	}

	@Override
	public EmailCheckerPriority priority() {
		return SPAM_BLOCKER_HIGH;
	}
}
