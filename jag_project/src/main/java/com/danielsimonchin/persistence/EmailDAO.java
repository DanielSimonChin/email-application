package com.danielsimonchin.persistence;

import java.sql.SQLException;
import java.util.List;
import jodd.mail.Email;

/**
 * Interface for CRUD Methods relating to an Email object and to the database.
 *
 * @author Daniel
 */
public interface EmailDAO {

    // Create
    public int create(Email email) throws SQLException;

    // Read
    public List<Email> findAll() throws SQLException;

    public Email findID(int id) throws SQLException;

    // Update
    public int update(Email email) throws SQLException;

    // Delete
    public int delete(int id) throws SQLException;
}
