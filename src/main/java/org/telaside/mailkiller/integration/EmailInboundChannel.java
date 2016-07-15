package org.telaside.mailkiller.integration;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.protocols.client.EmailRetriever;
import org.telaside.mailkiller.service.EmailAccountService;

public class EmailInboundChannel extends AbstractEndpoint {
	
	static private final Logger LOG = LoggerFactory.getLogger(EmailInboundChannel.class);
	
	@Autowired 
	private List<EmailRetriever> emailRetrievers;
	
	@Autowired
	private EmailAccountService emailAccountService;
	
	private MessageChannel messageChannel;
	public MessageChannel getMessageChannel() {
		return messageChannel;
	}
	public void setMessageChannel(MessageChannel messageChannel) {
		this.messageChannel = messageChannel;
	}
	
	@Value("${emailretrieve.thread-pool.size:4}")
	private int poolSize;

	private ExecutorService execService;

	@PostConstruct
	public void initialise() {
		execService = Executors.newScheduledThreadPool(poolSize);
	}

	@Override
	protected void doStart() {
		LOG.info("starting");
	}

	@Override
	protected void doStop() {
	}
	
	@Scheduled(initialDelay = 2000, fixedDelay = 600) // 60s between each run
    public void retrieveMails() {
		
		List<EmailAccount> accounts = emailAccountService.getValidEmailAccounts();
		List<Future<List<EmailReceived>>> futures = new ArrayList<>();
		
		for(EmailAccount account : accounts) {
			for(EmailRetriever retriever : emailRetrievers) {
				if(retriever.canHandle(account.getAccountType())) {
					Future<List<EmailReceived>> future = execService.submit(new Callable<List<EmailReceived>>() {
						@Override
						public List<EmailReceived> call() throws Exception {
							return retriever.retrieveMailsFor(account);
						}
					});
					futures.add(future);
				}
			}
		}
	}
}
