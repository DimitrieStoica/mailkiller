package org.telaside.mailkiller.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface EmailAccountRepository extends CrudRepository<EmailAccount, Long >{
	@Query("select a from EmailAccount a where a.login = ?1")
	EmailAccount findByEmailAddress(String emailAddress);

	@Query("select a from POP3EmailAccount a")
	List<POP3EmailAccount> getValidPOP3Account();
}
