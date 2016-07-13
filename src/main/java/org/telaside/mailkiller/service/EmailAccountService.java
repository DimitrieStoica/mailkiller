package org.telaside.mailkiller.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailAccountRepository;
import org.telaside.mailkiller.domain.EmailKillerUser;
import org.telaside.mailkiller.domain.POP3EmailAccount;

@Service
@Transactional
public class EmailAccountService {
	
	@Autowired
	private EmailAccountRepository accountRepository;
	
	@Transactional(readOnly=true)
	public EmailAccount findByEmailAddress(String emailAddress) {
		return accountRepository.findByEmailAddress(emailAddress);
	}

	public List<EmailAccount> getValidEmailAccounts() {
		return accountRepository.getValidEmailAccounts();
	}

	public List<POP3EmailAccount> getValidPOP3Accounts() {
		return accountRepository.getValidPOP3Accounts();
	}
	
	public List<EmailAccount> getValidAccountsFor(EmailKillerUser user) {
		return accountRepository.getValidAccountsFor(user);
	}
}
