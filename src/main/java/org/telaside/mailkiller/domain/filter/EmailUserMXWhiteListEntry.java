package org.telaside.mailkiller.domain.filter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("MTXWL")
public class EmailUserMXWhiteListEntry extends EmailUserBaseObjectFilterEntry {

}
