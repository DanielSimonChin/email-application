package com.danielsimonchin.fxbeans;

import java.util.Objects;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * JavaFX Bean for JAG Properties
 *
 * @author Daniel
 */
public class PropertyBean {

    private StringProperty userName;
    private StringProperty emailAddress;
    private StringProperty emailPassword;
    private StringProperty imapURL;
    private StringProperty smtpURL;
    private StringProperty imapPort;
    private StringProperty smtpPort;
    private StringProperty mysqlURL;
    private StringProperty mysqlDatabase;
    private StringProperty mysqlPort;
    private StringProperty mysqlUsername;
    private StringProperty mysqlPassword;

    /**
     * Constructor which takes string inputs for every form field and sets them
     * as SimpleStringProperty objects to their corresponding JavaFX Bean field.
     *
     * @param userName
     * @param emailAddress
     * @param emailPassword
     * @param imapURL
     * @param smtpURL
     * @param imapPort
     * @param smtpPort
     * @param mysqlURL
     * @param mysqlDatabase
     * @param mysqlPort
     * @param mysqlUsername
     * @param mysqlPassword
     */
    public PropertyBean(String userName, String emailAddress, String emailPassword, String imapURL, String smtpURL, String imapPort, String smtpPort, String mysqlURL, String mysqlDatabase, String mysqlPort, String mysqlUsername, String mysqlPassword) {
        this.userName = new SimpleStringProperty(userName);
        this.emailAddress = new SimpleStringProperty(emailAddress);
        this.emailPassword = new SimpleStringProperty(emailPassword);
        this.imapURL = new SimpleStringProperty(imapURL);
        this.smtpURL = new SimpleStringProperty(smtpURL);
        this.imapPort = new SimpleStringProperty(imapPort);
        this.smtpPort = new SimpleStringProperty(smtpPort);
        this.mysqlURL = new SimpleStringProperty(mysqlURL);
        this.mysqlDatabase = new SimpleStringProperty(mysqlDatabase);
        this.mysqlPort = new SimpleStringProperty(mysqlPort);
        this.mysqlUsername = new SimpleStringProperty(mysqlUsername);
        this.mysqlPassword = new SimpleStringProperty(mysqlPassword);
    }

    /**
     * Default constructor which ensures we don't have any nulls and sets every
     * field as an empty string.
     */
    public PropertyBean() {
        this("", "", "", "", "", "", "", "", "", "", "", "");
    }

    /**
     * @return the user's name
     */
    public String getUserName() {
        return this.userName.get();
    }

    /**
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    /**
     * @return the StringProperty userName
     */
    public StringProperty getUserNameProperty() {
        return userName;
    }

    /**
     * @return the user's email address
     */
    public String getEmailAddress() {
        return this.emailAddress.get();
    }

    /**
     * @param emailAddress
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress.set(emailAddress);
    }

    /**
     * @return the StringProperty emailAddress
     */
    public StringProperty getEmailAddressProperty() {
        return emailAddress;
    }

    /**
     * @return the user's email password
     */
    public String getEmailPassword() {
        return this.emailPassword.get();
    }

    /**
     * @param emailPassword
     */
    public void setEmailPassword(String emailPassword) {
        this.emailPassword.set(emailPassword);
    }

    /**
     * @return the StringProperty emailPassword
     */
    public StringProperty getEmailPasswordProperty() {
        return emailPassword;
    }

    /**
     * @return the user's imapURL
     */
    public String getImapURL() {
        return this.imapURL.get();
    }

    /**
     * @param imapURL
     */
    public void setImapURL(String imapURL) {
        this.imapURL.set(imapURL);
    }

    /**
     * @return the StringProperty imapURL
     */
    public StringProperty getImapURLProperty() {
        return imapURL;
    }

    /**
     * @return the user's smtpURL
     */
    public String getSmtpURL() {
        return this.smtpURL.get();
    }

    /**
     * @param smtpURL
     */
    public void setSmtpURL(String smtpURL) {
        this.smtpURL.set(smtpURL);
    }

    /**
     * @return the StringProperty smtpURL
     */
    public StringProperty getSmtpURLProperty() {
        return smtpURL;
    }

    /**
     * @return the user's imapPort
     */
    public String getImapPort() {
        return this.imapPort.get();
    }

    /**
     * @param imapPort
     */
    public void setImapPort(String imapPort) {
        this.imapPort.set(imapPort);
    }

    /**
     * @return the StringProperty imapPort
     */
    public StringProperty getImapPortProperty() {
        return imapPort;
    }

    /**
     * @return the user's smtpPort
     */
    public String getSmtpPort() {
        return this.smtpPort.get();
    }

    /**
     * @param smtpPort
     */
    public void setSmtpPort(String smtpPort) {
        this.smtpPort.set(smtpPort);
    }

    /**
     * @return the StringProperty smtpPort
     */
    public StringProperty getSmtpPortProperty() {
        return smtpPort;
    }

    /**
     * @return the user's mysqlURL
     */
    public String getmysqlURL() {
        return this.mysqlURL.get();
    }

