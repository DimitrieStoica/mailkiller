package org.telaside.mailkiller.checker;

import org.telaside.mailkiller.domain.EmailReceived;

public interface EmailChecker {
	void checkEmail(EmailReceived email, EmailCheckerDiagnostic diagnostic) throws Exception;
	String name();
	EmailCheckerPriority priority();
}
