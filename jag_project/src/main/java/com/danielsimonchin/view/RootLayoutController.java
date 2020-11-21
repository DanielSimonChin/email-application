package com.danielsimonchin.view;

import com.danielsimonchin.fxbeans.MailConfigFXBean;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.persistence.EmailDAOImpl;
import com.danielsimonchin.properties.EmailBean;
import com.danielsimonchin.properties.MailConfigBean;
import com.danielsimonchin.propertiesmanager.PropertiesManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
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
import javafx.stage.Modality;
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

        MailConfigBean mailConfigBean = generateMailConfigBean(mcBean);
        this.emailDAO = new EmailDAOImpl(mailConfigBean);

        //Setup all the sections of the application 
        initLeftSplitLayout();
        initUpperRightLayout();
        initLowerRightLayout(mailConfigBean);

        //The tree controller needs a reference to the table controller.
        emailFXTreeController.setTableController(emailFXTableController);
        //The table controller needs a reference to the html controller.
        emailFXTableController.setHtmlController(emailFXHTMLController);
        //The html controller needs a reference to the table controller whenever an email is sent, updated or deleted.
        emailFXHTMLController.setTableController(emailFXTableController);
        //The tree controller needs the reference to the html controller
        emailFXTreeController.setHTMLController(emailFXHTMLController);
        //Pass the mailConfigBean to the TreeController
        emailFXTreeController.setMailConfigBean(mailConfigBean);

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
     * Helper method that takes the propertyBean's contents and creates a
     * MailConfigBean when we want to access the email Application.
     *
     * @param propertyBean
     * @return A MailConfigBean
     */
    private MailConfigBean generateMailConfigBean(MailConfigFXBean propertyBean) {
        //use the user's inputs for the mySqlDatabaseURL, the mySQL port and the database name to construct "jdbc:mysql://localhost:3306/EMAILCLIENT"
        String constructedMySqlURL = "jdbc:mysql://" + propertyBean.getmysqlURL() + ":" + propertyBean.getmysqlPort() + "/" + propertyBean.getmysqlDatabase();
        return new MailConfigBean(propertyBean.getUserName(), propertyBean.getEmailAddress(), propertyBean.getEmailPassword(), propertyBean.getImapURL(), propertyBean.getSmtpURL(), propertyBean.getImapPort(), propertyBean.getSmtpPort(), constructedMySqlURL, propertyBean.getmysqlDatabase(), propertyBean.getmysqlPort(), propertyBean.getmysqlUsername(), propertyBean.getmysqlPassword());
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
    private void initLowerRightLayout(MailConfigBean mailConfigBean) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootLayoutController.class
                    .getResource("/fxml/EmailFXHTMLLayout.fxml"));
            BorderPane htmlView = (BorderPane) loader.load();

            // Give the controller the data object.
            emailFXHTMLController = loader.getController();
            emailFXHTMLController.setEmailDAO(emailDAO);
            emailFXHTMLController.setMailConfigBean(mailConfigBean);

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
        try {
            Stage newStage = new Stage();

            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(this.getClass().getResource("/fxml/AboutWebView.fxml"));

            AnchorPane webView = (AnchorPane) loader.load();

            newStage.setScene(new Scene(webView));

            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

        } catch (IOException ex) {
            LOG.error("Error loading the WebView About Page", ex);
            Platform.exit();
        }
    }

    /**
     * Allow the user to add an attachment to an email. The selected file gets
     * added to the FXBean property attachments
     *
     * @param event
     */
    @FXML
    void handleAddAttachment(ActionEvent event) {
        LOG.info("TODO : Implementation for the Add Attachment Menu Item");

        Stage stage = new Stage();

        stage.initModality(Modality.APPLICATION_MODAL);

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            //add this file to the list of files to be sent or saved
            emailFXHTMLController.getFormFXBean().getAttachments().add(selectedFile);

            //display the selected file in the html editor
            emailFXHTMLController.displayImagesInHtml(selectedFile);

            LOG.info("THE FXBEANS ATTACHMENTS:");
            for (File file : emailFXHTMLController.getFormFXBean().getAttachments()) {
                LOG.info("FILE NAME : " + file.getName());
            }
        }
    }

    /**
     * Allow user to save an attachment of a selected Email
     *
     * @param event
     */
    @FXML
    void handleSaveAttachment(ActionEvent event) {
        LOG.info("TODO : Implementation for the Save Attachment Menu Item");

        try {
            this.emailDAO.saveBlobToDisk(emailFXTableController.getEmailDataTable().getSelectionModel().selectedItemProperty().getValue().getEmailId());
        } catch (SQLException ex) {
            LOG.info("Caught an SQLException when saving the files");
        }
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
