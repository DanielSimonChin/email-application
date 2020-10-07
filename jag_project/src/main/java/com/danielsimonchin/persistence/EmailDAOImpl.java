package com.danielsimonchin.persistence;

import com.danielsimonchin.properties.EmailBean;
import com.danielsimonchin.properties.MailConfigBean;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import jodd.mail.MailServer;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;

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
     * This method calls other helper methods to create inserts in the
     * connecting tables: EmailToAddresses, Attachments
     *
     *
     * @param emailBean The emailBean that must be inserted into the tables:
     * Email, EmailToAddresses, Attachments
     * @return The number of records created, should always be 1 since one row
     * is inserted into the email table
     * @throws SQLException
     * @throws IOException
     */
    @Override
    public int createEmailRecord(EmailBean emailBean) throws SQLException, IOException {
        int result;
        String insertEmailQuery;
        //If the email is a draft, execute the query that does not involve sentDate and receiveDate.
        if (emailBean.getFolderKey() == 3) {
            insertEmailQuery = "INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,FOLDERID) VALUES (?,?,?,?,?)";
        } else {
            //Regular query for sent and received emails
            insertEmailQuery = "INSERT INTO Email (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,RECEIVEDATE,FOLDERID) VALUES (?,?,?,?,?,?,?)";
        }
        // Connection is only open for the operation and then immediately closed
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(insertEmailQuery, Statement.RETURN_GENERATED_KEYS);) {
            if (emailBean.getFolderKey() == 3) {
                fillPreparedStatementDraftEmail(ps, emailBean);
            } else if (emailBean.getFolderKey() == 1 || emailBean.getFolderKey() == 2) {
                fillPreparedStatementForEmailTable(ps, emailBean);
            }
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
                insertEmailToAddress(emailBean);
                //Call this helper method to insert all attachments related to the Email object
                insertAttachments(emailBean);
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
    private void insertEmailToAddress(EmailBean emailBean) throws SQLException {

        EmailAddress[] toList = emailBean.email.to();
        EmailAddress[] ccList = emailBean.email.cc();
        EmailAddress[] bccList = emailBean.email.bcc();
        if (toList.length >= 1) {
            for (EmailAddress address : toList) {
                int emailIndex = getEmailAddressIndex(address);
                executeEmailToAddressQuery(emailBean.getId(), emailIndex, "TO");
            }
        }
        if (ccList.length >= 1) {
            for (EmailAddress address : ccList) {
                int emailIndex = getEmailAddressIndex(address);
                executeEmailToAddressQuery(emailBean.getId(), emailIndex, "CC");
            }
        }
        if (bccList.length >= 1) {
            for (EmailAddress address : bccList) {
                int emailIndex = getEmailAddressIndex(address);
                executeEmailToAddressQuery(emailBean.getId(), emailIndex, "BCC");
            }
        }
        LOG.info("Email recipients for record #" + emailBean.getId() + " have been added in EmailToAddress");

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
    private void executeEmailToAddressQuery(int emailId, int addressId, String recipientCategory) throws SQLException {
        String insertEmailToAddressQuery = "INSERT INTO EmailToAddress (EmailID,AddressID,RecipientCategory) values (?,?,?)";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword()); // Using a prepared statement to handle the conversion
                // of special characters in the SQL statement and guard against
                // SQL Injection
                  PreparedStatement ps = connection.prepareStatement(insertEmailToAddressQuery);) {
            ps.setInt(1, emailId);
            ps.setInt(2, addressId);
            ps.setString(3, recipientCategory);
            int queryResult = ps.executeUpdate();
            if (queryResult == 1) {
                LOG.info("The address ID: \"" + addressId + "\" has been inserted as a \"" + recipientCategory + "\" for the email with ID: \"" + emailId + "\".");
            }

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
            if (rs.next()) {
                resultAddress = rs.getInt(1);
            }
        }
        //return the primary key of the email address
        return resultAddress;
    }

    public void fillPreparedStatementDraftEmail(PreparedStatement ps, EmailBean emailBean) throws SQLException {
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
            if (messages.get(0).getMimeType().equals("text/plain")) {
                plainText = messages.get(0).getContent();
            } else {
                htmlText = messages.get(1).getContent();
            }
        }
        ps.setString(3, plainText);
        ps.setString(4, htmlText);
        ps.setInt(5, emailBean.getFolderKey());
        LOG.info("PreparedStatement for inserting a row in Email table has been setup.");
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
            if (messages.get(0).getMimeType().equals("text/plain")) {
                plainText = messages.get(0).getContent();
            } else {
                htmlText = messages.get(1).getContent();
            }
        }
        ps.setString(3, plainText);
        ps.setString(4, htmlText);

        //Set the sent date as the current date since an Email object's default sentDate is null
        email.currentSentDate();
        ps.setTimestamp(5, new Timestamp(email.sentDate().getTime()));
        ps.setTimestamp(6, emailBean.getReceivedDate());
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
    private void insertAttachments(EmailBean emailBean) throws SQLException, IOException {
        Email email = emailBean.email;
        int queryResult = 0;
        String insertAttachmentQuery = "INSERT INTO ATTACHMENTS (EMAILID,FILENAME,CID,ATTACHMENT,IS_EMBEDDED) VALUES (?,?,?,?,?)";
        List<EmailAttachment<? extends DataSource>> attachments = email.attachments();
        if (attachments.size() >= 1) {
            for (EmailAttachment attachment : attachments) {
                try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(insertAttachmentQuery);) {
                    ps.setInt(1, emailBean.getId());
                    ps.setString(2, attachment.getName());
                    ps.setString(3, attachment.getContentId());
                    ps.setBytes(4, attachment.toByteArray());
                    if (attachment.isEmbedded()) {
                        ps.setInt(5, 1);
                    } else {
                        ps.setInt(5, 0);
                    }
                    queryResult = ps.executeUpdate();
                }
            }
        }
        if (queryResult > 0) {
            LOG.info("The attachments for the record #" + emailBean.getId() + " have been added in EmailToAttachments");
        }
    }

    @Override
    public List<EmailBean> findAll() throws SQLException, FileNotFoundException, IOException {
        List<EmailBean> rows = new ArrayList<>();

        String selectQuery = "SELECT EMAILID,FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,RECEIVEDATE,FOLDERID FROM EMAIL";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement pStatement = connection.prepareStatement(selectQuery);  ResultSet resultSet = pStatement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(createEmailBean(resultSet));
            }
        }
        LOG.info("# of records found : " + rows.size());
        return rows;
    }

    private EmailBean createEmailBean(ResultSet resultSet) throws SQLException, FileNotFoundException, IOException {
        LOG.info("Creating an EmailBean");
        EmailBean emailBean = new EmailBean();
        emailBean.setId(resultSet.getInt("EMAILID"));
        emailBean.setFolderKey(resultSet.getInt("FOLDERID"));
        emailBean.setReceivedDate(resultSet.getTimestamp("RECEIVEDATE"));
        emailBean.email = queryEmailTableFields(resultSet, emailBean.email);
        emailBean.email = queryEmailToAddressTableFields(resultSet, emailBean.email);
        emailBean.email = queryAttachmentsFields(resultSet, emailBean.email);
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
        String emailTableQuery = "SELECT EMAILID,FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE FROM EMAIL WHERE EMAIL.EMAILID = ?";
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
            }
        }
        LOG.info("Email object has set the recipient email addresses");
        return email;
    }

    private Email queryAttachmentsFields(ResultSet resultSet, Email email) throws SQLException, FileNotFoundException, IOException {
        LOG.info("BEFORE");
        List<File> regularAttachments = new ArrayList<>();
        List<File> embeddedAttachments = new ArrayList<>();
        String attachmentsQuery = "SELECT FILENAME,ATTACHMENT,IS_EMBEDDED FROM ATTACHMENTS WHERE EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(attachmentsQuery);) {
            ps.setInt(1, resultSet.getInt("EMAILID"));
            ResultSet emailAttachmentsResult = ps.executeQuery();
            while (emailAttachmentsResult.next()) {
                //Set the File object's name
                File image = new File(emailAttachmentsResult.getString("FILENAME"));
                FileOutputStream fos = new FileOutputStream(image);
                byte[] buffer = new byte[1];
                InputStream is = emailAttachmentsResult.getBinaryStream("ATTACHMENT");
                //The mock data has a null attachments, so only read from the InputStream if it isn't null.
                if (is != null) {
                    //As long as there are bytes to read, keep writing to the FileOutputStream
                    while (is.read(buffer) > 0) {

                        fos.write(buffer);
                    }
                }
                fos.close();
                //Add the File to the appropriate list depending on if the image is embedded or not
                if (emailAttachmentsResult.getInt("IS_EMBEDDED") == 1) {
                    embeddedAttachments.add(image);
                } else {
                    regularAttachments.add(image);
                }
            }
        }
        //Setting the files to the Email object's attachments
        LOG.info("REGULAR ATTACHMENTS FOR EMAILID:" + resultSet.getInt("EMAILID"));
        regularAttachments.forEach(attachment -> {
            LOG.info("Regular attachment: " + attachment.getName());
            email.attachment(EmailAttachment.with().content(attachment.getName()));
        });
        LOG.info("EMBEDDED ATTACHMENTS FOR EMAILID:" + resultSet.getInt("EMAILID"));
        embeddedAttachments.forEach(attachment -> {
            LOG.info("EMBEDDED attachment: " + attachment.getName());
            email.embeddedAttachment(EmailAttachment.with().content(new File(attachment.getName())));
        });
        LOG.info("AFTER");
        return email;
    }

    @Override
    public EmailBean findID(int id) throws SQLException, FileNotFoundException, IOException {
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
     * @param folderName Folder name
     * @return List of EmailBean of all emails that are in a specified folder.
     * @throws SQLException
     * @throws java.io.FileNotFoundException
     */
    @Override
    public List<EmailBean> findAllInFolder(String folderName) throws SQLException, FileNotFoundException, IOException {
        LOG.info("BEFORE 1");
        List<EmailBean> emailsInFolder = new ArrayList<>();
        String queryEmailsInFolder = "SELECT EMAIL.EMAILID,EMAIL.FROMADDRESS,EMAIL.SUBJECT,EMAIL.TEXTMESSAGE,EMAIL.HTMLMESSAGE,EMAIL.SENTDATE,EMAIL.RECEIVEDATE,EMAIL.FOLDERID FROM EMAIL INNER JOIN FOLDERS ON EMAIL.FOLDERID = FOLDERS.FOLDERID WHERE FOLDERNAME = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(queryEmailsInFolder);) {
            ps.setString(1, folderName);
            ResultSet allEmailsInFolder = ps.executeQuery();
            while (allEmailsInFolder.next()) {
                LOG.info("QUERY HAS BEEN RUN");
                //Add the EmailBean to the list
                emailsInFolder.add(createEmailBean(allEmailsInFolder));
            }
        }
        LOG.info("BEFORE 2");
        return emailsInFolder;
    }

    @Override
    public List<EmailBean> findEmailsBySender(String sender) throws SQLException, FileNotFoundException, IOException {
        List<EmailBean> emailsBySender = new ArrayList<>();
        String queryEmailsBySender = "SELECT EMAIL.EMAILID,EMAIL.FROMADDRESS,EMAIL.SUBJECT,EMAIL.TEXTMESSAGE,EMAIL.HTMLMESSAGE,EMAIL.SENTDATE,EMAIL.RECEIVEDATE,EMAIL.FOLDERID FROM EMAIL WHERE FROMADDRESS = ?";
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
    public List<EmailBean> findEmailsByRecipient(String recipientEmailAddress) throws SQLException, FileNotFoundException, IOException {
        List<EmailBean> emailsByRecipient = new ArrayList<>();
        String queryEmailsByRecipient = "SELECT EMAIL.EMAILID,EMAIL.FROMADDRESS,EMAIL.SUBJECT,EMAIL.TEXTMESSAGE,EMAIL.HTMLMESSAGE,EMAIL.SENTDATE,EMAIL.RECEIVEDATE,EMAIL.FOLDERID FROM EMAIL INNER JOIN EMAILTOADDRESS ON EMAIL.EMAILID = EMAILTOADDRESS.EMAILID INNER JOIN ADDRESSES ON EMAILTOADDRESS.ADDRESSID = ADDRESSES.ADDRESSID WHERE ADDRESSES.EMAILADDRESS = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(queryEmailsByRecipient);) {
            ps.setString(1, recipientEmailAddress);
            ResultSet resultEmails = ps.executeQuery();
            while (resultEmails.next()) {
                //Add the EmailBean to the list
                emailsByRecipient.add(createEmailBean(resultEmails));
            }
        }
        return emailsByRecipient;
    }

    @Override
    public List<EmailBean> findEmailsBySubject(String subject) throws SQLException, FileNotFoundException, IOException {
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
    public int updateDraft(EmailBean emailBean) throws SQLException, IOException {
        //Update the fields in the Email table with the new EmailBean information
        int emailTableUpdateResult = updateEmailTableFields(emailBean);
        if (emailTableUpdateResult != 1) {
            return -1;
        }
        //First insert new email recipients into the Addresses table if the updated draft has new email recipients that are not in the table yet.
        insertUnknownAddresses(emailBean.email);
        //Delete all associated recipients of the old Email in the EmailToAddress table.
        deleteEmailToAddressRow(emailBean.getId());
        //Update the recipients of the updated email. first delete all EmailToAddress Entries for a specific Email id, then insert the new ones.
        updateEmailRecipients(emailBean);
        //Delete all associate attachments of the old Email in the attachments table.
        deleteEmailAttachments(emailBean.getId());
        //Update the files of the old email and insert new files if needed.
        insertAttachments(emailBean);
        return 1;
    }

    private int updateEmailTableFields(EmailBean emailBean) throws SQLException {
        int tableUpdatesResult = -1;
        String emailTableUpdateQuery = "UPDATE EMAIL SET FROMADDRESS = ?, SUBJECT = ?, TEXTMESSAGE = ?, HTMLMESSAGE = ? WHERE EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(emailTableUpdateQuery);) {
            ps.setString(1, emailBean.email.from().toString());
            ps.setString(2, emailBean.email.subject());
            List<EmailMessage> messages = emailBean.email.messages();
            String plainText = "";
            String htmlText = "";

            LOG.info("INSIDE UPDATE METHOD THE SIZE IS " + messages.size());
            for (EmailMessage message : messages) {
                LOG.info("content: " + message.getContent() + " mime type: " + message.getMimeType());
            }
            //Starts at the end since a email's messages is a list which can be added to. We take the most recent messages added.
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (messages.get(i).getMimeType().equals("text/plain")) {
                    plainText = messages.get(i).getContent();
                    break;
                }
            }
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (messages.get(i).getMimeType().equals("text/html")) {
                    htmlText = messages.get(i).getContent();
                    break;
                }
            }

            LOG.info("NEW TEXT:" + plainText);
            LOG.info("NEW HTML:" + htmlText);
            ps.setString(3, plainText);
            ps.setString(4, htmlText);
            //if the new folder is changing from draft to sent, send the email and update its folder in the Email table
            if (emailBean.getFolderKey() == 2) {
                //Send the email
                sendDraftEmail(emailBean.email);
                updateSentEmail(emailBean);
            }
            //Set the emailId of the email we wish to update.
            ps.setInt(5, emailBean.getId());
            tableUpdatesResult = ps.executeUpdate();
            //Should be 1 since only one row in the Email table was affected
            return tableUpdatesResult;
        }
    }

    private void sendDraftEmail(Email email) {
        SmtpServer smtpServer = MailServer.create()
                .ssl(true)
                .host(this.mailConfigBean.getSmtpUrl())
                .auth(this.mailConfigBean.getUserEmailAddress(), this.mailConfigBean.getPassword())
                .buildSmtpMailServer();
        try ( // A session is the object responsible for communicating with the server
                 SendMailSession session = smtpServer.createSession()) {
            // Like a file we open the session, send the message and close the
            // session
            session.open();
            session.sendMail(email);
            //Setting the email's sent date as the current date
            email.currentSentDate();
            LOG.info("The Draft Email has been sent.");
        }
    }

    private void updateSentEmail(EmailBean emailBean) throws SQLException {
        int tableUpdatesResult = -1;
        String updateSentDateQuery = "UPDATE EMAIL SET SENTDATE = ?, FOLDERID = ? WHERE EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(updateSentDateQuery);) {
            ps.setTimestamp(1, new Timestamp(emailBean.email.sentDate().getTime()));
            ps.setInt(2, emailBean.getFolderKey());
            ps.setInt(3, emailBean.getId());
            tableUpdatesResult = ps.executeUpdate();
        }
        if (tableUpdatesResult > 0) {
            LOG.info("The email with the ID: " + emailBean.getId() + " has set its sentDate in the Email table.");
        }
    }

    @Override
    public int updateFolder(EmailBean emailBean) throws SQLException {
        int updateResult = -1;
        String updateFolderQuery = "UPDATE EMAIL SET FOLDERID = ? WHERE EMAILID = ?";
        if (emailBean.getFolderKey() == 3) {
            //throw custom exception
        }
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(updateFolderQuery);) {
            ps.setInt(1, emailBean.getFolderKey());
            ps.setInt(2, emailBean.getId());
            updateResult = ps.executeUpdate();
        }
        if (updateResult > 0) {
            LOG.info("The email with ID: " + emailBean.getId() + " has updated its folder");
        }
        return updateResult;

    }

    private void updateEmailRecipients(EmailBean emailBean) throws SQLException {
        EmailAddress[] toList = emailBean.email.to();
        EmailAddress[] ccList = emailBean.email.cc();
        EmailAddress[] bccList = emailBean.email.bcc();

        if (toList.length >= 1) {
            for (EmailAddress address : toList) {
                int emailIndex = getEmailAddressIndex(address);
                executeEmailToAddressQuery(emailBean.getId(), emailIndex, "TO");
            }
        }
        if (ccList.length >= 1) {
            for (EmailAddress address : ccList) {
                int emailIndex = getEmailAddressIndex(address);
                executeEmailToAddressQuery(emailBean.getId(), emailIndex, "CC");
            }
        }
        if (bccList.length >= 1) {
            for (EmailAddress address : bccList) {
                int emailIndex = getEmailAddressIndex(address);
                executeEmailToAddressQuery(emailBean.getId(), emailIndex, "BCC");
            }
        }
    }

    /**
     * Helper method that deletes all rows of recipients associated with an
     * Email. Makes it so that a draft email can have its recipients updated.
     *
     * @param emailId The Email row that will update its recipients (only Draft
     * emails can update the content)
     * @throws SQLException
     */
    private void deleteEmailToAddressRow(int emailId) throws SQLException {
        String deleteRowsToBeUpdated = "DELETE FROM EMAILTOADDRESS WHERE EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(deleteRowsToBeUpdated);) {
            ps.setInt(1, emailId);
            int resultDeletes = ps.executeUpdate();
            if (resultDeletes > 0) {
                LOG.info("The recipients for the email with ID: \"" + emailId + "\" have been deleted.");
            }
        }
    }

    private void deleteEmailAttachments(int emailId) throws SQLException {
        String deleteOldAttachments = "DELETE FROM ATTACHMENTS WHERE EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(deleteOldAttachments);) {
            ps.setInt(1, emailId);
            int resultDeletes = ps.executeUpdate();
            if (resultDeletes > 0) {
                LOG.info("The attachments for the email with ID: \"" + emailId + "\" have been deleted.");
            }
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
    public int deleteFolder(String foldername) throws SQLException {
        int folderDeletes = -1;
        //Delete all associated rows in the EmailToAddresses and Attachments table.
        deleteFolderEmails(foldername);
        //Delete the emails in the Email table that are part of this folder
        deleteEmailTableRowsInFolder(foldername);
        String deleteFolderQuery = "DELETE FROM FOLDERS WHERE FOLDERNAME = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  
                PreparedStatement ps = connection.prepareStatement(deleteFolderQuery);) {
            ps.setString(1, foldername);
            folderDeletes = ps.executeUpdate();
        }
        if(folderDeletes > 0){
            LOG.info("The folder \"" + foldername + "\" along with its emails has been deleted.");
        }
        return folderDeletes;
    }

    private void deleteFolderEmails(String foldername) throws SQLException {
        //first find out all the emailId's that are in this folder, then call other helpers to delete associated rows
        String emailsInFolder = "SELECT EMAIL.EMAILID FROM EMAIL INNER JOIN FOLDERS ON EMAIL.FOLDERID = FOLDERS.FOLDERID WHERE FOLDERNAME = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(emailsInFolder);) {
            ps.setString(1, foldername);
            ResultSet emailsToDelete = ps.executeQuery();
            while (emailsToDelete.next()) {
                deleteEmailToAddressRow(emailsToDelete.getInt("EMAILID"));
                deleteEmailAttachments(emailsToDelete.getInt("EMAILID"));
            }
        }
    }

    private void deleteEmailTableRowsInFolder(String foldername) throws SQLException {
        //first find out all the emailId's that are in this folder, then call other helpers to delete associated rows
        String emailsInFolder = "DELETE FROM EMAIL INNER JOIN FOLDERS ON EMAIL.FOLDERID = FOLDERS.FOLDERID WHERE FOLDERS.FOLDERNAME = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(emailsInFolder);) {
            ps.setString(1, foldername);
            int deleteResults = ps.executeUpdate();
        }
    }

    @Override
    public int deleteEmail(int emailId) throws SQLException {
        int queryResult = -1;
        deleteEmailToAddressRow(emailId);
        deleteEmailAttachments(emailId);
        String deleteEmailRow = "DELETE FROM EMAIL WHERE EMAILID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(deleteEmailRow);) {
            ps.setInt(1, emailId);
            queryResult = ps.executeUpdate();
            if (queryResult == 1) {
                LOG.info("The email with ID: \"" + emailId + "\" has been deleted from the Email table.");
            }
        }
        return queryResult;
    }
}
