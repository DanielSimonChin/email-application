/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.test.crudtests;

import com.danielsimonchin.business.SendAndReceive;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.persistence.EmailDAOImpl;
import com.danielsimonchin.properties.EmailBean;
import com.danielsimonchin.properties.MailConfigBean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import javax.mail.Flags;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailFilter;
import jodd.mail.EmailMessage;
import jodd.mail.MailServer;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests methods to test the function of the CRUD in EmailDAOImpl class.
 *
 * @author Daniel Simon Chin
 * @version Oct 7th, 2020
 */
public class CrudTests {

    private final static Logger LOG = LoggerFactory.getLogger(CrudTests.class);

    private SendAndReceive runMail;
    private String plainMsg = "Hello from plain text email: " + LocalDateTime.now();
    private String htmlMsg = "<html><META http-equiv=Content-Type "
            + "content=\"text/html; charset=utf-8\">"
            + "<body><h1>Here is my photograph embedded in "
            + "this email.</h1><img src='cid:FreeFall.jpg'>"
            + "<h2>I'm flying!</h2></body></html>";
    private String subject = "Jodd Test";
    private MailConfigBean mailConfigBean;

    //List of all recipients, will be used later when validating the emails received
    private List<MailConfigBean> allRecipients;
    //The mail config beans relating to the recipients of the email. The fields left as 0 or null are those relating to database that are not part of the email object.
    private MailConfigBean recipient1 = new MailConfigBean("recipient1", "danieldawsontest2@gmail.com", "Danieltester2", "imap.gmail.com", "smtp.gmail.com", "993", "465", "jdbc:mysql://localhost:3306/EMAILCLIENT", "EMAILCLIENT", "3306", "daniel", "danielpw");
    private MailConfigBean recipient2 = new MailConfigBean("recipient2", "danieldawsontest3@gmail.com", "Danieltester3", "imap.gmail.com", "smtp.gmail.com", "993", "465", "jdbc:mysql://localhost:3306/EMAILCLIENT", "EMAILCLIENT", "3306", "daniel", "danielpw");
    private MailConfigBean recipient3 = new MailConfigBean("recipient3", "recievedanieldawson1@gmail.com", "Danieltester4", "imap.gmail.com", "smtp.gmail.com", "993", "465", "jdbc:mysql://localhost:3306/EMAILCLIENT", "EMAILCLIENT", "3306", "daniel", "danielpw");

    //ArrayList of recipients in the To List
    private List<String> toList;
    //ArrayList of recipients in the CC List
    private List<String> ccList;
    //ArrayList of recipients in the BCC List
    private List<String> bccList;

    //ArrayList of attachment files
    private ArrayList<File> regularAttachments;
    //ArrayList of embedded attachment files
    private ArrayList<File> embeddedAttachments;

    /**
     * Before every test, re-instantiate all the lists and variables that will
     * be re-used in tests. Also seed the database and tables so that the
     * results of a test will not affect the next.
     */
    @Before
    public void createMailConfigBean() {
        //Use the root user to create the db and the tables. T
        this.mailConfigBean = new MailConfigBean("daniel", "danieldawsontest1@gmail.com", "Danieltester1", "imap.gmail.com", "smtp.gmail.com", "993", "465", "jdbc:mysql://localhost:3306/EMAILCLIENT", "EMAILCLIENT", "3306", "root", "logitech7790");
        seedDatabase("createEmailClientDB.sql");
        seedDatabase("createFoldersTable.sql");
        seedDatabase("createEmailTable.sql");
        seedDatabase("createAddressesTable.sql");
        seedDatabase("createAttachmentsTable.sql");
        seedDatabase("createEmailToAddressTable.sql");
        //Use the newly created user for the rest of the tests.
        this.mailConfigBean = new MailConfigBean("daniel", "danieldawsontest1@gmail.com", "Danieltester1", "imap.gmail.com", "smtp.gmail.com", "993", "465", "jdbc:mysql://localhost:3306/EMAILCLIENT", "EMAILCLIENT", "3306", "daniel", "danielpw");

        allRecipients = new ArrayList<>();
        toList = new ArrayList<>();
        ccList = new ArrayList<>();
        bccList = new ArrayList<>();
        allRecipients.add(recipient1);
        allRecipients.add(recipient2);
        allRecipients.add(recipient3);
        regularAttachments = new ArrayList<>();
        embeddedAttachments = new ArrayList<>();
        //This is to make sure that all emails in all recipient accounts have seen/read the emails sent. Prevents having a test affect the result of the next.
        allRecipients.stream().map(recipient -> MailServer.create()
                .host(recipient.getImapUrl())
                .ssl(true)
                .auth(recipient.getUserEmailAddress(), recipient.getPassword())
                .buildImapMailServer()).forEachOrdered(imapServer -> {
            try ( ReceiveMailSession session = imapServer.createSession()) {
                session.open();
                //read all the emails and mark them as seen
                ReceivedEmail[] receivedEmails = session.receiveEmailAndMarkSeen(EmailFilter.filter().flag(Flags.Flag.SEEN, false));
            }
        });

    }

