/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.telaside.mailkiller.protocols.server.pop3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telaside.mailkiller.domain.EmailReceived;
import org.telaside.mailkiller.service.EmailReceivedService;

/**
 * The handler class for POP3 connections.
 *
 */
public class POP3Handler {

	private static final Logger LOG = LoggerFactory.getLogger(POP3Handler.class);

	// POP3 Server identification string used in POP3 headers
	private static final String softwaretype = "MailKiller POP3 Server 0.1";

	// POP3 response prefixes
	private final static String OK_RESPONSE = "+OK"; // OK response. Requested
														// content
														// will follow

	private final static String ERR_RESPONSE = "-ERR"; // Error response.
														// Requested content
														// will not be provided.
														// This prefix
														// is followed by a more
														// detailed
														// error message

	// Authentication states for the POP3 interaction

	private final static int AUTHENTICATION_READY = 0; // Waiting for user id

	private final static int AUTHENTICATION_USERSET = 1; // User id provided,
															// waiting for
															// password

	private final static int TRANSACTION = 2; // A valid user id/password
												// combination
												// has been provided. In this
												// state
												// the client can access the
												// mailbox
												// of the specified user

	// private static final Mail DELETED = new MailImpl(); // A placeholder for
	// emails deleted
	// during the course of the POP3
	// transaction. This Mail instance
	// is used to enable fast checks as
	// to whether an email has been
	// deleted from the inbox.

	/**
	 * The per-service configuration data that applies to all handlers
	 */
	// private POP3HandlerConfigurationData theConfigData;

	/**
	 * The mail server's copy of the user's inbox
	 */
	// private MailRepository userInbox;

	/**
	 * The thread executing this handler
	 */
	private Thread handlerThread;

	/**
	 * The TCP/IP socket over which the POP3 interaction is occurring
	 */
	private Socket socket;

	/**
	 * The reader associated with incoming characters.
	 */
	private CRLFTerminatedReader in;

	/**
	 * The writer to which outgoing messages are written.
	 */
	private PrintWriter out;

	/**
	 * The socket's output stream
	 */
	private OutputStream outs;

	/**
	 * The current transaction state of the handler
	 */
	private int state;

	/**
	 * The user id associated with the POP3 dialogue
	 */
	private String user;

	/**
	 * A dynamic list representing the set of emails in the user's inbox at any
	 * given time during the POP3 transaction.
	 */
	private List<EmailReceived> userMailbox;

	private List<EmailReceived> backupUserMailbox; // A snapshot list representing the set
											// of
											// emails in the user's inbox at the
											// beginning
											// of the transaction

	private EmailReceivedService emailService;

	/**
	 * The watchdog being used by this handler to deal with idle timeouts.
	 */
	// private Watchdog theWatchdog;

	/**
	 * The watchdog target that idles out this handler.
	 */
	// private WatchdogTarget theWatchdogTarget = new POP3WatchdogTarget();

	/**
	 * Set the configuration data for the handler.
	 *
	 * @param theData
	 *            the configuration data
	 */
	// void setConfigurationData(POP3HandlerConfigurationData theData) {
	// theConfigData = theData;
	// }

	/**
	 * Set the Watchdog for use by this handler.
	 *
	 * @param theWatchdog
	 *            the watchdog
	 */
	// void setWatchdog(Watchdog theWatchdog) {
	// this.theWatchdog = theWatchdog;
	// }

	/**
	 * Gets the Watchdog Target that should be used by Watchdogs managing this
	 * connection.
	 *
	 * @return the WatchdogTarget
	 */
	// WatchdogTarget getWatchdogTarget() {
	// return theWatchdogTarget;
	// }

	/**
	 * Idle out this connection
	 */
	void idleClose() {
		LOG.error("POP3 Connection has idled out.");
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {
			// ignored
		} finally {
			socket = null;
		}

		synchronized (this) {
			// Interrupt the thread to recover from internal hangs
			if (handlerThread != null) {
				handlerThread.interrupt();
				handlerThread = null;
			}
		}

	}

