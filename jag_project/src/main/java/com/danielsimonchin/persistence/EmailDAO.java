package com.danielsimonchin.persistence;

import com.danielsimonchin.exceptions.CannotDeleteFolderException;
import com.danielsimonchin.exceptions.CannotMoveToDraftsException;
import com.danielsimonchin.exceptions.CannotRenameFolderException;
import com.danielsimonchin.exceptions.FolderAlreadyExistsException;
import com.danielsimonchin.exceptions.NotEnoughRecipientsException;
import com.danielsimonchin.properties.EmailBean;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface for CRUD Methods relating to an Email object and to the database.
 *
 * @author Daniel
 */
public interface EmailDAO {
    public int createEmailRecord(EmailBean emailBean) throws SQLException, IOException;

    public List<EmailBean> findAll() throws SQLException, FileNotFoundException,IOException;

    public EmailBean findID(int id) throws SQLException, FileNotFoundException,IOException;
    
    public List<EmailBean> findAllInFolder(String folderName) throws SQLException,FileNotFoundException,IOException;
    
    public List<EmailBean> findEmailsByRecipient(String recipientEmailAddress) throws SQLException, FileNotFoundException, IOException;
            
    public List<EmailBean> findEmailsBySubject(String subject) throws SQLException,FileNotFoundException,IOException;
    
    public int updateDraft(EmailBean emailBean) throws SQLException,IOException,NotEnoughRecipientsException;
    
    public int updateFolder(EmailBean emailBean) throws SQLException,CannotMoveToDraftsException;
    
    public int updateFolderName(String currentName, String newName) throws SQLException, CannotRenameFolderException;

    public int deleteEmail(int emailId) throws SQLException;
    
    public int createFolder(String folderName) throws SQLException,FolderAlreadyExistsException;
    
    public int deleteFolder(String foldername)throws SQLException,CannotDeleteFolderException;
}
