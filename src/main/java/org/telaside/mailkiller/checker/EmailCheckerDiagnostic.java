package org.telaside.mailkiller.checker;

public class EmailCheckerDiagnostic {
	private EmailCheckerStatus status;
	private String checkerName;
	private int checkerPriority;
	
	public EmailCheckerDiagnostic(EmailCheckerStatus status, String checkerName, int checkerPriority) {
		this.status = status;
		this.checkerName = checkerName;
		this.checkerPriority = checkerPriority;
	}
	
	public EmailCheckerStatus getStatus() {
		return status;
	}
	public String getCheckerName() {
		return checkerName;
	}
	public int getCheckerPriority() {
		return checkerPriority;
	}
	
	
}
