/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.properties;

import com.danielsimonchin.business.SendAndReceive;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
     * Takes as input a receivedEmail and converts it to an Email object. The bcc is ommitted since receivedEmails cant return a bcc field.
     * 
     * @param id
     * @param folderKey
     * @param receivedEmail 
     */
    public EmailBean(int id, int folderKey, ReceivedEmail receivedEmail) {
        this.id = id;
        this.folderKey = folderKey;
        this.receivedDate = new Timestamp(receivedEmail.sentDate().getTime());
        LOG.info("RECEIVED DATE: " + receivedDate);
        this.email = convertToEmail(receivedEmail);
    }

    private Email convertToEmail(ReceivedEmail receivedEmail) {
        Email email = new Email();
        email.from(receivedEmail.from().toString());
        email.subject(receivedEmail.subject());
        
        List<EmailMessage> messages = receivedEmail.messages();
        for (EmailMessage message : messages) {
            if (message.getMimeType().equals("text/plain")) {
                email.textMessage(message.getContent());
            } else {
                email.htmlMessage(message.getContent());
            }
        }
        retrieveRecipients(receivedEmail,email);
        retrieveAttachments(receivedEmail,email);
        return email;
    }
    private void retrieveRecipients(ReceivedEmail receivedEmail, Email email){
        List<String> toList = new ArrayList<>();
        List<String> ccList = new ArrayList<>();
        EmailAddress[] toRecipients = receivedEmail.to();
        EmailAddress[] ccRecipients = receivedEmail.cc();
        for(EmailAddress address : toRecipients){
            toList.add(address.getEmail());
        }
        for(EmailAddress address : ccRecipients){
            ccList.add(address.getEmail());
        }
        toList.forEach(emailAddress -> {
            email.to(emailAddress);
        });
        ccList.forEach(emailAddress -> {
            email.cc(emailAddress);
        });
    }
    private void retrieveAttachments(ReceivedEmail receivedEmail, Email email){
        List<EmailAttachment<? extends DataSource>> attachments = receivedEmail.attachments();
        for(EmailAttachment attachment : attachments){
            if(attachment.isEmbedded()){
                email.embeddedAttachment(EmailAttachment.with().content(new File(attachment.getName())));
            }
            else{
                email.attachment(EmailAttachment.with().content(attachment.getName())); 
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
     * @param folderKey
     */
    public void setFolderKey(int folderKey) {
        this.folderKey = folderKey;
    }

    public Timestamp getReceivedDate() {
        return this.receivedDate;
    }

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
                return false;
            }
        } else if (receivedDate != other.receivedDate) {
            return false;
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
        } else if (!email.from().toString().equals(otherEmail.from().toString())) {
            return false;
        }
        if (email.messages() == null) {
            if (otherEmail.messages() != null) {
                return false;
            }
        } else if (!compareMessages(email.messages(), otherEmail.messages())) {
            return false;
        }
        if (email.to() == null) {
            if (otherEmail.to() != null) {
                return false;
            }
        } else if (!compareRecipients(email.to(), otherEmail.to())) {
            return false;
        }
        if (email.cc() == null) {
            if (otherEmail.cc() != null) {
                return false;
            }
        } else if (!compareRecipients(email.cc(), otherEmail.cc())) {
            return false;
        }
        if (email.bcc() == null) {
            if (otherEmail.bcc() != null) {
                return false;
            }
        } else if (!compareRecipients(email.bcc(), otherEmail.bcc())) {
            return false;
        }
        if (email.attachments() == null) {
            if (otherEmail.attachments() != null) {
                return false;
            }
        } else if (!compareAttachments(email.attachments(), otherEmail.attachments())) {
            return false;
        }
        return true;
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
            LOG.info("COMPARING MESSAGES : " + list1.get(i + sizeDifference).getContent() + " " + list2.get(i).getContent());
            if (!list1.get(i + sizeDifference).getContent().equals(list2.get(i).getContent())) {
                return false;
            }
        }
        LOG.info("THE MESSAGES ARE SAME");
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
                LOG.info("COMPARING ADDRESS:" + list1[i].getEmail() + " " + list2[i].getEmail());
                //return false if the recipient at an index is not the same as the other bean's recipient at an index.
                if (!list1[i].getEmail().equals(list2[i].getEmail())) {
                    return false;
                }
            }
        }
        LOG.info("THE RECIPIENTS ARE SAME");
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
            for (int i = 0; i < list1.size(); i++) {
                LOG.info("ATTACHMENTS COMPARING : " + list1.get(i).getName() + " " + list2.get(i).getName());
                LOG.info("ATTACHMENTS SIZES : " + list1.get(i).getSize()+ " " + list2.get(i).getSize());
                if (!list1.get(i).getName().equals(list2.get(i).getName())) {
                    return false;
                }
            }
            /*
            //The counts for the first list's attachments
            int list1CountEmbedded = 0;
            int list1CountRegular = 0;
            for (EmailAttachment item : list1) {
                LOG.info(" LIST1 ATTACHMENT : " + item.getName());
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
                LOG.info(" LIST2 ATTACHMENT : " + item.getName());
                if (item.isEmbedded()) {
                    list2CountEmbedded++;
                } else {
                    list2CountRegular++;
                }
            }
            //return false if their regular and embedded attachments counts are not the same.
            if ((list1CountEmbedded != list2CountEmbedded) && (list1CountRegular != list2CountRegular)) {
                return false;
            }*/
        }
        return true;
    }

}
