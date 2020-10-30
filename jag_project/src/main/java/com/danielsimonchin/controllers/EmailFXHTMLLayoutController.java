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

    @FXML
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
}
