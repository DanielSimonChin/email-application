package com.danielsimonchin.business;

import data.MailConfigBean;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.activation.DataSource;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains two methods which send and receive an email. Takes in a set of
 * parameters, constructs an Email object and sends the email. Receiving the
 * email leave it on read and returns and array of ReceivedEmail.
 *
 * @author Daniel Simon Chin 1836462
 * @version September 20, 2020
 */
public class SendAndReceive {

    private final static Logger LOG = LoggerFactory.getLogger(SendAndReceive.class);
    private MailConfigBean mailConfigBean;

    public SendAndReceive(MailConfigBean mailConfigBean) {
        this.mailConfigBean = mailConfigBean;
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
    public Email sendEmail(List<String> toList, List<String> ccList, List<String> bccList, String subject, String textMsg, String htmlMsg, List<File> regularAttachments, List<File> embeddedAttachments) {
        //The constructed and sent email that will be returned
        Email email = null;
        if (hasMinimumOneAddress(toList, ccList, bccList) && checkEmail(this.mailConfigBean.getUserEmailAddress()) && verifyEmailList(toList) && verifyEmailList(ccList) && verifyEmailList(bccList)) {
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
                LOG.info("Email sent");
            }
        } else {
            throw new IllegalArgumentException("The input parameters are invalid!");
        }
        return email;
    }

    /**
     * Helper method that returns a boolean depending on if the combined total
     * of email addresses is at least 1. An email must have one recipient at
     * minimum.
     *
     * @param toList List of recipients in the TO List
     * @param ccList List of recipients in the cc List
     * @param bccList List of recipients in the bcc List
     * @return A boolean representing if the number of recipients is at least 1.
     */
    private boolean hasMinimumOneAddress(List<String> toList, List<String> ccList, List<String> bccList) {
        int numberOfAddresses = toList.size() + ccList.size() + bccList.size();
        return numberOfAddresses >= 1;
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
    private Email createEmail(List<String> toList, List<String> ccList, List<String> bccList, String subject, String textMsg, String htmlMsg, List<File> regularAttachments, List<File> embeddedAttachments) {
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
    private boolean verifyEmailList(List<String> emailList) {
        int count = 0;
        for (String email : emailList) {
            //throw an exception if the email is null
            if (email == null) {
                throw new NullPointerException("The input email address cannot be null.");
            } //call the checkEmail method to validate that the email is good
            else if (checkEmail(email)) {
                //increment the count if it is valid
                count++;
            }
        }
        //check if the total number of valid email addresses equals the number of total number of email addresses
        return count == emailList.size();
    }

    /**
     * Standard receive routine for Jodd using an ImapServer. Authenticates the
     * receiver bean and leaves are received emails on seen.
     *
     * @param mailConfigBean The recipient that will be authenticated to
     * retrieve its emails.
     * @return An array of ReceivedEmail of the recipient.
     */
    public ReceivedEmail[] receiveEmail(MailConfigBean mailConfigBean) {
        ReceivedEmail[] receivedEmails = new ReceivedEmail[]{};
        //The recipient email address must be valid and its host must be imap.gmail.com
        if (checkEmail(mailConfigBean.getUserEmailAddress()) && !(this.mailConfigBean.getHost().equals(mailConfigBean.getHost()))) {
            ImapServer imapServer = MailServer.create()
                    .host(mailConfigBean.getHost())
                    .ssl(true)
                    .auth(mailConfigBean.getUserEmailAddress(), mailConfigBean.getPassword())
                    .buildImapMailServer();

            try ( ReceiveMailSession session = imapServer.createSession()) {
                session.open();
                LOG.info("Message count: " + session.getMessageCount());
                receivedEmails = session.receiveEmailAndMarkSeen(EmailFilter.filter().flag(Flags.Flag.SEEN, false));
                if (receivedEmails != null) {
                    LOG.info("\n >>>> ReceivedEmail count = " + receivedEmails.length);
                    for (ReceivedEmail email : receivedEmails) {
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
            } catch (jodd.mail.MailException e) {
                //If the recipient's mail bean is invalid, and cannot log in, return null.
                LOG.info("The session cannot be opened because the mailConfigBean's credentials are invalid.");
                return null;
            }
        } else {
            //if the username is not a proper email address or if the recipient host is not imap.gmail.com
            LOG.info("One or many of the mailConfigBean's credentials were invalid.");
            return null;
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
