package com.danielsimonchin.view;

import com.danielsimonchin.business.SendAndReceive;
import com.danielsimonchin.exceptions.InvalidMailConfigBeanUsernameException;
import com.danielsimonchin.exceptions.InvalidRecipientImapURLException;
import com.danielsimonchin.exceptions.NotEnoughEmailRecipientsException;
import com.danielsimonchin.exceptions.NotEnoughRecipientsException;
import com.danielsimonchin.exceptions.RecipientEmailAddressNullException;
import com.danielsimonchin.exceptions.RecipientInvalidFormatException;
import com.danielsimonchin.exceptions.RecipientListNullException;
import com.danielsimonchin.fxbeans.FormFXBean;
import com.danielsimonchin.fxbeans.HTMLEditorFXBean;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.properties.EmailBean;
import com.danielsimonchin.properties.MailConfigBean;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
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

    private EmailFXTableLayoutController tableController;

    private MailConfigBean mailConfigBean;

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

    private FormFXBean formFXBean;
    private HTMLEditorFXBean htmlEditorFXBean;

    //the email that is selected will be passed to this controller.
    private EmailBean currentlySelectedEmail;

    @FXML
    void initialize() {
        formFXBean = new FormFXBean();
        htmlEditorFXBean = new HTMLEditorFXBean();

        Bindings.bindBidirectional(toRecipientField.textProperty(), formFXBean.getToFieldProperty());
        Bindings.bindBidirectional(ccRecipientField.textProperty(), formFXBean.getCcFieldProperty());
        Bindings.bindBidirectional(bccRecipientField.textProperty(), formFXBean.getBccFieldProperty());
        Bindings.bindBidirectional(subjectField.textProperty(), formFXBean.getSubjectFieldProperty());
    }

    /**
     * When the user clicks on an email, we must display its content in the form
     * and html section of the app.
     *
     * @param emailBean
     */
    public void displaySelectedEmail(EmailBean emailBean) {
        LOG.info("Displaying the selected email.");

        //reset the current email bean and the formFXBean's attachments so we have no information about the previously selected email.
        resetSelectedEmailBean();

        currentlySelectedEmail = emailBean;

        ObservableList<File> attachments = FXCollections.observableArrayList();
        emailBean.email.attachments().forEach(attachment -> {
            attachments.add(new File(attachment.getName()));
        });

        //set the formFXBean's attachments with the emailBean's attachments
        this.formFXBean.getAttachments().addAll(attachments);

        formFXBean.setToField(createRecipientListString(emailBean.email.to()));
        formFXBean.setCcField(createRecipientListString(emailBean.email.cc()));
        formFXBean.setBccField(createRecipientListString(emailBean.email.bcc()));

        formFXBean.setSubjectField(emailBean.email.subject());

        List<EmailMessage> messages = emailBean.email.messages();
        String htmlMessage = "";
        for (EmailMessage message : messages) {
            if (message.getMimeType().equals("text/html")) {
                htmlMessage = message.getContent();
            }
        }

        //removing all img tags so we can show the images once each using the email's attachments
        String htmlWithoutImages = htmlMessage.replaceAll("\\<.*?\\>", "");

        //Since we cannot bind the htmlEditor, we simply set its html message to the html message of the emailBean
        emailFXHTMLEditor.setHtmlText(htmlWithoutImages);
        htmlEditorFXBean.setHtmlMessage(htmlWithoutImages);

        //For every image, display it in the html editor
        emailBean.email.attachments().forEach(attachment -> {
            displayImagesInHtml(new File(attachment.getName()));
        });

    }

    /**
     * Creates a semi-colon seperated string of recipients
     *
     * @param recipients
     * @return a semi-colon seperated string of recipients
     */
    private String createRecipientListString(EmailAddress[] recipients) {
        String resultString = "";

        if (recipients.length > 0) {
            StringBuilder sb = new StringBuilder();

            for (EmailAddress recipient : recipients) {
                sb.append(recipient.getEmail()).append(";");
            }

            resultString = sb.deleteCharAt(sb.length() - 1).toString();
        }
        return resultString;
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
     * The EmailFXTableLayoutController will be passed from the root controller
     * so we can update the table whenever an email is sent, updated or deleted
     *
     * @param tableController
     */
    public void setTableController(EmailFXTableLayoutController tableController) {
        this.tableController = tableController;
    }

    /**
     * Pass a mailConfigBean object since this controller will be sending emails
     * using the SendAndReceive class
     *
     * @param mailConfigBean
     */
    public void setMailConfigBean(MailConfigBean mailConfigBean) {
        this.mailConfigBean = mailConfigBean;
    }

    /**
     * Event handler for deleting the currently displayed email in the html
     * editor. Removing it from the database. Gives a visual pop up confirming
     * if an email was removed.
     *
     * @param event
     */
    @FXML
    void onDeleteEmail(ActionEvent event) throws SQLException, IOException {
        if (this.currentlySelectedEmail != null) {
            if (this.emailDAO.deleteEmail(this.currentlySelectedEmail.getId()) == 1) {
                //We want to refresh the draft folder to display what changes were made to the drafts
                String folderName = this.emailDAO.getFolderName(this.currentlySelectedEmail.getFolderKey());

                this.tableController.displaySelectedFolder(folderName);

                clearFormAndHtmlEditor();

                popupAlert("deletedEmailTitle", "deletedEmailHeader", "deletedEmailMessage");

                resetSelectedEmailBean();
            }
        }
    }

    /**
     * Event handler for saving the currently displayed draft constructed in the
     * html editor. Everything that is in the form and html editor will be used
     * and added as an entry in the draft folder
     *
     * @param event
     */
    @FXML
    void onSaveDraft(ActionEvent event) throws SQLException, IOException, NotEnoughEmailRecipientsException, InvalidMailConfigBeanUsernameException, RecipientListNullException, RecipientEmailAddressNullException, RecipientInvalidFormatException, InvalidRecipientImapURLException {
        //if the user selected a existing draft email and clicked on save, then we do not create a new entry. We simply update the existing draft.
        if (this.currentlySelectedEmail != null && this.currentlySelectedEmail.getFolderKey() == 3) {
            List<File> regularAttachments = new ArrayList<>(this.formFXBean.getAttachments());
            List<File> embeddedAttachments = new ArrayList<>();

            //Construct an email object which will be used to create an EmailBean
            Email draftEmail = createEmail(convertRecipientTextFieldToList(formFXBean.getToField()), convertRecipientTextFieldToList(formFXBean.getCcField()), convertRecipientTextFieldToList(formFXBean.getBccField()), formFXBean.getSubjectField(), "", emailFXHTMLEditor.getHtmlText(), regularAttachments, embeddedAttachments);

            EmailBean emailBean = new EmailBean(this.currentlySelectedEmail.getId(), this.currentlySelectedEmail.getFolderKey(), null, draftEmail);

            //update the existing draft email
            this.emailDAO.updateDraft(emailBean);

            //We want to refresh the draft folder to display what changes were made to the drafts
            this.tableController.displaySelectedFolder("DRAFT");

            LOG.info("The DRAFT Email with ID: " + this.currentlySelectedEmail.getId() + " has been updated.");

            clearFormAndHtmlEditor();
            resetSelectedEmailBean();

            return;
        }

        //if no email was selected, then create a new entry in the draft folder
        List<File> regularAttachments = new ArrayList<>(this.formFXBean.getAttachments());
        List<File> embeddedAttachments = new ArrayList<>();

        //Construct an email object which will be used to create an EmailBean
        Email draftEmail = createEmail(convertRecipientTextFieldToList(formFXBean.getToField()), convertRecipientTextFieldToList(formFXBean.getCcField()), convertRecipientTextFieldToList(formFXBean.getBccField()), formFXBean.getSubjectField(), "", emailFXHTMLEditor.getHtmlText(), regularAttachments, embeddedAttachments);
        //The email's id will be set in the DAOImpl method, 3 is the the folder key for drafts
        EmailBean emailBean = new EmailBean(-1, 3, null, draftEmail);

        //add the draft email into the draft folder
        this.emailDAO.createEmailRecord(emailBean);

        //We want to refresh the draft folder to display what changes were made to the draft
        this.tableController.displaySelectedFolder("DRAFT");

        clearFormAndHtmlEditor();
        resetSelectedEmailBean();

        LOG.info("The DRAFT Email with ID: " + emailBean.getId() + " has been added to the DRAFT folder.");
    }

    /**
     * Helper method that creates and returns an Email object given the
     * parameters needed to create an email
     *
     * @param toList List of all recipients in the 'To' field
     * @param ccList List of all cc recipients
     * @param bccList List of all bcc recipients
     * @param subject Subject of email
     * @param textMsg plain text of the email
     * @param htmlMsg html text of the email
     * @param regularAttachments List of File objects
     * @param embeddedAttachments List of File objects that will be embedded in
     * the email
     * @return An Email object with the correct values and parameters needed.
     */
    private Email createEmail(List<String> toList, List<String> ccList, List<String> bccList, String subject, String textMsg, String htmlMsg, List<File> regularAttachments, List<File> embeddedAttachments) {
        Email email = Email.create().from(this.mailConfigBean.getUserEmailAddress());
        email.subject(subject);
        email.textMessage(textMsg);
        email.htmlMessage(htmlMsg);
        toList.forEach(emailAddress -> {
            email.to(emailAddress);
        });
        ccList.forEach(emailAddress -> {
            email.cc(emailAddress);
        });
        bccList.forEach(emailAddress -> {
            email.bcc(emailAddress);
        });
        regularAttachments.forEach(attachment -> {
            email.attachment(EmailAttachment.with().content(attachment.getName()));
        });
        embeddedAttachments.forEach(attachment -> {
            email.embeddedAttachment(EmailAttachment.with().content(new File(attachment.getName())));
        });

        return email;
    }

    /**
     * Event handler to send the email constructed in the html editor given that
     * the email has at the minimum a TO recipient. The sent email is then added
     * to the database into the sent folder.
     *
     * @param event
     */
    @FXML
    void onSendEmail(ActionEvent event) throws SQLException, IOException, NotEnoughRecipientsException {
        if (this.currentlySelectedEmail != null && this.currentlySelectedEmail.getFolderKey() == 3) {
            try {
                sendDraftEmail();
            } catch (NotEnoughEmailRecipientsException ex) {
                LOG.info("The email must have at least 1 recipient.");
                popupAlert("noRecipientsTitle", "noRecipientsHeader", "noRecipientsErrorMessage");
            } catch (InvalidMailConfigBeanUsernameException ex) {
                LOG.info("The username is invalid");
            } catch (RecipientListNullException ex) {
                LOG.info("The recipient list is null");
            } catch (RecipientEmailAddressNullException ex) {
                LOG.info("One of the email recipients is null");
            } catch (RecipientInvalidFormatException ex) {
                LOG.info("One or more of the email recipients has an invalid format");
                popupAlert("invalidRecipientTitle", "invalidRecipientHeader", "invalidRecipientMessage");
            } catch (InvalidRecipientImapURLException ex) {
                LOG.info("The Imap URL is invalid");
            }
            this.currentlySelectedEmail = new EmailBean();
            return;
        }

        //send the email using the sendEmail method and store the email in the database.
        SendAndReceive sendAndReceive = new SendAndReceive(this.mailConfigBean);

        List<String> toRecipients = convertRecipientTextFieldToList(formFXBean.getToField());
        List<String> ccRecipients = convertRecipientTextFieldToList(formFXBean.getCcField());
        List<String> bccRecipients = convertRecipientTextFieldToList(formFXBean.getBccField());

        String plainText = "";
        String htmlMessage = emailFXHTMLEditor.getHtmlText();

        List<File> regularAttachments = new ArrayList<>(this.formFXBean.getAttachments());
        List<File> embeddedAttachments = new ArrayList<>();

        Email resultEmail;
        try {
            resultEmail = sendAndReceive.sendEmail(toRecipients, ccRecipients, bccRecipients, formFXBean.getSubjectField(), plainText, htmlMessage, regularAttachments, embeddedAttachments);

            if (resultEmail != null) {
                //if the email was sent, clear the form and html section 
                clearFormAndHtmlEditor();

                //add the email to the sent folder for this user. The email id is set to -1 since the EmailDAO will set it in the createEmailRecord()
                EmailBean emailBean = new EmailBean(-1, 2, new Timestamp(resultEmail.currentSentDate().sentDate().getTime()), resultEmail);
                this.emailDAO.createEmailRecord(emailBean);

                //We want to refresh the draft folder to display what changes were made to the drafts
                this.tableController.displaySelectedFolder("SENT");

                LOG.info("The email with ID: " + emailBean.getId() + " has been added to the SENT folder.");
            }

        } catch (NotEnoughEmailRecipientsException ex) {
            LOG.info("The email must have at least 1 recipient.");
            popupAlert("noRecipientsTitle", "noRecipientsHeader", "noRecipientsErrorMessage");
        } catch (InvalidMailConfigBeanUsernameException ex) {
            LOG.info("The username is invalid");
        } catch (RecipientListNullException ex) {
            LOG.info("The recipient list is null");
        } catch (RecipientEmailAddressNullException ex) {
            LOG.info("One of the email recipients is null");
        } catch (RecipientInvalidFormatException ex) {
            LOG.info("One or more of the email recipients has an invalid format");
            popupAlert("invalidRecipientTitle", "invalidRecipientHeader", "invalidRecipientMessage");
        }
    }

    /**
     *
     * A draft email will call the updateDraft which will change its folder to
     * the SENT and send the email.
     *
     * @throws SQLException
     * @throws IOException
     * @throws NotEnoughEmailRecipientsException
     * @throws InvalidMailConfigBeanUsernameException
     * @throws RecipientListNullException
     * @throws RecipientEmailAddressNullException
     * @throws RecipientInvalidFormatException
     * @throws InvalidRecipientImapURLException
     */
    private void sendDraftEmail() throws SQLException, IOException, NotEnoughEmailRecipientsException, InvalidMailConfigBeanUsernameException, RecipientListNullException, RecipientEmailAddressNullException, RecipientInvalidFormatException, InvalidRecipientImapURLException {
        List<File> regularAttachments = new ArrayList<>(this.formFXBean.getAttachments());
        List<File> embeddedAttachments = new ArrayList<>();
        //Construct an email object which will be used to create an EmailBean
        Email draftEmail = createEmail(convertRecipientTextFieldToList(formFXBean.getToField()), convertRecipientTextFieldToList(formFXBean.getCcField()), convertRecipientTextFieldToList(formFXBean.getBccField()), formFXBean.getSubjectField(), "", emailFXHTMLEditor.getHtmlText(), regularAttachments, embeddedAttachments);

        //giving the folder id 2 since we wish to send this draft email. The email will be moved to the sent folder
        EmailBean emailBean = new EmailBean(this.currentlySelectedEmail.getId(), 2, null, draftEmail);

        //add the draft email into the draft folder
        this.emailDAO.updateDraft(emailBean);

        //Since we moved the email to the sent folder, the draft folder will no longer have it
        this.tableController.displaySelectedFolder("SENT");

        clearFormAndHtmlEditor();
    }

    /**
     * When the user clicks on the reply button, the currently selected email's
     * sender and subject prefixed with 'RE:' will be displayed in the form.
     *
     * @param event
     */
    @FXML
    void onReplyButton(ActionEvent event) {
        //Draft cannot be replied to, since it was never sent.
        if (this.currentlySelectedEmail != null && this.currentlySelectedEmail.getFolderKey() != 3 && this.currentlySelectedEmail.getFolderKey() != 0) {
            clearFormAndHtmlEditor();
            enableFormAndHTML();

            String recipient = this.currentlySelectedEmail.email.from().getEmail();
            String subject = "RE: " + this.currentlySelectedEmail.email.subject();

            toRecipientField.setText(recipient);
            subjectField.setText(subject);
        }
    }

    /**
     * Clicking the compose button enables the user to write in the form and
     * editor.
     *
     * @param event
     */
    @FXML
    void onComposeClick(ActionEvent event) {
        clearFormAndHtmlEditor();
        enableFormAndHTML();
        resetSelectedEmailBean();
        tableController.getEmailDataTable().getSelectionModel().clearSelection();
    }

    /**
     * The text field in the UI is seperated by the user with ';'. We split the
     * recipient emails and return them as List of string
     *
     * @param recipients
     * @return List of string representing all the recipients of an email
     */
    private List<String> convertRecipientTextFieldToList(String recipients) {
        if (recipients.equals("")) {
            return new ArrayList<>();
        }
        String[] emailRecipients = recipients.split(";");

        return new ArrayList(Arrays.asList(emailRecipients));
    }

    /**
     * Once an email is successfully sent, we clear the form and html editor for
     * future use.
     */
    public void clearFormAndHtmlEditor() {
        this.toRecipientField.clear();
        this.ccRecipientField.clear();
        this.bccRecipientField.clear();
        this.subjectField.clear();
        this.emailFXHTMLEditor.setHtmlText("");

        LOG.debug("The form fx bean after clearing" + this.formFXBean.toString());
        LOG.debug("The html editor fx bean after clearing" + this.emailFXHTMLEditor.getHtmlText());
    }

    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void popupAlert(String title, String header, String message) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle(resources.getString(title));
        dialog.setHeaderText(resources.getString(header));
        dialog.setContentText(resources.getString(message));
        dialog.show();
    }

    /**
     * If the selected email from the table is not a draft, then we cannot edit
     * it.
     */
    public void disableFormAndHTML() {
        this.toRecipientField.setDisable(true);
        this.ccRecipientField.setDisable(true);
        this.bccRecipientField.setDisable(true);
        this.subjectField.setDisable(true);
        this.emailFXHTMLEditor.setDisable(true);
    }

    /**
     * If the selected email from the table is a draft, we allow the user to
     * edit the email.
     */
    public void enableFormAndHTML() {
        this.toRecipientField.setDisable(false);
        this.ccRecipientField.setDisable(false);
        this.bccRecipientField.setDisable(false);
        this.subjectField.setDisable(false);
        this.emailFXHTMLEditor.setDisable(false);
    }

    /**
     * Whenever the user changes folder, we reset the currently selected email.
     */
    public void resetSelectedEmailBean() {
        this.currentlySelectedEmail = new EmailBean();
        this.formFXBean.setAttachments(FXCollections.observableArrayList());
    }

    /**
     * @return A reference to the formFXBean
     */
    public FormFXBean getFormFXBean() {
        return this.formFXBean;
    }

    /**
     * Whenever an attachment is added, we append the image to the html editor
     * to display what we have added
     *
     * @param file
     */
    public void displayImagesInHtml(File file) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.emailFXHTMLEditor.getHtmlText());

        sb.append("<img src=' ").append(file.toURI()).append("'/>");

        this.emailFXHTMLEditor.setHtmlText(sb.toString());
    }
}
