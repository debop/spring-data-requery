package org.springframework.data.requery.core;

import io.requery.Transaction;
import io.requery.TransactionIsolation;
import io.requery.sql.EntityDataStore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.NoTransactionException;
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

    public Transaction getCurrentTransaction() {
        RequeryTransactionHolder to = (RequeryTransactionHolder) TransactionSynchronizationManager.getResource(entityDataStore);
        if (to == null) {
            throw new NoTransactionException("No transaction is available for the current thread");
        }
        return to.getTransaction();
    }

    @Nonnull
    @Override
    protected Object doGetTransaction() {
        log.info("doGetTransaction ...");
        RequeryTransactionObject txObject = new RequeryTransactionObject();

        final RequeryTransactionHolder holder =
            (RequeryTransactionHolder) TransactionSynchronizationManager.getResource(entityDataStore);

        if (holder != null) {
            log.debug("Hodler exists. hodler={}", holder);
            txObject.setTransactionHolder(holder, false);

            if (txObject.getTransactionHolder().hasTransaction()) {
                log.info("doGetTransaction. transaction={}", txObject.getTransactionHolder().getTransaction());
            }
        }

        return txObject;
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        RequeryTransactionObject txObject = (RequeryTransactionObject) transaction;
        return txObject.hasTransactionHolder() && txObject.getTransactionHolder().hasTransaction();
    }

    @Override
    protected void doBegin(@Nonnull Object transaction, @Nonnull TransactionDefinition definition) {
        log.info("doBegin transaction... definition={}, timeout={}, readOnly={}",
                 definition, definition.getTimeout(), definition.isReadOnly());

        RequeryTransactionObject txObject = (RequeryTransactionObject) transaction;
        try {
            if (!txObject.hasTransactionHolder()) {

                TransactionIsolation isolation = getTransactionIsolation(definition.getIsolationLevel());
                Transaction trans = entityDataStore.transaction().begin(isolation);

                log.info("Requery transaction begin... transaction={}, definition={}", trans, definition);
                RequeryTransactionHolder holder = new RequeryTransactionHolder(trans, true);
                txObject.setTransactionHolder(holder);

                int timeout = determineTimeout(definition);
                if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                    holder.setTimeoutInMillis(timeout);
                }

                TransactionSynchronizationManager.bindResource(entityDataStore, holder);
            }
        } catch (Throwable ex) {
            if (txObject.isNewTransactionHolder()) {
                txObject.setNewTransactionHolder(false);
            }
            throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", ex);
        }
    }

    @Override
    protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
        super.prepareSynchronization(status, definition);
        log.info("prepareSynchronization. status={}, definition={}", status, definition);
    }

    @Override
    protected void doCommit(@Nonnull final DefaultTransactionStatus status) {
        log.info("doCommit ... status={}", status);

        // NOTE: New transaction 이 아니면 commit 하지 않고, Skip 해야 한다.
        RequeryTransactionObject txObject = (RequeryTransactionObject) status.getTransaction();
        log.info("doCommit ... txObject={}", txObject);
        if (txObject.isRollbackOnly()) {
            return;
        }
        try {
            if (txObject.isNewTransactionHolder()) {
                if (txObject.hasTransactionHolder() && txObject.getTransactionHolder().isTransactionActive()) {
                    log.info("Commit transaction. transaction={}", txObject.getTransactionHolder().getTransaction());
                    txObject.getTransactionHolder().getTransaction().commit();
                }
            }
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not commit requery transaction", ex);
        }
    }


    @Override
    protected void doRollback(@Nonnull final DefaultTransactionStatus status) {
        log.info("doRollback ... status={}", status);
        RequeryTransactionObject txObject = (RequeryTransactionObject) status.getTransaction();
        log.info("doRollback ... txObject={}", txObject);
        try {
            if (txObject.hasTransactionHolder() && txObject.getTransactionHolder().isTransactionActive()) {
                if (txObject.getTransactionHolder().getTransaction() != null) {
                    log.warn("Rollback transaction!!! transaction={}", txObject.getTransactionHolder().getTransaction());
                    txObject.getTransactionHolder().getTransaction().rollback();
                }
            }
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not rollback requery transaction", ex);
        }
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        log.info("Set rollback only ...");
        RequeryTransactionObject txObject = (RequeryTransactionObject) status.getTransaction();
        txObject.setRollbackOnly();
    }

    @Nonnull
    @Override
    protected Object doSuspend(@Nonnull final Object transaction) {
        log.info("Suspend transaction. transaction={}", transaction);
        return TransactionSynchronizationManager.unbindResource(entityDataStore);
    }

    @Override
    protected void doResume(@Nullable final Object transaction,
                            @Nonnull final Object suspendedResources) {
        log.info("Resume transaction. transaction={}, suspendedResources={}", transaction, suspendedResources);
        TransactionSynchronizationManager.bindResource(entityDataStore, suspendedResources);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        log.warn("doCleanupAfterCompletion... ");
        RequeryTransactionObject txObject = (RequeryTransactionObject) transaction;

        if (txObject.getTransactionHolder().hasTransaction()) {
            TransactionSynchronizationManager.unbindResourceIfPossible(entityDataStore);

            log.warn("Cleanup transaction ...");
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
    public static class RequeryTransactionObject extends RequeryTransactionObjectSupport {

        private boolean newTransactionHolder;

        void setTransactionHolder(RequeryTransactionHolder transactionHolder, boolean isNewTransactionHolder) {
            setTransactionHolder(transactionHolder);
            this.newTransactionHolder = isNewTransactionHolder;
        }

        @Override
        public boolean isRollbackOnly() {
            return hasTransactionHolder() && getTransactionHolder().isRollbackOnly();
        }

        void setRollbackOnly() {
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

        @Override
        public String toString() {
            return "newTransactionHolder=" + newTransactionHolder + ",isRollbackOnly=" + isRollbackOnly();
        }
    }
}
