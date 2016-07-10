package org.telaside.mailkiller.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.telaside.mailkiller.checker.EmailCheckerStatus;

public interface EmailReceivedRepository extends CrudRepository<EmailReceived, Long> {
	@Query("select r from EmailReceived r where r.messageId = ?1")
	public EmailReceived getReceivedEmailByMessageId(String id);

	@Query("select r from EmailReceived r where r.status = ?1")
	public List<EmailReceived> getReceivedEmailByStatus(EmailStatus status);

	@Query("select r from EmailReceived r where r.emailAccount.login = ?1 and r.status = ?2 and r.diagnostic = ?3 order by r.messageDate asc")
	public List<EmailReceived> getReceivedEmailsFor(String user, EmailStatus status, EmailCheckerStatus checker);
}
