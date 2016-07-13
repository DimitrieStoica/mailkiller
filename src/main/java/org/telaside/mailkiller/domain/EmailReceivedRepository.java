package org.telaside.mailkiller.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface EmailReceivedRepository extends CrudRepository<EmailReceived, Long> {
	@Query("select r from EmailReceived r where r.messageId = ?1")
	EmailReceived getReceivedEmailByMessageId(String id);

	@Query("select r from EmailReceived r where r.status = ?1")
	List<EmailReceived> getReceivedEmailByStatus(EmailStatus status);

	@Query("select r from EmailReceived r where r.emailAccount.login = ?1 and r.status = ?2 and r.diagnostic = ?3 order by r.messageDate asc")
	List<EmailReceived> getReceivedEmailsFor(String user, EmailStatus status, EmailCheckerStatus checker);

	@Query("select new org.telaside.mailkiller.domain.EmailAccountStatus(count(r.status), r.diagnostic) from EmailReceived r where r.emailAccount = ?1 and r.diagnostic is not null group by r.diagnostic")
	List<EmailAccountStatus> getEmailsPerStatus(EmailAccount account);

	@Query("select new org.telaside.mailkiller.domain.EmailSummary(r.headerFrom, r.subject, r.internalMessageId, r.messageDate, r.diagnostic, r.status) from EmailReceived r where r.emailAccount.user = ?1 and r.emailAccount = ?2 and r.diagnostic = ?3 order by  r.messageDate desc")
	List<EmailSummary> findSummariesFor(EmailKillerUser user, EmailAccount account, EmailCheckerStatus status);
	
	@Query("select r.id from EmailReceived r where r.emailAccount.user = ?1 and r.internalMessageId in (?3)")
	List<Long> getReceivedEmailsByUserAndIds(EmailKillerUser user, List<String> messageId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="update received_email set email_diag = ?1 where internal_id in (?2)", nativeQuery = true)
	void updateEmailCheckerStatus(String string, List<String> messageId);
}
