package org.telaside.mailkiller.domain.filter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("AWL")
public class EmailUserAddressWhiteListEntry extends EmailUserBaseObjectFilterEntry {

}
