package org.telaside.mailkiller.domain.filter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ABL")
public class EmailUserAddressBlackListEntry extends EmailUserBaseObjectFilterEntry {

}
