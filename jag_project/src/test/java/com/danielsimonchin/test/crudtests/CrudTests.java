package com.danielsimonchin.test.crudtests;

import com.danielsimonchin.business.SendAndReceive;
import com.danielsimonchin.exceptions.CannotDeleteFolderException;
import com.danielsimonchin.exceptions.CannotMoveToDraftsException;
import com.danielsimonchin.exceptions.CannotRenameFolderException;
import com.danielsimonchin.exceptions.FolderAlreadyExistsException;
import com.danielsimonchin.exceptions.InvalidMailConfigBeanUsernameException;
import com.danielsimonchin.exceptions.InvalidRecipientImapURLException;
import com.danielsimonchin.exceptions.NotEnoughEmailRecipientsException;
import com.danielsimonchin.exceptions.NotEnoughRecipientsException;
import com.danielsimonchin.exceptions.RecipientEmailAddressNullException;
import com.danielsimonchin.exceptions.RecipientInvalidFormatException;
import com.danielsimonchin.exceptions.RecipientListNullException;
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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests methods to test the function of the CRUD in EmailDAOImpl class.
 * The createEmailClientDB.sql file must have been executed before running these
 * unit tests. This allows us to us the created user in the script.
 *
 * @author Daniel Simon Chin
 * @version Oct 7th, 2020
 */
