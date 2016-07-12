package org.telaside.mailkiller.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.domain.EmailKillerUser;
import org.telaside.mailkiller.service.EmailKillerUserService;

@Service
public class EmailUserContextProvider {
	
	@Autowired
	private EmailKillerUserService emailAccountService;

	public EmailKillerUser contextEmailKillerUser() {
		return emailAccountService.findByPrincipalEmailAddress("m.meyer@telaside.com");
	}
}
