package com.danielsimonchin.controllers;

import com.danielsimonchin.properties.MailConfigBean;
import com.danielsimonchin.propertiesmanager.PropertiesManager;
import com.danielsimonchin.fxbeans.PropertyBean;
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
import org.slf4j.LoggerFactory;

public class MailConfigFXMLController {

    private Stage primaryStage;
    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(MailConfigFXMLController.class);

    private PropertyBean propertyBean;
    private PropertiesManager propertiesManager;
    private static MailConfigBean mcBean;

    private RootLayoutController rootLayout;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="userNameField"
    private TextField userNameField; // Value injected by FXMLLoader

    @FXML // fx:id="emailAddressField"
    private TextField emailAddressField; // Value injected by FXMLLoader

    @FXML // fx:id="emailPasswordField"
    private TextField emailPasswordField; // Value injected by FXMLLoader

    @FXML // fx:id="imapURLField"
    private TextField imapURLField; // Value injected by FXMLLoader

    @FXML // fx:id="smtpURLField"
    private TextField smtpURLField; // Value injected by FXMLLoader

    @FXML // fx:id="imapPortField"
    private TextField imapPortField; // Value injected by FXMLLoader

    @FXML // fx:id="smtpPortField"
    private TextField smtpPortField; // Value injected by FXMLLoader

    @FXML // fx:id="mysqlURLField"
    private TextField mysqlURLField; // Value injected by FXMLLoader

    @FXML // fx:id="mysqlPortField"
    private TextField mysqlPortField; // Value injected by FXMLLoader

    @FXML // fx:id="mysqlDatabaseField"
    private TextField mysqlDatabaseField; // Value injected by FXMLLoader

    @FXML // fx:id="mysqlUsernameField"
    private TextField mysqlUsernameField; // Value injected by FXMLLoader

    @FXML // fx:id="mysqlPasswordField"
    private TextField mysqlPasswordField; // Value injected by FXMLLoader

    @FXML
    void cancelAction(ActionEvent event) {
        //TODO clear all the text fields when clicked
        LOG.info("Will implement the feature to clear the form whenever the cancel button is pressed");
    }

    /**
     * Write the form changes to the MailConfig.properties file and open the application, therefore changing the scene.
     * @param event 
     */
    @FXML
    void saveConfigAction(ActionEvent event) {
        try {
            propertiesManager.writeTextProperties("", "MailConfig", propertyBean);
        } catch (IOException ex) {
            //Add dialog
        }
        //when you save, opens up the email application
        setupRootLayout();
    }

    private void setupRootLayout() {
        try {
            this.mcBean = generateMailConfigBean();
            
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootLayoutController.class
                  .getResource("/fxml/RootLayout.fxml"));

            BorderPane rootPane = (BorderPane) loader.load();

            rootLayout = loader.getController();

            Scene scene = new Scene(rootPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle(resources.getString("appTitle"));
            primaryStage.show();

        } catch (IOException ex) {
            LOG.error("initUpperLeftLayout error", ex);
            Platform.exit();
        }
    }

    /**
     * Helper method that takes the propertyBean's contents and creates a
     * MailConfigBean when we want to access the email Application.
     *
     * @return A MailConfigBean
     */
    private MailConfigBean generateMailConfigBean() {
        return new MailConfigBean(propertyBean.getUserName(), propertyBean.getEmailAddress(), propertyBean.getEmailPassword(), propertyBean.getImapURL(), propertyBean.getSmtpURL(), propertyBean.getImapPort(), propertyBean.getSmtpPort(), propertyBean.getmysqlURL(), propertyBean.getmysqlDatabase(), propertyBean.getmysqlPort(), propertyBean.getmysqlUsername(), propertyBean.getmysqlPassword());
    }
    
    public static MailConfigBean getMailConfigBean(){
        return mcBean;
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert userNameField != null : "fx:id=\"userNameField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert emailAddressField != null : "fx:id=\"emailAddressField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert emailPasswordField != null : "fx:id=\"emailPasswordField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert imapURLField != null : "fx:id=\"imapURLField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert smtpURLField != null : "fx:id=\"smtpURLField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert imapPortField != null : "fx:id=\"imapPortField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert smtpPortField != null : "fx:id=\"smtpPortField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert mysqlURLField != null : "fx:id=\"mysqlURLField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert mysqlPortField != null : "fx:id=\"mysqlPortField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert mysqlDatabaseField != null : "fx:id=\"mysqlDatabaseField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert mysqlUsernameField != null : "fx:id=\"mysqlUsernameField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";
        assert mysqlPasswordField != null : "fx:id=\"mysqlPasswordField\" was not injected: check your FXML file 'MailConfigFXML.fxml'.";

        propertyBean = new PropertyBean();
        propertiesManager = new PropertiesManager();
        try {
            propertiesManager.loadTextProperties(propertyBean, "", "MailConfig");
        } catch (IOException ex) {
            //Show dialog about file failure
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
    
    public void passStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }
}
