package org.telaside.mailkiller.checker;

import org.telaside.mailkiller.domain.EmailCheckerStatus;

public class EmailCheckerDiagnostic {
	private EmailCheckerStatus status = EmailCheckerStatus.UNKNOWN;
	private StringBuffer diagnostic = new StringBuffer();
	
	public EmailCheckerDiagnostic() {
	}
	
	public EmailCheckerStatus getStatus() {
		return status;
	}

	public void append(String diagnostic) {
		this.diagnostic.append(diagnostic).append("\n");
	}

	public void status(EmailCheckerStatus status) {
		this.status = status;
	}

	public String diagnostic() {
		return diagnostic.toString();
	}
}
