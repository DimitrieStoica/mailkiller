package org.telaside.mailkiller.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailAccountController {
	
	@RequestMapping(path="/hello", method=RequestMethod.GET)
	public String hello(Principal principal) {
		return "Hello " + principal.getName();
	}
}
