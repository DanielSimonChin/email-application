/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.view;

import com.danielsimonchin.fxbeans.MailConfigFXBean;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.persistence.EmailDAOImpl;
import com.danielsimonchin.propertiesmanager.PropertiesManager;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The controller for the root layout containing three embedded layouts. Calls
 * helper methods that sets up 3 individual panes and places them in their
 * respective sections of the parent Border pane.
 *
 * @author Daniel Simon Chin
 * @version Oct 31, 2020
 */
public class RootLayoutController {

    private final static Logger LOG = LoggerFactory.getLogger(RootLayoutController.class);

    private Stage primaryStage;

    private FileChooser fileChooser;

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

    /**
     * This method is called by the FXMLLoader when initialization is complete.
     * Creates an EmailDAO object to be passed to other controllers sets the 3
     * children layouts into the root layout.
     *
     * @throws IOException
     */
    @FXML
    void initialize() throws IOException {
        fileChooser = new FileChooser();

        //Read from the properties file so we create our MailConfigBean to initialize a EmailDAO object.
        PropertiesManager propertiesManager = new PropertiesManager();
        MailConfigFXBean mcBean = new MailConfigFXBean();
        propertiesManager.loadTextProperties(mcBean, "", "MailConfig");

        this.emailDAO = new EmailDAOImpl(MailConfigFXMLController.generateMailConfigBean(mcBean));

        //Setup all the sections of the application 
        initLeftSplitLayout();
        initUpperRightLayout();
        initLowerRightLayout();

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
     * Send the reference to the emailFXTableController to the
     * emailFXTreeController
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

    /**
     * When user clicks on the About menu item, it will display a html page
     * using a webViewer with a guide on what each button does.
     *
     * @param event
     */
    @FXML
    void handleAbout(ActionEvent event) {
        LOG.info("TODO : Implementation for the About Menu Item");
    }

    /**
     * Allow the user to add an attachment to an email.
     *
     * @param event
     */
    @FXML
    void handleAddAttachment(ActionEvent event) {
        LOG.info("TODO : Implementation for the Add Attachment Menu Item");
    }

    /**
     * Allow user to save an attachment of a selected Email
     *
     * @param event
     */
    @FXML
    void handleSaveAttachment(ActionEvent event) {
        LOG.info("TODO : Implementation for the Save Attachment Menu Item");
    }

    /**
     * The application will close when this Menu Item is clicked
     *
     * @param event
     */
    @FXML
    void handleClose(ActionEvent event) {
        Platform.exit();
    }

    /**
     * An Email being written in the htxml editor will be saved to the database
     * in the DRAFT folder.
     *
     * @param event
     */
    @FXML
    void handleSaveDraft(ActionEvent event) {
        LOG.info("TODO : Implementation for the Save Draft Menu Item");
    }

    /**
     * An email that is in the html editor along with the form for recipients
     * and subject will be sent.
     *
     * @param event
     */
    @FXML
    void handleSendEmail(ActionEvent event) {
        LOG.info("TODO : Implementation for the Send Email Menu Item");
    }

    /**
     * The user is able to return to the MailConfig form when clicking the
     * MenuItem in settings. Loads the MailConfigFXML.fxml and passes the stage
     * through the passStage() method.
     *
     * @param event
     */
    @FXML
    void handleReturnToConfig(ActionEvent event) {
        try {
            //Instantiate a FXMLLoader object
            FXMLLoader loader = new FXMLLoader();

            //Configure the FXMLLoader with the i18n locale resource bundles
            loader.setResources(resources);

            //Connect the FXMLLoader to the fxml file that is stored in the jar
            loader.setLocation(MailConfigFXMLController.class.getResource("/fxml/MailConfigFXML.fxml"));

            //The load command returns a reference to the root pane of the fxml file
            GridPane rootPane = (GridPane) loader.load();
            MailConfigFXMLController formLayout = loader.getController();

            formLayout.passStage(primaryStage);

            Scene scene = new Scene(rootPane);

            primaryStage.setTitle(resources.getString("title"));
            primaryStage.setScene(scene);

            primaryStage.show();

            LOG.info("The scene has changed from the Email Application to the MailConfig Form Interface.");

        } catch (IOException ex) {
            LOG.error("Error displaying the form from the application", ex);
            errorAlert("handleReturnToConfig()");
            Platform.exit();
        }
    }

    /**
     * Pass the primary Stage to this controller
     *
     * @param primaryStage
     */
    public void passStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
