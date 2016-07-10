package org.telaside.mailkiller.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class EmailUserAuthProvider implements AuthenticationProvider {
	
	static final private Logger LOG = LoggerFactory.getLogger(EmailUserAuthProvider.class);

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String principal = (String)authentication.getPrincipal();
		String credentials = (String)authentication.getCredentials();
		return authentication;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		LOG.info("support {} ?", authentication);
		return true;
	}
}
