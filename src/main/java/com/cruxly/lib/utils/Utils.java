package com.cruxly.lib.utils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.cruxly.lib.analytics.Application;
import com.cruxly.lib.analytics.StringUtils;
import com.cruxly.lib.analytics.TextSegment;
import com.cruxly.lib.model.EmailMessage;
import com.cruxly.lib.model.TextSegmentEx;

public class Utils {
	private static final String HTML_LINE_BREAK = "<p>";

	private static final Logger log = Logger.getLogger(Application.class.getName());
	
	public static String[] getRulesFromGrammarFile(String grammarName) {
		
		InputStream is = Utils.class.getClassLoader().getResourceAsStream(grammarName);
		if (is != null) {
			// log.info("Following file found in app " +
			// SURFACE_ANALYSIS_GRAMMER_TXT);
		} else {
			log.severe("Unable to load " + grammarName);
		}
		return parseSurfaceGrammar(is);
	}
	
	public static String [] parseSurfaceGrammar(InputStream inputStream)  {
		StringBuffer buffer = new StringBuffer();

		try {
			// Read it a text file
			int read = -1;
			byte[] bytes = new byte[500];
			while ((read = inputStream.read(bytes)) > 0) {
				buffer.append(new String(bytes,0,read));
			}

			inputStream.close();
		} catch (java.io.IOException e) {
			throw new RuntimeException("Unable to load surface_grammar");
		}

		return StringUtils.splitByNewlines(buffer.toString());
	}

	public static String getAnnotationsWithLineBreaks(TextSegmentEx[] segments) {
		StringBuffer speechActsBuffer = new StringBuffer();
		StringBuffer commitmentsBuffer = new StringBuffer();
		boolean foundActionItem = false;
		boolean foundCommitment = false;
		for (int i = 0; i < segments.length; i++) {
			TextSegment segment = segments[i].segment;
			if (segment.posStart > 0) {
				speechActsBuffer.append(HTML_LINE_BREAK);
				commitmentsBuffer.append(HTML_LINE_BREAK);
			}
			if (segments[i].type == Application.INSTANCE.analyzers.COMMITMENT) {
				commitmentsBuffer.append(segment.text.substring(
						segment.posStart, segment.posEnd));
				foundCommitment = true;
			} else {
				speechActsBuffer.append(segment.text.substring(
						segment.posStart, segment.posEnd));
				foundActionItem = true;
			}
			speechActsBuffer.append(HTML_LINE_BREAK);
			commitmentsBuffer.append(HTML_LINE_BREAK);
			if (segment.posEnd < segment.text.length()) {
				speechActsBuffer.append(HTML_LINE_BREAK);
				commitmentsBuffer.append(HTML_LINE_BREAK);
			}
		}
		if (!foundActionItem) {
			speechActsBuffer.append("No speech act detected");
			speechActsBuffer.append(HTML_LINE_BREAK);
		}
		if (!foundCommitment) {
			commitmentsBuffer.append("No commitments detected");
			commitmentsBuffer.append(HTML_LINE_BREAK);
		}
		String questions = "<b>Actions items:</b>" + HTML_LINE_BREAK;
		questions += speechActsBuffer.toString();
		questions = questions.replace('\n', ' ');
		questions = questions.replace('\r', ' ');

		String commitments = "<b>Commitments</b>" + HTML_LINE_BREAK;
		commitments += commitmentsBuffer.toString();
		commitments = commitments.replace('\n', ' ');
		commitments = commitments.replace('\r', ' ');

		String response = questions + commitments;

		return response;
	}

	public static EmailMessage getEmailMessage(String input) {
		String messageId = String.valueOf(new Date().getTime());
		String from = "foo@bar.com";
		String[] to = new String[]{"foo1@bar1.com"};
		Date date = new Date();
		String subject = "";
		String text = input;
		Object nativeMessage = null;
		EmailMessage emailMessage = new EmailMessage(messageId, from, to, date,
				subject, text, nativeMessage);
		return emailMessage;
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html
	 *            the html string to escape
	 * @return the escaped string
	 */
	@SuppressWarnings("unused")
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

	public static void sendMail(Address[] recipientAddresses, String messageBody) {

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("bot@cruxly.com", "Cruxly Bot"));
			InternetAddress[] replyTo = {new InternetAddress(
					"support@cruxly.com", "Cruxly Support")};
			message.setReplyTo(replyTo);

			for (Address address : recipientAddresses) {
				message.addRecipient(Message.RecipientType.TO, address);
			}

			message.setSubject("Cruxly analysis");

			message.setContent(messageBody, "text/html");
			Transport.send(message);
		} catch (AddressException e) {
			log.severe("Exception sending email: " + e.getMessage());
		} catch (MessagingException e) {
			log.severe("Exception sending email: " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.severe("Exception sending email: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private String getRandomSubString(String emailBody) {
		String randomSubString = emailBody.substring(
				getRandom(0, emailBody.length() / 2),
				getRandom(emailBody.length() / 2, emailBody.length() - 1));
		return randomSubString;
	}

	private static int getRandom(int from, int to) {
		if (from < to)
			return from + new Random().nextInt(Math.abs(to - from));
		return from - new Random().nextInt(Math.abs(to - from));
	}
}
