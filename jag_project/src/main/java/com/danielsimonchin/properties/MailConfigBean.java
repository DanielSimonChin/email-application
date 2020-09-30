package com.danielsimonchin.properties;

/**
 * Class to contain the information for an email account. This is sufficient for
 * this project but will need more fields if you wish the program to work with
 * mail systems other than GMail. This should be stored in properties file. If
 * you are feeling adventurous you can look into how you might encrypt the
 * password as it will be in a simple text file.
 *
 * @author Daniel Simon Chin
 * @version September 20, 2020
 */
public class MailConfigBean {

    private String name;
    private String userEmailAddress;
    private String password;
    private String imapUrl;
    private String smtpUrl;
    private String imapPort;
    private String smtpPort;
    private String databaseURL;
    private String databaseName;
    private String databasePort;
    private String databaseUserName;
    private String databasePassword;

    /**
     * Default Constructor
     */
    public MailConfigBean() {
        this.name = "";
        this.userEmailAddress = "";
        this.password = "";
        this.imapUrl = "";
        this.smtpUrl = "";
        this.imapPort = "";
        this.smtpPort = "";
        this.databaseURL = "";
        this.databaseName = "";
        this.databasePort = "";
        this.databaseUserName = "";
        this.databasePassword = "";
    }

    /**
     * Non-default constructor Takes as input all the parameters relating to an
     * Email Object and sets the fields.The non-applicable fields are left empty
     *
     * @param name
     * @param userEmailAddress
     * @param password
     * @param imapUrl
     * @param smtpUrl
     * @param imapPort
     * @param smtpPort
     * @param databaseURL
     * @param databaseName
     * @param databasePort
     * @param databaseUserName
     * @param databasePassword
     */
    public MailConfigBean(final String name, final String userEmailAddress, final String password, final String imapUrl, final String smtpUrl, final String imapPort, final String smtpPort, final String databaseURL, final String databaseName, final String databasePort, final String databaseUserName, final String databasePassword) {
        this.name = name;
        this.userEmailAddress = userEmailAddress;
        this.password = password;
        this.imapUrl = imapUrl;
        this.smtpUrl = smtpUrl;
        this.imapPort = imapPort;
        this.smtpPort = smtpPort;
        this.databaseURL = databaseURL;
        this.databaseName = databaseName;
        this.databasePort = databasePort;
        this.databaseUserName = databaseUserName;
        this.databasePassword = databasePassword;
    }

    /**
     * @return the user's name
     */
    public final String getName() {
        return this.name;
    }

    /**
     * @param name
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the userEmailAddress
     */
    public final String getUserEmailAddress() {
        return userEmailAddress;
    }

    /**
     * @param userEmailAddress
     */
    public final void setUserEmailAddress(final String userEmailAddress) {
        this.userEmailAddress = userEmailAddress;
    }

    /**
     * @return the password
     */
    public final String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public final void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @return the imapUrl
     */
    public final String getImapUrl() {
        return this.imapUrl;
    }

    /**
     * @param imapUrl
     */
    public final void setImapUrl(final String imapUrl) {
        this.imapUrl = imapUrl;
    }

    /**
     * @return the smtpUrl
     */
    public final String getSmtpUrl() {
        return this.smtpUrl;
    }

    /**
     * @param smtpUrl
     */
    public final void setSmtpUrl(final String smtpUrl) {
        this.smtpUrl = smtpUrl;
    }

    /**
     * @return the imapPort
     */
    public final String getImapPort() {
        return this.imapPort;
    }

    /**
     * @param imapPort
     */
    public final void setImapPort(final String imapPort) {
        this.imapPort = imapPort;
    }

    /**
     * @return the smtpPort
     */
    public final String getSmtpPort() {
        return this.smtpPort;
    }

    /**
     * @param smtpPort
     */
    public final void setSmtpPort(final String smtpPort) {
        this.smtpPort = smtpPort;
    }

    /**
     * @return the databaseURL
     */
    public final String getDatabaseUrl() {
        return this.databaseURL;
    }

    /**
     * @param databaseURL
     */
    public final void setDatabaseURL(final String databaseURL) {
        this.databaseURL = databaseURL;
    }

    /**
     * @return the databaseName
     */
    public final String getDatabaseName() {
        return this.databaseName;
    }

    /**
     * @param databaseName
     */
    public final void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * @return the databasePort
     */
    public final String getDatabasePort() {
        return this.databasePort;
    }

    /**
     * @param databasePort
     */
    public final void setDatabasePort(final String databasePort) {
        this.databasePort = databasePort;
    }

    /**
     * @return the databaseUserName
     */
    public final String getDatabaseUserName() {
        return this.databaseUserName;
    }

    /**
     * @param databaseUserName
     */
    public final void setDatabaseUserName(final String databaseUserName) {
        this.databaseUserName = databaseUserName;
    }

    /**
     * @return the databasePassword
     */
    public final String getDatabasePassword() {
        return this.databasePassword;
    }

    /**
     * @param databasePassword
     */
    public final void setDatabasePassword(final String databasePassword) {
        this.databasePassword = databasePassword;
    }
}
