package org.telaside.mailkiller.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface EmailKillerUserRepository extends CrudRepository<EmailKillerUser, Long> {
	
	@Query("select u from EmailKillerUser u where u.principalEmailAddress = ?1")
	EmailKillerUser findByPrincipalEmailAddress(String principal);
}
