/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

/**
 * Class to contain the information for an email account. This is sufficient for
 * this project but will need more fields if you wish the program to work with
 * mail systems other than GMail. This should be stored in properties file. If
 * you are feeling adventurous you can look into how you might encrypt the
 * password as it will be in a simple text file.
 *
 * @author Ken Fogel
 *
 */
public class MailConfigBean {

    private String host;
    private String userEmailAddress;
    private String password;

    /**
     * Default Constructor
     */
    public MailConfigBean() {
        this.host = "";
        this.userEmailAddress = "";
        this.password = "";
    }

    /**
     * Non-default constructor
     * 
     * @param host
     * @param userEmailAddress
     * @param password
     */
    public MailConfigBean(final String host, final String userEmailAddress, final String password) {
        this.host = host;
        this.userEmailAddress = userEmailAddress;
        this.password = password;
    }

    /**
     * @return the host
     */
    public final String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public final void setHost(final String host) {
        this.host = host;
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
}

