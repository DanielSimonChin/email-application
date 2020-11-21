package com.danielsimonchin.view;

import com.danielsimonchin.fxbeans.EmailTableFXBean;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.properties.EmailBean;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private EmailFXHTMLLayoutController htmlController;

    private EmailDAO emailDAO;
    @FXML
    private ResourceBundle resources;

    @FXML
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
                                if (newValue != null) {
                                    showEmailDetails(newValue);
                                }
                            } catch (SQLException ex) {
                                LOG.info("Caught SQLException when trying to display an email's details");
                            } catch (IOException ex) {
                                LOG.info("Caught IOException when trying to display an email's details");
                            }
                        });
    }

    /**
     * Rows in the table can be drag and dropped to another folder. We insert
     * the email's ID into the DragBoard which will be used in the onDragDropped
     * for the Tree.
     *
     * @param event
     */
    @FXML
    void dragDetected(MouseEvent event) {
        String selectedRow = "" + emailDataTable.getSelectionModel().getSelectedItem().getEmailId();
        LOG.debug("Selected row ID: " + selectedRow);
        if (selectedRow != null) {
            Dragboard db = emailDataTable.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();

            //pass the ID of the email
            content.putString(selectedRow);
            db.setContent(content);
            event.consume();
        }
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

    /**
     * Helper method that takes a observableList of EmailBean converts them into
     * EmailTableFXBean so we can set the items of the table
     *
     * @param allEmails
     * @return ObservableList of EmailTableFXBean
     */
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
     * @param folderName
     * @throws SQLException
     * @throws IOException
     */
    public void displaySelectedFolder(String folderName) throws SQLException, IOException {
        //emailDataTable.getSelectionModel().clearSelection();
        emailDataTable.getItems().clear();
        ObservableList<EmailBean> allEmails = this.emailDAO.findAllInFolder(folderName);
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
//        fromColumn.setPrefWidth(width / 3);
//        subjectColumn.setPrefWidth(width / 3);
//        dateColumn.setPrefWidth(width / 3);
    }
}
