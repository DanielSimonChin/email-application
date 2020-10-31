package com.danielsimonchin.fxbeans;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * JavaFX Bean for a folder in the folder Tree
 *
 * @author Daniel Simon Chin
 * @version Oct 31, 2020
 */
public class FolderFXBean {

    private IntegerProperty folderId;
    private StringProperty folderName;

    /**
     * Constructor which sets the input parameters to the associated FXBean.
     *
     * @param folderId
     * @param folderName
     */
    public FolderFXBean(int folderId, String folderName) {
        this.folderId = new SimpleIntegerProperty(folderId);
        this.folderName = new SimpleStringProperty(folderName);
    }

    /**
     * Default constructor which calls the first constructor and giving it
     * default values.
     */
    public FolderFXBean() {
        this(0, "");
    }

    /**
     * @return the folder's id
     */
    public int getFolderId() {
        return this.folderId.get();
    }

    /**
     * @param folderId
     */
    public void setFolderId(int folderId) {
        this.folderId.set(folderId);
    }

    /**
     * @return the folderId IntegerProperty
     */
    public IntegerProperty getFolderIdProperty() {
        return folderId;
    }

    /**
     * @return the folder's name
     */
    public String getFolderName() {
        return this.folderName.get();
    }

    /**
     * @param folderName
     */
    public void setFolderName(String folderName) {
        this.folderName.set(folderName);
    }

    /**
     * @return the folderName StringProperty
     */
    public StringProperty getFolderNameProperty() {
        return folderName;
    }
}
