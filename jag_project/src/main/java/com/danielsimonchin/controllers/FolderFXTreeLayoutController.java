/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.controllers;

import com.danielsimonchin.persistence.EmailDAO;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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

    @FXML // fx:id="folderFXTreeLayout"
    private AnchorPane folderFXTreeLayout; // Value injected by FXMLLoader

    @FXML // fx:id="folderFXTreeView"
    private TreeView<String> folderFXTreeView; // Value injected by FXMLLoader

    /**
     * This method is called by the FXMLLoader when initialization is complete.
     * It sets up the TreeView of the folders by calling a helper method to
     * retrieve all the folder names
     */
    @FXML
    void initialize() {
        // We need a root node for the tree and it must be the same type as all
        // nodes
        String rootString = new String();

        folderFXTreeView.setRoot(new TreeItem<>(rootString));
        // This cell factory is used to choose which field in the FihDta object
        // is used for the node name
        folderFXTreeView.setCellFactory((e) -> new TreeCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item);
                } else {
                    setText("");
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
     * @throws SQLException 
     */
    public void displayTree() throws SQLException {
        ObservableList<String> folderNames = emailDAO.getAllFolderNames();

        // Build an item for each fish and add it to the root
        if (folderNames != null) {
            folderNames.stream().map((folder) -> new TreeItem<>(folder)).map((item) -> {
                return item;
            }).forEachOrdered((item) -> {
                folderFXTreeView.getRoot().getChildren().add(item);
            });
        }

        // Open the tree
        folderFXTreeView.getRoot().setExpanded(true);

        //TODO: Add listeners to these tree items so they can be clicked to view the folder's contents
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
