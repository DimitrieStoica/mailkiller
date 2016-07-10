package org.telaside.mailkiller.protocols.pop3.client;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.domain.POP3EmailAccount;
import org.telaside.mailkiller.service.EmailAccountService;

@Service
public class POP3MailRetrieverScheduler {
	
	@Value("${pop3.thread-pool.size:4}")
	private int poolSize;
	
	@Autowired
	private POP3MailRetriever pop3Retriever;

	@Autowired
	private EmailAccountService emailAccountService;
	
	private ExecutorService execService;
	
	@PostConstruct
	public void init() {
		execService = Executors.newScheduledThreadPool(poolSize);
	}

	@Scheduled(initialDelay = 2000, fixedDelay = 60000) // 60s between each run
    public void retrieveMails() {
		List<POP3EmailAccount> pop3Accounts = emailAccountService.getValidPOP3Account();
		for(POP3EmailAccount account : pop3Accounts) {
			pop3Retriever.retrieveMailsFor(account);
		}
	}
}