@Ignore //Ignoring so maven does not run the tests during Phase3 (quicker)
public class CrudTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        //Use the created user to create the tables
        this.mailConfigBean = new MailConfigBean("daniel", "danieldawsontest1@gmail.com", "Danieltester1", "imap.gmail.com", "smtp.gmail.com", "993", "465", "jdbc:mysql://localhost:3306/EMAILCLIENT", "EMAILCLIENT", "3306", "daniel", "danielpw");
        seedDatabase("createFoldersTable.sql");
        seedDatabase("createEmailTable.sql");
        seedDatabase("createAddressesTable.sql");
        seedDatabase("createAttachmentsTable.sql");
        seedDatabase("createEmailToAddressTable.sql");

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
     * @throws com.danielsimonchin.exceptions.NotEnoughEmailRecipientsException
     * @throws
     * com.danielsimonchin.exceptions.InvalidMailConfigBeanUsernameException
     * @throws com.danielsimonchin.exceptions.RecipientListNullException
     * @throws com.danielsimonchin.exceptions.RecipientEmailAddressNullException
     * @throws com.danielsimonchin.exceptions.RecipientInvalidFormatException
     */
    @Test
    public void testEmailCreate() throws SQLException, IOException, NotEnoughEmailRecipientsException, InvalidMailConfigBeanUsernameException, RecipientListNullException, RecipientEmailAddressNullException, RecipientInvalidFormatException {
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
        //find and replicate the bean using the FindId to ensure that the table inserts were correct.
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
    public void testEmailCreateWithReceivedEmail() throws SQLException, IOException, NotEnoughEmailRecipientsException, InvalidMailConfigBeanUsernameException, RecipientListNullException, RecipientEmailAddressNullException, RecipientInvalidFormatException, InvalidRecipientImapURLException {
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
        //find and replicate the bean using the FindId to ensure that the table inserts were correct.
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
        //Adding an embedded attachment to the embedded attachment lists
        embeddedAttachments.add(new File("FreeFall.jpg"));
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

    /**
     * Testing the create folder method and passing a proper argument. The
     * "MyImportantDocuments" is not in the db yet so the program will add it to
     * the folders table
     *
     * @throws SQLException
     * @throws FolderAlreadyExistsException
     */
    @Test
    public void testFolderCreation() throws SQLException, FolderAlreadyExistsException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int foldersCreated = crud.createFolder("MyImportantDocuments");
        assertEquals(1, foldersCreated);
    }

    /**
     * Test that the create folder will create a folder even if it isn't
     * composed of letters. Modern email apps such as outlook creates it
     * regardless of the string's content
     *
     * @throws SQLException
     * @throws FolderAlreadyExistsException
     */
    @Test
    public void testFolderCreationWithSpecialCharacters() throws SQLException, FolderAlreadyExistsException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int foldersCreated = crud.createFolder("?!!..[]<>");
        assertEquals(1, foldersCreated);
    }

    /**
     * Testing that the method will throw a FolderAlreadyExistsException since
     * the inbox folder already exists in the db.
     *
     * @throws SQLException
     * @throws FolderAlreadyExistsException
     */
    @Test
    public void testFolderAlreadyExists() throws SQLException, FolderAlreadyExistsException {
        thrown.expect(FolderAlreadyExistsException.class);
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int foldersCreated = crud.createFolder("INBOX");
    }

    /**
     * Attempt to create a folder that we just added to the db, should throw a
     * custom exception
     *
     * @throws SQLException
     * @throws FolderAlreadyExistsException
     * @throws com.danielsimonchin.exceptions.CannotDeleteFolderException
     */
    @Test
    public void testFolderCreateAndThrow() throws SQLException, FolderAlreadyExistsException, CannotDeleteFolderException {
        thrown.expect(FolderAlreadyExistsException.class);
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int foldersCreated = crud.createFolder("MyImportantDocuments");
        int createSameFolder = crud.createFolder("MyImportantDocuments");
    }

    /**
     * The db starts off with 6 entries in the Email table. The findAll
     * constructs and returns all of their emailBeans
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testFindAll() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailBeanList = crud.findAll();
        assertEquals("testFindAll: ", 6, emailBeanList.size());
    }

    /**
     * FindAll the emails after creating an Email entry using the
     * createEmailRecord method. Should result in the size of all the emails + 1
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testFindAllAfterAddingAnEmail() throws SQLException, IOException {
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
        //Add the email to the db
        int createResult = crud.createEmailRecord(emailBean);
        List<EmailBean> emailBeanList = crud.findAll();
        assertEquals("testFindAll: ", 6 + 1, emailBeanList.size());
    }

    /**
     * FindAll the emailBeans, then compare them to the findById using the
     * equals method. All emailBeans should be valid.
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testFindAllCompareContent() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailBeanList = crud.findAll();
        List<EmailBean> comparisonList = new ArrayList<>();
        for (int i = 1; i <= emailBeanList.size(); i++) {
            comparisonList.add(crud.findID(i));
        }
        int countValid = 0;
        for (int i = 0; i < emailBeanList.size(); i++) {
            //increment the count if the contents of the emailBean is valid
            if (emailBeanList.get(i).equals(comparisonList.get(i))) {
                countValid++;
            }
        }
        assertEquals(emailBeanList.size(), countValid);
    }

    /**
     * Insert a sent email into the db and retrieve it using FindID. Then
     * compare the two results using the equals method in EmailBean class.
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testFindById() throws SQLException, IOException, NotEnoughEmailRecipientsException, InvalidMailConfigBeanUsernameException, RecipientListNullException, RecipientEmailAddressNullException, RecipientInvalidFormatException {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding one person into the CC list
        ccList.add("newrecipient1@gmail.com");
        //Adding one person into the BCC list
        bccList.add("newrecipient2@gmail.com");
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        //Adding an embedded attachment to the embedded attachment lists
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

    /**
     * Insert a draft email into the db and compare the createdBean with the
     * retrievedBean using findID. Check that its date received is null and
     * belongs in the draft folder
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testFindByIdDraft() throws SQLException, IOException {
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
        Email resultEmail = createEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //draft email which has no receivedDate 
        EmailBean emailBean = new EmailBean(-1, 3, null, resultEmail);
        int result = crud.createEmailRecord(emailBean);
        EmailBean resultBean = crud.findID(emailBean.getId());
        boolean testResult = false;
        if (emailBean.equals(resultBean) && resultBean.getFolderKey() == 3 && resultBean.getReceivedDate() == null) {
            testResult = true;
        }
        assertTrue(testResult);
    }

    /**
     * Find all the emails in a folder and return a list of EmailBean. Uses a
     * helper method that returns the count of emails in that folder
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testFindAllInSentFolder() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Finding all emails in the SENT folder
        List<EmailBean> emailsInFolder = crud.findAllInFolder("SENT");
        LOG.info("list size " + emailsInFolder.size());
        assertEquals(emailsInFolder.size(), checkCountEmailsInFolder("SENT"));
    }

    /**
     * Check that the returned list of EmailBean object from the FindAllInFolder
     * method returns the correct amount of emails. Uses helper that returns
     * count of emails in a folder.
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testFindAllInDraftFolder() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Finding all emails in the DRAFT folder
        List<EmailBean> emailsInFolder = crud.findAllInFolder("DRAFT");
        LOG.info("list size " + emailsInFolder.size());
        assertEquals(emailsInFolder.size(), checkCountEmailsInFolder("DRAFT"));
    }

    /**
     * Create a new folder and check that the amount of emails in that folder is
     * 0.
     *
     * @throws SQLException
     * @throws IOException
     * @throws FolderAlreadyExistsException
     */
    @Test
    public void testFindAllInFolderAfterFolderCreation() throws SQLException, IOException, FolderAlreadyExistsException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int folderCreation = crud.createFolder("MyImportantDocuments");
        //Finding all emails in the MyImportantDocuments folder
        List<EmailBean> emailsInFolder = crud.findAllInFolder("MyImportantDocuments");
        LOG.info("list size " + emailsInFolder.size());
        //Should be zero since this folder has not added any emails yet
        assertEquals(emailsInFolder.size(), checkCountEmailsInFolder("MyImportantDocuments"));
    }

    /**
     * Helper method that returns the count of emails in a certain folder.
     *
     * @param folderName
     * @return int representing how many rows in Email table are part of a
     * folder
     * @throws SQLException
     */
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

    /**
     * Check that the count of emailBeans with that recipient
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testFindByRecipient() throws SQLException, FileNotFoundException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailsByRecipient = crud.findEmailsByRecipient("danieldawsontest2@gmail.com");
        LOG.info("REAL SIZE " + emailsByRecipient.size());
        assertEquals(emailsByRecipient.size(), checkCountByRecipient("danieldawsontest2@gmail.com"));
    }

    /**
     * Sends an email to a new address that doesn't exist in the db yet and then
     * finds all the emails that are send to that recipient. Compares two
     * emailBeans using the overwritten equals method
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testFindByRecipientCheckContents() throws SQLException, FileNotFoundException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Adding one person into the TO list
        toList.add("newrecipient1@gmail.com");
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        Email email = createEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        //The id will be set in the create method, the folder is 3 for "drafts", a draft has a null received date, give the bean an email object
        EmailBean emailBean = new EmailBean(-1, 3, null, email);
        int createResult = crud.createEmailRecord(emailBean);

        List<EmailBean> emailsByRecipient = crud.findEmailsByRecipient("newrecipient1@gmail.com");
        LOG.info("REAL SIZE " + emailsByRecipient.size());
        assertEquals(emailBean, emailsByRecipient.get(0));
    }

    /**
     * This method makes sure that when checking for an email recipient that has
     * never been sent an Email, the program will simply return an empty list or
     * EmailBean. It should never throw an exception for not finding any
     * results.
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testFindByRecipientsUnkownAddress() throws SQLException, FileNotFoundException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailsByRecipient = crud.findEmailsByRecipient("noEmailsSentHere@gmail.com");
        assertEquals(0, emailsByRecipient.size());
    }

    /**
     * Helper method that returns the count of emails that have a specific
     * recipient in the TO,CC OR BCC field
     *
     * @param recipient A recipient which will be searched across all emails
     * @return int represent the count of emails found
     * @throws SQLException
     */
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

    /**
     * Ensure that the returned List of EmailBean is the same as the actual
     * emails with a specific Subject. Uses a helper method to return the count
     * of emails with a specific subject.
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testFindBySubject() throws SQLException, FileNotFoundException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailsBySubject = crud.findEmailsBySubject("SQL TESTING");
        emailsBySubject.forEach(bean -> {
            LOG.info("ID: " + bean.getId() + " has subject:  " + bean.email.subject());
        });
        assertEquals(emailsBySubject.size(), checkCountBySubject("SQL TESTING"));
    }

    /**
     * Add an email with a unique subject, retrieve it using the findBySubject
     * method and compare the two beans using equals() method.
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testFindBySubjectCheckContents() throws SQLException, FileNotFoundException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Adding one person into the TO list
        toList.add("newrecipient1@gmail.com");
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        Email email = createEmail(toList, ccList, bccList, "MY EMAIL SUBJECT", plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        //The id will be set in the create method, the folder is 3 for "drafts", a draft has a null received date, give the bean an email object
        EmailBean emailBean = new EmailBean(-1, 3, null, email);
        int createResult = crud.createEmailRecord(emailBean);
        List<EmailBean> emailsBySubject = crud.findEmailsBySubject("MY EMAIL SUBJECT");
        boolean testResult = false;
        if (emailsBySubject.size() == 1 && emailBean.equals(emailsBySubject.get(0))) {
            testResult = true;
        }
        assertTrue(testResult);
    }

    /**
     * This ensures that the program does not fail if it does not find any
     * matching emails with this subject. It simply returns an empty List.
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testFindBySubjectNoResultsFound() throws SQLException, FileNotFoundException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailsBySubject = crud.findEmailsBySubject("NO EMAIL HAS THIS SUBJECT YET");
        assertEquals(0, emailsBySubject.size());
    }

    /**
     * Check the amount of emails that have this subject
     *
     * @param subject
     * @return int returning how many emails have this subject
     * @throws SQLException
     */
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

    /**
     * Retrieve a draft email from the mock data, changes multiple fields in the
     * email object.Call the update method and then findById. Now that the
     * update method was called, the EmailBean returned will be identical to the
     * EmailBean that was modified.
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws com.danielsimonchin.exceptions.NotEnoughRecipientsException
     */
    @Test
    public void testUpdateDraftEmail() throws SQLException, FileNotFoundException, IOException, NotEnoughRecipientsException {
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

        //changing text AND html messages
        beforeUpdateBean.email.textMessage("THIS IS A NEW TEXT MESSAGE!");
        beforeUpdateBean.email.htmlMessage("MY NEW HTML MESSAGE!");

        //Adding a new embedded attachments
        beforeUpdateBean.email.embeddedAttachment(EmailAttachment.with().content(new File("FreeFall.jpg")));

        int resultUpdates = crud.updateDraft(beforeUpdateBean);
        EmailBean afterUpdateBean = crud.findID(4);
        assertEquals(beforeUpdateBean, afterUpdateBean);
    }

    /**
     * Retrieves one of the mock email (4) and update it's folder to "sent" so
     * that the method will send the draft email.
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws com.danielsimonchin.exceptions.NotEnoughRecipientsException
     */
    @Test
    public void testSendDraftEmail() throws SQLException, FileNotFoundException, IOException, NotEnoughRecipientsException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Retrieving one of the email in the draft folder (mock data)
        EmailBean draftToBeSent = crud.findID(4);
        //Setting the folder to "2" meaning I want to send the draft email
        draftToBeSent.setFolderKey(2);
        //Call the updateDraft method to perform the updates and send the email.
        int resultUpdates = crud.updateDraft(draftToBeSent);
        //Get the emailBean after the update
        EmailBean afterUpdateBean = crud.findID(4);
        boolean testResult = false;
        if (afterUpdateBean.getFolderKey() == 2 && draftToBeSent.equals(afterUpdateBean)) {
            testResult = true;
        }
        assertTrue(testResult);
    }

    /**
     * Test that if the user decides to remove all fields from a draft and leave
     * it empty. The updateDraft method will execute the changes accordingly.
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NotEnoughRecipientsException
     */
    @Test
    public void testSendEmptyDraft() throws SQLException, FileNotFoundException, IOException, NotEnoughRecipientsException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Retrieving one of the email in the draft folder (mock data)
        EmailBean draftToBeSent = crud.findID(4);
        //Re-initialize the bean's Email object so it is comletely empty.
        draftToBeSent.email = new Email();
        //Call the updateDraft method to perform the updates and send the email.
        int resultUpdates = crud.updateDraft(draftToBeSent);
        assertEquals(1, resultUpdates);
    }

    /**
     * Check that the method will throw an exception if the draft that is to be
     * sent is not given any recipients.
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NotEnoughRecipientsException
     */
    @Test
    public void testSendDraftNoRecipients() throws SQLException, FileNotFoundException, IOException, NotEnoughRecipientsException {
        thrown.expect(NotEnoughRecipientsException.class);
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Retrieving one of the email in the draft folder (mock data)
        EmailBean draftToBeSent = crud.findID(4);
        //Setting the folder to "2" meaning I want to send the draft email
        draftToBeSent.setFolderKey(2);
        //Re-initialize the bean's Email object so it has no recipients. This will cause the program to throw an exception
        draftToBeSent.email = new Email();
        //Call the updateDraft method to perform the updates and send the email.
        int resultUpdates = crud.updateDraft(draftToBeSent);
    }

    /**
     * Retrieve the email with index 1 (Inbox) and move it to the inbox folder.
     * Expect the method to return 1 since one row has been changed in the Email
     * table
     *
     * @throws SQLException
     * @throws IOException
     * @throws CannotMoveToDraftsException
     */
    @Test
    public void testUpdateFolderToInbox() throws SQLException, IOException, CannotMoveToDraftsException, NotEnoughEmailRecipientsException, InvalidMailConfigBeanUsernameException, RecipientListNullException, RecipientEmailAddressNullException, RecipientInvalidFormatException {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        runMail = new SendAndReceive(mailConfigBean);

        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //since we are sending it, it does not have a receivedDate so we leave it as null. The emailId will be set in the create method. 2 represents the sent folder
        EmailBean emailBean = new EmailBean(-1, 2, null, resultEmail);
        //execute the inserts into createEmailRecordthe Email,EmailToAddresses and Attachments table.
        int result = crud.createEmailRecord(emailBean);
        emailBean.setFolderKey(1);
        int queryResult = crud.updateFolder(emailBean);
        assertEquals(1, queryResult);
    }

    /**
     * Expect an exception when the user tries to move a sent or inbox email to
     * the draft folder.
     *
     * @throws SQLException
     * @throws IOException
     * @throws CannotMoveToDraftsException
     */
    @Test
    public void testUpdateFolderToDraftError() throws SQLException, IOException, CannotMoveToDraftsException {
        thrown.expect(CannotMoveToDraftsException.class);
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //This email is originally in the sent folder
        EmailBean toUpdateFolder = crud.findID(1);
        //set the new folder to draft(not allowed)
        toUpdateFolder.setFolderKey(3);
        //call the method to update the email's folder
        int queryResult = crud.updateFolder(toUpdateFolder);
    }

    /**
     * Delete the email with index 1, along with its recipients and addresses in
     * connecting tables. Expect to return 1 since 1 row was deleted in the
     * EmailTable.
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testDeleteById() throws SQLException, IOException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int deleteResult = crud.deleteEmail(1);
        assertEquals(1, deleteResult);
    }

    /**
     * Create a new folder, then delete the along with all its emails.Returns 1
     * since 1 folder was removed from Folders table.
     *
     * @throws SQLException
     * @throws com.danielsimonchin.exceptions.CannotDeleteFolderException
     * @throws com.danielsimonchin.exceptions.FolderAlreadyExistsException
     */
    @Test
    public void deleteFolderTest() throws SQLException, CannotDeleteFolderException, FolderAlreadyExistsException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int createFolderResult = crud.createFolder("My Documents");
        int deleteFolderResult = crud.deleteFolder("My Documents");
        assertEquals(1, deleteFolderResult);
    }

    /**
     * Expect an exception to be thrown when the folder that the user wants to
     * delete is either INBOX,SENT,DRAFT.
     *
     * @throws SQLException
     * @throws CannotDeleteFolderException
     * @throws FolderAlreadyExistsException
     */
    @Test
    public void deleteFolderThrowTest() throws SQLException, CannotDeleteFolderException, FolderAlreadyExistsException {
        thrown.expect(CannotDeleteFolderException.class);
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int deleteFolderResult = crud.deleteFolder("SENT");
        assertEquals(1, deleteFolderResult);
    }

    /**
     * Create a folder and
     *
     * @throws SQLException
     * @throws FolderAlreadyExistsException
     * @throws CannotRenameFolderException
     */
    @Test
    public void testRenameFolder() throws SQLException, FolderAlreadyExistsException, CannotRenameFolderException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int createFolderResult = crud.createFolder("My Documents");
        int updateFolderNameResult = crud.updateFolderName("My Documents", "MY NEW FOLDER NAME");
        assertEquals(1, updateFolderNameResult);
    }

    /**
     * When attempting to change the name, but the user provides the same folder
     * name, the method returns 1. Indicating that not changes were made
     *
     * @throws SQLException
     * @throws FolderAlreadyExistsException
     * @throws CannotRenameFolderException
     */
    @Test
    public void testRenameFolderSameName() throws SQLException, FolderAlreadyExistsException, CannotRenameFolderException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int createFolderResult = crud.createFolder("My Documents");
        int updateFolderNameResult = crud.updateFolderName("My Documents", "My Documents");
        assertEquals(1, updateFolderNameResult);
    }

    /**
     * Expect to throw a CannotRenameFolderException when trying to rename an
     * essential folder such as INBOX,SENT,DRAFT
     *
     * @throws SQLException
     * @throws FolderAlreadyExistsException
     * @throws CannotRenameFolderException
     */
    @Test
    public void testCannotRenameFolderSent() throws SQLException, FolderAlreadyExistsException, CannotRenameFolderException {
        thrown.expect(CannotRenameFolderException.class);
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int updateFolderNameResult = crud.updateFolderName("SENT", "My Documents");
    }

    /**
     * Expect to throw a CannotRenameFolderException when trying to rename an
     * essential folder such as INBOX,SENT,DRAFT
     *
     * @throws SQLException
     * @throws FolderAlreadyExistsException
     * @throws CannotRenameFolderException
     */
    @Test
    public void testCannotRenameFolderInbox() throws SQLException, FolderAlreadyExistsException, CannotRenameFolderException {
        thrown.expect(CannotRenameFolderException.class);
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int updateFolderNameResult = crud.updateFolderName("INBOX", "My Documents");
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
