package com.danielsimonchin.view;

import com.danielsimonchin.fxbeans.EmailTableFXBean;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.persistence.FakeEmailDAOPersistence;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private AnchorPane emailFXTable;

    @FXML
    private TableView<EmailTableFXBean> emailDataTable;

    @FXML
    private TableColumn<EmailTableFXBean, String> fromColumn;

    @FXML
    private TableColumn<EmailTableFXBean, String> subjectColumn;

    @FXML
    private TableColumn<EmailTableFXBean, Timestamp> dateColumn;

    @FXML
    void initialize() {
        //TOOD: Connects the property in the EmailBean object to the column in the
        // table
        fromColumn.setCellValueFactory(cellData -> cellData.getValue()
                .getFromFieldProperty());
        subjectColumn.setCellValueFactory(cellData -> cellData.getValue()
                .getSubjectFieldProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue()
                .getDateFieldProperty());

        adjustColumnWidths();

        emailDataTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) -> showEmailDetails(newValue));

        //The table rows can be dragged and dropped into a folder. Not complete yet.
        emailDataTable.setOnDragDetected((MouseEvent event) -> {
            //TODO : incomplete event handler

            /* drag was detected, start drag-and-drop gesture */
            LOG.debug("onDragDetected");

            /* allow any transfer mode */
            Dragboard db = emailDataTable.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();
            //content.putString(emailDataTable.getSelectionModel().getSelectedItem().getValue().toString());

            db.setContent(content);

            event.consume();
        });

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
        emailDataTable.setItems(FakeEmailDAOPersistence.findAllEmails());
    }

    /**
     * The FolderFXTreeLayoutController needs a reference to the this
     * controller. With that reference it can call this method to retrieve a
     * reference to the TableView and change its selection
     *
     * @return emailDataTable
     */
    public TableView<EmailTableFXBean> getEmailDataTable() {
        return emailDataTable;
    }

    /**
     * The content of the email selected in the table will be shown in the html
     * editor.
     *
     * @param emailData
     */
    private void showEmailDetails(EmailTableFXBean emailData) {
        //TODO: display the contents of the email in the section of the app where an email must be shown.
        LOG.info("DISPLAY THE EMAIL INFO WHEN CLICKED");
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
