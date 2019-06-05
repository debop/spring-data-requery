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
public class RequeryTransactionManager extends AbstractRequeryTransactionManager
    implements ResourceTransactionManager, InitializingBean {
    private static final long serialVersionUID = 3291422158479490099L;

    // private EntityDataStore entityDataStore;
    private boolean enforceReadOnly = false;

    public RequeryTransactionManager(@Nonnull final EntityDataStore entityDataStore) {
        super(entityDataStore);
        // this.entityDataStore = entityDataStore;
        // setNestedTransactionAllowed(true);
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
//    @Override
    protected Object doGetTransaction() {
        log.info("doGetTransaction ...");
        RequeryTransactionObject txObject = new RequeryTransactionObject();

        final RequeryTransactionHolder holder =
            (RequeryTransactionHolder) TransactionSynchronizationManager.getResource(entityDataStore);

        if (holder != null) {
            txObject.setTransactionHolder(holder, false);
            log.info("doGetTransaction. transaction={}", txObject.getTransactionHolder().getTransaction());
        }

        return txObject;
    }

    //    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        RequeryTransactionObject txObject = (RequeryTransactionObject) transaction;
        return txObject.hasTransactionHolder() && txObject.getTransactionHolder().isTransactionActive();
    }

    //    @Override
    protected void doBegin(@Nonnull Object transaction, @Nonnull TransactionDefinition definition) {
//        if (enforceReadOnly || definition.isReadOnly()) {
//            return;
//        }
        log.debug("doBegin transaction... definition={}, timeout={}, readOnly={}",
                  definition, definition.getTimeout(), definition.isReadOnly());

        RequeryTransactionObject txObject = (RequeryTransactionObject) transaction;
        try {
            if (!txObject.hasTransactionHolder()) {

                Transaction trans = entityDataStore.transaction().begin();
                log.info("Requery transaction begin... transaction={}, definition={}", trans, definition);
                RequeryTransactionHolder holder = new RequeryTransactionHolder(trans, true);
                txObject.setTransactionHolder(holder);

//                int timeout = determineTimeout(definition);
//                if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
//                    holder.setTimeoutInMillis(timeout);
//                }

                TransactionSynchronizationManager.bindResource(entityDataStore, holder);
            }
        } catch (Throwable ex) {
            if (txObject.isNewTransactionHolder()) {
                txObject.setNewTransactionHolder(false);
            }
            throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", ex);
        }
    }

//    @Override
//    protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
//        super.prepareSynchronization(status, definition);
//        log.info("prepareSynchronization. status={}", status);
//    }

    //    @Override
    protected void doCommit(@Nonnull final DefaultTransactionStatus status) {
        log.debug("doCommit ... status={}", status);
        RequeryTransactionObject txObject = (RequeryTransactionObject) status.getTransaction();
        try {
            log.info("Commit transaction. transaction={}", txObject.getTransactionHolder().getTransaction());
            // txObject.getTransactionHolder().getTransaction().commit();

//            if (txObject.hasTransactionHolder() && txObject.getTransactionHolder().isTransactionActive()) {
//                if (txObject.getTransactionHolder().getTransaction() != null) {
//                    log.info("Commit transaction. transaction={}", txObject.getTransactionHolder().getTransaction());
//                    txObject.getTransactionHolder().getTransaction().commit();
//                }
//            }
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not commit requery transaction", ex);
        }
    }


    //    @Override
    protected void doRollback(@Nonnull final DefaultTransactionStatus status) {
        log.debug("doRollback ... status={}", status);
        RequeryTransactionObject txObject = (RequeryTransactionObject) status.getTransaction();
        try {
            log.warn("Rollback transaction!!! transaction={}", txObject.getTransactionHolder().getTransaction());
            Transaction transaction = txObject.getTransactionHolder().getTransaction();
            transaction.rollback();

//            if (txObject.hasTransactionHolder() && txObject.getTransactionHolder().isTransactionActive()) {
//                if (txObject.getTransactionHolder().getTransaction() != null) {
//                    log.warn("Rollback transaction!!! transaction={}", txObject.getTransactionHolder().getTransaction());
//                    txObject.getTransactionHolder().getTransaction().rollback();
//                }
//            }
        } catch (Exception ex) {
            throw new TransactionSystemException("Could not rollback requery transaction", ex);
        }
    }

    //    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        log.debug("Set rollback only ...");
        RequeryTransactionObject txObject = (RequeryTransactionObject) status.getTransaction();
        txObject.setRollbackOnly();
    }


    @Nonnull
//    @Override
    protected Object doSuspend(@Nonnull final Object transaction) {
        log.debug("Suspend transaction. transaction={}", transaction);
        return TransactionSynchronizationManager.unbindResource(entityDataStore);
    }

    //    @Override
    protected void doResume(@Nullable final Object transaction,
                            @Nonnull final Object suspendedResources) {
        log.debug("Resume transaction. transaction={}, suspendedResources={}", transaction, suspendedResources);
        TransactionSynchronizationManager.bindResource(entityDataStore, suspendedResources);
    }

    //    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        log.warn("doCleanupAfterCompletion... ");
        RequeryTransactionObject txObject = (RequeryTransactionObject) transaction;

        if (txObject.isNewTransactionHolder() || txObject.getTransactionHolder().isRollbackOnly()) {
            TransactionSynchronizationManager.unbindResource(entityDataStore);

            log.warn("Cleanup transaction ...");
            txObject.getTransactionHolder().getTransaction().rollback();
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
        private boolean mustRestoreAutoCommit;


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
    }
}
