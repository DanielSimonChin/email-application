package com.danielsimonchin.persistence;

import com.danielsimonchin.properties.MailConfigBean;
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

/**
 * This class implements the EmailDAO. Contains multiple methods that perform
 * CRUD operations relating to Email objects and a database.
 *
 * @author Daniel
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
     *
     * @param email The email object which is to be made into an entry in the tables.
     * @return The number of records created, should always be 1
     * @throws SQLException
     */
    @Override
    public int create(Email email) throws SQLException {
        int result;
        String createQuery = "INSERT INTO Email (FromAddress,Subject,TextMessage,HtmlMessage,Folder,SentDate) values (?,?,?,?,?,?)";

        // Connection is only open for the operation and then immediately closed
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword()); // Using a prepared statement to handle the conversion
                // Using a prepared statement to handle the conversion
                // of special characters in the SQL statement and guard against
                // SQL Injection
                  PreparedStatement ps = connection.prepareStatement(createQuery, Statement.RETURN_GENERATED_KEYS);) {
            fillPreparedStatementForEmailTable(ps, email);
            result = ps.executeUpdate();
        }
        LOG.info("# of records created : " + result);
        return result;
    }

    /**
     * Set the parameterized values of the PreparedStatement using the Email
     * object given as input. The primary key will be auto-incremented so is not
     * assigned here.
     *
     * @param ps
     * @param email
     */
    private void fillPreparedStatementForEmailTable(PreparedStatement ps, Email email) throws SQLException {
        ps.setString(1, email.from().toString());
        ps.setString(2, email.subject());
        List<EmailMessage> messages = email.messages();
        String plainText = "";
        String htmlText = "";
        if(messages.size() == 2){
            plainText = messages.get(0).getContent();
            htmlText = messages.get(1).getContent();
        }
        else if(messages.size() == 1){
            if(messages.get(0).getMimeType().equals("TEXT/PLAIN")){
                plainText = messages.get(0).getContent();
            }
            else{
                htmlText = messages.get(1).getContent();
            }
        }
        ps.setString(3, plainText);
        ps.setString(4, htmlText);
        ps.setString(5, "Inbox");
        ps.setTimestamp(6, new Timestamp(email.sentDate().getTime()));
    }

    @Override
    public List<Email> findAll() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
