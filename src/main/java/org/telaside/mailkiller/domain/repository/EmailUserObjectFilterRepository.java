package org.telaside.mailkiller.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.domain.EmailKillerUser;
import org.telaside.mailkiller.domain.filter.EmailUserBaseObjectFilterEntry;
import org.telaside.mailkiller.domain.filter.EmailUserDomainBlackListEntry;

@Service
public interface EmailUserObjectFilterRepository extends CrudRepository<EmailUserBaseObjectFilterEntry, Long>{
	
	@Query("select bl from EmailUserDomainBlackListEntry bl where bl.user = ?1")
	List<EmailUserDomainBlackListEntry> getDomainBlackListFor(EmailKillerUser user);
}
