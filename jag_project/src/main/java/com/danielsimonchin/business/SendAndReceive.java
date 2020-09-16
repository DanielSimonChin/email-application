package com.danielsimonchin.business;

import data.MailConfigBean;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.mail.Flags;
import jodd.mail.EmailFilter;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;
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
    
    private MailConfigBean mailConfigBean;

    public SendAndReceive(MailConfigBean mc) {
        this.mailConfigBean = mc;
    }

    /**
     * Creates an Email object, sends it and returns the Email object. Validates
     * all the email addresses in the ArrayLists.
     *
     * @param toList List of all recipients in the 'To' field
     * @param ccList List of all cc recipients
     * @param bccList List of all bcc recipients
     * @param subject Subject of email
     * @param textMsg plain text of the email
     * @param htmlMsg html text of the email
     * @param regularAttachments List of File objects
     * @param embeddedAttachments List of File objects that will be embedded in
     * the email
     * @return An Email object which was created and sent.
     */
    public Email sendEmail(ArrayList<String> toList, ArrayList<String> ccList, ArrayList<String> bccList, String subject, String textMsg, String htmlMsg, ArrayList<File> regularAttachments, ArrayList<File> embeddedAttachments) {
        //The constructed and sent email that will be returned
        Email email;
        if (checkEmail(this.mailConfigBean.getUserEmailAddress()) && verifyEmailList(toList) && verifyEmailList(ccList) && verifyEmailList(bccList)) {
            // Create am SMTP server object
            SmtpServer smtpServer = MailServer.create()
                    .ssl(true)
                    .host(this.mailConfigBean.getHost())
                    .auth(this.mailConfigBean.getUserEmailAddress(), this.mailConfigBean.getPassword())
                    .buildSmtpMailServer();

            //Create an email with the needed parameters
            email = createEmail(toList, ccList, bccList, subject, textMsg, htmlMsg, regularAttachments, embeddedAttachments);
            // Like a file we open the session, send the message and close the
            // session
            try ( // A session is the object responsible for communicating with the server
                     SendMailSession session = smtpServer.createSession()) {
                // Like a file we open the session, send the message and close the
                // session
                session.open();
                session.sendMail(email);
            }
        } else {
            throw new IllegalArgumentException("Unable to send email because either send or recieve addresses are invalid");
        }
        return email;
    }

    /**
     * Helper method that creates and returns an Email object given the
     * parameters needed to create an email
     *
     * @param toList List of all recipients in the 'To' field
     * @param ccList List of all cc recipients
     * @param bccList List of all bcc recipients
     * @param subject Subject of email
     * @param textMsg plain text of the email
     * @param htmlMsg html text of the email
     * @param regularAttachments List of File objects
     * @param embeddedAttachments List of File objects that will be embedded in
     * the email
     * @return An Email object with the correct values and parameters needed.
     */
    private Email createEmail(ArrayList<String> toList, ArrayList<String> ccList, ArrayList<String> bccList, String subject, String textMsg, String htmlMsg, ArrayList<File> regularAttachments, ArrayList<File> embeddedAttachments) {
        // Using the fluent style of coding create a plain text message
        Email email = Email.create().from(this.mailConfigBean.getUserEmailAddress());
        email.subject(subject);
        email.textMessage(textMsg);
        email.htmlMessage(htmlMsg);
        toList.forEach(emailAddress -> {
            email.to(emailAddress);
        });
        ccList.forEach(emailAddress -> {
            email.cc(emailAddress);
        });
        bccList.forEach(emailAddress -> {
            email.bcc(emailAddress);
        });
        regularAttachments.forEach(attachment -> {
            email.attachment(EmailAttachment.with().content(attachment.getName()));
        });
        embeddedAttachments.forEach(attachment -> {
            email.embeddedAttachment(EmailAttachment.with().content(new File(attachment.getName())));
        });

        return email;
    }

    /**
     * Verify that all MailConfigs in list is valid by calling the checkEmail()
     * method.
     *
     * @param emailList the list of Mails needed to be checked
     * @return true if all emails are valid, false otherwise.
     */
    private boolean verifyEmailList(ArrayList<String> emailList) {
        int count = 0;
        for (String email : emailList) {
            //call the checkEmail method to validate that the email is good
            if (checkEmail(email)) {
                //increment the count if it is valid
                count++;
            }
        }
        //check if the total number of valid email addresses equals the number of total number of email addresses
        return count == emailList.size();
    }

    /**
     * Standard receive routine for Jodd using an ImapServer.
     * @param mailConfigBean
     * @return 
     */
    public ReceivedEmail[] receiveEmail(MailConfigBean mailConfigBean) {
        ReceivedEmail[] receivedEmails = new ReceivedEmail[]{};
        if (checkEmail(mailConfigBean.getUserEmailAddress())) {
            ImapServer imapServer = MailServer.create()
                    .host(mailConfigBean.getHost())
                    .ssl(true)
                    .auth(mailConfigBean.getUserEmailAddress(),mailConfigBean.getPassword())
                    .buildImapMailServer();

            try ( ReceiveMailSession session = imapServer.createSession()) {
                session.open();
                receivedEmails = session.receiveEmailAndMarkSeen(EmailFilter.filter().flag(Flags.Flag.SEEN, false));
            }
        } else {
            throw new IllegalArgumentException("Unable to send email because either send or recieve addresses are invalid");
        }
        return receivedEmails;
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
}
