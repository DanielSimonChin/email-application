package com.danielsimonchin.controllers;

import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.properties.EmailBean;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailFXTableLayoutController {

    private final static Logger LOG = LoggerFactory.getLogger(EmailFXTableLayoutController.class);

    private EmailDAO emailDAO;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane emailFXTable;

    @FXML
    private TableView<EmailBean> emailDataTable;

    @FXML
    private TableColumn<EmailBean, String> fromColumn;

    @FXML
    private TableColumn<EmailBean, String> subjectColumn;

    @FXML
    private TableColumn<EmailBean, Timestamp> dateColumn;

    @FXML
    void initialize() {
        // Connects the property in the EmailBean object to the column in the
        // table
        //TODO
        
        adjustColumnWidths();
        
        //Add listeners so when clicking on a cell, it will show the email contents in either the webViewer or the htmlEditor(if it is a draft).
        
        //add listener for onDrag detected so we can drag and drop emails into sent/inbox folders
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

    
    public void displayTable() throws SQLException, IOException {
        //At first, we dispay all the emails in the db, since a folder has not been selected yet.
        emailDataTable.setItems(this.emailDAO.findAll());
    }
    
    /**
     * The FolderFXTreeLayoutController needs a reference to the this controller. With
     * that reference it can call this method to retrieve a reference to the
     * TableView and change its selection
     *
     * @return emailDataTable
     */
    public TableView<EmailBean> getEmailDataTable() {
        return emailDataTable;
    }
    
    private void showEmailDetails(EmailBean emailBean) {
        //TODO, display the contents of the email in the emailViewer section of the app.
        //PHASE 4 TODO
    }
    
    /**
     * Sets the width of the columns based on a percentage of the overall width
     *
     * This needs to enhanced so that it uses the width of the anchor pane it is
     * in and then changes the width as the table grows.
     */
    private void adjustColumnWidths() {
        // Get the current width of the table
        double width = emailFXTable.getPrefWidth();
        // Set width of each column
        fromColumn.setPrefWidth(width * .25);
        subjectColumn.setPrefWidth(width * .25);
        dateColumn.setPrefWidth(width * .25);
    
    }
    
   
}
