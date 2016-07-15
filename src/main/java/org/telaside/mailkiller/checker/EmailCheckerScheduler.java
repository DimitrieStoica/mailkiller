package org.telaside.mailkiller.checker;

import static org.telaside.mailkiller.domain.EmailStatus.DIAGNOSED;

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
import org.springframework.stereotype.Component;
import org.telaside.mailkiller.domain.EmailCheckerStatus;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.EmailStatus;
import org.telaside.mailkiller.service.EmailDiagnosticService;
import org.telaside.mailkiller.service.EmailReceivedService;

import com.google.common.collect.Lists;

@Component
public class EmailCheckerScheduler {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmailCheckerScheduler.class);
	
	@Value("${emailretrieve.thread-pool.size:4}")
	private int poolSize;
	
	@Value("${emailcheck.batch.size:40}")
	private int batchSize;

	@Autowired
	private EmailReceivedService emailService;
	
	@Autowired
	private EmailDiagnosticService emailDiagnosticService;

	@Autowired
	List<EmailChecker> mailCheckers;
	
	@Autowired 
	private EmailCheckerDiagnose diagnose;
	
	private ExecutorService execService;
	
	
	@PostConstruct
	public void init() {
		execService = Executors.newScheduledThreadPool(poolSize);
	}
	
	@Autowired
	private EmailReceivedPipeline emailReceivedPipeline;
	
	@Scheduled(initialDelay = 2000, fixedDelay = 60000) // 60s between each run
	public void checkReceivedEmails() {
		List<EmailReceived> receivedEmails = emailService.getReceivedEmailByStatus(EmailStatus.RECEIVED);
		List<Future<Object>> futures = new ArrayList<>();
		for (List<EmailReceived> partition : Lists.partition(receivedEmails, batchSize)) {
			Future<Object> future = execService.submit(new Callable<Object>() {
				@Override
				public Object call() {
					emailReceivedPipeline.executeOn(partition);
					return null;
				}});
			futures.add(future);
		}
		waitForEnd(futures);
	}
	
	private void waitForEnd(List<Future<Object>> futures) {
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
