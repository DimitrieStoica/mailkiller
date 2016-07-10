package org.telaside;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.telaside.mailkiller.MailkillerApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MailkillerApplication.class)
@WebAppConfiguration
public class MailkillerApplicationTests {

	@Test
	public void contextLoads() {
	}

}
