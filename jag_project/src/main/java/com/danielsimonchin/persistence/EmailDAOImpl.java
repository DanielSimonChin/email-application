package com.danielsimonchin.persistence;

import com.danielsimonchin.properties.EmailBean;
import com.danielsimonchin.properties.MailConfigBean;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * @throws IOException
     */
    @Override
    public int createEmailRecord(EmailBean emailBean) throws SQLException, IOException {
        int result;
        String insertEmailTableQuery = "INSERT INTO Email (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,RECEIVEDATE,FOLDERID) VALUES (?,?,?,?,?,?,?)";

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
                //Call this helper method to insert all attachments related to the Email object
                insertEmailToAttachments(recordNum, emailBean.email);
            }
        }
        LOG.info("# of records created : " + result);
        return result;
    }

    /**
     * Helper method that loops through all the recipients and checks if the
     * email address is in the Addresses table
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
     * Query to return the rows which contain this email address. If no results
     * are found, call a helper to insert the new email address.
     *
     * @param emailAddress An email address which will be searched in the
     * Addresses table
     * @throws SQLException
     */
    private void checkAddressInTable(String emailAddress) throws SQLException {
        String queryFindAddress = "SELECT ADDRESSID FROM ADDRESSES WHERE EMAILADDRESS = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(queryFindAddress);) {
            ps.setString(1, emailAddress);
            ResultSet queryResults = ps.executeQuery();
            //If the address is not in the table, insert it using the helper method insertNewAddress
            if (!queryResults.next()) {
                LOG.info("The email address \"" + emailAddress + "\" is not currently in the Addresses table.");
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
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(insertAddressQuery);) {
            ps.setString(1, emailAddress);
            //The amount if row(s) inserted
            int countInserted = ps.executeUpdate();
            //If the count of rows inserted is 1, display what has been added.
            if (countInserted == 1) {
                LOG.info("The email address \"" + emailAddress + "\" has been added to the Addresses table.");
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
        //return the primary key of the email address
        return resultAddress;
    }

    /**
     * Set the parameterized values of the PreparedStatement using the Email
     * object given as input. The primary key will be auto-incremented so is not
     * assigned here.
     *
     * @param ps A prepared statement that needs to be filled up with the
     * contents of an Email object
     * @param emailBean The composition bean that contains an Email object as
     * private field
     */
    private void fillPreparedStatementForEmailTable(PreparedStatement ps, EmailBean emailBean) throws SQLException {
        LOG.info("FILLING THE PREPARED STATEMENT");
        Email email = emailBean.email;
        ps.setString(1, email.from().toString());
        ps.setString(2, email.subject());
        List<EmailMessage> messages = email.messages();
        String plainText = "";
        String htmlText = "";
        //If the email has both a text and html message, assign them both
        if (messages.size() == 2) {
            plainText = messages.get(0).getContent();
            htmlText = messages.get(1).getContent();
            //If the email has either a text or html message, assign a single message.
        } else if (messages.size() == 1) {
            if (messages.get(0).getMimeType().equals("TEXT/PLAIN")) {
                plainText = messages.get(0).getContent();
            } else {
                htmlText = messages.get(1).getContent();
            }
        }
        ps.setString(3, plainText);
        ps.setString(4, htmlText);
        //If the folder of the email is not a draft, you are allowed to set the sent and receivedDate
        if (emailBean.getFolderKey() != 3) {
            //Set the sent date as the current date since an Email object's default sentDate is null
            email.currentSentDate();
            ps.setTimestamp(5, new Timestamp(email.sentDate().getTime()));
            ps.setTimestamp(6, emailBean.getReceivedDate());
        }
        ps.setInt(7, emailBean.getFolderKey());
        LOG.info("PreparedStatement for inserting a row in Email table has been setup.");
    }

    /**
     * Insert a row in EmailToAttachments for every attachment in the list of
     * attachments.
     *
     * @param recordNum The Email row's primary key
     * @param email The email object which contains attachments that must be
     * inserted into the db
     * @throws SQLException
     */
    private void insertEmailToAttachments(int recordNum, Email email) throws SQLException, IOException {
        String insertEmailToAttachmentsQuery = "INSERT INTO EmailToAttachments (EMAILID,ATTACHMENTID) VALUES (?,?)";
        List<EmailAttachment<? extends DataSource>> attachments = email.attachments();
        if (attachments.size() >= 1) {
            for (EmailAttachment attachment : attachments) {
                //First check if the file exists in the database, if not, then insert it
                insertUnknownAttachment(attachment);
                //Now that it exists in the db, get its attachment primary key and insert into EmailToAttachments
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
     * Inserting an image by converting it to a byte array and making an insert
     * into the Attachments table
     *
     * @param attachment The attachment that will be inserted in the db
     * @throws SQLException
     * @throws IOException
     */
    private void insertUnknownAttachment(EmailAttachment attachment) throws SQLException, IOException {
        String queryInsertAttachment = "INSERT INTO ATTACHMENTS (FILENAME,CID,IMAGE,IS_EMBEDDED) VALUES(?,?,?,?)";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(queryInsertAttachment);) {

            //If the address is not in the table, insert it using the helper method insertNewAddress
            if (getAttachmentIndex(attachment) == -1) {
                LOG.info("The attachment \"" + attachment.getName() + "\" is not currently in the Attachments table.");
                ps.setString(1, attachment.getName());
                ps.setString(2, attachment.getContentId());
                //Retreive the image and convert it to a byte array to be stored
                Path p = FileSystems.getDefault().getPath("", attachment.getName());
                byte[] imageBytes = Files.readAllBytes(p);
                ps.setBytes(3, imageBytes);
                int isEmbedded = 0;
                if (attachment.isEmbedded()) {
                    isEmbedded = 1;
                }
                ps.setInt(4, isEmbedded);
                int countInserts = ps.executeUpdate();
                if (countInserts == 1) {
                    LOG.info("The attachment \"" + attachment.getName() + "\" has been added to the Attachments table.");
                }
            }
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
        int resultAttachmentID = -1;
        String getEmailAddressQuery = "SELECT Attachments.AttachmentID FROM Attachments WHERE Attachments.FileName = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(getEmailAddressQuery);) {
            ps.setString(1, attachment.getName());
            ResultSet rs = ps.executeQuery();
            //Set the attachment's primary key to be returned
            if (rs.next()) {
                resultAttachmentID = rs.getInt(1);
            }
        }
        //Return the primary key of the attachment
        return resultAttachmentID;
    }

    @Override
    public List<EmailBean> findAll() throws SQLException {
        List<EmailBean> rows = new ArrayList<>();

        String selectQuery = "SELECT EMAILID,FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,RECEIVEDATE,FOLDERID FROM EMAIL";
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
        emailBean.setReceivedDate(resultSet.getTimestamp("RECEIVEDATE"));
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
            while (emailToAddressResultSet.next()) {
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
            while (emailToAttachmentResultSet.next()) {
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
    public EmailBean findID(int id) throws SQLException {
        EmailBean emailBean = new EmailBean();
        String findEmailById = "SELECT EMAILID,FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,RECEIVEDATE,FOLDERID FROM EMAIL WHERE EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(findEmailById);) {
            ps.setInt(1, id);
            ResultSet emailRowResult = ps.executeQuery();
            if (emailRowResult.next()) {
                emailBean = createEmailBean(emailRowResult);
            }
        }
        return emailBean;
    }

    /**
     * Finds and returns a List of EmailBean of all emails that are in a
     * specified folder.
     *
     * @param folderKey Folder primary key
     * @return List of EmailBean of all emails that are in a specified folder.
     * @throws SQLException
     */
    @Override
    public List<EmailBean> findAllInFolder(int folderKey) throws SQLException {
        List<EmailBean> emailsInFolder = new ArrayList<>();
        String queryEmailsInFolder = "SELECT EMAILID,FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,RECEIVEDATE,FOLDERID FROM EMAIL WHERE FOLDERID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(queryEmailsInFolder);) {
            ps.setInt(1, folderKey);
            ResultSet allEmailsInFolder = ps.executeQuery();
            while (allEmailsInFolder.next()) {
                //Add the EmailBean to the list
                emailsInFolder.add(createEmailBean(allEmailsInFolder));
            }
        }
        return emailsInFolder;
    }

    @Override
    public List<EmailBean> findEmailsBySender(String sender) throws SQLException {
        List<EmailBean> emailsBySender = new ArrayList<>();
        String queryEmailsBySender = "SELECT EMAILID,FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,RECEIVEDATE,FOLDERID FROM EMAIL WHERE FROMADDRESS = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(queryEmailsBySender);) {
            ps.setString(1, sender);
            ResultSet resultEmails = ps.executeQuery();
            while (resultEmails.next()) {
                //Add the EmailBean to the list
                emailsBySender.add(createEmailBean(resultEmails));
            }
        }
        return emailsBySender;
    }

    @Override
    public List<EmailBean> findEmailsBySubject(String subject) throws SQLException {
        List<EmailBean> emailsBySubject = new ArrayList<>();
        String queryEmailsBySubject = "SELECT EMAILID,FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,RECEIVEDATE,FOLDERID FROM EMAIL WHERE SUBJECT = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(queryEmailsBySubject);) {
            ps.setString(1, subject);
            ResultSet resultEmails = ps.executeQuery();
            while (resultEmails.next()) {
                //Add the EmailBean to the list
                emailsBySubject.add(createEmailBean(resultEmails));
            }
        }
        return emailsBySubject;
    }

    @Override
    public int updateDraft(EmailBean emailBean) throws SQLException {
        int emailTableUpdateResult = updateEmailTableFields(emailBean);
        if(emailTableUpdateResult != 1){
            return -1;
        }
        return 1;
    }

    private int updateEmailTableFields(EmailBean emailBean) throws SQLException {
        int tableUpdatesResult = -1;
        String emailTableUpdateQuery = "UPDATE EMAIL SET FROMADDRESS = ?, SUBJECT = ?, TEXTMESSAGE = ?, HTMLMESSAGE = ? WHERE EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  
                PreparedStatement ps = connection.prepareStatement(emailTableUpdateQuery);) {
            ps.setString(1, emailBean.email.from().toString());
            ps.setString(2, emailBean.email.subject());
            List<EmailMessage> messages = emailBean.email.messages();
            String plainText = "";
            String htmlText = "";
            //If the email has both a text and html message, assign them both
            if (messages.size() == 2) {
                plainText = messages.get(0).getContent();
                htmlText = messages.get(1).getContent();
                //If the email has either a text or html message, assign a single message.
            } else if (messages.size() == 1) {
                if (messages.get(0).getMimeType().equals("TEXT/PLAIN")) {
                    plainText = messages.get(0).getContent();
                } else {
                    htmlText = messages.get(1).getContent();
                }
            }
            ps.setString(3, plainText);
            ps.setString(4, htmlText);
            ps.setInt(5, emailBean.getId());
            tableUpdatesResult = ps.executeUpdate();
            //Should be 1 since only one row in the Email table was affected
            return tableUpdatesResult;
        }
    }

    /**
     * This method checks if a folder exists in the db, if not then creates a
     * new folder and inserts it in the Folders table.
     *
     * @param folderName New folder name
     * @return An int representing how many inserts were made in Folders table.
     * @throws SQLException
     */
    @Override
    public int createFolder(String folderName) throws SQLException {
        //first check if this folder exists yet
        if (checkFolderExists(folderName)) {
            throw new IllegalArgumentException("The folder \"" + folderName + "\" already exists.");
        }
        int resultInsert;
        String insertNewFolder = "INSERT INTO FOLDERS (FOLDERNAME) VALUES (?)";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(insertNewFolder);) {
            ps.setString(1, folderName);
            resultInsert = ps.executeUpdate();
            if (resultInsert == 1) {
                LOG.info("The folder \"" + folderName + "\" has been created.");
            }
        }
        return resultInsert;
    }

    /**
     * Helper method that determines if a folder exists already.
     *
     * @param folderName
     * @return True if it exists, false otherwise.
     * @throws SQLException
     */
    private boolean checkFolderExists(String folderName) throws SQLException {
        String checkExists = "SELECT FOLDERID,FOLDERNAME FROM FOLDERS WHERE FOLDERNAME = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(checkExists);) {
            ps.setString(1, folderName);
            ResultSet existingFolderQuery = ps.executeQuery();
            //return false if it doesn't exists yet
            if (!existingFolderQuery.next()) {
                return false;
            }
        }
        return true;
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
