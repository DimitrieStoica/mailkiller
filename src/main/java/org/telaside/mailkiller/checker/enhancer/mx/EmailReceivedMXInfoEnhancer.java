package org.telaside.mailkiller.checker.enhancer.mx;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.checker.MXUtility;
import org.telaside.mailkiller.checker.enhancer.EmailEnhancer;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.enhanced.EmailReceivedBaseEnhanceEntry;
import org.telaside.mailkiller.domain.enhanced.EmailReceivedMXEnhanceEntry;

@Service
public class EmailReceivedMXInfoEnhancer implements EmailEnhancer {
	
	@Autowired
	private MXUtility mxUtility;

	@Override
	public List<EmailReceivedBaseEnhanceEntry> enhanceInfoAboutEmail(EmailReceived email) {
		
		List<EmailReceivedBaseEnhanceEntry> results = new ArrayList<EmailReceivedBaseEnhanceEntry>();
		String domain = email.getDomain();
		if(domain != null) {
			try {
				List<String> mxs = mxUtility.getMXServers(domain);
				for(String mx : mxs) {
					results.add(new EmailReceivedMXEnhanceEntry(email, mx));
				}
			} catch(Exception e) {
				
			}
		}
		return results;
	}
}
