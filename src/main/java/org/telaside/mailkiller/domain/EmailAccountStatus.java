package org.telaside.mailkiller.domain;

import static com.google.common.base.MoreObjects.toStringHelper;

public class EmailAccountStatus {
	
	private long count;
	private EmailCheckerStatus status;
	
	public EmailAccountStatus() {}
	
	public EmailAccountStatus(long count, EmailCheckerStatus status) {
		this.count = count;
		this.status = status;
	}
	
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public EmailCheckerStatus getStatus() {
		return status;
	}
	public void setStatus(EmailCheckerStatus status) {
		this.status = status;
	}
	
	public String toString() {
		return toStringHelper(this)
				.add("count", count)
				.add("status", status)
				.toString();
	}
}
