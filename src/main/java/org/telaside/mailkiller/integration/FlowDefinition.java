package org.telaside.mailkiller.integration;

//@Configuration
//@EnableIntegration
public class FlowDefinition {
	
//	@Bean
//	public MessageChannel newEmailsChannel() {
//		return new DirectChannel();
//	}
//	
//	@Bean
//	@InboundChannelAdapter(value="newEmailsChannel", poller = @Poller(fixedDelay = "60000"))
//	public MessageSource<List<EmailReceived>> newEmails() {
//		return new NewEmailsMessageSource();
//	}
//	
//	@Bean
//	@ServiceActivator()
//	public Splitter r() {
//		return null;
//	}

//	@Bean
//	@InboundChannelAdapter(value = "inputChannel") //, poller = @Poller(fixedDelay = "60000"))
//	public MessageSource<EmailReceived> emailRetrieve() {
//		return new AccountRetrieveHandler();
//	}

//	@Bean
//	@Transformer(inputChannel = "inputChannel", outputChannel = "httpChannel")
//	public ObjectToMapTransformer toMapTransformer() {
//		return new ObjectToMapTransformer();
//	}
//
//	@Bean
//	@ServiceActivator(inputChannel = "httpChannel")
//	public MessageHandler httpHandler() {
//		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://foo/service");
//		handler.setExpectedResponseType(String.class);
//		handler.setOutputChannelName("outputChannel");
//		return handler;
//	}
//
//	@Bean
//	@ServiceActivator(inputChannel = "outputChannel")
//	public LoggingHandler loggingHandler() {
//		return new LoggingHandler("info");
//	}
}
