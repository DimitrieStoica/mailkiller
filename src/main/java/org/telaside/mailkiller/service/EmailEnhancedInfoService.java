package org.telaside.mailkiller.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telaside.mailkiller.domain.EmailEnhancedInfoRepository;
import org.telaside.mailkiller.domain.enhanced.EmailReceivedBaseEnhanceEntry;

@Service
@Transactional
public class EmailEnhancedInfoService {
	
	@Autowired
	private EmailEnhancedInfoRepository emailEnhancedInfoRepository;

	public void saveEnhancedInfosForEmail(List<EmailReceivedBaseEnhanceEntry> infos) {
		for(EmailReceivedBaseEnhanceEntry info : infos) {
			emailEnhancedInfoRepository.save(info);
		}
	}

}
