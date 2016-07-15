package org.telaside.mailkiller.domain;

import static com.google.common.base.MoreObjects.toStringHelper;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="acc_type")
@Table(name="email_account", 
	indexes = @Index(name = "acc_login_idx", columnList = "acc_login")
)
public abstract class EmailAccount {
	@Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "bigint unsigned")
	private Long id;
	
	@Column(name="valid")
	@Type(type="true_false")
	private boolean isValid;
	
	@ManyToOne(fetch=EAGER)
	@JoinColumn(name="user_id")
	private EmailKillerUser user;
	
	@Column(name = "acc_login", nullable = false, length = 255)
	private String login;
	
	@Column(name = "acc_uid", nullable = false, length = 255)
	private String uid = UUID.randomUUID().toString();
	
	@Column(name = "acc_password", nullable = false, length = 255)
	private String password;
	
	@Column(name = "acc_remote_server", nullable = false, length = 255)
	private String remoteServer;
	
	@OneToMany(mappedBy="emailAccount", fetch=LAZY)
	List<EmailReceived> emails;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public boolean isValid() {
		return isValid;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	public List<EmailReceived> getEmails() {
		return emails;
	}
	public void setEmails(List<EmailReceived> emails) {
		this.emails = emails;
	}

	public EmailKillerUser getUser() {
		return user;
	}
	public void setUser(EmailKillerUser user) {
		this.user = user;
	}
	public String getRemoteServer() {
		return remoteServer;
	}
	public void setRemoteServer(String remoteServer) {
		this.remoteServer = remoteServer;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	abstract public String getAccountType();
	
	public String toString() {
		return toStringHelper(this)
				.add("login", login)
				.toString();
	}

}
