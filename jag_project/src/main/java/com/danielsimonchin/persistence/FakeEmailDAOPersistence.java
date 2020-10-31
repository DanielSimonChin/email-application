/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.persistence;

import com.danielsimonchin.fxbeans.EmailTableFXBean;
import com.danielsimonchin.fxbeans.FolderFXBean;
import java.sql.Timestamp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A fake data class that returns lists of FX BEANS we need for display purposes
 * in the User interface.
 *
 * @author Daniel Simon Chin
 * @version October 31, 2020
 */
public class FakeEmailDAOPersistence {

    /**
     * Fake data method that returns a list of FolderFXBeans
     *
     * @return ObservableList of FolderFXBeans
     */
    public static ObservableList<FolderFXBean> findAllFolders() {
        ObservableList<FolderFXBean> folderDataList = FXCollections
                .observableArrayList();

        folderDataList.add(new FolderFXBean(1, "Cat Pictures"));
        folderDataList.add(new FolderFXBean(2, "Dawson"));
        folderDataList.add(new FolderFXBean(2, "Monkey Pictures"));
        folderDataList.add(new FolderFXBean(2, "My important documents"));

        return folderDataList;
    }

    /**
     * Fake data method that returns a list of all emails.
     *
     * @return ObservableList of EmailTableFXBean
     */
    public static ObservableList<EmailTableFXBean> findAllEmails() {
        ObservableList<EmailTableFXBean> emailsInFolder = FXCollections
                .observableArrayList();

        emailsInFolder.add(new EmailTableFXBean(1, "sender1@gmail.com", "My subject", new Timestamp(System.currentTimeMillis())));
        emailsInFolder.add(new EmailTableFXBean(2, "sender2@gmail.com", "Info", new Timestamp(System.currentTimeMillis())));
        emailsInFolder.add(new EmailTableFXBean(3, "sender3@gmail.com", "Our meeting", new Timestamp(System.currentTimeMillis())));
        emailsInFolder.add(new EmailTableFXBean(4, "sender4@gmail.com", "Dawson Con", new Timestamp(System.currentTimeMillis())));
        emailsInFolder.add(new EmailTableFXBean(5, "sender5@gmail.com", "Phase 3", new Timestamp(System.currentTimeMillis())));
        emailsInFolder.add(new EmailTableFXBean(6, "sender6@gmail.com", "Phase 4", new Timestamp(System.currentTimeMillis())));
        emailsInFolder.add(new EmailTableFXBean(7, "sender7@gmail.com", "Phase 1", new Timestamp(System.currentTimeMillis())));
        emailsInFolder.add(new EmailTableFXBean(8, "sender8@gmail.com", "Phase 2", new Timestamp(System.currentTimeMillis())));

        return emailsInFolder;
    }
}
