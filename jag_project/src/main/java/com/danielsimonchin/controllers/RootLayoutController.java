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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootLayoutController {
    private final static Logger LOG = LoggerFactory.getLogger(RootLayoutController.class);

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="rootAnchorPane"
    private AnchorPane rootAnchorPane; // Value injected by FXMLLoader

    @FXML // fx:id="upperLeftSplit"
    private AnchorPane upperLeftSplit; // Value injected by FXMLLoader

    @FXML // fx:id="upperRightSplit"
    private AnchorPane upperRightSplit; // Value injected by FXMLLoader

    @FXML // fx:id="lowerLeftSplit"
    private AnchorPane lowerLeftSplit; // Value injected by FXMLLoader

    @FXML // fx:id="lowerRightSplit"
    private AnchorPane lowerRightSplit; // Value injected by FXMLLoader
    
    private EmailDAO emailDAO;
    
    private FolderFXTreeLayoutController emailFXTreeController;
    private EmailFXTableLayoutController emailFXTableController;
    private EmailFXHTMLLayoutController emailFXHTMLController;
    //private EmailFXWebLayoutController emailFXWebController;


    
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() throws IOException {
        //retrieve the generated MailConfigBean from the MailConfigFXMLController class
        this.emailDAO = new EmailDAOImpl(MailConfigFXMLController.getMailConfigBean());

        //Setup all the sections of the application 
        initUpperLeftLayout();
        initUpperRightLayout();
        initLowerRightLayout();
        initLowerLeftLayout();
        
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
    private void initUpperLeftLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootLayoutController.class
                    .getResource("/fxml/FolderFXTreeLayout.fxml"));
            AnchorPane treeView = (AnchorPane) loader.load();

            // Give the controller the data object.
            emailFXTreeController = loader.getController();
            emailFXTreeController.setEmailDAO(emailDAO);

            upperLeftSplit.getChildren().add(treeView);
        } catch (IOException ex) {
            LOG.error("initUpperLeftLayout error", ex);
            errorAlert("initUpperLeftLayout()");
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
            AnchorPane htmlView = (AnchorPane) loader.load();

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
     * The WebView Layout
     */
    private void initLowerLeftLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootLayoutController.class
                    .getResource("/fxml/EmailFXWebLayout.fxml"));
            AnchorPane webView = (AnchorPane) loader.load();

            // Retrieve the controller if you must send it messages
            //fishFXWebViewController = loader.getController();
            lowerLeftSplit.getChildren().add(webView);
        } catch (IOException ex) {
            LOG.error("initLowerLeftLayout error", ex);
            errorAlert("initLowerLeftLayout()");
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

