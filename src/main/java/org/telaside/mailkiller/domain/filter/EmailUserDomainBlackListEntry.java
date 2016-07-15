package org.telaside.mailkiller.domain.filter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.telaside.mailkiller.domain.EmailKillerUser;

@Entity
@DiscriminatorValue("DBL")
public class EmailUserDomainBlackListEntry extends EmailUserBaseObjectFilterEntry {
	public EmailUserDomainBlackListEntry() {}
	
	public EmailUserDomainBlackListEntry(EmailKillerUser user, String filter) {
		super(user, filter);
	}
}
