package com.danielsimonchin.view;

import com.danielsimonchin.fxbeans.EmailTableFXBean;
import com.danielsimonchin.fxbeans.FolderFXBean;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.persistence.FakeEmailDAOPersistence;
import com.danielsimonchin.properties.EmailBean;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import jodd.mail.Email;
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

    private EmailFXHTMLLayoutController htmlController;

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
    private TableColumn<EmailTableFXBean, LocalDateTime> dateColumn;

    @FXML
    void initialize() {
        //TOOD: Connects the property in the EmailBean object to the column in the table
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
                        (observable, oldValue, newValue) -> {
                            try {
                                showEmailDetails(newValue);
                            } catch (SQLException ex) {
                                LOG.info("Caught SQLException when trying to display an email's details");
                            } catch (IOException ex) {
                                LOG.info("Caught IOException when trying to display an email's details");
                            }
                        });

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

    public void setHtmlController(EmailFXHTMLLayoutController emailFXHTMLController) {
        this.htmlController = emailFXHTMLController;
    }

    /**
     * When program first starts, the default folder is INBOX
     *
     * @throws SQLException
     * @throws IOException
     */
    public void displayTable() throws SQLException, IOException {
        ObservableList<EmailBean> allEmails = this.emailDAO.findAllInFolder("Inbox");
        emailDataTable.setItems(convertToTableBean(allEmails));
    }

    private ObservableList<EmailTableFXBean> convertToTableBean(ObservableList<EmailBean> allEmails) {
        ObservableList<EmailTableFXBean> tableFXBeans = FXCollections
                .observableArrayList();
        for (int i = 0; i < allEmails.size(); i++) {
            tableFXBeans.add(allEmails.get(i).convertToEmailTableFXBean());
        }
        return tableFXBeans;
    }

    /**
     * This is called from the Tree controller whenever a folder is clicked, we
     * update the table view with all the emails in that folder.
     *
     * @param folderBean
     * @throws SQLException
     * @throws IOException
     */
    public void displaySelectedFolder(FolderFXBean folderBean) throws SQLException, IOException {
        emailDataTable.getSelectionModel().clearSelection();
        ObservableList<EmailBean> allEmails = this.emailDAO.findAllInFolder(folderBean.getFolderName());
        emailDataTable.setItems(convertToTableBean(allEmails));
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
     * editor and the form.
     *
     * @param emailData
     */
    private void showEmailDetails(EmailTableFXBean emailData) throws SQLException, IOException {
        if (emailData == null) {
            LOG.info("EMAIL DATA IS NULL");
        }
        EmailBean selectedEmail = this.emailDAO.findID(emailData.getEmailId());

        //The html controller will handle displaying the email in the form and html sections.
        htmlController.displaySelectedEmail(selectedEmail);
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
