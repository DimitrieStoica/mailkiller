package org.telaside.mailkiller.checker;

import org.telaside.mailkiller.domain.EmailReceived;

public interface EmailChecker {
	EmailCheckerStatus checkEmail(EmailReceived email) throws Exception;
	String checkerName();
	int priority();
}
