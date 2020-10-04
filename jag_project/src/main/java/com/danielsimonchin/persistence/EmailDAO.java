package com.danielsimonchin.persistence;

import com.danielsimonchin.properties.EmailBean;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jodd.mail.Email;

/**
 * Interface for CRUD Methods relating to an Email object and to the database.
 *
 * @author Daniel
 */
public interface EmailDAO {
    public int createEmailRecord(EmailBean emailBean) throws SQLException, IOException;

    public List<EmailBean> findAll() throws SQLException;

    public EmailBean findID(int id) throws SQLException;
    
    public List<EmailBean> findAllInFolder(int folderKey) throws SQLException;
    
    public List<EmailBean> findEmailsBySender(String sender) throws SQLException;
    
    public List<EmailBean> findEmailsBySubject(String subject) throws SQLException;
    
    public void updateDraft(EmailBean emailBean) throws SQLException;

    public int update(Email email) throws SQLException;

    public int delete(int id) throws SQLException;
    
    public int createFolder(String folderName) throws SQLException;
}
