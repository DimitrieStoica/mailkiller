package org.telaside.mailkiller.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("POP3")
public class POP3EmailAccount extends EmailAccount {

	@Override
	public String getAccountType() {
		return "POP3";
	}
}
