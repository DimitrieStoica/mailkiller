package org.telaside.mailkiller.domain;

import static org.telaside.mailkiller.domain.EmailStatus.RECEIVED;

import java.io.CharArrayReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;


@Entity
@Table(name = "received_email", 
	indexes = @Index(name = "email_status_idx", columnList = "email_status")
)
public class EmailReceived {
	
	static private final Logger LOG = LoggerFactory.getLogger(EmailReceived.class);
	
	// Contains formats of the Date: field - probably not enough
	// TODO: check Sun, 10 Jul 2016 12:06:42 00200. - cannot be parsed.
	static private final String[] formats = {
			"EEE, d MMM yyyy HH:mm:ss Z", 
			"d MMM yyyy HH:mm:ss Z"
	};

	@Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "bigint unsigned")
	private Long id;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="email_acc_id")
	private EmailAccount emailAccount;
	
    @Column(name = "raw_data", columnDefinition = "LONGTEXT")
	private char[] rawEmail;
    
	@Column(name = "message_date", columnDefinition = "datetime")
	private Date messageDate;

    @Column(name = "received_at", nullable = false, columnDefinition = "datetime")
	private Date receivedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_status", length = 20)
	private EmailStatus status;
	
    @Enumerated(EnumType.STRING)
    @Column(name = "email_diag", length = 20)
	private EmailCheckerStatus diagnostic;

    @Column(name = "h_from", length = 255)
	private String headerFrom;
    
    @Column(name = "i_from", length = 255)
	private String from;

    @Column(name = "h_subject", length = 255)
	private String subject;
    @Column(name = "h_id", length = 255, nullable = false, unique = true)
	private String messageId;
	
	@Column(name = "internal_id", length = 255, nullable = false, unique = true)
	private String internalMessageId = UUID.randomUUID().toString();

    @Transient
	private Reader mailReader;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	
	public EmailAccount getEmailAccount() {
		return emailAccount;
	}
	public void setEmailAccount(EmailAccount emailAccount) {
		this.emailAccount = emailAccount;
	}

	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
    public String getInternalMessageId() {
		return internalMessageId;
	}
	public void setInternalMessageId(String internalMessageId) {
		this.internalMessageId = internalMessageId;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}
	public Date getMessageDate() {
		return messageDate;
	}
	public void setMessageDate(Date messageDate) {
		this.messageDate = messageDate;
	}
	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}
	public String getHeaderFrom() {
		return headerFrom;
	}
	public void setHeaderFrom(String headerFrom) {
		this.headerFrom = headerFrom;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public EmailStatus getStatus() {
		return status;
	}
	public void setStatus(EmailStatus status) {
		this.status = status;
	}
	
	public Reader getMailReader() {
		//LOG.info("Full message is {}", new String(rawEmail));
		return new CharArrayReader(this.rawEmail == null ? new char[0] : this.rawEmail);
	}

	public EmailCheckerStatus getDiagnostic() {
		return diagnostic;
	}
	public void setDiagnostic(EmailCheckerStatus diagnostic) {
		this.diagnostic = diagnostic;
	}
	
	public char[] getRawEmail() {
		return rawEmail;
	}
	public void setRawEmail(char[] rawEmail) {
		this.rawEmail = rawEmail;
	}
	
	public void parseMailDate(String stringDate) {
		for(String format : formats) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				setMessageDate(sdf.parse(stringDate));
				return;
			} catch(ParseException e) {
				LOG.debug("Cannot parse {} with {} - trying different format", stringDate, format);
			}
		}
		LOG.error("No format availale for {}. setting date to 0", stringDate);
		setMessageDate(new Date(0));
	}
	
	public void assignFromFields(String from) {
		this.setHeaderFrom(from);
		this.setFrom(fromInternetAddress());
	}
	
	public String fromInternetAddress() {
		int start = headerFrom.lastIndexOf('<');
		if(start == -1) {
			return headerFrom.trim();
		}
		int end = headerFrom.lastIndexOf('>');
		return headerFrom.substring(start + 1, end);
	}
	
	public int messageSize() {
		return rawEmail == null ? 0 : rawEmail.length;
	}
	
	public String[] fromNameAndDomain() {
		return from.split("@");
	}
	
	public void assignReceivedFields(EmailAccount emailAccount) {
		setEmailAccount(emailAccount);
		setReceivedDate(new Date());
		setStatus(RECEIVED);
	}
	
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("messageId", messageId)
				.add("from", from)
				.add("subject", subject)
				.add("status", status)
				.add("rawEmailSize", rawEmail == null ? -1 : rawEmail.length)
				.toString();
	}
}
