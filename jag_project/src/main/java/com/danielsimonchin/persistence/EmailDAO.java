package com.danielsimonchin.persistence;

import com.danielsimonchin.exceptions.CannotDeleteFolderException;
import com.danielsimonchin.exceptions.CannotMoveToDraftsException;
import com.danielsimonchin.exceptions.CannotRenameFolderException;
import com.danielsimonchin.exceptions.FolderAlreadyExistsException;
import com.danielsimonchin.exceptions.InvalidMailConfigBeanUsernameException;
import com.danielsimonchin.exceptions.InvalidRecipientImapURLException;
import com.danielsimonchin.exceptions.NotEnoughEmailRecipientsException;
import com.danielsimonchin.exceptions.RecipientEmailAddressNullException;
import com.danielsimonchin.exceptions.RecipientInvalidFormatException;
import com.danielsimonchin.exceptions.RecipientListNullException;
import com.danielsimonchin.fxbeans.FolderFXBean;
import com.danielsimonchin.properties.EmailBean;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.collections.ObservableList;

/**
 * Interface for CRUD Methods relating to an Email object and to the database.
 *
 * @author Daniel
 */
public interface EmailDAO {
    public int createEmailRecord(EmailBean emailBean) throws SQLException, IOException;

    public ObservableList<EmailBean> findAll() throws SQLException, FileNotFoundException,IOException;

    public EmailBean findID(int id) throws SQLException, FileNotFoundException,IOException;
    
    public ObservableList<EmailBean> findAllInFolder(String folderName) throws SQLException,FileNotFoundException,IOException;
    
    public List<EmailBean> findEmailsByRecipient(String recipientEmailAddress) throws SQLException, FileNotFoundException, IOException;
            
    public List<EmailBean> findEmailsBySubject(String subject) throws SQLException,FileNotFoundException,IOException;
    
    public int updateDraft(EmailBean emailBean) throws IOException,SQLException, NotEnoughEmailRecipientsException, InvalidMailConfigBeanUsernameException, RecipientListNullException, RecipientEmailAddressNullException, RecipientInvalidFormatException, InvalidRecipientImapURLException;
    
    public int updateFolder(EmailBean emailBean) throws SQLException,CannotMoveToDraftsException;
    
    public int updateFolderName(String currentName, String newName) throws SQLException, CannotRenameFolderException;

    public int deleteEmail(int emailId) throws SQLException;
    
    public int createFolder(String folderName) throws SQLException,FolderAlreadyExistsException;
    
    public int deleteFolder(String foldername)throws SQLException,CannotDeleteFolderException;
    
    public ObservableList<FolderFXBean> getAllFolders() throws SQLException;
    
    public String getFolderName(int id) throws SQLException;
    
    public int getFolderID(String folderName) throws SQLException;
    
    public void saveBlobToDisk(int emailID) throws SQLException;
}
