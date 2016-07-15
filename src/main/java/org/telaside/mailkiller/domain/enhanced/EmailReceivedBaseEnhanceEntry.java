package org.telaside.mailkiller.domain.enhanced;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.telaside.mailkiller.domain.EmailReceived;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="uei_type")
@Table(name="email_enhance_info", 
	indexes = @Index(name = "eei_email_enhance_idx", columnList = "eei_email,eei_info")
)
public abstract class EmailReceivedBaseEnhanceEntry {
	@Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "bigint unsigned")
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="eei_email")
	private EmailReceived email;

    @Column(name = "eei_info", length = 255)
	private String info;
    
    public EmailReceivedBaseEnhanceEntry() {}
	
	public EmailReceivedBaseEnhanceEntry(EmailReceived email, String info) {
		this.email = email;
		this.info = info;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public EmailReceived getEmail() {
		return email;
	}

	public void setEmail(EmailReceived email) {
		this.email = email;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
}
