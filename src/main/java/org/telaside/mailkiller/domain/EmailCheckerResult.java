package org.telaside.mailkiller.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="email_checker_result")
public class EmailCheckerResult {
	@Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "bigint unsigned")
	private Long id;
	
	@ManyToOne()
	@JoinColumn(name="chk_email_id")
	private EmailReceived emailChecked;
	
	@Column(name = "chk_diag_txt", columnDefinition = "LONGTEXT")
	private String diagnostic;
	
    @Enumerated(EnumType.STRING)
    @Column(name = "chk_diag", length = 20)
	private EmailCheckerStatus status;
    
    @Column(name = "chk_checker_name", length = 255)
	private String checker;

    @Column(name = "chk_checker_priority")
	private int priority;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public EmailReceived getEmailChecked() {
		return emailChecked;
	}
	public void setEmailChecked(EmailReceived emailChecked) {
		this.emailChecked = emailChecked;
	}
	public String getDiagnostic() {
		return diagnostic;
	}
	public void setDiagnostic(String diagnostic) {
		this.diagnostic = diagnostic;
	}
	public EmailCheckerStatus getStatus() {
		return status;
	}
	public void setStatus(EmailCheckerStatus status) {
		this.status = status;
	}
	public String getChecker() {
		return checker;
	}
	public void setChecker(String checker) {
		this.checker = checker;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
