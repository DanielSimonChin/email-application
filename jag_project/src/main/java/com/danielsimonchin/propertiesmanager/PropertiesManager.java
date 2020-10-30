package com.danielsimonchin.propertiesmanager;

import com.danielsimonchin.fxbeans.PropertyBean;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import java.nio.file.Path;
import static java.nio.file.Paths.get;
import java.util.Properties;

/**
 * Reading and Writing to a properties file.
 *
 * @author Daniel Simon Chin
 * @version Oct 31, 2020
 */
public class PropertiesManager {

    /**
     * Updates a MailConfigBean object with the contents of the properties file
     *
     * @param propertyBean
     * @param path
     * @param propFileName
     * @return true if the file's properties have been loaded, false otherwise.
     * @throws java.io.IOException
     */
    public final boolean loadTextProperties(final PropertyBean propertyBean, final String path, final String propFileName) throws IOException {

        boolean found = false;
        Properties prop = new Properties();

        Path txtFile = get(path, propFileName + ".properties");

        // File must exist
        if (Files.exists(txtFile)) {
            try ( InputStream propFileStream = newInputStream(txtFile);) {
                prop.load(propFileStream);
            }
            propertyBean.setUserName(prop.getProperty("userName"));
            propertyBean.setEmailAddress(prop.getProperty("emailAddress"));
            propertyBean.setEmailPassword(prop.getProperty("emailPassword"));
            propertyBean.setImapURL(prop.getProperty("imapURL"));
            propertyBean.setSmtpURL(prop.getProperty("smtpURL"));
            propertyBean.setImapPort(prop.getProperty("imapPort"));
            propertyBean.setSmtpPort(prop.getProperty("smtpPort"));
            propertyBean.setmysqlURL(prop.getProperty("mysqlURL"));
            propertyBean.setmysqlDatabase(prop.getProperty("mysqlDatabase"));
            propertyBean.setmysqlPort(prop.getProperty("mysqlPort"));
            propertyBean.setmysqlUsername(prop.getProperty("mysqlUsername"));
            propertyBean.setmysqlPassword(prop.getProperty("mysqlPassword"));
            found = true;
        }
        return found;
    }

    /**
     * Creates a plain text properties file based on the parameters
     *
     * @param path Must exist, will not be created
     * @param propFileName Name of the properties file
     * @param propertyBean The bean to store into the properties
     * @throws IOException
     */
    public final void writeTextProperties(final String path, final String propFileName, final PropertyBean propertyBean) throws IOException {

        Properties prop = new Properties();

        prop.setProperty("userName", propertyBean.getUserName());
        prop.setProperty("emailAddress", propertyBean.getEmailAddress());
        prop.setProperty("emailPassword", propertyBean.getEmailPassword());
        prop.setProperty("imapURL", propertyBean.getImapURL());
        prop.setProperty("smtpURL", propertyBean.getSmtpURL());
        prop.setProperty("imapPort", propertyBean.getImapPort());
        prop.setProperty("smtpPort", propertyBean.getSmtpPort());
        prop.setProperty("mysqlURL", propertyBean.getmysqlURL());
        prop.setProperty("mysqlDatabase", propertyBean.getmysqlDatabase());
        prop.setProperty("mysqlPort", propertyBean.getmysqlPort());
        prop.setProperty("mysqlUsername", propertyBean.getmysqlUsername());
        prop.setProperty("mysqlPassword", propertyBean.getmysqlPassword());

        Path txtFile = get(path, propFileName + ".properties");

        // Creates the file or if file exists it is truncated to length of zero
        // before writing
        try ( OutputStream propFileStream = newOutputStream(txtFile)) {
            prop.store(propFileStream, "SMTP Properties");
        }
    }
}