	/**
	 * @param emailService
	 * @see org.apache.avalon.cornerstone.services.connection.ConnectionHandler#handleConnection(Socket)
	 */
	public void handleConnection(EmailReceivedService emailService, Socket connection) throws IOException {

		this.emailService = emailService;
		String remoteHost = "";
		String remoteIP = "";

		try {
			this.socket = connection;
			synchronized (this) {
				handlerThread = Thread.currentThread();
			}
			in = new CRLFTerminatedReader(new BufferedInputStream(socket.getInputStream(), 512), "ASCII");
			remoteIP = socket.getInetAddress().getHostAddress();
			remoteHost = socket.getInetAddress().getHostName();
		} catch (Exception e) {
			if (LOG.isErrorEnabled()) {
				StringBuffer exceptionBuffer = new StringBuffer(256).append("Cannot open connection from ")
						.append(remoteHost).append(" (").append(remoteIP).append("): ").append(e.getMessage());
				LOG.error(exceptionBuffer.toString(), e);
			}
		}

		if (LOG.isInfoEnabled()) {
			StringBuffer logBuffer = new StringBuffer(128).append("Connection from ").append(remoteHost).append(" (")
					.append(remoteIP).append(") ");
			LOG.info(logBuffer.toString());
		}

		try {
			outs = new BufferedOutputStream(socket.getOutputStream(), 1024);
			out = new InternetPrintWriter(outs, true);
			state = AUTHENTICATION_READY;
			user = "unknown";
			StringBuffer responseBuffer = new StringBuffer(256).append(OK_RESPONSE).append(" ").append("pulprout")
					.append(" POP3 server (").append(POP3Handler.softwaretype).append(") ready ");
			out.println(responseBuffer.toString());

			// theWatchdog.start();
			while (parseCommand(readCommandLine())) {
				// theWatchdog.reset();
			}
			// theWatchdog.stop();
			if (LOG.isInfoEnabled()) {
				StringBuffer logBuffer = new StringBuffer(128).append("Connection for ").append(user).append(" from ")
						.append(remoteHost).append(" (").append(remoteIP).append(") closed.");
				LOG.info(logBuffer.toString());
			}
		} catch (Exception e) {
			out.println(ERR_RESPONSE + " Error closing connection.");
			out.flush();
			StringBuffer exceptionBuffer = new StringBuffer(128).append("Exception during connection from ")
					.append(remoteHost).append(" (").append(remoteIP).append(") : ").append(e.getMessage());
			LOG.error(exceptionBuffer.toString(), e);
		} finally {
			resetHandler();
		}
	}

	/**
	 * Resets the handler data to a basic state.
	 */
	private void resetHandler() {

		// if (theWatchdog != null) {
		// ContainerUtil.dispose(theWatchdog);
		// theWatchdog = null;
		// }

		// Close and clear streams, sockets

		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (IOException ioe) {
			// Ignoring exception on close
		} finally {
			socket = null;
		}

		try {
			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
			// Ignored
		} finally {
			in = null;
		}

		try {
			if (out != null) {
				out.close();
			}
		} catch (Exception e) {
			// Ignored
		} finally {
			out = null;
		}

		try {
			if (outs != null) {
				outs.close();
			}
		} catch (Exception e) {
			// Ignored
		} finally {
			outs = null;
		}

		synchronized (this) {
			handlerThread = null;
		}

		// Clear user data
		user = null;
		// userInbox = null;
		if (userMailbox != null) {
			userMailbox.clear();
			userMailbox = null;
		}

		if (backupUserMailbox != null) {
			backupUserMailbox.clear();
			backupUserMailbox = null;
		}

		// Clear config data
		// theConfigData = null;
	}

	/**
	 * Implements a "stat". If the handler is currently in a transaction state,
	 * this amounts to a rollback of the mailbox contents to the beginning of
	 * the transaction. This method is also called when first entering the
	 * transaction state to initialize the handler copies of the user inbox.
	 *
	 */
	private void stat() {
		userMailbox = emailService.getClearMessagesFor(user);
	}

	/**
	 * Reads a line of characters off the command line.
	 *
	 * @return the trimmed input line
	 * @throws IOException
	 *             if an exception is generated reading in the input characters
	 */
	final String readCommandLine() throws IOException {
		for (;;)
			try {
				String commandLine = in.readLine();
				if (commandLine != null) {
					commandLine = commandLine.trim();
				}
				return commandLine;
			} catch (CRLFTerminatedReader.TerminationException te) {
				writeLoggedFlushedResponse("-ERR Syntax error at character position " + te.position()
						+ ". CR and LF must be CRLF paired.  See RFC 1939 #3.");
			}
	}

