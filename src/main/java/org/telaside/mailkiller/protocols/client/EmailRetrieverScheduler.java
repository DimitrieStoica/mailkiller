package org.telaside.mailkiller.protocols.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.service.EmailAccountService;

@Service
public class EmailRetrieverScheduler {
	
	static private final Logger LOG = LoggerFactory.getLogger(EmailRetrieverScheduler.class);
	
	@Value("${emailretrieve.thread-pool.size:4}")
	private int poolSize;
	
	@Autowired
	private List<EmailRetriever> emailRetrievers;

	@Autowired
	private EmailAccountService emailAccountService;
	
	private ExecutorService execService;
	
	@PostConstruct
	public void init() {
		execService = Executors.newScheduledThreadPool(poolSize);
	}

	@Scheduled(initialDelay = 2000, fixedDelay = 60000) // 60s between each run
    public void retrieveMails() {
		List<EmailAccount> accounts = emailAccountService.getValidEmailAccounts();
		List<Future<Object>> futures = new ArrayList<>();
		for(EmailAccount account : accounts) {
			for(EmailRetriever retriever : emailRetrievers) {
				if(retriever.canHandle(account.getAccountType())) {
					Future<Object> future = execService.submit(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							retriever.retrieveMailsFor(account);
							return null;
						}
					});
					futures.add(future);
				}
			}
		}
		
		for(Future<Object> future : futures) {
			try {
				future.get();
			} catch(InterruptedException ie) {
				return;
			} catch(ExecutionException ee) {
				LOG.error("Future {} terminated in exception", future, ee.getCause());
			}
		}
	}
}
