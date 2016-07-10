package org.telaside.mailkiller.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "email_killer_user", 
	indexes = @Index(name = "p_email_address_idx", columnList = "p_email_address")
)
public class EmailKillerUser {
	@Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "bigint unsigned")
	private Long id;
	
	@Column(name = "p_email_address", nullable = false, length = 255)
	private String principalEmailAddress;
	@Column(name = "hashed_password", nullable = false, length = 255)
	private String hashedPassword;
	@Column(name = "salt", nullable = false, length = 255)
	private String salt;
	
	@OneToMany(mappedBy="user")
	private List<EmailAccount> accounts;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getPrincipalEmailAddress() {
		return principalEmailAddress;
	}
	public void setPrincipalEmailAddress(String principalEmailAddress) {
		this.principalEmailAddress = principalEmailAddress;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}

	public List<EmailAccount> getAccounts() {
		return accounts;
	}
	public void setAccounts(List<EmailAccount> accounts) {
		this.accounts = accounts;
	}
}
