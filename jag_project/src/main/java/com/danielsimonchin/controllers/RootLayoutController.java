/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.controllers;

import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.persistence.EmailDAOImpl;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootLayoutController {
    private final static Logger LOG = LoggerFactory.getLogger(RootLayoutController.class);

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private BorderPane leftSplit;

    @FXML
    private BorderPane upperRightSplit;

    @FXML
    private BorderPane lowerRightSplit;
    
    private EmailDAO emailDAO;
    
    private FolderFXTreeLayoutController emailFXTreeController;
    private EmailFXTableLayoutController emailFXTableController;
    private EmailFXHTMLLayoutController emailFXHTMLController;


    
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() throws IOException {
        //retrieve the generated MailConfigBean from the MailConfigFXMLController class
        this.emailDAO = new EmailDAOImpl(MailConfigFXMLController.getMailConfigBean());

        //Setup all the sections of the application 
        initLeftSplitLayout();
        initUpperRightLayout();
        initLowerRightLayout();

        
        // Tell the tree about the table
        setTableControllerToTree();
        try {
            emailFXTreeController.displayTree();
            emailFXTableController.displayTable();
        } catch (SQLException ex) {
            LOG.error("initialize error", ex);
            errorAlert("initialize()");
            Platform.exit();
        }

    }
    
    /**
     * Send the reference to the FishFXTableController to the
     * FishFXTreeController
     */
    private void setTableControllerToTree() {
        emailFXTreeController.setTableController(emailFXTableController);
    }
    
        /**
     * The TreeView Layout
     */
    private void initLeftSplitLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootLayoutController.class
                    .getResource("/fxml/FolderFXTreeLayout.fxml"));
            AnchorPane treeView = (AnchorPane) loader.load();

            // Give the controller the data object.
            emailFXTreeController = loader.getController();
            emailFXTreeController.setEmailDAO(emailDAO);

            leftSplit.getChildren().add(treeView);
        } catch (IOException ex) {
            LOG.error("initLeftSplitLayout error", ex);
            errorAlert("initLeftSplitLayout()");
            Platform.exit();
        }
    }
    
    /**
     * The TableView Layout
     */
    private void initUpperRightLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootLayoutController.class
                    .getResource("/fxml/EmailFXTableLayout.fxml"));
            AnchorPane tableView = (AnchorPane) loader.load();

            // Give the controller the data object.
            emailFXTableController = loader.getController();
            emailFXTableController.setEmailDAO(emailDAO);

            upperRightSplit.getChildren().add(tableView);
        } catch (IOException ex) {
            LOG.error("initUpperRightLayout error", ex);
            errorAlert("initUpperRightLayout()");
            Platform.exit();
        }
    }
    
    /**
     * The HTMLEditor Layout
     */
    private void initLowerRightLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootLayoutController.class
                    .getResource("/fxml/EmailFXHTMLLayout.fxml"));
            BorderPane htmlView = (BorderPane) loader.load();

            // Give the controller the data object.
            emailFXHTMLController = loader.getController();
            emailFXHTMLController.setEmailDAO(emailDAO);

            lowerRightSplit.getChildren().add(htmlView);
        } catch (IOException ex) {
            LOG.error("initLowerRightLayout error", ex);
            errorAlert("initLowerRightLayout()");
            Platform.exit();
        }
    }
    
    
    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String msg) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle(resources.getString("sqlError"));
        dialog.setHeaderText(resources.getString("sqlError"));
        dialog.setContentText(resources.getString(msg));
        dialog.show();
    }
}

