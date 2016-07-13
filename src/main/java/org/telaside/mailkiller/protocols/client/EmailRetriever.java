package org.telaside.mailkiller.protocols.client;

import org.telaside.mailkiller.domain.EmailAccount;

public interface EmailRetriever {
	boolean canHandle(String type);
	void retrieveMailsFor(EmailAccount emailAccount);
	
}
