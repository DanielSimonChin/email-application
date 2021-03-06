package com.danielsimonchin.business;

import com.danielsimonchin.exceptions.InvalidMailConfigBeanUsernameException;
import com.danielsimonchin.exceptions.InvalidRecipientImapURLException;
import com.danielsimonchin.exceptions.NotEnoughEmailRecipientsException;
import com.danielsimonchin.exceptions.RecipientEmailAddressNullException;
import com.danielsimonchin.exceptions.RecipientInvalidFormatException;
import com.danielsimonchin.exceptions.RecipientListNullException;
import com.danielsimonchin.properties.MailConfigBean;
import java.io.File;
import java.util.List;
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
     * Creates an Email object, sends it and returns the Email object.Validates
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
     * @throws com.danielsimonchin.exceptions.NotEnoughEmailRecipientsException
     * @throws
     * com.danielsimonchin.exceptions.InvalidMailConfigBeanUsernameException
     * @throws com.danielsimonchin.exceptions.RecipientListNullException
     * @throws com.danielsimonchin.exceptions.RecipientEmailAddressNullException
     * @throws com.danielsimonchin.exceptions.RecipientInvalidFormatException
     */
    public Email sendEmail(List<String> toList, List<String> ccList, List<String> bccList, String subject, String textMsg, String htmlMsg, List<File> regularAttachments, List<File> embeddedAttachments) throws NotEnoughEmailRecipientsException, InvalidMailConfigBeanUsernameException, RecipientListNullException, RecipientEmailAddressNullException, RecipientInvalidFormatException {
        //The constructed and sent email that will be returned
        Email email = null;
        //Call a helper method to check if the email address is properly formatted.
        if (checkEmail(this.mailConfigBean.getUserEmailAddress())) {
            if (hasMinimumOneAddress(toList, ccList, bccList) && verifyEmailList(toList) && verifyEmailList(ccList) && verifyEmailList(bccList)) {
                // Create am SMTP server object
                SmtpServer smtpServer = MailServer.create()
                        .ssl(true)
                        .host(this.mailConfigBean.getSmtpUrl())
                        .auth(this.mailConfigBean.getUserEmailAddress(), this.mailConfigBean.getPassword())
                        .buildSmtpMailServer();

                //Create an email with the needed parameters
                email = createEmail(toList, ccList, bccList, subject, textMsg, htmlMsg, regularAttachments, embeddedAttachments);
                //A session to the server should only be created if an Email object is created and set with the proper information.
                if (email != null) {
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
                }
            }
        } else {
            throw new InvalidMailConfigBeanUsernameException("The MailConfigBean's user email address \"" + this.mailConfigBean.getUserEmailAddress() + "\" has an invalid format.");
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
     * @throws NotEnoughEmailRecipientsException
     */
    private boolean hasMinimumOneAddress(List<String> toList, List<String> ccList, List<String> bccList) throws NotEnoughEmailRecipientsException {
        int numberOfAddresses = 0;
        //Only increment the count if the lists are not null, to avoid null pointer exception
        if (toList != null) {
            numberOfAddresses += toList.size();
        }
        if (ccList != null) {
            numberOfAddresses += ccList.size();
        }
        if (bccList != null) {
            numberOfAddresses += bccList.size();
        }

        if (numberOfAddresses == 0) {
            throw new NotEnoughEmailRecipientsException("The email to be sent must have a minimum of one recipient.");
        }
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
     * @throws RecipientListNullException
     */
    private boolean verifyEmailList(List<String> emailList) throws RecipientListNullException, RecipientEmailAddressNullException, RecipientInvalidFormatException {
        if (emailList == null) {
            throw new RecipientListNullException("The recipient list was null");
        }
        int count = 0;
        for (String email : emailList) {
            //throw an exception if the email is null
            if (email == null) {
                throw new RecipientEmailAddressNullException("The input email address cannot be null.");
            }

            //call the checkEmail method to validate that the email is good
            if (checkEmail(email)) {
                //increment the count if it is valid
                count++;
            } else {
                throw new RecipientInvalidFormatException("The email address \"" + email + "\" has an invalid format.");
            }
        }
        //check if the total number of valid email addresses equals the number of total number of email addresses
        return count == emailList.size();
    }

    /**
     * Standard receive routine for Jodd using an ImapServer.Authenticates the
     * receiver bean and leaves are received emails on seen.
     *
     * @param mailConfigBean The recipient that will be authenticated to
     * retrieve its emails.
     * @return An array of ReceivedEmail of the recipient.
     * @throws InvalidRecipientImapURLException
     * @throws RecipientInvalidFormatException
     */
    public ReceivedEmail[] receiveEmail(MailConfigBean mailConfigBean) throws InvalidRecipientImapURLException, RecipientInvalidFormatException {
        ReceivedEmail[] receivedEmails = new ReceivedEmail[]{};
        //First ensure that the recipient mailConfigBean has a valid address, if not then throw a custom exception
        if (checkEmail(mailConfigBean.getUserEmailAddress())) {
            if (this.mailConfigBean.getImapUrl().equals(mailConfigBean.getImapUrl())) {
                ImapServer imapServer = MailServer.create()
                        .host(mailConfigBean.getImapUrl())
                        .ssl(true)
                        .auth(mailConfigBean.getUserEmailAddress(), mailConfigBean.getPassword())
                        .buildImapMailServer();

                try ( ReceiveMailSession session = imapServer.createSession()) {
                    session.open();

                    receivedEmails = session.receiveEmailAndMarkSeen(EmailFilter.filter().flag(Flags.Flag.SEEN, false));
                }
            } else {
                throw new InvalidRecipientImapURLException("The recipient's imap url must be imap.gmail.com");
            }
        } else {
            throw new RecipientInvalidFormatException("The MailConfigBean's username is not properly formatted");
        }
        return receivedEmails;
    }

    /**
     * Use the RFC2822AddressParser to validate that the email string could be a
     * valid address
     *
     * @param address
     * @return true is OK, throw an exception if it is not an email address of a
     * proper format.
     */
    private boolean checkEmail(String address) {
        return RFC2822AddressParser.STRICT.parseToEmailAddress(address) != null;
    }
}
