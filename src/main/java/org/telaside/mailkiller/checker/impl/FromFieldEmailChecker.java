package org.telaside.mailkiller.checker.impl;

import java.io.IOException;
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
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.checker.EmailChecker;
import org.telaside.mailkiller.checker.EmailCheckerStatus;
import org.telaside.mailkiller.domain.EmailReceived;

@Service
public class FromFieldEmailChecker implements EmailChecker {

	static private final Logger LOG = LoggerFactory.getLogger(FromFieldEmailChecker.class);

	private static final String MX_ATTRIB = "MX";
	private static String[] MX_ATTRIBS = { MX_ATTRIB };

	//@Value("${fromfieldchecker.checkdomain:microsoft.com")
	private String checkDomain = "microsoft.com";

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
	public EmailCheckerStatus checkEmail(EmailReceived email) throws Exception {
		LOG.debug(">>>>>>>> --------- checking {}", email);
		String[] nameAndDomain = email.fromNameAndDomain(); //from.split("@");
		if (nameAndDomain.length != 2) {
			LOG.debug("{} does not split on @", email);
			return EmailCheckerStatus.CERTAINLY_SPAM;
		}
		String user = nameAndDomain[0];
		String domain = nameAndDomain[1];
		LOG.info("Checking user {} @ {}", user, domain);
		return nslLookup(checkDomain, user, domain);
	}

	private EmailCheckerStatus nslLookup(String checkDomain, String user, String domain) throws Exception, IOException {
		List<String> mailHosts = getMXServers(domain);
		if (mailHosts == null || mailHosts.size() == 0) {
			LOG.debug("No MX record for {}", domain);
			return EmailCheckerStatus.PROBABLY_SPAM;
		}
		LOG.debug("lookupMailHosts returned {}", mailHosts);
		SMTPClient smtpClient = new SMTPClient();
		for (String mailHost : mailHosts) {
			try {
				LOG.info("Checking on {}", mailHost);
				smtpClient.setConnectTimeout(30000);
				smtpClient.setDefaultTimeout(30000);
				smtpClient.connect(mailHost);
				LOG.info("Connected on {}", mailHost);
				smtpClient.login(checkDomain);
				smtpClient.setSender("checkspam@" + checkDomain);
				String rcptArg = "<" + user + "@" + domain + ">";
				LOG.debug("rcpt for {}", rcptArg);
				int rcpt = smtpClient.rcpt(rcptArg);
				LOG.info("----- rcpt returned {} {} for {}@{}",
						new Object[] { rcpt, smtpClient.getReplyStrings(), user, domain });
				if (rcpt < 300) {
					LOG.debug("Check has worked !!! - return code is {}", rcpt);
					return EmailCheckerStatus.CLEAR;
				}
			} catch (Exception e) {
				LOG.error("Could not check {}, error is {}", mailHost, e.getMessage());
			} finally {
				if(smtpClient.isConnected()) {
					smtpClient.disconnect();
				}
			}
		}
		return EmailCheckerStatus.UNKNOWN;
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
			LOG.error("Domain {} has no DNS entry - probably smap");
		}
		return servers;
	}

	@Override
	public String checkerName() {
		return "Check the email using the 'From' field";
	}

	@Override
	public int priority() {
		return 0;
	}
}
