package org.telaside.mailkiller.checker.enhancer;

import java.util.List;

import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.enhanced.EmailReceivedBaseEnhanceEntry;

public interface EmailEnhancer {
	List<EmailReceivedBaseEnhanceEntry> enhanceInfoAboutEmail(EmailReceived email);
}
