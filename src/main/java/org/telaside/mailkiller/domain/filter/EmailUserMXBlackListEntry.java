package org.telaside.mailkiller.domain.filter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("MXBL")
public class EmailUserMXBlackListEntry extends EmailUserBaseObjectFilterEntry {

}
