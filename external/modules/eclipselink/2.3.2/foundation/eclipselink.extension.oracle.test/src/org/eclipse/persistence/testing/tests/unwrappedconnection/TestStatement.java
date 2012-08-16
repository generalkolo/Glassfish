/*******************************************************************************
 * Copyright (c) 1998, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.unwrappedconnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class TestStatement implements Statement{
    
    private Statement statement;
    
    public TestStatement(Statement aStatement){
        statement = aStatement;
    }

    public void addBatch(String sql) throws SQLException {
        statement.addBatch(sql);
    }

    public void cancel() throws SQLException {
        statement.cancel();
    }

    public void clearBatch() throws SQLException {
        statement.clearBatch();
    }

    public void clearWarnings() throws SQLException {
        statement.clearWarnings();
    }

    public void close() throws SQLException {
        statement.close();
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return statement.execute(sql, autoGeneratedKeys);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return statement.execute(sql, columnIndexes);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return statement.execute(sql, columnNames);
    }

    public boolean execute(String sql) throws SQLException {
        return statement.execute(sql);
    }

    public int[] executeBatch() throws SQLException {
        return statement.executeBatch();
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return statement.executeQuery(sql);
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return statement.executeUpdate(sql, autoGeneratedKeys);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return statement.executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return statement.executeUpdate(sql, columnNames);
    }

    public int executeUpdate(String sql) throws SQLException {
        return statement.executeUpdate(sql);
    }

    public Connection getConnection() throws SQLException {
        return statement.getConnection();
    }

    public int getFetchDirection() throws SQLException {
        return statement.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        return statement.getFetchSize();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return statement.getGeneratedKeys();
    }

    public int getMaxFieldSize() throws SQLException {
        return statement.getMaxFieldSize();
    }

    public int getMaxRows() throws SQLException {
        return statement.getMaxRows();
    }

    public boolean getMoreResults() throws SQLException {
        return statement.getMoreResults();
    }

    public boolean getMoreResults(int current) throws SQLException {
        return statement.getMoreResults(current);
    }

    public int getQueryTimeout() throws SQLException {
        return statement.getQueryTimeout();
    }

    public ResultSet getResultSet() throws SQLException {
        return statement.getResultSet();
    }

    public int getResultSetConcurrency() throws SQLException {
        return statement.getResultSetConcurrency();
    }

    public int getResultSetHoldability() throws SQLException {
        return statement.getResultSetHoldability();
    }

    public int getResultSetType() throws SQLException {
        return statement.getResultSetType();
    }

    public int getUpdateCount() throws SQLException {
        return statement.getUpdateCount();
    }

    public SQLWarning getWarnings() throws SQLException {
        return statement.getWarnings();
    }

    public void setCursorName(String name) throws SQLException {
        statement.setCursorName(name);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        statement.setEscapeProcessing(enable);
    }

    public void setFetchDirection(int direction) throws SQLException {
        statement.setFetchDirection(direction);
    }

    public void setFetchSize(int rows) throws SQLException {
        statement.setFetchSize(rows);
    }

    public void setMaxFieldSize(int max) throws SQLException {
        statement.setFetchSize(max);
    }

    public void setMaxRows(int max) throws SQLException {
        statement.setMaxRows(max);
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        statement.setQueryTimeout(seconds);
    }

    public boolean isClosed()  throws SQLException {
        return false;       
    }

    public boolean isPoolable()  throws SQLException {
        return false;       
    }

    public void setPoolable(boolean poolable)  throws SQLException {
    }

    public boolean isWrapperFor(Class<?> iFace) throws SQLException{
        return false;
    }

    public <T>T unwrap(Class<T> iFace)  throws SQLException {
        return iFace.cast(this);
    }
    
}