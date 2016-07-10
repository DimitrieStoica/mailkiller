package org.telaside.mailkiller.checker;

import static org.telaside.mailkiller.checker.EmailCheckerStatus.PROBABLY_SPAM;
import static org.telaside.mailkiller.checker.EmailCheckerStatus.UNKNOWN;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.domain.EmailReceived;

@Service
public class EmailCheckerDiagnose {
	
	static private final Logger LOG = LoggerFactory.getLogger(EmailCheckerDiagnose.class);
	
	public EmailCheckerStatus diagnose(EmailReceived receivedEmail, List<EmailCheckerDiagnostic> diags) {
		EmailCheckerStatus status = UNKNOWN;
		for(EmailCheckerDiagnostic diag : diags) {
			switch(diag.getStatus()) {
				case CLEAR :
				case CERTAINLY_SPAM :
					status = diag.getStatus();
					break;
				case PROBABLY_SPAM :
					status = diag.getStatus();
					continue;
				case COULD_BE_SPAM :
					if(status != PROBABLY_SPAM) {
						status = diag.getStatus();
						continue;
					}
				case UNKNOWN :
					continue;
			}
			break;
		}
		LOG.debug("Diag is {} for {}", status, receivedEmail.getSubject());
		return status;
	}
}
