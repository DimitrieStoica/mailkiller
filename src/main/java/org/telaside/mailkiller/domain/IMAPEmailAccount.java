package org.telaside.mailkiller.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("IMAP")
public class IMAPEmailAccount extends EmailAccount {

	@Override
	public String getAccountType() {
		return "IMAP";
	}
}