	/**
	 * This method parses POP3 commands read off the wire in handleConnection.
	 * Actual processing of the command (possibly including additional back and
	 * forth communication with the client) is delegated to one of a number of
	 * command specific handler methods. The primary purpose of this method is
	 * to parse the raw command string to determine exactly which handler should
	 * be called. It returns true if expecting additional commands, false
	 * otherwise.
	 *
	 * @param rawCommand
	 *            the raw command string passed in over the socket
	 *
	 * @return whether additional commands are expected.
	 */
	private boolean parseCommand(String rawCommand) {
		if (rawCommand == null) {
			return false;
		}
		boolean returnValue = true;
		String command = rawCommand;
		StringTokenizer commandLine = new StringTokenizer(command, " ");
		int arguments = commandLine.countTokens();
		if (arguments == 0) {
			return true;
		} else if (arguments > 0) {
			command = commandLine.nextToken().toUpperCase(Locale.US);
		}
		if (LOG.isDebugEnabled()) {
			// Don't display password in logger
			if (!command.equals("PASS")) {
				LOG.debug("Command received: " + rawCommand);
			} else {
				LOG.debug("Command received: PASS <password omitted>");
			}
		}
		String argument = null;
		if (arguments > 1) {
			argument = commandLine.nextToken();
		}
		String argument1 = null;
		if (arguments > 2) {
			argument1 = commandLine.nextToken();
		}

		LOG.info("Command {}({}, {})", command, argument, argument1);

		if (command.equals("USER")) {
			doUSER(command, argument, argument1);
		} else if (command.equals("PASS")) {
			doPASS(command, argument, argument1);
		} else if (command.equals("STAT")) {
			doSTAT(command, argument, argument1);
		} else if (command.equals("LIST")) {
			doLIST(command, argument, argument1);
		} else if (command.equals("UIDL")) {
			doUIDL(command, argument, argument1);
		} else if (command.equals("RSET")) {
			doRSET(command, argument, argument1);
		} else if (command.equals("DELE")) {
			doDELE(command, argument, argument1);
		} else if (command.equals("NOOP")) {
			doNOOP(command, argument, argument1);
		} else if (command.equals("RETR")) {
			doRETR(command, argument, argument1);
		} else if (command.equals("TOP")) {
			doTOP(command, argument, argument1);
		} else if (command.equals("QUIT")) {
			returnValue = false;
			doQUIT(command, argument, argument1);
		} else {
			doUnknownCmd(command, argument, argument1);
		}
		return returnValue;
	}

