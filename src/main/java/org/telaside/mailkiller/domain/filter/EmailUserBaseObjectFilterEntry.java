package org.telaside.mailkiller.domain.filter;

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

import org.telaside.mailkiller.domain.EmailKillerUser;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="ufi_type")
@Table(name="user_object_filter", 
	indexes = @Index(name = "ufi_user_filter_idx", columnList = "ufi_user,ufi_filter")
)
public abstract class EmailUserBaseObjectFilterEntry {
	@Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "bigint unsigned")
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ufi_user")
	private EmailKillerUser user;

    @Column(name = "ufi_filter", length = 255)
	private String filter;
    
    public EmailUserBaseObjectFilterEntry() {}
	
	public EmailUserBaseObjectFilterEntry(EmailKillerUser user, String filter) {
		this.user = user;
		this.filter = filter;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}

	public EmailKillerUser getUser() {
		return user;
	}
	public void setUser(EmailKillerUser user) {
		this.user = user;
	}
}
