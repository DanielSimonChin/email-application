package com.danielsimonchin.view;

import com.danielsimonchin.fxbeans.FolderFXBean;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.persistence.FakeEmailDAOPersistence;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
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

    private EmailDAO emailDAO;

    @FXML
    private ResourceBundle resources;
    
    @FXML // URL location of the FXML file that was given to the FXMLLoader
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

        folderFXTreeView.setRoot(new TreeItem<FolderFXBean>(rootFolder));
        // This cell factory is used to choose which field in the FihDta object
        // is used for the node name
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
        ObservableList<FolderFXBean> folders = FakeEmailDAOPersistence.findAllFolders();

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

        //TODO: Add listeners to these tree items so they can be clicked to view the folder's contents
        // Listen for selection changes and show the fishData details when changed.
        folderFXTreeView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) -> showFolderContents(newValue));
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
    void handleDragDropped(DragEvent event) {
        //TODO : implement the action for dropping an email into a folder.
        
        LOG.debug("onDragDropped");
        Dragboard db = event.getDragboard();
        boolean success = false;

        //let the source know whether the string was successfully transferred
        // and used
        event.setDropCompleted(success);

        event.consume();
    }

    /**
     * To be able to test the selection handler for the tree, this method
     * displays the emails in a selected folder.
     *
     * @param folderData
     */
    private void showFolderContents(TreeItem<FolderFXBean> folderData) {
        //TODO : in phase 4, We send the EmailDAOImpl the folder name and find all the emails to be displayed from that folder.
        LOG.info("SHOW A FOLDER'S EMAILS IN THE TABLE");
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
     * The handler for adding a folder to the list of folder and display the
     * updated folders in the TreeView.
     *
     * @param event
     */
    @FXML
    void handleAddFolder(ActionEvent event) {
        LOG.info("Implement adding the folder event");
        //TODO: after adding folder, need to display table once again with updated folders.
    }
}
