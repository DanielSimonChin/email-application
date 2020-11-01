package com.danielsimonchin.view;

import com.danielsimonchin.persistence.EmailDAO;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The controller class for the Border Pane representing the html editor and the
 * form for an email's recipient and subject.
 *
 * @author Daniel Simon Chin
 * @version Oct 31, 2020
 */
public class EmailFXHTMLLayoutController {

    private final static Logger LOG = LoggerFactory.getLogger(EmailFXHTMLLayoutController.class);

    private EmailDAO emailDAO;
    @FXML
    private ResourceBundle resources;

    @FXML
    private BorderPane emailFXHTMLLayout;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private TextField toRecipientField;

    @FXML
    private TextField ccRecipientField;

    @FXML
    private TextField bccRecipientField;

    @FXML
    private TextField subjectField;

    @FXML
    private HTMLEditor emailFXHTMLEditor;

    @FXML
    void initialize() {
        //Todo, implement binding the gui components to its corresponding  FormFXBean

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
     * Event handler for deleting the currently displayed draft in the html
     * editor. Removing it from the database.
     *
     * @param event
     */
    @FXML
    void onDeleteDraft(ActionEvent event) {
        //TODO: delete the displayed draft in the htmleditor
    }

    /**
     * Event handler for saving the currently displayed draft constructed in the
     * html editor
     *
     * @param event
     */
    @FXML
    void onSaveDraft(ActionEvent event) {
        //TODO: save the draft presented in the htmleditor
    }

    /**
     * Event handler to send the email constructed in the html editor given that
     * the email has a the minimum a TO recipient.
     *
     * @param event
     */
    @FXML
    void onSendEmail(ActionEvent event) {
        //TODO: send the email created in the htmleditor
    }
}
