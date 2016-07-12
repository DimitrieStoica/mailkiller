package org.telaside.mailkiller.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.telaside.mailkiller.controller.dto.GlobalStatsDTO;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailAccountStatus;
import org.telaside.mailkiller.domain.EmailCheckerStatus;
import org.telaside.mailkiller.domain.EmailKillerUser;
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
	@RequestMapping(path="/mailstat/globalstats", method=RequestMethod.GET)
	public GlobalStatsDTO globalStats() {
		EmailKillerUser user = emailUserContextProvider.contextEmailKillerUser();
		GlobalStatsDTO globalStatsDTO = new GlobalStatsDTO();
		globalStatsDTO.forUser(user);
		List<EmailAccount> accounts = emailAccountService.getValidAccountsFor(user);
		for(EmailAccount account : accounts) {
			List<EmailAccountStatus> emailsPerStatus = emailReceivedService.getEmailsPerStatus(account);
			globalStatsDTO.addAccountStats(account, emailsPerStatus);
			LOG.info("Received {}", emailsPerStatus);
		}
		return globalStatsDTO;
	}
	
	@RequestMapping(path="/emailsfor/{account}/status/{status}", method=RequestMethod.GET)
	public List<EmailSummary> emailsForOfStatus(@PathVariable String account, @PathVariable EmailCheckerStatus status) {
		EmailKillerUser user = emailUserContextProvider.contextEmailKillerUser();
		List<EmailSummary> summaries = emailReceivedService.emailSummaries(user, account, status);
		LOG.info("Returning {} email summaries for {}/{}", summaries.size(), account, status);
		return summaries;
	}
}
