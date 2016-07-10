package org.telaside.mailkiller.protocols.pop3.server;

import java.net.ServerSocket;
import java.net.Socket;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telaside.mailkiller.service.EmailReceivedService;

@Service
public class POP3Listener {

	private static final Logger LOG = LoggerFactory.getLogger(POP3Listener.class);
	
	@Value("${pop3.secure.port:2222}")
	private int securedPort;
	
	@Value("${pop3.nonsecure.port:1111}")
	private int nonSecuredPort;

	@Autowired
	private EmailReceivedService emailService;

	@PostConstruct
	public void startServers() throws Exception {
		startPOP3();
		startPOP3TLS();
	}
	
	class RunServer implements Runnable {
		boolean isSecure;
		int port;
		
		public RunServer(boolean isSecure, int port) {
			this.isSecure = isSecure;
			this.port = port;
		}
		
		@Override
		public void run() {
			LOG.info("POP3Listener started port {} secure {}", port, isSecure);
			try (ServerSocket socket = createServerSocket()) {
				for (;;) {
					LOG.info("POP3Listener listens to incoming connection.");
					Socket connection = socket.accept();
					LOG.info("POP3Listener accepting a connection");
					new Thread(new Runnable() {
						public void run() {
							try {
								new POP3Handler().handleConnection(emailService, connection);
							} catch (Exception e) {
								LOG.error("POP3Handler throwed an exception", e);
							}
						}
					}).start();
				}
			} catch (Exception e) {
				LOG.error("Cannot bound to {}", port, e);
			}
		}
		
		ServerSocket createServerSocket() throws Exception {
			if(isSecure) {
				SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
				return ssf.createServerSocket(port);
			}
			return new ServerSocket(port);
		}
	}
	
	private void startPOP3TLS() {
		new Thread(new RunServer(true, securedPort)).start();
	}
	
	private void startPOP3() {
		new Thread(new RunServer(false, nonSecuredPort)).start();
	}
}
