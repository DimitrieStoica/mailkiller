package org.telaside.mailkiller.checker;

import static javax.naming.Context.INITIAL_CONTEXT_FACTORY;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.naming.NameNotFoundException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MXUtility {
	
	static private final Logger LOG = LoggerFactory.getLogger(MXUtility.class);
	
	static private InitialDirContext idc;
	
	private static final String MX_ATTRIB = "MX";
	private static String[] MX_ATTRIBS = { MX_ATTRIB };
	
	@PostConstruct
	public void initialise() throws Exception {
		Properties env = new Properties();
		env.put(INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
		idc = new InitialDirContext(env);
	}

	public List<String> getMXServers(String domain) throws Exception {
		List<String> servers = new ArrayList<String>();
		try {
			Attributes attrs = idc.getAttributes(domain, MX_ATTRIBS);
			Attribute attr = attrs.get(MX_ATTRIB);
			if (attr != null) {
				for (int i = 0; i < attr.size(); i++) {
					String mxAttr = (String) attr.get(i);
					String[] parts = mxAttr.split(" ");
					servers.add(parts[parts.length - 1]);
				}
			}
		} catch(NameNotFoundException nnfe) {
			LOG.error("Domain {} has no DNS entry - probably SPAM", domain);
		}
		return servers;
	}
}
