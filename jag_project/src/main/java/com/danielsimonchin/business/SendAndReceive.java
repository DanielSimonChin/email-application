package com.danielsimonchin.business;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import javax.activation.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Flags;

import jodd.mail.EmailFilter;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.ImapServer;
import jodd.mail.MailServer;
import jodd.mail.RFC2822AddressParser;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;

/**
 * This is a demo of the code necessary to carry out the following tasks: 1)
 * Send plain text email 2) send plain text message with multiple cc 3) send
 * html email with an embedded image and an attachment 4) Receive email
 * including attachments
 *
 * Removed System.out.println with LOG.info Added an HTML section to sendEmail()
 *
 * @author Daniel Simon Chin 1836462
 * @version 2.1
 *
 */
public class SendAndReceive {
     // Real programmers use logging
    private final static Logger LOG = LoggerFactory.getLogger(SendAndReceive.class);

    // These must be updated to your email accounts
    // These must be updated to your email accounts
    private final String smtpServerName = "smtp.gmail.com";
    private final String imapServerName = "imap.gmail.com";
    // To use this program you need to fill in the following with two Gmail accounts
    // The cc fields can contain any valid email address
    private final String emailSend = "danieldawsontest1@gmail.com";
    private final String emailReceive = "danieldawsontest2@gmail.com";
    private final String emailSendPwd = "Danieltester1";
    private final String emailReceivePwd = "Danieltester2";
    private final String emailCC1 = "danieldawsontest3@gmail.com";
    private final String emailCC2 = "recievedanieldawson1@gmail.com";

    private final int secondsToSleep = 3;

    /**
     * This method is where the different uses of Jodd are demonstrated
     */
    /*public void perform() {
        // Send an ordinary text message
        sendEmail();
        try {
            Thread.sleep(secondsToSleep * 1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        receiveEmail();
        sendEmailWithCC();
        try {
            Thread.sleep(secondsToSleep * 1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        receiveEmail();
        try {
            sendWithEmbeddedAndAttachment();
            try {
                Thread.sleep(secondsToSleep * 1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            receiveEmail();
        } catch (Exception ex) {
            LOG.debug("Problem sending with embedded attachment.", ex);
        }

    }*/

    /**
     * Standard send routine using Jodd. Jodd knows about GMail so no need to
     * include port information
     */
    public void sendEmail() {

        if (checkEmail(emailSend) && checkEmail(emailReceive)) {
            // Create am SMTP server object
            SmtpServer smtpServer = MailServer.create()
                    .ssl(true)
                    .host(smtpServerName)
                    .auth(emailSend, emailSendPwd)
                    //.debugMode(true)
                    .buildSmtpMailServer();

            // Using the fluent style of coding create a plain text message
            Email email = Email.create().from(emailSend)
                    .to(emailReceive)
                    .subject("Jodd Test")
                    .textMessage("Hello from plain text email: " + LocalDateTime.now())
                    .htmlMessage("<html><META http-equiv=Content-Type "
                            + "content=\"text/html; charset=utf-8\">"
                            + "<body><h1>HTML Message</h1>"
                            + "<h2>Here is some text in the HTML message</h2></body></html>");

            // Like a file we open the session, send the message and close the
            // session
            try ( // A session is the object responsible for communicating with the server
                     SendMailSession session = smtpServer.createSession()) {
                // Like a file we open the session, send the message and close the
                // session
                session.open();
                session.sendMail(email);
                LOG.info("Email sent");
            }
        } else {
            LOG.info("Unable to send email because either send or recieve addresses are invalid");
        }
    }

    /**
     * Example with CC field
     */
    public void sendEmailWithCC() {

        if (checkEmail(emailSend) && checkEmail(emailReceive) && checkEmail(emailCC1) && checkEmail(emailCC2)) {

            // Create am SMTP server object
            SmtpServer smtpServer = MailServer.create()
                    .ssl(true)
                    .host(smtpServerName)
                    .auth(emailSend, emailSendPwd)
                    //.debugMode(true)
                    .buildSmtpMailServer();

            // Using the fluent style requires EmailMessage
            Email email = Email.create().from(emailSend)
                    .to(emailReceive)
                    .cc(new String[]{emailCC1, emailCC2})
                    .subject("Jodd Test").textMessage("Hello from plain text email with cc");

            // Like a file we open the session, send the message and close the
            // session
            try ( // A session is the object responsible for communicating with the server
                     SendMailSession session = smtpServer.createSession()) {
                // Like a file we open the session, send the message and close the
                // session
                session.open();
                session.sendMail(email);
                LOG.info("Email sent");
            }
        } else {
            LOG.info("Unable to send email because either send or recieve addresses are invalid");
        }
    }

