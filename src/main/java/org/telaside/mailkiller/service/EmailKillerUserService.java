package org.telaside.mailkiller.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telaside.mailkiller.domain.EmailKillerUser;
import org.telaside.mailkiller.domain.EmailKillerUserRepository;

@Service
@Transactional
public class EmailKillerUserService {
	
	@Autowired
	private EmailKillerUserRepository emailKillerUserRepository;
	
	public EmailKillerUser findByPrincipalEmailAddress(String principalEmail) {
		return emailKillerUserRepository.findByPrincipalEmailAddress((principalEmail));
	}
}
