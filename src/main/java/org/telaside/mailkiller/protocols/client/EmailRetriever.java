package org.telaside.mailkiller.protocols.client;

import java.util.List;

import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailReceived;

public interface EmailRetriever {
	boolean canHandle(String type);
	List<EmailReceived> retrieveMailsFor(EmailAccount emailAccount);
	
}
