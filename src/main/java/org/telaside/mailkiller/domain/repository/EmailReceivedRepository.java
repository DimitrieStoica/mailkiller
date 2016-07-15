package org.telaside.mailkiller.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.telaside.mailkiller.domain.EmailAccount;
import org.telaside.mailkiller.domain.EmailAccountStatus;
import org.telaside.mailkiller.domain.EmailCheckerResult;
import org.telaside.mailkiller.domain.EmailCheckerStatus;
import org.telaside.mailkiller.domain.EmailKillerUser;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.domain.EmailStatus;
import org.telaside.mailkiller.domain.EmailSummary;

public interface EmailReceivedRepository extends CrudRepository<EmailReceived, Long> {
	@Query("select r from EmailReceived r where r.messageId = ?1")
	EmailReceived getReceivedEmailByMessageId(String id);

	@Query("select r from EmailReceived r where r.status = ?1")
	List<EmailReceived> getReceivedEmailByStatus(EmailStatus status);

	@Query("select r from EmailReceived r where r.emailAccount.login = ?1 and r.status = ?2 and r.diagnostic = ?3 order by r.messageDate asc")
	List<EmailReceived> getReceivedEmailsFor(String user, EmailStatus status, EmailCheckerStatus checker);

	@Query("select new org.telaside.mailkiller.domain.EmailAccountStatus(count(r.status), r.diagnostic) from EmailReceived r where r.emailAccount = ?1 and r.status in ?2 and r.diagnostic in ?3 group by r.diagnostic")
	List<EmailAccountStatus> getEmailsPerStatus(EmailAccount account, List<EmailStatus> statuses, List<EmailCheckerStatus> diags);

	@Query("select new org.telaside.mailkiller.domain.EmailSummary(r.headerFrom, r.subject, r.internalMessageId, r.messageDate, r.diagnostic, r.status) from EmailReceived r where r.emailAccount.user = ?1 and r.status = ?2 and r.emailAccount = ?3 and r.diagnostic = ?4 order by  r.messageDate desc")
	List<EmailSummary> findSummariesFor(EmailKillerUser user, EmailStatus status, EmailAccount account, EmailCheckerStatus diagnostic);
	
	@Query("select r.id from EmailReceived r where r.emailAccount.user = ?1 and r.internalMessageId in (?3)")
	List<Long> getReceivedEmailsByUserAndIds(EmailKillerUser user, List<String> messageId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="update received_email set email_diag = ?1 where internal_id in (?2)", nativeQuery = true)
	void updateEmailCheckerStatus(String string, List<String> messageId);

	@Modifying(clearAutomatically = true)
	@Query(value="update received_email set email_status = ?1 where internal_id in (?2)", nativeQuery = true)
	void updateEmailStatus(String string, List<String> messageId);

	@Query("select r.domain from EmailReceived r where r.domain is not null and r.internalMessageId in ?1")
	List<String> getDomainFromMessages(List<String> messageIds);
	
	@Query("select r.from from EmailReceived r where r.from is not null and r.messageId in ?1")
	List<String> getFromFromMessages(List<String> ids);
	
	@Query("select r from EmailCheckerResult r where r.emailChecked.emailAccount.user = ?1 and r.emailChecked.internalMessageId = ?2")
	List<EmailCheckerResult> getEmailCheckerResult(EmailKillerUser user, String internalMessageId);
}
