package org.telaside.mailkiller.domain;

public enum EmailCheckerStatus {
	CLEAR("CLEAR"),
	CERTAINLY_SPAM("CERTAINLY_SPAM"),
	PROBABLY_SPAM("PROBABLY_SPAM"),
	COULD_BE_SPAM("COULD_BE_SPAM"),
	UNKNOWN("UNKNOWN");
	
	private String status;

	private EmailCheckerStatus(String status) {
		this.status = status;
	}

	public String toString() {
		return status;
	}

	public boolean equalsStatus(String otherStatus) {
		return (otherStatus == null) ? false : status.equals(otherStatus);
	}
}
