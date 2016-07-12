package org.telaside.mailkiller.checker;

import static org.telaside.mailkiller.domain.EmailStatus.DIAGNOSED;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telaside.mailkiller.domain.EmailCheckerStatus;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.EmailStatus;
import org.telaside.mailkiller.service.EmailDiagnosticService;
import org.telaside.mailkiller.service.EmailReceivedService;

@Component
public class EmailCheckerScheduler {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmailCheckerScheduler.class);
	
	@Autowired
	private EmailReceivedService emailService;
	
	@Autowired
	private EmailDiagnosticService emailDiagnosticService;

	@Autowired
	List<EmailChecker> mailCheckers;
	
	@Autowired 
	private EmailCheckerDiagnose diagnose;
	
	@Scheduled(initialDelay = 2000, fixedDelay = 60000) // 60s between each run
	public void checkReceivedEmails() {
		List<EmailReceived> receivedEmails = emailService.getReceivedEmailByStatus(EmailStatus.RECEIVED);
		int total = receivedEmails.size();
		int current = 0;
		LOG.info("{} new email(s) to check.", total);
		for(EmailReceived receivedEmail : receivedEmails) {
			LOG.debug("----> Checking {}/{}", ++current, total);
			List<EmailCheckerDiagnostic> diags = new ArrayList<EmailCheckerDiagnostic>(mailCheckers.size());
			for(EmailChecker mailChecker : mailCheckers) {
				EmailCheckerDiagnostic diagnostic = new EmailCheckerDiagnostic();
				try {
					mailChecker.checkEmail(receivedEmail, diagnostic);
				} catch(Exception e) {
					LOG.error("Exception while checking with '{}'", mailChecker.checkerName(), e);
				}
				diags.add(diagnostic);
				emailDiagnosticService.newEmailCheckerResult(receivedEmail, mailChecker, diagnostic);
			}
			EmailCheckerStatus status = diagnose.diagnose(receivedEmail, diags);
			receivedEmail.setStatus(DIAGNOSED);
			receivedEmail.setDiagnostic(status);
			emailService.save(receivedEmail);
		}
	}
}
