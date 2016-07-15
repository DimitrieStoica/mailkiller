package org.telaside.mailkiller.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailKillerUser;
import org.telaside.mailkiller.domain.POP3EmailAccount;

public interface EmailAccountRepository extends CrudRepository<EmailAccount, Long >{
	@Query("select a from EmailAccount a where a.login = ?1")
	EmailAccount findByEmailAddress(String emailAddress);

	@Query("select a from POP3EmailAccount a")
	List<POP3EmailAccount> getValidPOP3Accounts();

	@Query("select a from EmailAccount a where a.user = ?1")
	List<EmailAccount> getValidAccountsFor(EmailKillerUser user);

	@Query("select a from EmailAccount a where a.isValid = true")
	List<EmailAccount> getValidEmailAccounts();
}
