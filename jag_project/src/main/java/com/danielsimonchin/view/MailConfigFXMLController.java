package com.danielsimonchin.view;

import com.danielsimonchin.properties.MailConfigBean;
import com.danielsimonchin.propertiesmanager.PropertiesManager;
import com.danielsimonchin.fxbeans.MailConfigFXBean;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The controller for the MailConfig form at the start of the program. Binds the
 * form fields to the JavaFX Bean PropertyBean. Allows for user to clear all
 * fields or to save the field contents and proceed to the app.
 *
 * @author Daniel Simon Chin
 * @version Oct 31, 2020
 */
public class MailConfigFXMLController {

    private Stage primaryStage;
    private final static Logger LOG = LoggerFactory.getLogger(MailConfigFXMLController.class);

    private MailConfigFXBean propertyBean;
    private PropertiesManager propertiesManager;

    private RootLayoutController rootLayout;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private ResourceBundle resources;

    @FXML
    private TextField userNameField;

    @FXML
    private TextField emailAddressField;

    @FXML
    private TextField emailPasswordField;

    @FXML
    private TextField imapURLField;

    @FXML
    private TextField smtpURLField;

    @FXML
    private TextField imapPortField;

    @FXML
    private TextField smtpPortField;

    @FXML
    private TextField mysqlURLField;

    @FXML
    private TextField mysqlPortField;

    @FXML
    private TextField mysqlDatabaseField;

    @FXML
    private TextField mysqlUsernameField;

    @FXML
    private TextField mysqlPasswordField;

    /**
     * This event handler will clear all the text fields of the form
     *
     * @param event
     */
    @FXML
    void clearAction(ActionEvent event) {
        propertyBean.setUserName("");
        propertyBean.setEmailAddress("");
        propertyBean.setEmailPassword("");
        propertyBean.setImapURL("");
        propertyBean.setSmtpURL("");
        propertyBean.setImapPort("");
        propertyBean.setSmtpPort("");
        propertyBean.setmysqlURL("");
        propertyBean.setmysqlPort("");
        propertyBean.setmysqlDatabase("");
        propertyBean.setmysqlUsername("");
        propertyBean.setmysqlPassword("");
    }

    /**
     * Write the form changes to the MailConfig.properties file and open the
     * application, therefore changing the scene.
     *
     * @param event
     */
    @FXML
    void saveConfigAction(ActionEvent event) {
        //if all the fields are filled, then proceed to the application.
        if (checkFormFilled()) {
            try {
                propertiesManager.writeTextProperties("", "MailConfig", propertyBean);
            } catch (IOException ex) {
                LOG.info("Error writing to the properties file");
            }
            //when you save, opens up the email application.
            setupRootLayout();
        } else {
            //TODO implement at pop up window saying to fill up the form first
            LOG.info("Will implement a pop up window asking to fill all fields.");
        }

    }

    /**
     * Checks if the form is completely filled up.
     *
     * @return true if filled, false otherwise.
     */
    private boolean checkFormFilled() {
        return (!userNameField.getText().isEmpty() && !emailAddressField.getText().isEmpty() && !emailPasswordField.getText().isEmpty() && !imapURLField.getText().isEmpty() && !smtpURLField.getText().isEmpty() && !imapPortField.getText().isEmpty() && !smtpPortField.getText().isEmpty() && !mysqlURLField.getText().isEmpty() && !mysqlPortField.getText().isEmpty() && !mysqlDatabaseField.getText().isEmpty() && !mysqlUsernameField.getText().isEmpty() && !mysqlPasswordField.getText().isEmpty());
    }

    /**
     * Sets up the Root border pane that will contain 3 other embedded
     * containers representing the FolderTree, Email table and html editor.
     * Changes the scene so that the application is presented
     */
    private void setupRootLayout() {
        try {
            //mcBean = generateMailConfigBean(propertyBean);

            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootLayoutController.class
                    .getResource("/fxml/RootLayout.fxml"));

            BorderPane rootPane = (BorderPane) loader.load();

            rootLayout = loader.getController();
            rootLayout.passStage(primaryStage);

            Scene scene = new Scene(rootPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle(resources.getString("appTitle"));
            primaryStage.show();

        } catch (IOException ex) {
            LOG.error("RootLayout error", ex);
            Platform.exit();
        }
    }

    /**
     * This method is called by the FXMLLoader when initialization is complete.
     * Will bind the form fields to the JAVAFX Bean. So we are able to read and
     * write to the properties file MailConfig.properties
     */
    @FXML
    void initialize() {
        propertyBean = new MailConfigFXBean();
        propertiesManager = new PropertiesManager();
        try {
            propertiesManager.loadTextProperties(propertyBean, "", "MailConfig");
        } catch (IOException ex) {
            //TODO : Show dialog about file failure
        }

        Bindings.bindBidirectional(userNameField.textProperty(), propertyBean.getUserNameProperty());
        Bindings.bindBidirectional(emailAddressField.textProperty(), propertyBean.getEmailAddressProperty());
        Bindings.bindBidirectional(emailPasswordField.textProperty(), propertyBean.getEmailPasswordProperty());
        Bindings.bindBidirectional(imapURLField.textProperty(), propertyBean.getImapURLProperty());
        Bindings.bindBidirectional(smtpURLField.textProperty(), propertyBean.getSmtpURLProperty());
        Bindings.bindBidirectional(imapPortField.textProperty(), propertyBean.getImapPortProperty());
        Bindings.bindBidirectional(smtpPortField.textProperty(), propertyBean.getSmtpPortProperty());
        Bindings.bindBidirectional(mysqlURLField.textProperty(), propertyBean.getmysqlURLProperty());
        Bindings.bindBidirectional(mysqlPortField.textProperty(), propertyBean.getmysqlPortProperty());
        Bindings.bindBidirectional(mysqlDatabaseField.textProperty(), propertyBean.getmysqlDatabaseProperty());
        Bindings.bindBidirectional(mysqlUsernameField.textProperty(), propertyBean.getmysqlUsernameProperty());
        Bindings.bindBidirectional(mysqlPasswordField.textProperty(), propertyBean.getmysqlPasswordProperty());
    }

    /**
     * The MainApp will pass its stage so we can utilize it in this controller.
     *
     * @param primaryStage
     */
    public void passStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
