package org.telaside.mailkiller.checker.impl;

import static org.telaside.mailkiller.checker.EmailCheckerPriority.SPAM_ALERTER;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.CLEAR;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.PROBABLY_SPAM;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.UNKNOWN;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import org.apache.commons.net.smtp.SMTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.checker.EmailChecker;
import org.telaside.mailkiller.checker.EmailCheckerDiagnostic;
import org.telaside.mailkiller.checker.EmailCheckerPriority;
import org.telaside.mailkiller.checker.MXUtility;
import org.telaside.mailkiller.domain.EmailReceived;

@Service
public class FromFieldEmailChecker implements EmailChecker {

	static private final Logger LOG = LoggerFactory.getLogger(FromFieldEmailChecker.class);

	@Value("${fromfieldchecker.timeout.connect:30000}")
	private int connectTimeout;

	@Value("${fromfieldchecker.timeout.default:30000}")
	private int defaultTimeout;

	@Value("${fromfieldchecker.check.domain:microsoft.com}")
	private String checkDomain = "microsoft.com";

	@Value("${fromfieldchecker.check.user:checkspam@microsoft.com}")
	private String checkUserDomain;
	
	@Autowired
	private MXUtility mxUtility;

	@Override
	public void checkEmail(EmailReceived email, EmailCheckerDiagnostic diagnostic) throws Exception {
		LOG.debug(">>>>>>>> --------- checking {}", email);
		String[] nameAndDomain = email.fromNameAndDomain(); //from.split("@");
		if (nameAndDomain.length != 2) {
			LOG.debug("{} does not split on @", email);
			diagnostic.append(String.format("From %s is badly formed", email.getHeaderFrom()));
			diagnostic.status(PROBABLY_SPAM);
		}
		String user = nameAndDomain[0];
		String domain = nameAndDomain[1];
		LOG.debug("Checking user {} @ {}", user, domain);
		nslLookup(diagnostic, user, domain);
	}

	private void nslLookup(EmailCheckerDiagnostic diagnostic, String user, String domain) throws Exception, IOException {
		List<String> mailHosts = mxUtility.getMXServers(domain);
		if (mailHosts == null || mailHosts.size() == 0) {
			LOG.error("No MX record for {}", domain);
			diagnostic.append(String.format("No MX record for %s", domain));
			diagnostic.status(PROBABLY_SPAM);
			return;
		}
		diagnostic.status(UNKNOWN);
	
		String rcptArg = "<" + user + "@" + domain + ">";
		
		SMTPClient smtpClient = new SMTPClient();
		for(String mailHost : mailHosts) {
			try {
				smtpLogin(smtpClient, mailHost);
				LOG.debug("rcpt for {}", rcptArg);
				int rcpt = smtpClient.rcpt(rcptArg);
				diagnostic.append(String.format("rcpt %s on %s returned %s", 
						mailHost, rcptArg, rcpt, smtpClient.getReplyStrings()));
				LOG.debug("----- rcpt returned {} {} for {}@{}",
						new Object[] { rcpt, smtpClient.getReplyStrings(), user, domain });
				if(rcpt < 300) {
					LOG.debug("Check has worked !!! - return code is {}", rcpt);
					diagnostic.status(CLEAR);
					return;
				} else {
					if(rcpt == 550) {
						diagnostic.append("RCPT returned 550 - User do not exist");
						diagnostic.status(PROBABLY_SPAM);
						return;
					}
					diagnostic.status(UNKNOWN);
				}
			} catch (Exception e) {
				String error = String.format("Error for smtp %s - %s", mailHost, e.getMessage());
				LOG.debug(error);
				diagnostic.append(error);
			} finally {
				if(smtpClient.isConnected()) {
					try { smtpClient.disconnect(); } catch(Exception e) {};
				}
			}
		}
	}
	private void smtpLogin(SMTPClient smtpClient, String mailHost) throws SocketException, IOException {
		smtpClient.setConnectTimeout(connectTimeout);
		smtpClient.setDefaultTimeout(defaultTimeout);
		smtpClient.connect(mailHost);
		LOG.debug("Connected on {}", mailHost);
		smtpClient.login(checkDomain);
		smtpClient.setSender(checkUserDomain);
	}

	@Override
	public String name() {
		return "Email 'From' field checker";
	}

	@Override
	public EmailCheckerPriority priority() {
		return SPAM_ALERTER;
	}
}
