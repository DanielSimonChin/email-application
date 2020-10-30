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

/**
 * The controller for the Email table component of the UI. Will implement
 * handlers for dragging and dropping emails into folders. And will display the
 * list of emails according to the selected folder. If no folder is selected, it
 * defaults to displaying all the emails.
 *
 * @author Daniel Simon Chin
 * @version Oct 31, 2020
 */
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
        //TOOD: Connects the property in the EmailBean object to the column in the
        // table

        adjustColumnWidths();

        //TODO: Add listeners so when clicking on a cell, it will show the email contents in either the webViewer or the htmlEditor(if it is a draft).
        //TODO: Add listener for onDrag detected so we can drag and drop emails into sent/inbox folders
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
     * When program first starts, we want to display all emails since no folder
     * has been selected yet.
     *
     * @throws SQLException
     * @throws IOException
     */
    public void displayTable() throws SQLException, IOException {
        //TODO: Need to implement converting an EmailBean to a JAVAFX BEAN in phase 4
        emailDataTable.setItems(this.emailDAO.findAll());
    }

    /**
     * The FolderFXTreeLayoutController needs a reference to the this
     * controller. With that reference it can call this method to retrieve a
     * reference to the TableView and change its selection
     *
     * @return emailDataTable
     */
    public TableView<EmailBean> getEmailDataTable() {
        return emailDataTable;
    }

    private void showEmailDetails(EmailBean emailBean) {
        //TODO: display the contents of the email in the section of the app where an email must be shown.
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
