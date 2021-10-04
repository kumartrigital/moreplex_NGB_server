package org.mifosplatform.organisation.message.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailService {

	/*
	 * private String host = ""; private int port = 0; private String username = "";
	 * private String password = "";
	 * 
	 * public EmailService(String host, int port, String username, String password)
	 * { System.out.println("username" + username + " " + password);
	 * 
	 * this.host = host; this.port = port; this.username = username; this.password =
	 * password; System.out.println("username" + username + " " + password);
	 * sendMail(); }
	 */
	/*
	 * private void sendMail() {
	 * 
	 * Properties prop = new Properties(); prop.put("mail.smtp.auth", true);
	 * prop.put("mail.smtp.starttls.enable", "true"); prop.put("mail.smtp.host",
	 * host); prop.put("mail.smtp.port", port); prop.put("mail.smtp.ssl.trust",
	 * host);
	 * 
	 * 
	 * Session session = Session.getInstance(prop, new Authenticator() {
	 * 
	 * @Override protected PasswordAuthentication getPasswordAuthentication() {
	 * return new PasswordAuthentication(username, password); } });
	 * 
	 * Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
	 * protected PasswordAuthentication getPasswordAuthentication() { return new
	 * PasswordAuthentication(username, password); } });
	 * 
	 * 
	 * try { System.out.println("username"+ username + " "+ password);
	 * 
	 * 
	 * Message message = new MimeMessage(session); message.setFrom(new
	 * InternetAddress("mplexbilling@gmail.com"));
	 * message.setRecipients(Message.RecipientType.TO,
	 * InternetAddress.parse("rajkumar03954@gmail.com"));
	 * message.setSubject("Mail Subject");
	 * 
	 * String msg = "This is my first email using JavaMailer";
	 * 
	 * MimeBodyPart mimeBodyPart = new MimeBodyPart(); mimeBodyPart.setContent(msg,
	 * "text/html");
	 * 
	 * MimeBodyPart attachmentBodyPart = new MimeBodyPart();
	 * attachmentBodyPart.attachFile(new File("pom.xml"));
	 * 
	 * Multipart multipart = new MimeMultipart();
	 * multipart.addBodyPart(mimeBodyPart);
	 * multipart.addBodyPart(attachmentBodyPart);
	 * 
	 * message.setContent(multipart);
	 * 
	 * Transport.send(message);
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } }
	 */
	// 1) get the session object

	public void sendMail() {

		Properties properties = System.getProperties();

		properties.put("mail.smtp.auth", true);
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", 587);
		properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

		Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("mplexbilling@gmail.com", "Forusbyus");
			}
		});

		// 2) compose message
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress("mplexbilling@gmail.com"));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress("rajkumar03954@gmail.com"));
			message.setSubject("hello");

			StringBuilder messageBuilder = new StringBuilder().append("hi").append("hello").append("hello");

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(messageBuilder.toString(), "text/html");

			MimeBodyPart mimeBodyAttachPart = new MimeBodyPart();

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// 6) set the multiplart object to the message object
			message.setContent(multipart);

			// 7) send message
			Transport.send(message);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
	//	new EmailService("smtp.gmail.com", 587, "mplexbilling@gmail.com", "Forusbyus");
	
	EmailService email = new EmailService();
	email.sendMail();
	}

}