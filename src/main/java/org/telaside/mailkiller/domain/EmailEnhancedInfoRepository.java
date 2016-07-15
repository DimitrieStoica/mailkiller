package org.telaside.mailkiller.domain;

import org.springframework.data.repository.CrudRepository;
import org.telaside.mailkiller.domain.enhanced.EmailReceivedBaseEnhanceEntry;

public interface EmailEnhancedInfoRepository extends CrudRepository<EmailReceivedBaseEnhanceEntry, Long> {

}
