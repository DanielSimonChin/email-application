/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller is for the About help page which gives an in depth guide on
 * how to use the application. It is initialized every time the user clicks on
 * the Menu Item for About.
 *
 * @author Daniel
 */
public class AboutWebViewController {

    private final static Logger LOG = LoggerFactory.getLogger(EmailFXHTMLLayoutController.class);

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private WebView aboutWebView;

    @FXML
    void initialize() {
        final String html = "AboutPage.html";
        final java.net.URI uri = java.nio.file.Paths.get(html).toAbsolutePath().toUri();
        LOG.info("uri= " + uri.toString());

        // create WebView with specified local content
        aboutWebView.getEngine().load(uri.toString());

    }
}
