package org.telaside.mailkiller.domain;

public enum EmailStatus {
	
	RECEIVED("RECEIVED"),
	TO_BE_DELETED("TO_BE_DELETED"),
	DELETED("DELETED"),
	DIAGNOSED("DIAGNOSED");

	private String status;

	private EmailStatus(String status) {
		this.status = status;
	}

	public String toString() {
		return status;
	}

	public boolean equalsStatus(String otherStatus) {
		return (otherStatus == null) ? false : status.equals(otherStatus);
	}
}
