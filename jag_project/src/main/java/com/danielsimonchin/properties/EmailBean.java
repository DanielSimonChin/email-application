/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.properties;

import com.danielsimonchin.fxbeans.EmailTableFXBean;
import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.activation.DataSource;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.ReceivedEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A composition bean class that allows us to have information relating to
 * folder and receivedDate
 *
 * @author Daniel Simon Chin
 * @version Oct 5th, 2020
 */
public class EmailBean {

    private final static Logger LOG = LoggerFactory.getLogger(EmailBean.class);
    private int id;
    private int folderKey;
    private Timestamp receivedDate;
    public Email email;

    /**
     * Default constructor, assign empty values to the fields.
     */
    public EmailBean() {
        this.id = 0;
        this.folderKey = 0;
        this.receivedDate = new Timestamp(0);
        this.email = new Email();
    }

    /**
     * Initialize the fields for the composition bean
     *
     * @param id
     * @param folderKey
     * @param receivedDate
     * @param email
     */
    public EmailBean(int id, int folderKey, Timestamp receivedDate, Email email) {
        this.id = id;
        this.folderKey = folderKey;
        this.receivedDate = receivedDate;
        this.email = email;
    }

    /**
     * Takes as input a receivedEmail and converts it to an Email object. The
     * bcc is ommitted since receivedEmails cant return a bcc field.
     *
     * @param id
     * @param folderKey
     * @param receivedEmail
     */
    public EmailBean(int id, int folderKey, ReceivedEmail receivedEmail) {
        this.id = id;
        this.folderKey = folderKey;
        this.receivedDate = new Timestamp(receivedEmail.sentDate().getTime());
        this.email = convertToEmail(receivedEmail);
    }

    /**
     * Convert a receivedEmail into an Email object with helper methods that
     * attach recipients and attachments.
     *
     * @param receivedEmail
     * @return A constructed Email object with set fields copied from the
     * receivedEmail
     */
    private Email convertToEmail(ReceivedEmail receivedEmail) {
        Email email = new Email();
        email.from(receivedEmail.from().toString());
        email.subject(receivedEmail.subject());

        List<EmailMessage> messages = receivedEmail.messages();
        //set the messages based on their mime type
        for (EmailMessage message : messages) {
            if (message.getMimeType().equals("text/plain")) {
                email.textMessage(message.getContent());
            } else {
                email.htmlMessage(message.getContent());
            }
        }
        //call helpers to set the attachments and recipients
        retrieveRecipients(receivedEmail, email);
        retrieveAttachments(receivedEmail, email);
        return email;
    }

    /**
     * Helper method that sets the receivedEmail's recipients and copies them
     * into the Email object.
     *
     * @param receivedEmail
     * @param email
     */
    private void retrieveRecipients(ReceivedEmail receivedEmail, Email email) {
        List<String> toList = new ArrayList<>();
        List<String> ccList = new ArrayList<>();
        EmailAddress[] toRecipients = receivedEmail.to();
        EmailAddress[] ccRecipients = receivedEmail.cc();
        for (EmailAddress address : toRecipients) {
            toList.add(address.getEmail());
        }
        for (EmailAddress address : ccRecipients) {
            ccList.add(address.getEmail());
        }
        toList.forEach(emailAddress -> {
            email.to(emailAddress);
        });
        ccList.forEach(emailAddress -> {
            email.cc(emailAddress);
        });
    }

    /**
     * Set the receivedEmail's attachments and copy them into the Email object.
     *
     * @param receivedEmail
     * @param email
     */
    private void retrieveAttachments(ReceivedEmail receivedEmail, Email email) {
        List<EmailAttachment<? extends DataSource>> attachments = receivedEmail.attachments();
        for (EmailAttachment attachment : attachments) {
            if (!attachment.isEmbedded()) {
                email.attachment(EmailAttachment.with().content(attachment.getName()));
            } else {
                email.embeddedAttachment(EmailAttachment.with().content(new File(attachment.getName())));
            }
        }
    }

    /**
     * @return The EmailBean's id
     */
    public int getId() {
        return this.id;
    }

    /**
     * @param id Set the id of the EmailBean
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return The EmailBean's folderKey
     */
    public int getFolderKey() {
        return this.folderKey;
    }

    /**
     * @param folderKey FolderId
     */
    public void setFolderKey(int folderKey) {
        this.folderKey = folderKey;
    }

    /**
     * @return the Email's receivedDate field
     */
    public Timestamp getReceivedDate() {
        return this.receivedDate;
    }

    /**
     * @param receivedDate Date email is received
     */
    public void setReceivedDate(Timestamp receivedDate) {
        this.receivedDate = receivedDate;
    }

