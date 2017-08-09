/*
 * Copyright (c) 2010-2014. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.common.jdbc;

import org.axonframework.unitofwork.CurrentUnitOfWork;
import org.axonframework.unitofwork.UnitOfWork;
import org.axonframework.unitofwork.UnitOfWorkListenerAdapter;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wrapper for a ConnectionProvider that checks if a connection is already attached to the Unit of Work, favoring that
 * connection over creating a new one.
 *
 * @author Allard Buijze
 * @since 2.2
 */
public class UnitOfWorkAwareConnectionProviderWrapper implements ConnectionProvider {

    private static final String CONNECTION_RESOURCE_NAME = Connection.class.getName();

    private final ConnectionProvider delegate;
    private final boolean inherited;

    /**
     * Initializes a ConnectionProvider, using given <code>delegate</code> to create a new instance, when on is not
     * already attached to the Unit of Work. Nested Unit of Work will inherit the same connection.
     *
     * @param delegate The connection provider creating connections, when required
     */
    public UnitOfWorkAwareConnectionProviderWrapper(ConnectionProvider delegate) {
        this(delegate, true);
    }

    /**
     * Initializes a ConnectionProvider, using given <code>delegate</code> to create a new instance, when on is not
     * already attached to the Unit of Work. Given <code>attachAsInheritedResource</code> flag indicates whether
     * the resource should be inherited by nested Unit of Work.
     *
     * @param delegate                  The connection provider creating connections, when required
     * @param attachAsInheritedResource whether or not nested Units of Work should inherit connections
     */
    public UnitOfWorkAwareConnectionProviderWrapper(ConnectionProvider delegate, boolean attachAsInheritedResource) {
        this.delegate = delegate;
        this.inherited = attachAsInheritedResource;
    }


    @Override
    public Connection getConnection() throws SQLException {
        if (!CurrentUnitOfWork.isStarted()) {
            return delegate.getConnection();
        }

        UnitOfWork uow = CurrentUnitOfWork.get();
        Connection connection = uow.getResource(CONNECTION_RESOURCE_NAME);
        if (connection == null || connection.isClosed()) {
            final Connection delegateConnection = delegate.getConnection();
            connection = ConnectionWrapperFactory.wrap(delegateConnection,
                                                       UoWAttachedConnection.class,
                                                       new UoWAttachedConnectionImpl(delegateConnection),
                                                       new ConnectionWrapperFactory.NoOpCloseHandler());
            uow.attachResource(CONNECTION_RESOURCE_NAME, connection, inherited);
            uow.registerListener(new ConnectionManagingUnitOfWorkListenerAdapter());
        }
        return connection;
    }

    private static interface UoWAttachedConnection {

        void forceClose();
    }

    private static class UoWAttachedConnectionImpl implements UoWAttachedConnection {

        private final Connection delegateConnection;

        public UoWAttachedConnectionImpl(Connection delegateConnection) {
            this.delegateConnection = delegateConnection;
        }

        @Override
        public void forceClose() {
            JdbcUtils.closeQuietly(delegateConnection);
        }
    }

    private static class ConnectionManagingUnitOfWorkListenerAdapter extends UnitOfWorkListenerAdapter {

        @Override
        public void afterCommit(UnitOfWork unitOfWork) {
            if (!unitOfWork.isTransactional()) {
                onPrepareTransactionCommit(unitOfWork, null);
            }
        }

        @Override
        public void onPrepareTransactionCommit(UnitOfWork unitOfWork, Object transaction) {
            Connection connection = unitOfWork.getResource(CONNECTION_RESOURCE_NAME);
            try {
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            } catch (SQLException e) {
                throw new JdbcTransactionException("Unable to commit transaction", e);
            }
        }

        @Override
        public void onCleanup(UnitOfWork unitOfWork) {
            Connection connection = unitOfWork.getResource(CONNECTION_RESOURCE_NAME);
            JdbcUtils.closeQuietly(connection);
            if (connection instanceof UoWAttachedConnection) {
                ((UoWAttachedConnection) connection).forceClose();
            }
        }

        @Override
        public void onRollback(UnitOfWork unitOfWork, Throwable failureCause) {
            Connection connection = unitOfWork.getResource(CONNECTION_RESOURCE_NAME);
            try {
                if (!connection.isClosed() && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                throw new JdbcTransactionException("Unable to rollback transaction", e);
            }
        }
    }
}
