package com.danielsimonchin.runjag;

import com.danielsimonchin.view.MailConfigFXMLController;
import com.danielsimonchin.propertiesmanager.PropertiesManager;
import com.danielsimonchin.fxbeans.MailConfigFXBean;
import com.danielsimonchin.view.RootLayoutController;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main application in which is initialized the MailConfigFXML Controller to
 * have a user log into their email.
 *
 * @author Daniel Simon Chin
 * @version Oct 31, 2020
 */
public class RunApp extends Application {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass().getName());
    private Stage primaryStage;
    private Parent rootPane;

    private MailConfigFXMLController formLayout;

    private MailConfigFXBean propertyBean;
    private PropertiesManager propertiesManager;

    private Locale currentLocale;

    /**
     * Set the locale to a specific language. The program utilizes
     * internationalization.
     */
    public RunApp() {
        super();
        //The program will use the computer's default language to determine which message bundle to use.
        currentLocale = new Locale("en", "CA");
        //currentLocale = Locale.getDefault();
        LOG.debug("Locale = " + currentLocale);
    }

    /**
     * Set the title of the stage and call a helper method to load the
     * MailConfigFXMLController.
     *
     * @param primaryStage
     * @throws IOException
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        

        boolean isRead = retrieveMailConfig();
        //the file was read and the properties were set.
        if (isRead) {
            //if all properties are empty, then open the mail config form.
            if (checkPropertyBeanEmpty(propertyBean)) {
                this.primaryStage.setTitle(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString("title"));
                initRootLayout();
            } //if they are not empty, then open the app right away.
            else {
                this.primaryStage.setTitle(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString("appTitle"));
                loadApplicationWithoutForm();
            }
        } else {
            //Since the file does not exist, we create it using the writeTextProperties method which will be called in the form's save button.
            initRootLayout();
        }

        Scene scene = new Scene(rootPane);

        primaryStage.setScene(scene);
        primaryStage.show();
        LOG.info("Program started");
    }

    /**
     * Calls the loadTextProperties to check if properties file exists and loads
     * from it.
     *
     * @return true if it successfully loaded, false otherwise
     * @throws IOException
     */
    private boolean retrieveMailConfig() throws IOException {
        propertiesManager = new PropertiesManager();
        propertyBean = new MailConfigFXBean();
        boolean propertiesRead = propertiesManager.loadTextProperties(propertyBean, "", "MailConfig");

        LOG.debug(propertyBean.toString());
        return propertiesRead;
    }

    /**
     * Checks if any of the properties are blank
     *
     * @param mcBean
     * @return true if it is all the properties are empty, false otherwise.
     */
    private boolean checkPropertyBeanEmpty(MailConfigFXBean mcBean) {
        //if all the properties are empty strings, then we return true
        return (mcBean.getUserName().isEmpty() && mcBean.getEmailAddress().isEmpty() && mcBean.getEmailPassword().isEmpty() && mcBean.getImapURL().isEmpty() && mcBean.getSmtpURL().isEmpty() && mcBean.getImapPort().isEmpty() && mcBean.getSmtpPort().isEmpty() && mcBean.getmysqlURL().isEmpty() && mcBean.getmysqlDatabase().isEmpty() && mcBean.getmysqlPort().isEmpty() && mcBean.getmysqlUsername().isEmpty() && mcBean.getmysqlPassword().isEmpty());
    }

    /**
     * This is only called if the properties file is already created and filled
     * with values. We load the main application instead of a form.
     */
    private void loadApplicationWithoutForm() {
        try {
            //Instantiate a FXMLLoader object
            FXMLLoader loader = new FXMLLoader();

            //Configure the FXMLLoader with the i18n locale resource bundles
            loader.setResources(ResourceBundle.getBundle("MessagesBundle", currentLocale));

            //Connect the FXMLLoader to the fxml file that is stored in the jar
            loader.setLocation(RunApp.class.getResource("/fxml/RootLayout.fxml"));

            //The load command returns a reference to the root pane of the fxml file
            rootPane = (BorderPane) loader.load();
            RootLayoutController rootLayoutController = loader.getController();
            rootLayoutController.passStage(primaryStage);

        } catch (IOException ex) {
            LOG.error("Error displaying form", ex);
            errorAlert("initRootLayout()");
            Platform.exit();
        }
    }

    /**
     * When the platform is exited, the last thing that happens is logging that
     * the stage is closing
     */
    @Override
    public void stop() {
        LOG.info("Stage is closing");
    }

    /**
     * Load the MailConfigFXMLController and pass it the primary stage. This is
     * called when the properties form either doesn't exist or is missing
     * properties.
     */
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
            formLayout = loader.getController();
            formLayout.passStage(primaryStage);

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
