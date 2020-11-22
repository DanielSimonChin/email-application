package com.danielsimonchin.view;

import com.danielsimonchin.business.SendAndReceive;
import com.danielsimonchin.exceptions.CannotDeleteFolderException;
import com.danielsimonchin.exceptions.CannotMoveToDraftsException;
import com.danielsimonchin.exceptions.FolderAlreadyExistsException;
import com.danielsimonchin.exceptions.InvalidRecipientImapURLException;
import com.danielsimonchin.exceptions.RecipientInvalidFormatException;
import com.danielsimonchin.fxbeans.FolderFXBean;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.properties.EmailBean;
import com.danielsimonchin.properties.MailConfigBean;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import jodd.mail.ReceivedEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller is for the tree layout of the folders in the JAG project. The
 * container for the tree layout will be a child of the root layout. This
 * controller will implement handlers for accepting drag and drops to place
 * emails into a folder. It will allow users to select a folder and display the
 * contents in the TableView.
 *
 * @author Daniel Simon Chin
 * @version Oct 31, 2020
 */
public class FolderFXTreeLayoutController {

    private final static Logger LOG = LoggerFactory.getLogger(FolderFXTreeLayoutController.class);

    private EmailFXTableLayoutController emailFXTableController;

    private EmailFXHTMLLayoutController htmlController;

    private MailConfigBean mailConfigBean;

    private EmailDAO emailDAO;

    @FXML
    private ResourceBundle resources;

    @FXML
    private TextField newFolderInput;

    @FXML
    private URL location;

    @FXML
    private AnchorPane folderFXTreeLayout;

    @FXML
    private TreeView<FolderFXBean> folderFXTreeView;