    /**
     * @param mysqlURL
     */
    public void setmysqlURL(String mysqlURL) {
        this.mysqlURL.set(mysqlURL);
    }

    /**
     * @return the StringProperty mysqlURL
     */
    public StringProperty getmysqlURLProperty() {
        return mysqlURL;
    }

    /**
     * @return the user's mysqlDatabase
     */
    public String getmysqlDatabase() {
        return this.mysqlDatabase.get();
    }

    /**
     * @param mysqlDatabase
     */
    public void setmysqlDatabase(String mysqlDatabase) {
        this.mysqlDatabase.set(mysqlDatabase);
    }

    /**
     * @return the StringProperty mysqlDatabase
     */
    public StringProperty getmysqlDatabaseProperty() {
        return mysqlDatabase;
    }

    /**
     * @return the user's mysqlPort
     */
    public String getmysqlPort() {
        return this.mysqlPort.get();
    }

    /**
     * @param mysqlPort
     */
    public void setmysqlPort(String mysqlPort) {
        this.mysqlPort.set(mysqlPort);
    }

    /**
     * @return the StringProperty mysqlPort
     */
    public StringProperty getmysqlPortProperty() {
        return mysqlPort;
    }

    /**
     * @return the user's mysqlUsername
     */
    public String getmysqlUsername() {
        return this.mysqlUsername.get();
    }

    /**
     * @param mysqlUsername
     */
    public void setmysqlUsername(String mysqlUsername) {
        this.mysqlUsername.set(mysqlUsername);
    }

    /**
     * @return the StringProperty mysqlUsername
     */
    public StringProperty getmysqlUsernameProperty() {
        return mysqlUsername;
    }

    /**
     * @return the user's mysqlPassword
     */
    public String getmysqlPassword() {
        return this.mysqlPassword.get();
    }

    /**
     * @param mysqlPassword
     */
    public void setmysqlPassword(String mysqlPassword) {
        this.mysqlPassword.set(mysqlPassword);
    }

    /**
     * @return the StringProperty mysqlPassword
     */
    public StringProperty getmysqlPasswordProperty() {
        return mysqlPassword;
    }

    /**
     * Override the toString for a PropertyBean
     *
     * @return resulting toString() String
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PropertyBean{userName=").append(userName);
        sb.append(", emailAddress=").append(emailAddress);
        sb.append(", emailPassword=").append(emailPassword);
        sb.append(", imapURL=").append(imapURL);
        sb.append(", smtpURL=").append(smtpURL);
        sb.append(", imapPort=").append(imapPort);
        sb.append(", smtpPort=").append(smtpPort);
        sb.append(", mysqlURL=").append(mysqlURL);
        sb.append(", mysqlDatabase=").append(mysqlDatabase);
        sb.append(", mysqlPort=").append(mysqlPort);
        sb.append(", mysqlUsername=").append(mysqlUsername);
        sb.append(", mysqlPassword=").append(mysqlPassword);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Generate and returns the hashCode of a PropertyBean object.
     *
     * @return The generated hashCode.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.userName);
        hash = 53 * hash + Objects.hashCode(this.emailAddress);
        hash = 53 * hash + Objects.hashCode(this.emailPassword);
        hash = 53 * hash + Objects.hashCode(this.imapURL);
        hash = 53 * hash + Objects.hashCode(this.smtpURL);
        hash = 53 * hash + Objects.hashCode(this.imapPort);
        hash = 53 * hash + Objects.hashCode(this.smtpPort);
        hash = 53 * hash + Objects.hashCode(this.mysqlURL);
        hash = 53 * hash + Objects.hashCode(this.mysqlDatabase);
        hash = 53 * hash + Objects.hashCode(this.mysqlPort);
        hash = 53 * hash + Objects.hashCode(this.mysqlUsername);
        hash = 53 * hash + Objects.hashCode(this.mysqlPassword);
        return hash;
    }

    /**
     * Override of the equals method to compare two PropertyBean objects.
     *
     * @param obj
     * @return true if they are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PropertyBean other = (PropertyBean) obj;
        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }
        if (!Objects.equals(this.emailAddress, other.emailAddress)) {
            return false;
        }
        if (!Objects.equals(this.emailPassword, other.emailPassword)) {
            return false;
        }
        if (!Objects.equals(this.imapURL, other.imapURL)) {
            return false;
        }
        if (!Objects.equals(this.smtpURL, other.smtpURL)) {
            return false;
        }
        if (!Objects.equals(this.imapPort, other.imapPort)) {
            return false;
        }
        if (!Objects.equals(this.smtpPort, other.smtpPort)) {
            return false;
        }
        if (!Objects.equals(this.mysqlURL, other.mysqlURL)) {
            return false;
        }
        if (!Objects.equals(this.mysqlDatabase, other.mysqlDatabase)) {
            return false;
        }
        if (!Objects.equals(this.mysqlPort, other.mysqlPort)) {
            return false;
        }
        if (!Objects.equals(this.mysqlUsername, other.mysqlUsername)) {
            return false;
        }
        if (!Objects.equals(this.mysqlPassword, other.mysqlPassword)) {
            return false;
        }
        return true;
    }

}