    /**
     * An override of the equals method to compare two EmailBean objects and
     * their Email fields.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EmailBean other = (EmailBean) obj;
        if (id == 0) {
            if (other.id != 0) {
                return false;
            }
        } else if (id != other.id) {
            return false;
        }
        if (folderKey == 0) {
            if (other.folderKey != 0) {
                return false;
            }
        } else if (folderKey != other.folderKey) {
            return false;
        }
        if (receivedDate == null) {
            if (other.receivedDate != null) {
                LOG.info("bad1");
                return false;
            }
        } else if (!receivedDate.equals(other.receivedDate)) {
            return false;
        }
        if (email == null && other.email == null) {
            return true;
        }
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        }
        Email otherEmail = other.email;
        if (email.subject() == null) {
            if (otherEmail.subject() != null) {
                return false;
            }
        } else if (!email.subject().equals(otherEmail.subject())) {
            return false;
        }
        if (email.from() == null) {
            if (otherEmail.from() != null) {
                return false;
            }
        }
        if (email.from() != null && otherEmail.from() != null) {
            if (!email.from().toString().equals(otherEmail.from().toString())) {
                return false;
            }
        }
        if (email.messages() == null) {
            if (otherEmail.messages() != null) {
                return false;
            }
        }
        if (email.messages() != null && otherEmail.messages() != null) {
            if (!compareMessages(email.messages(), otherEmail.messages())) {
                return false;
            }

        }
        if (email.to() == null) {
            if (otherEmail.to() != null) {
                return false;
            }
        }
        if (email.to() != null && otherEmail.to() != null) {
            if (!compareRecipients(email.to(), otherEmail.to())) {
                return false;
            }
        }
        if (email.cc() == null) {
            if (otherEmail.cc() != null) {
                return false;
            }
        }
        if (email.cc() != null && otherEmail.cc() != null) {
            if (!compareRecipients(email.cc(), otherEmail.cc())) {
                return false;
            }
        }
        if (email.bcc() == null) {
            if (otherEmail.bcc() != null) {
                return false;
            }
        }
        if (email.bcc() != null && otherEmail.bcc() != null) {
            if (!compareRecipients(email.bcc(), otherEmail.bcc())) {
                return false;
            }
        }
        if (email.attachments() == null) {
            if (otherEmail.attachments() != null) {
                return false;
            }
        }
        if (email.attachments() != null && otherEmail.attachments() != null) {
            if (!compareAttachments(email.attachments(), otherEmail.attachments())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.id;
        hash = 53 * hash + this.folderKey;
        hash = 53 * hash + Objects.hashCode(this.receivedDate);
        hash = 53 * hash + Objects.hashCode(this.email);
        return hash;
    }

    /**
     * Compare the EmailMessage object of an Email and verify that they have
     * identical contents.
     *
     * @param list1 First list of EmailMessage
     * @param list2 Second list of EmailMessage
     * @return return true if they match, false otherwise
     */
    private boolean compareMessages(List<EmailMessage> list1, List<EmailMessage> list2) {
        if (list1 == null) {
            if (list2 != null) {
                return false;
            }
        }
        if (list1 == null && list2 == null) {
            return true;
        }
        int sizeDifference = list1.size() - list2.size();
        //The size of the list2 is the actual text and html indexes that we need since the textMessage and htmlMessages fields can be appended to. We want the most recent changes
        for (int i = list2.size() - 1; i >= 0; i--) {
            if (!list1.get(i + sizeDifference).getContent().equals(list2.get(i).getContent())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare the recipients of a EmailBean to the recipients of another.
     *
     * @param list1 First EmailBean's recipients
     * @param list2 Second EmailBean's recipients
     * @return return true if they are the same, false otherwise
     */
    private boolean compareRecipients(EmailAddress[] list1, EmailAddress[] list2) {
        if (list1 == null) {
            if (list2 != null) {
                return false;
            }
        }

        if (list1.length != list2.length) {
            return false;
        } else {
            for (int i = 0; i < list1.length; i++) {
                //return false if the recipient at an index is not the same as the other bean's recipient at an index.
                if (!list1[i].getEmail().equals(list2[i].getEmail())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compare the attachments of two EmailBean objects. Check that they have
     * the same number of regular and embedded attachments.
     *
     * @param list1 First list of attachments
     * @param list2 Second list of attachments
     * @return true if they are identical, false otherwise.
     */
    private boolean compareAttachments(List<EmailAttachment<? extends DataSource>> list1, List<EmailAttachment<? extends DataSource>> list2) {
        if (list1 == null) {
            if (list2 != null) {
                return false;
            }
        }
        if (list1.size() != list2.size()) {
            return false;
        } else {
            //The counts for the first list's attachments
            int list1CountEmbedded = 0;
            int list1CountRegular = 0;
            for (EmailAttachment item : list1) {
                if (item.isEmbedded()) {
                    list1CountEmbedded++;
                } else {
                    list1CountRegular++;
                }
            }
            //The counts for the second list's attachments
            int list2CountEmbedded = 0;
            int list2CountRegular = 0;
            for (EmailAttachment item : list2) {
                if (item.isEmbedded()) {
                    list2CountEmbedded++;
                } else {
                    list2CountRegular++;
                }
            }
            //return false if their regular and embedded attachments counts are not the same.
            if ((list1CountEmbedded != list2CountEmbedded) && (list1CountRegular != list2CountRegular)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EmailBean{id=").append(id);
        sb.append(", folderKey=").append(folderKey);
        sb.append(", receivedDate=").append(receivedDate);
        sb.append(", email=").append(email);
        sb.append('}');
        return sb.toString();
    }

    /**
     * A conversion method from EmailBean to EmailTableFXBean.
     *
     * @return the resulting EmailTableFXBean
     */
    public EmailTableFXBean convertToEmailTableFXBean() {
        if (this.receivedDate == null) {
            //if there is no received or sent date, it is a draft email.
            if (email.sentDate() == null) {
                return new EmailTableFXBean(id, email.from().getEmail(), email.subject(), null);
            }

            Date dateSent = new Date(email.sentDate().getTime());
            LocalDateTime sentDate = dateSent.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            //returning a sent email
            return new EmailTableFXBean(id, email.from().getEmail(), email.subject(), sentDate);
        }
        return new EmailTableFXBean(id, email.from().getEmail(), email.subject(), receivedDate.toLocalDateTime());
    }

}