    /**
     * This method is called by the FXMLLoader when initialization is complete.
     * It sets up the TreeView of the folders by calling a helper method to
     * retrieve all the folder names
     */
    @FXML
    void initialize() {
        // We need a root node for the tree and it must be the same type as all
        // nodes
        FolderFXBean rootFolder = new FolderFXBean();

        folderFXTreeView.setRoot(new TreeItem<>(rootFolder));

        folderFXTreeView.setCellFactory((e) -> new TreeCell<FolderFXBean>() {
            @Override
            protected void updateItem(FolderFXBean item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getFolderName());
                    setGraphic(getTreeItem().getGraphic());
                } else {
                    setText("");
                    setGraphic(null);
                }
            }
        });
    }

    /**
     * The RootLayoutController calls this method to provide a reference to the
     * EmailDAO object.
     *
     * @param emailDAO
     */
    public void setEmailDAO(EmailDAO emailDAO) {
        this.emailDAO = emailDAO;
    }

    /**
     * Get all the folder names of the database and add them to the TreeView.
     *
     * @throws SQLException
     */
    public void displayTree() throws SQLException {
        //clear all the children since we are re-displaying it
        folderFXTreeView.getRoot().getChildren().clear();

        ObservableList<FolderFXBean> folders = this.emailDAO.getAllFolders();

        // Build an item for each email and add it to the root
        if (folders != null) {
            for (FolderFXBean folder : folders) {
                TreeItem<FolderFXBean> item = new TreeItem<>(folder);
                item.setGraphic(new ImageView(getClass().getResource("/images/foldericon.png").toExternalForm()));
                folderFXTreeView.getRoot().getChildren().add(item);
            }
        }

        // Open the tree
        folderFXTreeView.getRoot().setExpanded(true);

        folderFXTreeView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            try {
                                showFolderContents(newValue);
                            } catch (SQLException ex) {
                                LOG.error("Could not display the folder contents ");
                            } catch (IOException ex) {
                                LOG.error("Got an IOExceptio when trying to display the folder contents");
                            }
                        });
    }

    /**
     * This method prevents dropping the value on anything buts the TreeView.
     *
     * @param event
     */
    @FXML
    void handleDragOver(DragEvent event) {
        /* data is dragged over the target */
        LOG.debug("onDragOver");

        // Accept it only if it is not dragged from the same control and if it
        // has a string data
        if (event.getGestureSource() != folderFXTreeView && event.getDragboard().hasString()) {
            // allow for both copying and moving, whatever user chooses
            event.acceptTransferModes(TransferMode.ANY);
        }
        event.consume();
    }

    /**
     * When the email is dropped into a folder, take that email out of the table
     * and place it into the new folder.
     *
     * @param event
     */
    @FXML
    void handleDragDropped(DragEvent event) throws SQLException, IOException {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (event.getDragboard().hasString()) {
            //The ID of the email which we drag and dropped
            String tableID = db.getString();
            LOG.debug("Table ID: " + tableID);

            //Get the emailBean of the email we wish to move
            EmailBean emailToMove = this.emailDAO.findID(Integer.parseInt(tableID));

            //Emails cannot be moved out of the DRAFTS folder
            if (emailToMove.getFolderKey() == 3) {
                errorAlert("folderChangeTitle", "folderChangeHeader", "folderChangeMessage");
                return;
            }

            String folderType = event.getTarget().toString();
            //retrieve the folder name of the TreeItem that was dropped upon
            String folderName = folderType.split("\"")[1];
            LOG.debug("FOLDER DROPPED ON :" + folderName);

            //get the id of the new folder to be updated
            emailToMove.setFolderKey(this.emailDAO.getFolderID(folderName));

            try {
                //the email we wish to move is updated to its new folder
                this.emailDAO.updateFolder(emailToMove);
                //Emails cannot be moved into the DRAFTS folder
            } catch (CannotMoveToDraftsException ex) {
                errorAlert("folderChangeTitle", "folderChangeHeader", "folderChangeMessage");
                return;
            }

            emailFXTableController.displaySelectedFolder(folderName);

            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * To be able to test the selection handler for the tree, this method
     * displays the emails in a selected folder.
     *
     * @param folderData
     */
    private void showFolderContents(TreeItem<FolderFXBean> folderBean) throws SQLException, IOException {
        if (folderBean.getValue().getFolderName().equals("INBOX")) {
            refreshInboxForReceivedEmails();
        }

        //the user can only create emails or edit drafts when they select the draft folder
        if (folderBean.getValue().getFolderName().equals("DRAFT")) {
            htmlController.enableFormAndHTML();
        } else {
            htmlController.disableFormAndHTML();
        }

        emailFXTableController.getEmailDataTable().getSelectionModel().clearSelection();
        htmlController.resetSelectedEmailBean();
        htmlController.clearFormAndHtmlEditor();

        emailFXTableController.displaySelectedFolder(folderBean.getValue().getFolderName());
    }

    /**
     * Whenever the folder INBOX is clicked, we receive emails that were sent to
     * the current account.
     *
     * @throws SQLException
     * @throws IOException
     */
    private void refreshInboxForReceivedEmails() throws SQLException, IOException {
        try {
            SendAndReceive runMail = new SendAndReceive(mailConfigBean);

            //retreive all the emails sent to the current user
            ReceivedEmail[] receivedEmails = runMail.receiveEmail(mailConfigBean);

            for (ReceivedEmail email : receivedEmails) {
                //The email id will be set in the method, the key for inbox is 1.
                emailDAO.createEmailRecord(new EmailBean(-1, 1, email));
            }
        } catch (InvalidRecipientImapURLException ex) {
            LOG.info("The recipient IMAP URL is invalid");
        } catch (RecipientInvalidFormatException ex) {
            LOG.info("The user's mailConfigBean username is invalid");
        }
    }

    /**
     * Pass the reference of the MailConfigBean to this controller so we can
     * call the receiveEmails method in refreshInboxForReceivedEmails()
     *
     * @param mcBean
     */
    public void setMailConfigBean(MailConfigBean mcBean) {
        this.mailConfigBean = mcBean;
    }

    /**
     * The RootLayoutController calls this method to provide a reference to the
     * FishFXTableController from which it can request a reference to the
     * TreeView.With theTreeView reference it can change the selection in the
     * TableView.
     *
     * @param emailFXTableController
     */
    public void setTableController(EmailFXTableLayoutController emailFXTableController) {
        this.emailFXTableController = emailFXTableController;
    }

    /**
     * Pass the html controller reference to this controller
     *
     * @param htmlController
     */
    public void setHTMLController(EmailFXHTMLLayoutController htmlController) {
        this.htmlController = htmlController;
    }

    /**
     * The handler for adding a folder to the list of folder and display the
     * updated folders in the TreeView. The user cannot add a folder that
     * already exists
     *
     * @param event
     */
    @FXML
    void handleAddFolder(ActionEvent event) throws SQLException, FolderAlreadyExistsException {
        if (!newFolderInput.getText().isEmpty()) {
            try {
                //create the new folder
                int foldersCreated = this.emailDAO.createFolder(newFolderInput.getText());

                newFolderInput.clear();

                //reload the folder tree with the updated folder added.
                displayTree();
            } catch (FolderAlreadyExistsException ex) {
                errorAlert("addFolderTitle", "addFolderAlreadyExistsHeader", "addFolderAlreadyExists");
            }
        }
    }

    /**
     * The user must click on a folder and then click on the delete folder
     * button. The INBOX, SENT AND DRAFT folders cannot be deleted.
     *
     * @param event
     */
    @FXML
    void handleDeleteFolder(ActionEvent event) {
        //if no folder was selected yet.
        if (folderFXTreeView.getSelectionModel().selectedItemProperty().getValue() == null) {
            errorAlert("deleteFolderTitle", "deleteFolderNotSelected", "selectFolderBeforeDeleting");
            return;
        }

        try {
            //remove the folder and redisplay the TreeView
            this.emailDAO.deleteFolder(folderFXTreeView.getSelectionModel().selectedItemProperty().getValue().getValue().getFolderName());
            displayTree();
        } catch (SQLException ex) {
            LOG.info("Caught an SQLException when trying to delete a folder");
        } catch (CannotDeleteFolderException ex) {
            errorAlert("deleteFolderTitle", "folderCannotBeDeletedHeader", "folderCannotBeDeleted");
        }
    }

    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String title, String header, String message) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle(resources.getString(title));
        dialog.setHeaderText(resources.getString(header));
        dialog.setContentText(resources.getString(message));
        dialog.show();
    }
}