	/**
	 * Handler method called upon receipt of a USER command. Reads in the user
	 * id.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doUSER(String command, String argument, String argument1) {
		String responseString = null;
		if (state == AUTHENTICATION_READY && argument != null) {
			user = argument;
			state = AUTHENTICATION_USERSET;
			responseString = OK_RESPONSE;
		} else {
			responseString = ERR_RESPONSE;
		}
		writeLoggedFlushedResponse(responseString);
	}

	/**
	 * Handler method called upon receipt of a PASS command. Reads in and
	 * validates the password.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doPASS(String command, String argument, String argument1) {
		String responseString = null;
		if (state == AUTHENTICATION_USERSET && argument != null) {
			String passArg = argument;
			if (true) { // theConfigData.getUsersRepository().test(user,
						// passArg)) {
				StringBuffer responseBuffer = new StringBuffer(64).append(OK_RESPONSE).append(" Welcome ").append(user);
				responseString = responseBuffer.toString();
				state = TRANSACTION;
				writeLoggedFlushedResponse(responseString);
				// userInbox = theConfigData.getMailServer().getUserInbox(user);
				stat();
			} else {
				responseString = ERR_RESPONSE + " Authentication failed.";
				state = AUTHENTICATION_READY;
				writeLoggedFlushedResponse(responseString);
			}
		} else {
			responseString = ERR_RESPONSE;
			writeLoggedFlushedResponse(responseString);
		}
	}

	/**
	 * Handler method called upon receipt of a STAT command. Returns the number
	 * of messages in the mailbox and its aggregate size.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doSTAT(String command, String argument, String argument1) {
		String responseString = null;
		if (state == TRANSACTION) {
			long size = 0;
			int count = 0;
			try {
				for (EmailReceived email : userMailbox) {
					count++;
					size += email.messageSize();
				}
				StringBuffer responseBuffer = new StringBuffer(32).append(OK_RESPONSE).append(" ").append(count)
						.append(" ").append(size);
				responseString = responseBuffer.toString();
				writeLoggedFlushedResponse(responseString);
			} catch (Exception e) {// MessagingException me) {
				responseString = ERR_RESPONSE;
				writeLoggedFlushedResponse(responseString);
			}
		} else {
			responseString = ERR_RESPONSE;
			writeLoggedFlushedResponse(responseString);
		}
	}

	/**
	 * Handler method called upon receipt of a LIST command. Returns the number
	 * of messages in the mailbox and its aggregate size, or optionally, the
	 * number and size of a single message.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doLIST(String command, String argument, String argument1) {
		String responseString = null;
		if (state == TRANSACTION) {
			if (argument == null) {
				long size = 0;
				int count = 0;
				try {
					for (EmailReceived email : userMailbox) {
						count++;
						size += email.messageSize();
					}

					StringBuffer responseBuffer = new StringBuffer(32).append(OK_RESPONSE).append(" ").append(count)
							.append(" ").append(size);
					responseString = responseBuffer.toString();
					writeLoggedFlushedResponse(responseString);
					count = 0;
					for (EmailReceived email : userMailbox) {
						 responseBuffer =
						 new StringBuffer(16)
								 .append(count++)
								 .append(" ")
								 .append(email.messageSize());
						 out.println(responseBuffer.toString());
					}
					out.println(".");
					out.flush();
				} catch (Exception e) { // MessagingException me) {
					responseString = ERR_RESPONSE;
					writeLoggedFlushedResponse(responseString);
				}
			} else {
				int num = 0;
				try {
					num = Integer.parseInt(argument);
					EmailReceived mail = userMailbox.get(num); 
					 StringBuffer responseBuffer =
					 new StringBuffer(64)
						 .append(OK_RESPONSE)
						 .append(" ")
						 .append(num)
						 .append(" ")
						 .append(mail.messageSize());
					 responseString = responseBuffer.toString();
					 writeLoggedFlushedResponse(responseString);
					// } else {
					// StringBuffer responseBuffer 
					// new StringBuffer(64)
					// .append(ERR_RESPONSE)
					// .append(" Message (")
					// .append(num)
					// .append(") already deleted.");
					// responseString = responseBuffer.toString();
					// writeLoggedFlushedResponse(responseString);
					// }
					 } catch (IndexOutOfBoundsException npe) {
						 StringBuffer responseBuffer =
						 new StringBuffer(64)
						 	.append(ERR_RESPONSE)
						 	.append(" Message (")
							.append(num).append(") does not exist.");
						 responseString = responseBuffer.toString();
						 writeLoggedFlushedResponse(responseString);
					 } catch (NumberFormatException nfe) {
					StringBuffer responseBuffer = new StringBuffer(64).append(ERR_RESPONSE).append(" ").append(argument)
							.append(" is not a valid number");
					responseString = responseBuffer.toString();
					writeLoggedFlushedResponse(responseString);
				} catch (Exception e) { // MessagingException me) {
					responseString = ERR_RESPONSE;
					writeLoggedFlushedResponse(responseString);
				}
			}
		} else {
			responseString = ERR_RESPONSE;
			writeLoggedFlushedResponse(responseString);
		}
	}

	/**
	 * Handler method called upon receipt of a UIDL command. Returns a listing
	 * of message ids to the client.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doUIDL(String command, String argument, String argument1) {
		String responseString = null;
		if (state == TRANSACTION) {
			if (argument == null) {
				responseString = OK_RESPONSE + " unique-id listing follows";
				writeLoggedFlushedResponse(responseString);
				int count = 0;
				for (EmailReceived email : userMailbox) {
					StringBuffer responseBuffer =
						new StringBuffer(64)
						 .append(count++)
						 .append(" ")
						 .append(email.getMessageId());
					 out.println(responseBuffer.toString());
				 }
				out.println(".");
				out.flush();
			} else {
				int num = 0;
				try {
					num = Integer.parseInt(argument);
					EmailReceived email = userMailbox.get(num);
					 StringBuffer responseBuffer =
					 new StringBuffer(64)
					 .append(OK_RESPONSE)
					 .append(" ")
					 .append(num)
					 .append(" ")
					 .append(email.getMessageId());
					 responseString = responseBuffer.toString();
					 writeLoggedFlushedResponse(responseString);
					// } else {
					// StringBuffer responseBuffer =
					// new StringBuffer(64)
					// .append(ERR_RESPONSE)
					// .append(" Message (")
					// .append(num)
					// .append(") already deleted.");
					// responseString = responseBuffer.toString();
					// writeLoggedFlushedResponse(responseString);
					// }
				} catch (IndexOutOfBoundsException npe) {
					StringBuffer responseBuffer = new StringBuffer(64).append(ERR_RESPONSE).append(" Message (")
							.append(num).append(") does not exist.");
					responseString = responseBuffer.toString();
					writeLoggedFlushedResponse(responseString);
				} catch (NumberFormatException nfe) {
					StringBuffer responseBuffer = new StringBuffer(64).append(ERR_RESPONSE).append(" ").append(argument)
							.append(" is not a valid number");
					responseString = responseBuffer.toString();
					writeLoggedFlushedResponse(responseString);
				}
			}
		} else {
			writeLoggedFlushedResponse(ERR_RESPONSE);
		}
	}

	/**
	 * Handler method called upon receipt of a RSET command. Calls stat() to
	 * reset the mailbox.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doRSET(String command, String argument, String argument1) {
		String responseString = null;
		if (state == TRANSACTION) {
			stat();
			responseString = OK_RESPONSE;
		} else {
			responseString = ERR_RESPONSE;
		}
		writeLoggedFlushedResponse(responseString);
	}

	/**
	 * Handler method called upon receipt of a DELE command. This command
	 * deletes a particular mail message from the mailbox.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doDELE(String command, String argument, String argument1) {
		String responseString = null;
		if (state == TRANSACTION) {
			int num = 0;
			try {
				num = Integer.parseInt(argument);
			} catch (Exception e) {
				responseString = ERR_RESPONSE + " Usage: DELE [mail number]";
				writeLoggedFlushedResponse(responseString);
				return;
			}
			try {
				EmailReceived email = userMailbox.get(num);
				// Mail mc = (Mail) userMailbox.get(num);
				// if (mc == DELETED) {
				// StringBuffer responseBuffer =
				// new StringBuffer(64)
				// .append(ERR_RESPONSE)
				// .append(" Message (")
				// .append(num)
				// .append(") already deleted.");
				// responseString = responseBuffer.toString();
				// writeLoggedFlushedResponse(responseString);
				// } else {
				 //userMailbox.remove(num);
				emailService.markToBeDeleted(email);
				 writeLoggedFlushedResponse(OK_RESPONSE + " Message deleted");
				// }
				//writeLoggedFlushedResponse(OK_RESPONSE + " Message deleted");
			} catch (IndexOutOfBoundsException iob) {
				StringBuffer responseBuffer = new StringBuffer(64).append(ERR_RESPONSE).append(" Message (").append(num)
						.append(") does not exist.");
				responseString = responseBuffer.toString();
				writeLoggedFlushedResponse(responseString);
			}
		} else {
			responseString = ERR_RESPONSE;
			writeLoggedFlushedResponse(responseString);
		}
	}

	/**
	 * Handler method called upon receipt of a NOOP command. Like all good
	 * NOOPs, does nothing much.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doNOOP(String command, String argument, String argument1) {
		String responseString = null;
		if (state == TRANSACTION) {
			responseString = OK_RESPONSE;
			writeLoggedFlushedResponse(responseString);
		} else {
			responseString = ERR_RESPONSE;
			writeLoggedFlushedResponse(responseString);
		}
	}

	/**
	 * Handler method called upon receipt of a RETR command. This command
	 * retrieves a particular mail message from the mailbox.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doRETR(String command, String argument, String argument1) {
		String responseString = null;
		if (state == TRANSACTION) {
			int num = 0;
			try {
				num = Integer.parseInt(argument.trim());
			} catch (Exception e) {
				responseString = ERR_RESPONSE + " Usage: RETR [mail number]";
				writeLoggedFlushedResponse(responseString);
				return;
			}
			try {
				EmailReceived email = userMailbox.get(num);
				// Mail mc = (Mail) userMailbox.get(num);
				// if (mc != DELETED) {
				responseString = OK_RESPONSE + " Message follows";
				writeLoggedFlushedResponse(responseString);
				try {
					ExtraDotOutputStream edouts = new ExtraDotOutputStream(outs);
					writeMessageContentTo(email, edouts);
					edouts.flush();
					edouts.checkCRLFTerminator();
					edouts.flush();
				} catch(IOException ioe) {
					 responseString = ERR_RESPONSE + " Error while retrieving message.";
					 writeLoggedFlushedResponse(responseString);
				} finally {
					out.println(".");
					out.flush();
				}
				// } else {
//				StringBuffer responseBuffer = new StringBuffer(64).append(ERR_RESPONSE).append(" Message (").append(num)
//						.append(") already deleted.");
//				responseString = responseBuffer.toString();
//				writeLoggedFlushedResponse(responseString);
				// }
//				 } catch (IOException ioe) {
//				 responseString = ERR_RESPONSE + " Error while retrieving message.";
//				 writeLoggedFlushedResponse(responseString);
//			} catch (RuntimeException me) {
//				responseString = ERR_RESPONSE + " Error while retrieving message.";
//				writeLoggedFlushedResponse(responseString);
				 } catch (IndexOutOfBoundsException iob) {
				 StringBuffer responseBuffer =
					 new StringBuffer(64)
					 .append(ERR_RESPONSE)
					 .append(" Message (")
					 .append(num)
					 .append(") does not exist.");
				 responseString = responseBuffer.toString();
				 writeLoggedFlushedResponse(responseString);
			}
		} else {
			responseString = ERR_RESPONSE;
			writeLoggedFlushedResponse(responseString);
		}
	}

	/**
	 * Handler method called upon receipt of a TOP command. This command
	 * retrieves the top N lines of a specified message in the mailbox.
	 *
	 * The expected command format is TOP [mail message number] [number of lines
	 * to return]
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doTOP(String command, String argument, String argument1) {
		String responseString = null;
		if (state == TRANSACTION) {
			int num = 0;
			int lines = 0;
			try {
				num = Integer.parseInt(argument);
				lines = Integer.parseInt(argument1);
			} catch (NumberFormatException nfe) {
				responseString = ERR_RESPONSE + " Usage: TOP [mail number] [Line number]";
				writeLoggedFlushedResponse(responseString);
				return;
			}
			try {
			EmailReceived mail = userMailbox.get(num);
			responseString = OK_RESPONSE + " Message follows";
			writeLoggedFlushedResponse(responseString);
			try {
//				 for (Enumeration e = mc.getMessage().getAllHeaderLines(); e.hasMoreElements(); ) {
//					 out.println(e.nextElement());
//				 }
//				 out.println();
				ExtraDotOutputStream edouts = new ExtraDotOutputStream(outs);
				writeMessageContentTo(mail,edouts,lines);
				edouts.flush();
				edouts.checkCRLFTerminator();
				edouts.flush();
			 } finally {
				out.println(".");
				out.flush();
			 }
//			 } else {
//			StringBuffer responseBuffer = new StringBuffer(64).append(ERR_RESPONSE).append(" Message (").append(num)
//					.append(") already deleted.");
//			responseString = responseBuffer.toString();
//			writeLoggedFlushedResponse(responseString);
//			 }
			 } catch (IOException ioe) {
				 responseString = ERR_RESPONSE + " Error while retrieving message.";
				 writeLoggedFlushedResponse(responseString);
//			 } catch (MessagingException me) {
//			 responseString = ERR_RESPONSE + " Error while retrieving message.";
//			 writeLoggedFlushedResponse(responseString);
			 } catch (IndexOutOfBoundsException iob) {
				 StringBuffer exceptionBuffer =
				 new StringBuffer(64)
					 .append(ERR_RESPONSE)
					 .append(" Message (")
					 .append(num)
					 .append(") does not exist.");
				 responseString = exceptionBuffer.toString();
				 writeLoggedFlushedResponse(responseString);
			 }
		} else {
			responseString = ERR_RESPONSE;
			writeLoggedFlushedResponse(responseString);
		}
	}

	/**
	 * Writes the content of the message, up to a total number of lines, out to
	 * an OutputStream.
	 *
	 * @param out
	 *            the OutputStream to which to write the content
	 * @param lines
	 *            the number of lines to write to the stream
	 *
	 * @throws MessagingException
	 *             if the MimeMessage is not set for this MailImpl
	 * @throws IOException
	 *             if an error occurs while reading or writing from the stream
	 */
	public void writeMessageContentTo(EmailReceived message, OutputStream out) throws IOException {
		try(BufferedReader br = new BufferedReader(message.getMailReader())) {
			String line;
			for(;;) {
				if ((line = br.readLine()) == null) {
					break;
				}
				//LOG.info("Sending {}", line);
				line += "\r\n";
				out.write(line.getBytes());
			}
		}
	}
	public void writeMessageContentTo(EmailReceived message, OutputStream out, int lines) throws IOException {
		String line;
		BufferedReader br;
		if (message != null) {
			br = new BufferedReader(message.getMailReader());
			try {
				while (lines-- > 0) {
					if ((line = br.readLine()) == null) {
						break;
					}
					line += "\r\n";
					out.write(line.getBytes());
				}
			} finally {
				br.close();
			}
		} else {
			throw new RuntimeException("No message set for this MailImpl.");
		}
	}

