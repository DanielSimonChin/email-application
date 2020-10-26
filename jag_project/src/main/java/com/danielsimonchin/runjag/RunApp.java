/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.runjag;

import com.danielsimonchin.controllers.MailConfigFXMLController;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.persistence.EmailDAOImpl;
import com.danielsimonchin.propertiesmanager.PropertiesManager;
import com.danielsimonchin.propertybean.PropertyBean;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel
 */
public class RunApp extends Application {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass().getName());
    private Stage primaryStage;
    private Parent rootPane;
    
    private MailConfigFXMLController formLayout;
    
    private PropertyBean propertyBean;
    private PropertiesManager propertiesManager;

    private Locale currentLocale;
    

    public RunApp() {
        super();
        currentLocale = new Locale("fr", "CA");
        LOG.debug("Locale = " + currentLocale);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString("title"));

        //retrieveMailConfig();
        initRootLayout();
        Scene scene = new Scene(rootPane);

        primaryStage.setScene(scene);
        primaryStage.show();
        LOG.info("Program started");
    }
//    private void retrieveMailConfig() throws IOException{
//        propertiesManager = new PropertiesManager();
//        propertyBean = new PropertyBean();
//        propertiesManager.loadTextProperties(propertyBean, "", "MailConfig");
//        LOG.debug(propertyBean.toString());
//    }

    @Override
    public void stop() {
        LOG.info("Stage is closing");
    }

    public void initRootLayout() {
        try {
            //Instantiate a FXMLLoader object
            FXMLLoader loader = new FXMLLoader();

            //Configure the FXMLLoader with the i18n locale resource bundles
            loader.setResources(ResourceBundle.getBundle("MessagesBundle", currentLocale));

            //Connect the FXMLLoader to the fxml file that is stored in the jar
            loader.setLocation(RunApp.class.getResource("/fxml/MailConfigFXML.fxml"));

            //The load command returns a reference to the root pane of the fxml file
            rootPane = (GridPane) loader.load();
            LOG.info("TEST 1");
            formLayout = loader.getController();
            LOG.info("TEST 2");
            formLayout.passStage(primaryStage);
            LOG.info("TEST 3");


        } catch (IOException ex) {
            LOG.error("Error displaying form", ex);
            errorAlert("initRootLayout()");
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
        dialog.setTitle(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString("sqlError"));
        dialog.setHeaderText(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString("sqlError"));
        dialog.setContentText(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString(msg));
        dialog.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
