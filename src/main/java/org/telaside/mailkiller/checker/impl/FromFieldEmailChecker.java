package org.telaside.mailkiller.checker.impl;

import static org.telaside.mailkiller.domain.EmailCheckerStatus.CERTAINLY_SPAM;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.CLEAR;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.UNKNOWN;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.net.smtp.SMTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.checker.EmailChecker;
import org.telaside.mailkiller.checker.EmailCheckerDiagnostic;
import org.telaside.mailkiller.domain.EmailReceived;

@Service
public class FromFieldEmailChecker implements EmailChecker {

	static private final Logger LOG = LoggerFactory.getLogger(FromFieldEmailChecker.class);

	private static final String MX_ATTRIB = "MX";
	private static String[] MX_ATTRIBS = { MX_ATTRIB };
	
	@Value("${fromfieldchecker.timeout.connect:30000}")
	private int connectTimeout;

	@Value("${fromfieldchecker.timeout.default:30000}")
	private int defaultTimeout;

	@Value("${fromfieldchecker.check.domain:microsoft.com}")
	private String checkDomain = "microsoft.com";

	@Value("${fromfieldchecker.check.user:checkspam@microsoft.com}")
	private String checkUserDomain;

	private InitialDirContext idc;

	public FromFieldEmailChecker() {
		try {
			Properties env = new Properties();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
			idc = new InitialDirContext(env);
		} catch (Exception e) {
			LOG.error("Cannot init FromFieldEmailChecker()", e);
			throw new RuntimeException(e);
		}
	}
	@Override
	public void checkEmail(EmailReceived email, EmailCheckerDiagnostic diagnostic) throws Exception {
		LOG.debug(">>>>>>>> --------- checking {}", email);
		String[] nameAndDomain = email.fromNameAndDomain(); //from.split("@");
		if (nameAndDomain.length != 2) {
			LOG.debug("{} does not split on @", email);
			diagnostic.append(String.format("From %s is badly formed", email.getHeaderFrom()));
			diagnostic.status(CERTAINLY_SPAM);
		}
		String user = nameAndDomain[0];
		String domain = nameAndDomain[1];
		LOG.debug("Checking user {} @ {}", user, domain);
		nslLookup(diagnostic, user, domain);
	}

	private void nslLookup(EmailCheckerDiagnostic diagnostic, String user, String domain) throws Exception, IOException {
		List<String> mailHosts = getMXServers(domain);
		if (mailHosts == null || mailHosts.size() == 0) {
			LOG.error("No MX record for {}", domain);
			diagnostic.append(String.format("No MX record for %s", domain));
			diagnostic.status(CERTAINLY_SPAM);
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
						diagnostic.status(CERTAINLY_SPAM);
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

	public List<String> getMXServers(String domain) throws Exception {
		List<String> servers = new ArrayList<String>();
		try {
			Attributes attrs = idc.getAttributes(domain, MX_ATTRIBS);
			Attribute attr = attrs.get(MX_ATTRIB);
			if (attr != null) {
				for (int i = 0; i < attr.size(); i++) {
					String mxAttr = (String) attr.get(i);
					String[] parts = mxAttr.split(" ");
					servers.add(parts[parts.length - 1]);
				}
			}
		} catch(NameNotFoundException nnfe) {
			LOG.error("Domain {} has no DNS entry - probably SPAM", domain);
		}
		return servers;
	}

	@Override
	public String checkerName() {
		return "Email 'From' field checker";
	}

	@Override
	public int priority() {
		return 0;
	}
}
