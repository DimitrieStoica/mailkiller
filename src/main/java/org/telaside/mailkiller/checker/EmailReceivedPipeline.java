package org.telaside.mailkiller.checker;

import static org.telaside.mailkiller.checker.EmailCheckerPriority.SPAM_BLOCKER_HIGH;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.CERTAINLY_SPAM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.checker.enhancer.EmailEnhancer;
import org.telaside.mailkiller.domain.EmailCheckerStatus;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.EmailStatus;
import org.telaside.mailkiller.domain.enhanced.EmailReceivedBaseEnhanceEntry;
import org.telaside.mailkiller.service.EmailDiagnosticService;
import org.telaside.mailkiller.service.EmailEnhancedInfoService;
import org.telaside.mailkiller.service.EmailReceivedService;

@Service
public class EmailReceivedPipeline {
	
	static private final Logger LOG = LoggerFactory.getLogger(EmailReceivedPipeline.class);
	
	@Autowired
	private EmailDiagnosticService emailDiagnosticService;

	@Autowired
	List<EmailChecker> emailCheckers;
	
	@Autowired
	List<EmailEnhancer> emailEnhancers;

	@Autowired 
	private EmailCheckerDiagnose diagnose;
	
	@Autowired
	private EmailReceivedService emailService;

	@Autowired
	private EmailEnhancedInfoService emailEnhancedInfoService;
	
	@PostConstruct
	public void initialise() {
		Collections.sort(emailCheckers, new Comparator<EmailChecker>(){
			  public int compare(EmailChecker emc1, EmailChecker emc2) {
				  return (emc1.priority().priority() - emc2.priority().priority());
			  }
		});
		LOG.info("Checker list sorted...");
		for(EmailChecker checker : emailCheckers) {
			LOG.info("{} {}", checker.name(), checker.priority());
		}
	}
	

	private void enhance(EmailReceived newEmail) {
		for(EmailEnhancer enhancer : emailEnhancers) {
			List<EmailReceivedBaseEnhanceEntry> infos = enhancer.enhanceInfoAboutEmail(newEmail);
			emailEnhancedInfoService.saveEnhancedInfosForEmail(infos);
		}
	}

	private void diagnose(EmailReceived newEmail) {
		List<EmailCheckerDiagnostic> diags = new ArrayList<EmailCheckerDiagnostic>(emailCheckers.size());
		for(EmailChecker mailChecker : emailCheckers) {
			EmailCheckerDiagnostic diagnostic = new EmailCheckerDiagnostic();
			try {
				mailChecker.checkEmail(newEmail, diagnostic);
			} catch(Exception e) {
				LOG.error("Exception while checking with '{}'", mailChecker.name(), e);
			}
			diags.add(diagnostic);
			emailDiagnosticService.newEmailCheckerResult(newEmail, mailChecker, diagnostic);
			if(diagnostic.getStatus() == CERTAINLY_SPAM && mailChecker.priority() == SPAM_BLOCKER_HIGH) {
				break;
			}
		}
		EmailCheckerStatus status = diagnose.diagnose(newEmail, diags);
		newEmail.setStatus(EmailStatus.DIAGNOSED);
		newEmail.setDiagnostic(status);
	}

	public void executeOn(List<EmailReceived> newEmails) {
		if(newEmails != null) {
			for(EmailReceived newEmail : newEmails) {
				enhance(newEmail);
				diagnose(newEmail);
			}
			emailService.save(newEmails);
		}
	}

}
