package org.telaside.mailkiller.controller;

import static org.telaside.mailkiller.domain.EmailCheckerStatus.CERTAINLY_SPAM;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.CLEAR;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.PROBABLY_SPAM;
import static org.telaside.mailkiller.domain.EmailCheckerStatus.UNKNOWN;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.telaside.mailkiller.controller.dto.EmailCheckerResultDTO;
import org.telaside.mailkiller.controller.dto.GlobalStatsDTO;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailAccountStatus;
import org.telaside.mailkiller.domain.EmailCheckerResult;
import org.telaside.mailkiller.domain.EmailCheckerStatus;
import org.telaside.mailkiller.domain.EmailKillerUser;
import org.telaside.mailkiller.domain.EmailStatus;
import org.telaside.mailkiller.domain.EmailSummary;
import org.telaside.mailkiller.security.EmailUserContextProvider;
import org.telaside.mailkiller.service.EmailAccountService;
import org.telaside.mailkiller.service.EmailReceivedService;

@RestController
public class EmailServicesController {
	
	static private final Logger LOG = LoggerFactory.getLogger(EmailServicesController.class);

	@Autowired 
	private EmailUserContextProvider emailUserContextProvider;
	@Autowired 
	private EmailAccountService emailAccountService;
	@Autowired 
	private EmailReceivedService emailReceivedService;
	
	@Transactional
	@RequestMapping(path="/mailstat/{type}/globalstats", method=RequestMethod.GET)
	public GlobalStatsDTO globalStats(@PathVariable String type) {
		EmailKillerUser user = emailUserContextProvider.contextEmailKillerUser();
		GlobalStatsDTO globalStatsDTO = new GlobalStatsDTO();
		globalStatsDTO.forUser(user);
		List<EmailAccount> accounts = emailAccountService.getValidAccountsFor(user);
		for(EmailAccount account : accounts) {
			List<EmailAccountStatus> emailsPerStatus = emailReceivedService.getEmailsPerStatus(account, statusList(type));
			globalStatsDTO.addAccountStats(account, emailsPerStatus);
			LOG.debug("Received {}", emailsPerStatus);
		}
		return globalStatsDTO;
	}
	
	private List<EmailCheckerStatus> statusList(String type) {
		return ("spam".equals(type) ? Arrays.asList(CERTAINLY_SPAM) : Arrays.asList(CLEAR, PROBABLY_SPAM, UNKNOWN));
	}

	@RequestMapping(path="/mailstat/{type}/{account}/stats", method=RequestMethod.GET)
	public List<EmailAccountStatus> statsFor(@PathVariable String type, @PathVariable String account) {
		EmailAccount emailAccount = emailAccountService.findByEmailAddress(account);
		List<EmailAccountStatus> emailsPerStatus = emailReceivedService.getEmailsPerStatus(emailAccount, statusList(type));
		return emailsPerStatus;
	}
	
	@RequestMapping(path="/actonemails/delbandomain", method=RequestMethod.POST)
	public void delBanUserEmails(@RequestBody List<String> messageIds) {
		LOG.info("Del and ban domain on {}", messageIds);
		if(messageIds == null || messageIds.size() == 0) {
			return;
		}
		EmailKillerUser user = emailUserContextProvider.contextEmailKillerUser();
		emailReceivedService.deleteEmailsAndBanDomains(user, messageIds);
	}

	@RequestMapping(path="/actonemails/clear", method=RequestMethod.POST)
	public void clearEmails(@PathVariable String action, @RequestBody List<String> messageIds) {
		LOG.info("{} on {}", action, messageIds);
		if(messageIds == null || messageIds.size() == 0 || action == null) {
			return;
		}
		EmailKillerUser user = emailUserContextProvider.contextEmailKillerUser();
		emailReceivedService.clearEmails(user, messageIds);
	}
	
	@RequestMapping(path="/email/{emailid}/checkerresult", method=RequestMethod.GET)
	public EmailCheckerResultDTO getEmailCheckerResult(@PathVariable String emailid) {
		EmailKillerUser user = emailUserContextProvider.contextEmailKillerUser();
		List<EmailCheckerResult> results = emailReceivedService.getEmailCheckerResult(user, emailid);
		return mapEmailCheckerResultDTO(results);
	}
	
	private EmailCheckerResultDTO mapEmailCheckerResultDTO(List<EmailCheckerResult> results) {
		EmailCheckerResultDTO emailCheckerResultDTO = new EmailCheckerResultDTO();
		StringBuffer sb = new StringBuffer();
		for(EmailCheckerResult result : results) {
			sb.append(result.getDiagnostic());
		}
		emailCheckerResultDTO.setDiagnostic(sb.toString());
		return emailCheckerResultDTO;
	}

	@RequestMapping(path="/emailsfor/{account}/checkerstatus/{diagnostic}", method=RequestMethod.GET)
	public List<EmailSummary> emailsForOfStatus(@PathVariable String account, @PathVariable EmailCheckerStatus diagnostic) {
		EmailKillerUser user = emailUserContextProvider.contextEmailKillerUser();
		List<EmailSummary> summaries = emailReceivedService.emailSummaries(user, account, EmailStatus.DIAGNOSED, diagnostic);
		LOG.info("Returning {} email summaries for {}/{}", summaries.size(), account, diagnostic);
		return summaries;
	}
}
