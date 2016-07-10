package org.telaside.mailkiller.checker;

import static org.telaside.mailkiller.domain.EmailStatus.DIAGNOSED;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.EmailStatus;
import org.telaside.mailkiller.service.EmailReceivedService;

@Component
public class EmailCheckerScheduler {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmailCheckerScheduler.class);
	
	@Autowired
	private EmailReceivedService emailService;
	
	@Autowired
	List<EmailChecker> mailCheckers;
	
	@Autowired 
	private EmailCheckerDiagnose diagnose;
	
	@Scheduled(initialDelay = 2000, fixedDelay = 60000) // 60s between each run
	public void checkReceivedEmails() {
		LOG.info("Getting new emails to check");
		List<EmailReceived> receivedEmails = emailService.getReceivedEmailByStatus(EmailStatus.RECEIVED);
		LOG.info("{} email(s) to check.", receivedEmails.size());
		int total = receivedEmails.size();
		int current = 0;
		for(EmailReceived receivedEmail : receivedEmails) {
			LOG.info("----> Checking {}/{}", ++current, total);
			List<EmailCheckerDiagnostic> diags = new ArrayList<EmailCheckerDiagnostic>(mailCheckers.size());
			for(EmailChecker mailChecker : mailCheckers) {
				EmailCheckerStatus status;
				try {
					status = mailChecker.checkEmail(receivedEmail);
				} catch(Exception e) {
					LOG.error("Exception while checking with '{}'", mailChecker.checkerName(), e);
					status = EmailCheckerStatus.UNKNOWN;
				}
				EmailCheckerDiagnostic emailCheckerDiagnostic = new EmailCheckerDiagnostic(status, mailChecker.checkerName(), mailChecker.priority());
				diags.add(emailCheckerDiagnostic);
			}
			EmailCheckerStatus status = diagnose.diagnose(receivedEmail, diags);
			receivedEmail.setStatus(DIAGNOSED);
			receivedEmail.setDiagnostic(status);
			emailService.save(receivedEmail);
		}
	}
}
