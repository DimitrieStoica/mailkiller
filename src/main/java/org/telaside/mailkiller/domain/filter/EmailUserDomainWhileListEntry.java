package org.telaside.mailkiller.domain.filter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DWL")
public class EmailUserDomainWhileListEntry extends EmailUserBaseObjectFilterEntry {

}
