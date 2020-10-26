/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.controllers;

import com.danielsimonchin.persistence.EmailDAO;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailFXWebLayoutController {
    
    private final static Logger LOG = LoggerFactory.getLogger(EmailFXWebLayoutController.class);
    private EmailDAO emailDAO;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private WebView emailFXWebView;

    @FXML
    void initialize() {
        final String html = "example.html";
        final java.net.URI uri = java.nio.file.Paths.get(html).toAbsolutePath().toUri();
        LOG.info("uri= " + uri.toString());

        // create WebView with specified local content
        emailFXWebView.getEngine().load(uri.toString());

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

