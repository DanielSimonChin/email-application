/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.controllers;

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

public class EmailFXHTMLLayoutController {

    private final static Logger LOG = LoggerFactory.getLogger(EmailFXHTMLLayoutController.class);

    private EmailDAO emailDAO;
    @FXML
    private ResourceBundle resources;

    @FXML
    private BorderPane emailFXHTMLLayout;

    @FXML
    private URL location;

    //This will be a key=value pair in the email.properties
    @FXML
    private TextField toRecipientField;

    //This will be a key=value pair in the email.properties
    @FXML
    private TextField ccRecipientField;

    //This will be a key=value pair in the email.properties
    @FXML
    private TextField bccRecipientField;

    //This will be a key=value pair in the email.properties
    @FXML
    private TextField subjectField;

    //This will be a key=value pair in the email.properties
    @FXML
    private HTMLEditor emailFXHTMLEditor;

    @FXML
    void handleAbout(ActionEvent event) {
        LOG.info("Will implement the about button which will display The user's name, email and info relating to the app");
    }

    @FXML
    void handleClose(ActionEvent event) {
        LOG.info("Will implement the feature to close the application.");
    }

    @FXML
    void saveDraft(ActionEvent event) {
        LOG.info("Will implement the feature to save a draft email into the draft folder whenever the save button is clicked");
    }

    @FXML
    void initialize() {
        assert emailFXHTMLLayout != null : "fx:id=\"emailFXHTMLLayout\" was not injected: check your FXML file 'EmailFXHTMLLayout.fxml'.";
        assert emailFXHTMLEditor != null : "fx:id=\"emailFXHTMLEditor\" was not injected: check your FXML file 'EmailFXHTMLLayout.fxml'.";

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

    @FXML
    void sendEmail(ActionEvent event) {
        LOG.info("Implement sending the draft email");
    }

    @FXML
    void handleClear(ActionEvent event) {
        LOG.info("Implement clearing all the fields in the lowerRightSplit");
    }
}