    /**
     * Testing that the createEmailRecord method can take a sent email and make
     * the proper inserts into the db.
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testEmailCreate() throws SQLException, IOException {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding one person into the CC list
        ccList.add(recipient2.getUserEmailAddress());
        //Adding one person into the BCC list
        bccList.add(recipient3.getUserEmailAddress());
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        //Adding an embedded attachment to the embedded attachment lists
        embeddedAttachments.add(new File("FreeFall.jpg"));
        //Instantiate a SendAndReceive object to utilize its methods
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //since we are sending it, it does not have a receivedDate so we leave it as null. The emailId will be set in the create method. 2 represents the sent folder
        EmailBean emailBean = new EmailBean(-1, 2, null, resultEmail);
        //execute the inserts into the Email,EmailToAddresses and Attachments table.
        int result = crud.createEmailRecord(emailBean);
        EmailBean findByIdBean = crud.findID(emailBean.getId());
        assertEquals(emailBean, findByIdBean);
    }

    /**
     * Send and receive and Email. Pass all the receivedEmail objects to the
     * create method and count the number of rows inserted into the Email table.
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testEmailCreateWithReceivedEmail() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding one person into the CC list
        ccList.add(recipient2.getUserEmailAddress());
        //Adding one person into the BCC list
        bccList.add(recipient3.getUserEmailAddress());
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        //Adding an embedded attachment to the embedded attachment lists
        embeddedAttachments.add(new File("FreeFall.jpg"));
        //Instantiate a SendAndReceive object to utilize its methods
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        int countEmailsInserted = 0;
        for (MailConfigBean recipient : allRecipients) {
            //The email array for each recipient. In this case is 1 each, since each recipient was only sent 1 email.
            ReceivedEmail[] receivedEmails = runMail.receiveEmail(recipient);
            for (ReceivedEmail item : receivedEmails) {
                //Creating an email bean initializing it with a ReceivedEmail object.
                EmailBean emailBean = new EmailBean(-1, 1, item);
                countEmailsInserted += crud.createEmailRecord(emailBean);
            }
        }
        assertEquals(3, countEmailsInserted);
    }

    /**
     * Calling the create method with a draft email bean as input. The sent and
     * received dates should be null in this case.
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testEmailCreateWithDraftEmail() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding one person into the CC list
        ccList.add(recipient2.getUserEmailAddress());
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        Email email = createEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        //The id will be set in the create method, the folder is 3 for "drafts", a draft has a null received date, give the bean an email object
        EmailBean emailBean = new EmailBean(-1, 3, null, email);
        int createResult = crud.createEmailRecord(emailBean);
        EmailBean findByIdBean = crud.findID(emailBean.getId());
        assertEquals(emailBean, findByIdBean);
    }

    /**
     * Testing that the create method will add new email addresses to the
     * addresses table if it encounters an address that is not in the db yet.
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testEmailCreateWithUnkownRecipients() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding one person into the CC list
        ccList.add(recipient2.getUserEmailAddress());
        //Adding new recipients into the bcc fields
        bccList.add("newrecipient1@gmail.com");
        bccList.add("newrecipient2@gmail.com");
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        Email email = createEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        //The id will be set in the create method, the folder is 3 for "drafts", a draft has a null received date, give the bean an email object
        EmailBean emailBean = new EmailBean(-1, 3, null, email);
        int createResult = crud.createEmailRecord(emailBean);
        EmailBean findByIdBean = crud.findID(emailBean.getId());
        assertEquals(emailBean, findByIdBean);
    }

    /**
     * Helper method that constructs and fills an Email object for testing
     * purposes
     *
     * @param toList
     * @param ccList
     * @param bccList
     * @param subject
     * @param textMsg
     * @param htmlMsg
     * @param regularAttachments
     * @param embeddedAttachments
     * @return An Email object with its fields set.
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

    @Test
    public void testFindAll() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailBeanList = crud.findAll();
        assertEquals("testFindAll: ", 4, emailBeanList.size());
    }

    @Test
    public void testFindById() throws SQLException, IOException {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding one person into the CC list
        ccList.add(recipient2.getUserEmailAddress());
        //Adding one person into the BCC list
        bccList.add(recipient3.getUserEmailAddress());
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        //Adding an embedded attachment to the embedded attachment lists
        //embeddedAttachments.add(new File("FreeFall.jpg"));
        embeddedAttachments.add(new File("FreeFall.jpg"));
        //Instantiate a SendAndReceive object to utilize its methods
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);

        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //since we are sending it, it does not have a receivedDate so we leave it as null.
        EmailBean emailBean = new EmailBean(-1, 2, null, resultEmail);
        int result = crud.createEmailRecord(emailBean);
        EmailBean resultBean = crud.findID(emailBean.getId());
        assertEquals("Test findById :", emailBean, resultBean);
    }

    @Test
    public void testFindAllInFolder() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Finding all emails in the SENT folder
        List<EmailBean> emailsInFolder = crud.findAllInFolder("SENT");
        assertEquals(emailsInFolder.size(), checkCountEmailsInFolder("SENT"));
    }

    private int checkCountEmailsInFolder(String folderName) throws SQLException {
        int countFound = -1;
        String countEmailsInFolder = "SELECT COUNT(*) AS 'EMAILCOUNT' FROM EMAIL INNER JOIN FOLDERS ON EMAIL.FOLDERID = FOLDERS.FOLDERID WHERE FOLDERNAME = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(countEmailsInFolder);) {
            ps.setString(1, folderName);
            ResultSet countResult = ps.executeQuery();
            if (countResult.next()) {
                countFound = countResult.getInt("EMAILCOUNT");
            }
        }
        return countFound;
    }

    @Test
    public void testFindBySender() throws SQLException, FileNotFoundException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailsBySender = crud.findEmailsBySender("danieldawsontest1@gmail.com");
        assertEquals(emailsBySender.size(), checkCountBySender("danieldawsontest1@gmail.com"));
    }

    private int checkCountBySender(String sender) throws SQLException {
        int countFound = -1;
        String countEmailsBySender = "SELECT COUNT(*) AS 'EMAILCOUNT' FROM EMAIL WHERE FROMADDRESS = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(countEmailsBySender);) {
            ps.setString(1, sender);
            ResultSet countResult = ps.executeQuery();
            if (countResult.next()) {
                countFound = countResult.getInt("EMAILCOUNT");
            }
        }
        return countFound;
    }

    @Test
    public void testFindByRecipient() throws SQLException, FileNotFoundException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailsByRecipient = crud.findEmailsByRecipient("danieldawsontest2@gmail.com");
        LOG.info("REAL SIZE " + emailsByRecipient.size());
        assertEquals(emailsByRecipient.size(), checkCountByRecipient("danieldawsontest2@gmail.com"));
    }

    private int checkCountByRecipient(String recipient) throws SQLException {
        int countFound = -1;
        String countEmailsBySender = "SELECT COUNT(*) AS 'EMAILCOUNT' FROM EMAILTOADDRESS INNER JOIN ADDRESSES ON EMAILTOADDRESS.ADDRESSID = ADDRESSES.ADDRESSID WHERE ADDRESSES.EMAILADDRESS = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(countEmailsBySender);) {
            ps.setString(1, recipient);
            ResultSet countResult = ps.executeQuery();
            if (countResult.next()) {
                countFound = countResult.getInt("EMAILCOUNT");
            }
        }
        return countFound;
    }

    @Test
    public void testFindBySubject() throws SQLException, FileNotFoundException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailsBySubject = crud.findEmailsBySubject("SQL TESTING");
        emailsBySubject.forEach(bean -> {
            LOG.info("ID: " + bean.getId() + " has subject:  " + bean.email.subject());
        });
        assertEquals(emailsBySubject.size(), checkCountBySubject("SQL TESTING"));
    }

    private int checkCountBySubject(String subject) throws SQLException {
        int countFound = -1;
        String countEmailsBySubject = "SELECT COUNT(*) AS 'EMAILCOUNT' FROM EMAIL WHERE SUBJECT = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  PreparedStatement ps = connection.prepareStatement(countEmailsBySubject);) {
            ps.setString(1, subject);
            ResultSet countResult = ps.executeQuery();
            if (countResult.next()) {
                countFound = countResult.getInt("EMAILCOUNT");
            }
        }
        return countFound;
    }

    @Test
    public void testFolderCreation() throws SQLException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int foldersCreated = crud.createFolder("MyImportantDocuments");
        assertEquals(1, foldersCreated);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFolderAlreadyExists() throws SQLException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int foldersCreated = crud.createFolder("INBOX");
        fail("The folder was created.");
    }

    @Test
    public void testUpdateDraftEmail() throws SQLException, FileNotFoundException, IOException {

        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Retrieving one of the email in the draft folder (mock data)
        EmailBean beforeUpdateBean = crud.findID(4);

        List<EmailMessage> messages = beforeUpdateBean.email.messages();
        LOG.info("SIZE BEFORE: " + messages.size());
        //Adding one person to the TO recipients
        beforeUpdateBean.email.to("newrecipient1@gmail.com");
        //Adding one person to the CC recipients
        beforeUpdateBean.email.to("newrecipient2@gmail.com");
        //changing the subject
        beforeUpdateBean.email.subject("Testing updating a subject");

        //changing text
        beforeUpdateBean.email.textMessage("THIS IS A NEW TEXT MESSAGE!");
        beforeUpdateBean.email.htmlMessage("MY NEW HTML MESSAGE!");

        //Adding a new embedded attachments
        beforeUpdateBean.email.embeddedAttachment(EmailAttachment.with().content(new File("panda.jpeg")));

        beforeUpdateBean.setFolderKey(2);
        int resultUpdates = crud.updateDraft(beforeUpdateBean);

        EmailBean afterUpdateBean = crud.findID(4);

        int s1 = beforeUpdateBean.email.to().length;
        int s2 = afterUpdateBean.email.to().length;
        LOG.info("TO SIZES: " + s1 + " " + s2);
        assertEquals(beforeUpdateBean, afterUpdateBean);
        //need to fix equals for recipients
    }

    @Test
    public void testUpdateFolder() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        EmailBean emailBean = crud.findID(1);
        emailBean.setFolderKey(2);
        int queryResult = crud.updateFolder(emailBean);
        assertEquals(1, queryResult);
    }

    @Test
    public void testDeleteById() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int deleteResult = crud.deleteEmail(4);
        assertEquals(1, deleteResult);
    }

    /**
     * This routine recreates the database before every test. This makes sure
     * that a destructive test will not interfere with any other test. Does not
     * support stored procedures.
     *
     * This routine is courtesy of Bartosz Majsak, an Arquillian developer at
     * JBoss
     */
    private void seedDatabase(String filename) {
        LOG.info("@Before seeding");
        final String seedDataScript = loadAsString(filename);
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());) {
            for (String statement : splitStatements(new StringReader(seedDataScript), ";")) {
                connection.prepareStatement(statement).execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed seeding database", e);
        }
    }

    /**
     * The following methods support the seedDatabse method
     */
    private String loadAsString(final String path) {

        try ( InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);  Scanner scanner = new Scanner(inputStream)) {
            return scanner.useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new RuntimeException("Unable to close input stream.", e);
        }
    }

    private List<String> splitStatements(Reader reader, String statementDelimiter) {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        final StringBuilder sqlStatement = new StringBuilder();
        final List<String> statements = new LinkedList<>();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || isComment(line)) {
                    continue;
                }
                sqlStatement.append(line);
                if (line.endsWith(statementDelimiter)) {
                    statements.add(sqlStatement.toString());
                    sqlStatement.setLength(0);
                }
            }
            return statements;
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing sql", e);
        }
    }

    private boolean isComment(final String line) {
        return line.startsWith("--") || line.startsWith("//") || line.startsWith("/*") || line.startsWith("*");
    }
}