    /**
     * Standard receive routine for Jodd using an ImapServer. Assumes the
     * existence of a folder c:\temp
     */
    public void receiveEmail() {

        if (checkEmail(emailReceive)) {
            ImapServer imapServer = MailServer.create()
                    .host(imapServerName)
                    .ssl(true)
                    .auth(emailReceive, emailReceivePwd)
                    //.debugMode(true)
                    .buildImapMailServer();

            try ( ReceiveMailSession session = imapServer.createSession()) {
                session.open();
                LOG.info("Message count: " + session.getMessageCount());
                ReceivedEmail[] emails = session.receiveEmailAndMarkSeen(EmailFilter.filter().flag(Flags.Flag.SEEN, false));
                if (emails != null) {
                    LOG.info("\n >>>> ReceivedEmail count = " + emails.length);
                    for (ReceivedEmail email : emails) {
                        LOG.info("\n\n===[" + email.messageNumber() + "]===");

                        // common info
                        LOG.info("FROM:" + email.from());
                        // Handling array in email object
                        LOG.info("TO:" + Arrays.toString(email.to()));
                        LOG.info("CC:" + Arrays.toString(email.cc()));
                        LOG.info("SUBJECT:" + email.subject());
                        LOG.info("PRIORITY:" + email.priority());
                        LOG.info("SENT DATE:" + email.sentDate());
                        LOG.info("RECEIVED DATE: " + email.receivedDate());

                        // process messages
                        List<EmailMessage> messages = email.messages();

                        messages.stream().map((msg) -> {
                            LOG.info("------");
                            return msg;
                        }).map((msg) -> {
                            LOG.info(msg.getEncoding());
                            return msg;
                        }).map((msg) -> {
                            LOG.info(msg.getMimeType());
                            return msg;
                        }).forEachOrdered((msg) -> {
                            LOG.info(msg.getContent());
                        });

                        // process attachments
                        List<EmailAttachment<? extends DataSource>> attachments = email.attachments();
                        if (attachments != null) {
                            LOG.info("+++++");
                            attachments.stream().map((attachment) -> {
                                LOG.info("name: " + attachment.getName());
                                return attachment;
                            }).map((attachment) -> {
                                LOG.info("cid: " + attachment.getContentId());
                                return attachment;
                            }).map((attachment) -> {
                                LOG.info("size: " + attachment.getSize());
                                return attachment;
                            }).forEachOrdered((attachment) -> {
                                attachment.writeToFile(
                                        new File("c:\\temp", attachment.getName()));
                            });
                        }
                    }
                }
            }
        } else {
            LOG.info("Unable to send email because either send or recieve addresses are invalid");
        }
    }

    /**
     * Here we create an email message that contains html, embedded image, and
     * an attachment
     *
     * @throws Exception In case we don't find the file to attach/embed
     */
    public void sendWithEmbeddedAndAttachment() throws Exception {

        if (checkEmail(emailSend) && checkEmail(emailReceive)) {
            SmtpServer smtpServer = MailServer.create()
                    .ssl(true)
                    .host(smtpServerName)
                    .auth(emailSend, emailSendPwd)
                    //.debugMode(true)
                    .buildSmtpMailServer();

            // Using the fluent style of coding create a plain text message
            Email email = Email.create().from(emailSend)
                    .to(emailReceive)
                    .subject("Jodd Test").textMessage("Hello from plain text email: " + LocalDateTime.now())
                    .htmlMessage("<html><META http-equiv=Content-Type "
                            + "content=\"text/html; charset=utf-8\">"
                            + "<body><h1>Here is my photograph embedded in "
                            + "this email.</h1><img src='cid:FreeFall.jpg'>"
                            + "<h2>I'm flying!</h2></body></html>")
                    .embeddedAttachment(EmailAttachment.with().content(new File("FreeFall.jpg")))
                    .attachment(EmailAttachment.with().content("WindsorKen180.jpg"));

            // Like a file we open the session, send the message and close the
            // session
            try ( // A session is the object responsible for communicating with the server
                     SendMailSession session = smtpServer.createSession()) {
                // Like a file we open the session, send the message and close the
                // session
                session.open();
                session.sendMail(email);
                LOG.info("Email sent");
            }
        } else {
            LOG.info("Unable to send email because either send or recieve addresses are invalid");
        }
    }

    /**
     * Use the RFC2822AddressParser to validate that the email string could be a
     * valid address
     *
     * @param address
     * @return true is OK, false if not
     */
    private boolean checkEmail(String address) {
        return RFC2822AddressParser.STRICT.parseToEmailAddress(address) != null;
    }

    /**
     * It all begins here
     *
     * @param args
     */
    public static void main(String[] args) {

        //SendAndReceive m = new SendAndReceive();
        //m.perform();
        //System.exit(0);
    }
}
   