	/**
	 * Handler method called upon receipt of a QUIT command. This method handles
	 * cleanup of the POP3Handler state.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doQUIT(String command, String argument, String argument1) {
		String responseString = null;
		if (state == AUTHENTICATION_READY || state == AUTHENTICATION_USERSET) {
			responseString = OK_RESPONSE + " Apache James POP3 Server signing off.";
			writeLoggedFlushedResponse(responseString);
			return;
		}
		// List toBeRemoved = ListUtils.subtract(backupUserMailbox,
		// userMailbox);
		try {
			// userInbox.remove(toBeRemoved);
			// for (Iterator it = toBeRemoved.iterator(); it.hasNext(); ) {
			// Mail mc = (Mail) it.next();
			// userInbox.remove(mc.getName());
			// }
			responseString = OK_RESPONSE + " Apache James POP3 Server signing off.";
			writeLoggedFlushedResponse(responseString);
		} catch (Exception ex) {
			responseString = ERR_RESPONSE + " Some deleted messages were not removed";
			writeLoggedFlushedResponse(responseString);
			LOG.error("Some deleted messages were not removed: " + ex.getMessage());
		}
	}

	/**
	 * Handler method called upon receipt of an unrecognized command. Returns an
	 * error response and logs the command.
	 *
	 * @param command
	 *            the command parsed by the parseCommand method
	 * @param argument
	 *            the first argument parsed by the parseCommand method
	 * @param argument1
	 *            the second argument parsed by the parseCommand method
	 */
	private void doUnknownCmd(String command, String argument, String argument1) {
		writeLoggedFlushedResponse(ERR_RESPONSE);
	}

	/**
	 * This method logs at a "DEBUG" level the response string that was sent to
	 * the POP3 client. The method is provided largely as syntactic sugar to
	 * neaten up the code base. It is declared private and final to encourage
	 * compiler inlining.
	 *
	 * @param responseString
	 *            the response string sent to the client
	 */
	private final void logResponseString(String responseString) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sent: " + responseString);
		}
	}

	/**
	 * Write and flush a response string. The response is also logged. Should be
	 * used for the last line of a multi-line response or for a single line
	 * response.
	 *
	 * @param responseString
	 *            the response string sent to the client
	 */
	final void writeLoggedFlushedResponse(String responseString) {
		out.println(responseString);
		out.flush();
		logResponseString(responseString);
	}

	/**
	 * Write a response string. The response is also logged. Used for multi-line
	 * responses.
	 *
	 * @param responseString
	 *            the response string sent to the client
	 */
	final void writeLoggedResponse(String responseString) {
		out.println(responseString);
		logResponseString(responseString);
	}
}
