package org.telaside.mailkiller.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telaside.mailkiller.checker.EmailChecker;
import org.telaside.mailkiller.checker.EmailCheckerDiagnostic;
import org.telaside.mailkiller.domain.EmailCheckerResult;
import org.telaside.mailkiller.domain.EmailCheckerResultRepository;
import org.telaside.mailkiller.domain.EmailReceived;

@Service
@Transactional
public class EmailDiagnosticService {
	static private final Logger LOG = LoggerFactory.getLogger(EmailDiagnosticService.class);
	
	@Autowired
	private EmailCheckerResultRepository emailCheckerResultRepository;
	
	public EmailCheckerResult newEmailCheckerResult(EmailReceived email, EmailChecker checker, EmailCheckerDiagnostic diagnostic) {
		EmailCheckerResult emailCheckerResult = new EmailCheckerResult();
		emailCheckerResult.setChecker(checker.checkerName());
		emailCheckerResult.setPriority(checker.priority());
		emailCheckerResult.setDiagnostic(diagnostic.diagnostic());
		return emailCheckerResultRepository.save(emailCheckerResult);
	}
}
