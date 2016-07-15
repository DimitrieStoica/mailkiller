package org.telaside.mailkiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan
//@EnableIntegration
//@ImportResource(locations="classpath:si-email.xml")
public class MailkillerApplication {
	
	private static final Logger LOG = LoggerFactory.getLogger(MailkillerApplication.class);

	public static void main(String[] args) {
		LOG.info("Mail killer started...");
		SpringApplication.run(MailkillerApplication.class, args);
	}
}
