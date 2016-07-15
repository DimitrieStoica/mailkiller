package org.telaside.mailkiller.domain.enhanced;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.telaside.mailkiller.domain.EmailReceived;

@Entity
@DiscriminatorValue("MX")
public class EmailReceivedMXEnhanceEntry extends EmailReceivedBaseEnhanceEntry {
	
	public EmailReceivedMXEnhanceEntry() {}

	public EmailReceivedMXEnhanceEntry(EmailReceived email, String mx) {
		super(email, mx);
	}
}
