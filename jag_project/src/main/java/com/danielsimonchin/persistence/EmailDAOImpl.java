package com.danielsimonchin.persistence;

import com.danielsimonchin.properties.EmailBean;
import com.danielsimonchin.properties.MailConfigBean;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import jodd.mail.Email;
import jodd.mail.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.activation.DataSource;
import jodd.mail.EmailAddress;
import jodd.mail.EmailAttachment;

/**
 * This class implements the EmailDAO. Contains multiple methods that perform
 * CRUD operations relating to Email objects and a database.
 *
 * @author Daniel Simon Chin
 * @version October 5th, 2020
 */
public class EmailDAOImpl implements EmailDAO {

    private MailConfigBean mailConfigBean;

    private final static Logger LOG = LoggerFactory.getLogger(EmailDAOImpl.class);

    /**
     * Set the mailConfigBean param as a field to be used when creating a
     * database Connection.
     *
     * @param mailConfigBean The mailConfigBean which will be used to access the
     * databaseURL, username and password.
     */
    public EmailDAOImpl(MailConfigBean mailConfigBean) {
        this.mailConfigBean = mailConfigBean;
    }

    /**
     * This method adds an Email object as a record to the database. The column
     * list does not include ID as this is an auto increment value in the table.
     * This method calls other helper methods to create inserts in the bridging
     * tables: EmailToAddresses, EmailToAttachments
     *
     * @param emailBean The emailBean that must be inserted into the tables:
     * Email, EmailToAddresses, EmailToAttachments
     * @return The number of records created, should always be 1 since one row
     * is inserted into the email table
     * @throws SQLException
     */
    @Override
    public int createEmailRecord(EmailBean emailBean) throws SQLException {
        int result;
        String insertEmailTableQuery = "INSERT INTO Email (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,FOLDERID) VALUES (?,?,?,?,?,?)";

        // Connection is only open for the operation and then immediately closed
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(insertEmailTableQuery, Statement.RETURN_GENERATED_KEYS);) {
            fillPreparedStatementForEmailTable(ps, emailBean);
            result = ps.executeUpdate();
            // Retrieve generated primary key value to insert data into the bridging tables
            try ( ResultSet rs = ps.getGeneratedKeys();) {
                int recordNum = -1;
                if (rs.next()) {
                    //They primary key of the Email row
                    recordNum = rs.getInt(1);
                    //Set the emailBean's primary key field
                    emailBean.setId(recordNum);
                }
                LOG.debug("New record ID is " + recordNum);
                //Call this helper method to check if a recipient has been added yet in the Addresses table.
                insertUnknownAddresses(emailBean.email);
                //Call this helper method to create a row insert in EmailToAddress
                insertEmailToAddress(recordNum, emailBean.email);
                insertEmailToAttachments(recordNum, emailBean.email);
            }
        }
        LOG.info("# of records created : " + result);
        return result;
    }

    /**
     * Helper method that loops through all the recipients and checks if the email address is in the Addresses table
     * 
     * @param email The email object which we will loop through its recipients
     * @throws SQLException 
     */
    private void insertUnknownAddresses(Email email) throws SQLException {
        EmailAddress[] toList = email.to();
        EmailAddress[] ccList = email.cc();
        EmailAddress[] bccList = email.bcc();
        for (EmailAddress toListMember : toList) {
            checkAddressInTable(toListMember.getEmail());
        }
        for (EmailAddress ccListMember : ccList) {
            checkAddressInTable(ccListMember.getEmail());
        }
        for (EmailAddress bccListMember : bccList) {
            checkAddressInTable(bccListMember.getEmail());
        }
    }

    /**
     * Query to return the rows which contain this email address. If no results are found, call a helper to insert the new email address.
     * 
     * @param emailAddress An email address which will be searched in the Addresses table
     * @throws SQLException 
     */
    private void checkAddressInTable(String emailAddress) throws SQLException {
        String queryFindAddress = "SELECT ADDRESSID FROM ADDRESSES WHERE EMAILADDRESS = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  
                PreparedStatement ps = connection.prepareStatement(queryFindAddress);) {
            ps.setString(1,emailAddress);
            ResultSet queryResults = ps.executeQuery();
            //If the address is not in the table, insert it using the helper method insertNewAddress
            if(!queryResults.next()){
                LOG.info("The email address \""+ emailAddress + "\" is not currently in the Addresses table." );
                insertNewAddress(emailAddress);
            }
        }
    }
    /**
     * Execute a query to insert a new email address in the Addresses table.
     * 
     * @param emailAddress The new email address to be added
     * @throws SQLException 
     */
    private void insertNewAddress(String emailAddress) throws SQLException {
        String insertAddressQuery = "INSERT INTO ADDRESSES (EMAILADDRESS) VALUES (?)";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  
                PreparedStatement ps = connection.prepareStatement(insertAddressQuery);) {
            ps.setString(1,emailAddress);
            //The amount if row(s) inserted
            int countInserted = ps.executeUpdate();
            //If the count of rows inserted is 1, display what has been added.
            if(countInserted == 1){
                LOG.info("The email address \""+ emailAddress + "\" has been added to the Addresses table." );
            }
        }
    }

    /**
     * Goes through all the Email's recipients in their lists and makes an
     * insert into the bridging table. Uses a helper method that creates a
     * PreparedStatement and executes the insert.
     *
     * @param recordNum The primary key of the row in the Email table
     * @param email The Email object which has recipients which must be added to
     * the EmailToAddresses Table
     * @throws SQLException
     */
    private void insertEmailToAddress(int recordNum, Email email) throws SQLException {

        EmailAddress[] toList = email.to();
        EmailAddress[] ccList = email.cc();
        EmailAddress[] bccList = email.bcc();
        if (toList.length >= 1) {
            for (EmailAddress address : toList) {
                int emailIndex = getEmailAddressIndex(address);
                executeEmailToAddressQuery(recordNum, emailIndex, "TO");
            }
        }
        if (ccList.length >= 1) {
            for (EmailAddress address : ccList) {
                int emailIndex = getEmailAddressIndex(address);
                executeEmailToAddressQuery(recordNum, emailIndex, "CC");
            }
        }
        if (bccList.length >= 1) {
            for (EmailAddress address : bccList) {
                int emailIndex = getEmailAddressIndex(address);
                executeEmailToAddressQuery(recordNum, emailIndex, "BCC");
            }
        }
        LOG.info("Email recipients for record #" + recordNum + " have been added in EmailToAddress");

    }

    /**
     * Helper method that executes the inserts for every recipient into the
     * EmailToAddress table.
     *
     * @param recordNum Primary key of a row in the Email table.
     * @param emailIndex Primary key of a row in the Addresses table.
     * @param category A string representing the recipient Category (TO,CC,BCC)
     * @throws SQLException
     */
    private void executeEmailToAddressQuery(int recordNum, int emailIndex, String category) throws SQLException {
        String insertEmailToAddressQuery = "INSERT INTO EmailToAddress (EmailID,AddressID,RecipientCategory) values (?,?,?)";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword()); // Using a prepared statement to handle the conversion
                // of special characters in the SQL statement and guard against
                // SQL Injection
                  PreparedStatement ps = connection.prepareStatement(insertEmailToAddressQuery);) {
            ps.setInt(1, recordNum);
            ps.setInt(2, emailIndex);
            ps.setString(3, category);
            ps.executeUpdate();
        }
    }

    /**
     * Query and return the index of an email address in the addresses table to
     * insert it into the EmailToAddress table
     *
     * @param emailAddress The EmailAddress object that we need to retrieve its
     * AddressID (primary key index)
     * @return An int representing the email's primary key index in the
     * Addresses table
     * @throws SQLException
     */
    private int getEmailAddressIndex(EmailAddress emailAddress) throws SQLException {
        int resultAddress = 0;
        String getEmailAddressQuery = "SELECT Addresses.AddressID FROM Addresses WHERE Addresses.EmailAddress = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(getEmailAddressQuery);) {
            ps.setString(1, emailAddress.getEmail());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                resultAddress = rs.getInt(1);
            }
        }
        return resultAddress;
    }

    /**
     * Set the parameterized values of the PreparedStatement using the Email
     * object given as input. The primary key will be auto-incremented so is not
     * assigned here.
     *
     * @param ps
     * @param email
     */
    private void fillPreparedStatementForEmailTable(PreparedStatement ps, EmailBean emailBean) throws SQLException {
        Email email = emailBean.email;
        ps.setString(1, email.from().toString());
        ps.setString(2, email.subject());
        List<EmailMessage> messages = email.messages();
        String plainText = "";
        String htmlText = "";
        if (messages.size() == 2) {
            plainText = messages.get(0).getContent();
            htmlText = messages.get(1).getContent();
        } else if (messages.size() == 1) {
            if (messages.get(0).getMimeType().equals("TEXT/PLAIN")) {
                plainText = messages.get(0).getContent();
            } else {
                htmlText = messages.get(1).getContent();
            }
        }
        ps.setString(3, plainText);
        ps.setString(4, htmlText);
        //Set the sent date as the current date since an Email object's default sentDate is null
        email.currentSentDate();
        Timestamp sqlTimeStamp = new Timestamp(email.sentDate().getTime());
        ps.setTimestamp(5, sqlTimeStamp);
        ps.setInt(6, emailBean.getFolderKey());
        LOG.info("PreparedStatement for inserting a row in Email table has been setup");
    }

    /**
     * Insert a row in EmailToAttachments for every attachment in the lists of
     * attachments.
     *
     * @param recordNum
     * @param email
     * @throws SQLException
     */
    private void insertEmailToAttachments(int recordNum, Email email) throws SQLException {
        String insertEmailToAttachmentsQuery = "INSERT INTO EmailToAttachments (EMAILID,ATTACHMENTID) VALUES (?,?)";
        List<EmailAttachment<? extends DataSource>> attachments = email.attachments();
        if (attachments.size() >= 1) {
            for (EmailAttachment attachment : attachments) {
                int attachmentID = getAttachmentIndex(attachment);
                try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(insertEmailToAttachmentsQuery);) {
                    ps.setInt(1, recordNum);
                    ps.setInt(2, attachmentID);
                    ps.executeUpdate();
                }
            }
            LOG.info("The attachments for the record #" + recordNum + " have been added in EmailToAttachments");
        }
    }

    /**
     * Query and return the index of an attachment in the Attachments table to
     * insert it into the EmailToAddresses table
     *
     * @param attachment The EmailAttachment object that we need to retrieve its
     * AttachmentID (primary key index)
     * @return An int representing the Attachment's primary key index in the
     * Attachments table
     * @throws SQLException
     */
    private int getAttachmentIndex(EmailAttachment attachment) throws SQLException {
        int resultAttachmentID = 0;
        String getEmailAddressQuery = "SELECT Attachments.AttachmentID FROM Attachments WHERE Attachments.FileName = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(getEmailAddressQuery);) {
            ps.setString(1, attachment.getName());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                resultAttachmentID = rs.getInt(1);
            }
        }
        return resultAttachmentID;
    }

    @Override
    public List<EmailBean> findAll() throws SQLException {
        List<EmailBean> rows = new ArrayList<>();

        String selectQuery = "SELECT EMAILID,FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,FOLDERID FROM EMAIL";
        LOG.info("IN FIND ALL");
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement pStatement = connection.prepareStatement(selectQuery);  ResultSet resultSet = pStatement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(createEmailBean(resultSet));
            }
        }
        LOG.info("# of records found : " + rows.size());
        return rows;
    }

    private EmailBean createEmailBean(ResultSet resultSet) throws SQLException {
        LOG.info("CREATING A BEAN");
        EmailBean emailBean = new EmailBean();
        emailBean.setId(resultSet.getInt("EMAILID"));
        emailBean.setFolderKey(resultSet.getInt("FOLDERID"));
        emailBean.setReceivedDate(new Timestamp(0));
        emailBean.email = queryEmailTableFields(resultSet, emailBean.email);
        emailBean.email = queryEmailToAddressTableFields(resultSet, emailBean.email);
        emailBean.email = queryEmailToAttachmentTableFields(resultSet, emailBean.email);
        LOG.info("EmailBean successfully created.");
        return emailBean;
    }

    /**
     * Execute a query and assign the needed fields for a email object with
     * everything related to the Email table.
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private Email queryEmailTableFields(ResultSet resultSet, Email email) throws SQLException {
        String emailTableQuery = "SELECT EMAILID,FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,FOLDERID FROM EMAIL WHERE EMAIL.EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(emailTableQuery);) {
            ps.setInt(1, resultSet.getInt("EMAILID"));
            ResultSet emailTableResultSet = ps.executeQuery();
            if (emailTableResultSet.next()) {
                email.from(emailTableResultSet.getString("FROMADDRESS"));
                email.subject(emailTableResultSet.getString("SUBJECT"));
                email.textMessage(emailTableResultSet.getString("TEXTMESSAGE"));
                email.htmlMessage(emailTableResultSet.getString("HTMLMESSAGE"));
                LOG.info("Email object has set the From, subject, textMessage and HtmlMessage");
            }
        }
        return email;
    }

    private Email queryEmailToAddressTableFields(ResultSet resultSet, Email email) throws SQLException {
        String emailToAddressQuery = "SELECT ADDRESSES.EMAILADDRESS, EMAILTOADDRESS.RECIPIENTCATEGORY FROM ADDRESSES INNER JOIN EMAILTOADDRESS ON ADDRESSES.ADDRESSID = EMAILTOADDRESS.ADDRESSID WHERE EMAILTOADDRESS.EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(emailToAddressQuery);) {
            ps.setInt(1, resultSet.getInt("EMAILID"));
            ResultSet emailToAddressResultSet = ps.executeQuery();
            if (emailToAddressResultSet.next()) {
                if (emailToAddressResultSet.getString("RECIPIENTCATEGORY").equals("TO")) {
                    email.to(emailToAddressResultSet.getString("EMAILADDRESS"));
                } else if (emailToAddressResultSet.getString("RECIPIENTCATEGORY").equals("CC")) {
                    email.cc(emailToAddressResultSet.getString("EMAILADDRESS"));
                } else if (emailToAddressResultSet.getString("RECIPIENTCATEGORY").equals("BCC")) {
                    email.bcc(emailToAddressResultSet.getString("EMAILADDRESS"));
                }
                LOG.info("Email object has set the recipient email addresses");
            }
        }
        return email;
    }

    private Email queryEmailToAttachmentTableFields(ResultSet resultSet, Email email) throws SQLException {
        String emailToAttachmentQuery = "SELECT ATTACHMENTS.FILENAME,ATTACHMENTS.IS_EMBEDDED FROM ATTACHMENTS INNER JOIN EMAILTOATTACHMENTS ON ATTACHMENTS.ATTACHMENTID = EMAILTOATTACHMENTS.ATTACHMENTID WHERE EMAILTOATTACHMENTS.EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(emailToAttachmentQuery);) {
            ps.setInt(1, resultSet.getInt("EMAILID"));
            ResultSet emailToAttachmentResultSet = ps.executeQuery();
            if (emailToAttachmentResultSet.next()) {
                if (emailToAttachmentResultSet.getInt("IS_EMBEDDED") == 1) {
                    email.embeddedAttachment(EmailAttachment.with().content(new File(emailToAttachmentResultSet.getString("FILENAME"))));
                } else {
                    email.attachment(EmailAttachment.with().content(emailToAttachmentResultSet.getString("FILENAME")));
                }
                LOG.info("Email object has set the attachments");
            }
        }
        return email;
    }

    @Override
    public Email findID(int id) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int update(Email email) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int delete(int id) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
