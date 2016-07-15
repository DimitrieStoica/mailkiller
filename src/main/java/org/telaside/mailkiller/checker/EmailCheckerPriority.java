package org.telaside.mailkiller.checker;

public enum EmailCheckerPriority {
	
	SPAM_BLOCKER_HIGH("SPAM_BLOCKER_HIGH", 0),
	SPAM_BLOCKER_LOW("SPAM_BLOCKER_LOW", 100),
	SPAM_ALERTER("SPAM_ALERTER", 1000);
	
	private String value;
	private int priority;
	
	private EmailCheckerPriority(String value, int priority) {
		this.value = value;
		this.priority = priority;
	}
	
	public String toString() {
		return value;
	}
	
	public int priority() {
		return priority;
	}
}
