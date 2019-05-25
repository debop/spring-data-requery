package org.springframework.data.requery.core;

import io.requery.TransactionIsolation;
import io.requery.sql.EntityDataStore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;

/**
 * Requery용 {@link DataSourceTransactionManager}
 *
 * @author debop
 * @since 18. 6. 14
 */
@Slf4j
public class RequeryTransactionManager extends AbstractPlatformTransactionManager
    implements ResourceTransactionManager, InitializingBean {
    private static final long serialVersionUID = 3291422158479490099L;

    private EntityDataStore entityDataStore;
    private boolean enforceReadOnly = false;

    public RequeryTransactionManager() { }

    public RequeryTransactionManager(@Nonnull final EntityDataStore entityDataStore) {
        this.entityDataStore = entityDataStore;
        setNestedTransactionAllowed(true);
        afterPropertiesSet();
    }

    public void setEnforceReadOnly(boolean enforceReadOnly) {
        this.enforceReadOnly = enforceReadOnly;
    }

    public boolean isEnforceReadOnly() {
        return this.enforceReadOnly;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.entityDataStore == null) {
            throw new IllegalArgumentException("Property 'entityDataStore' is required");
        }
    }

    @Nonnull
    @Override
    public Object getResourceFactory() {
        return entityDataStore;
    }

    @Nonnull
    @Override
    protected Object doGetTransaction() {
        log.debug("Get requery transaction object.");
        RequeryTransactionObject txObject = new RequeryTransactionObject();

        final TransactionHolder transaction =
            (TransactionHolder) TransactionSynchronizationManager.getResource(entityDataStore);

        txObject.setTransactionHolder(transaction, false);
        return txObject;
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        RequeryTransactionObject txObject = (RequeryTransactionObject) transaction;
        return txObject.hasTransactionHolder() && txObject.getTransactionHolder().isTransactionActive();
    }

    @Override
    protected void doBegin(@Nonnull final Object transaction, @Nonnull final TransactionDefinition definition) {
        if (enforceReadOnly || definition.isReadOnly()) {
            return;
        }
        log.debug("Begin transaction... definition={}, timeout={}, readOnly={}",
                  definition, definition.getTimeout(), definition.isReadOnly());

        RequeryTransactionObject txObject = (RequeryTransactionObject) transaction;
        try {
            if (!txObject.hasTransactionHolder()) {
                txObject.setTransactionHolder(new TransactionHolder(entityDataStore.transaction()), true);
            }

            int timeout = determineTimeout(definition);
            if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                txObject.getTransactionHolder().setTimeoutInSeconds(timeout);
            }
            if (txObject.isNewTransactionHolder()) {
                txObject.getTransactionHolder().getCurrentTransaction().begin();
                TransactionSynchronizationManager.bindResource(entityDataStore, txObject.getTransactionHolder());
            }
        } catch (Throwable ex) {
            if (txObject.isNewTransactionHolder()) {
                txObject.setNewTransactionHolder(false);
            }
            throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", ex);
        }
    }

    @Nonnull
    @Override
    protected Object doSuspend(@Nonnull final Object transaction) {
        log.debug("Suspend transaction. transaction={}", transaction);
        return TransactionSynchronizationManager.unbindResource(entityDataStore);
    }

    @Override
    protected void doResume(@Nullable final Object transaction,
                            @Nonnull final Object suspendedResources) {
        log.debug("Resume transaction. transaction={}, suspendedResources={}", transaction, suspendedResources);
        TransactionSynchronizationManager.bindResource(entityDataStore, suspendedResources);
    }

    @Override
    protected void doCommit(@Nonnull final DefaultTransactionStatus status) {
        try {
            RequeryTransactionObject txObject = (RequeryTransactionObject) status.getTransaction();
            if (txObject.hasTransactionHolder() && txObject.getTransactionHolder().isTransactionActive()) {
                log.info("Commit transaction. transaction={}", txObject.getTransactionHolder().getCurrentTransaction());
                txObject.getTransactionHolder().getCurrentTransaction().commit();
            }
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not commit requery transaction", ex);
        }
    }

    @Override
    protected void doRollback(@Nonnull final DefaultTransactionStatus status) {
        try {
            RequeryTransactionObject txObject = (RequeryTransactionObject) status.getTransaction();
            if (txObject.hasTransactionHolder() && txObject.getTransactionHolder().isTransactionActive()) {
                log.warn("Rollback transaction!!! transaction={}", txObject.getTransactionHolder().getCurrentTransaction());
                txObject.getTransactionHolder().getCurrentTransaction().rollback();
            }
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not rollback requery transaction", ex);
        }
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        log.debug("Set rollback only ...");
        RequeryTransactionObject txObject = (RequeryTransactionObject) status.getTransaction();
        txObject.setRollbackOnly();
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        RequeryTransactionObject txObject = (RequeryTransactionObject) transaction;

        if (txObject.isNewTransactionHolder()) {
            TransactionSynchronizationManager.unbindResource(entityDataStore);
        }
        if (txObject.isNewTransactionHolder()) {
            txObject.getTransactionHolder().released();
            txObject.getTransactionHolder().clear();
        }
    }

    private @Nullable
    TransactionIsolation getTransactionIsolation(int isolationLevel) {
        switch (isolationLevel) {
            case Connection.TRANSACTION_NONE:
                return TransactionIsolation.NONE;
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                return TransactionIsolation.READ_UNCOMMITTED;
            case Connection.TRANSACTION_READ_COMMITTED:
                return TransactionIsolation.READ_COMMITTED;
            case Connection.TRANSACTION_REPEATABLE_READ:
                return TransactionIsolation.REPEATABLE_READ;
            case Connection.TRANSACTION_SERIALIZABLE:
                return TransactionIsolation.SERIALIZABLE;

            default:
                return null;
        }
    }

    @Getter
    @Setter
    private static class RequeryTransactionObject extends RequeryTransactionObjectSupport {

        private boolean newTransactionHolder;
        private boolean mustRestoreAutoCommit;


        public void setTransactionHolder(TransactionHolder transactionHolder, boolean isNewTransactionHolder) {
            super.setTransactionHolder(transactionHolder);
            this.newTransactionHolder = isNewTransactionHolder;
        }

        @Override
        public boolean isRollbackOnly() {
            return hasTransactionHolder() && getTransactionHolder().isRollbackOnly();
        }

        public void setRollbackOnly() {
            if (hasTransactionHolder()) {
                getTransactionHolder().setRollbackOnly();
            }
        }

        @Override
        public void flush() {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationUtils.triggerFlush();
            }
        }
    }
}
