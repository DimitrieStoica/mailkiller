package org.telaside.mailkiller.protocols.pop3.client;

import static org.telaside.mailkiller.domain.MailHeaderPrefix.H_DATE;
import static org.telaside.mailkiller.domain.MailHeaderPrefix.H_DATE_LENGTH;
import static org.telaside.mailkiller.domain.MailHeaderPrefix.H_FROM;
import static org.telaside.mailkiller.domain.MailHeaderPrefix.H_FROM_LENGTH;
import static org.telaside.mailkiller.domain.MailHeaderPrefix.H_ID;
import static org.telaside.mailkiller.domain.MailHeaderPrefix.H_ID_LENGTH;
import static org.telaside.mailkiller.domain.MailHeaderPrefix.H_SUBJECT;
import static org.telaside.mailkiller.domain.MailHeaderPrefix.H_SUBJECT_LENGTH;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.Reader;

import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3MessageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.service.EmailReceivedService;

@Service
public class POP3MailRetriever {
	
	private final static Logger LOG = LoggerFactory.getLogger(POP3MailRetriever.class);
	
	@Autowired
	private EmailReceivedService emailService;
	
    public void retrieveMailsFor(EmailAccount emailAccount) {
    	try {
	    	LOG.info("Retrieve emails");
	    	POP3Client pop3Client = new POP3Client();
	    	LOG.info("Login into pop3 server");
	    	pop3Client.connect(emailAccount.getRemoteServer());
	    	pop3Client.login(emailAccount.getLogin(), emailAccount.getPassword());
	    	LOG.info("connected...");
	    	POP3MessageInfo[] messageInfos = pop3Client.listMessages();
	    	if(messageInfos != null) {
		    	for(POP3MessageInfo messageInfo : messageInfos) {
		    		Reader message = pop3Client.retrieveMessageTop(messageInfo.number, 0);
		    		EmailReceived receivedMessage = retrieveMessage(message);
		    		if(emailService.emailExist(receivedMessage)) {
		    			LOG.debug("Message {} already there", receivedMessage.getMessageId());
		    			continue;
		    		}
		    		message = pop3Client.retrieveMessage(messageInfo.number);
		    		receivedMessage = retrieveMessage(message);
		    		receivedMessage.assignReceivedFields(emailAccount);
			    	emailService.saveIfNew(receivedMessage);
		    	}
	    	}
    	} catch(Exception e) {
    		LOG.error("Error while retrieving emails", e);
    	}
    }

	private EmailReceived retrieveMessage(Reader message) throws Exception {
		char messageChar[] = new char[16384];
		EmailReceived receivedMessage = new EmailReceived();
		CharArrayWriter mailWriter = new CharArrayWriter();
		int length;
		
		while((length = message.read(messageChar)) > 0) {
			mailWriter.write(messageChar, 0, length);
		}
		receivedMessage.setRawEmail(mailWriter.toCharArray());
		
		BufferedReader reader = new BufferedReader(receivedMessage.getMailReader());
		String line;
		int index = 0;
		while((line = reader.readLine()) != null) {
			LOG.debug("{} - Line {}", index++, line);
			if(line.startsWith(H_FROM)) {
				receivedMessage.assignFromFields(line.substring(H_FROM_LENGTH));
			} else if(line.startsWith(H_DATE)) {
				receivedMessage.parseMailDate(line.substring(H_DATE_LENGTH));
				continue;
			} else if(line.startsWith(H_SUBJECT)) {
				receivedMessage.setSubject(line.substring(H_SUBJECT_LENGTH));
				continue;
			} else if(line.length() > H_ID_LENGTH 
					&& line.substring(0, H_ID_LENGTH).toLowerCase().startsWith(H_ID)) { // can be -ID or -Id
				line = line.substring(H_ID_LENGTH).replaceAll("<", "").replaceAll(">", "");
				receivedMessage.setMessageId(line);
				continue;
			} else if(line.length() == 0) {
				LOG.debug("End of the headers");
				break;
			}
		}
		return receivedMessage;
	}
}
