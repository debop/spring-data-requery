/*
 * Copyright 2018 Coupang Ltd.
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

package org.springframework.data.requery.repository.support;

import io.requery.sql.EntityDataStore;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.requery.configs.RequeryTestConfiguration;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.RandomData;
import org.springframework.data.requery.domain.basic.BasicUser;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.data.requery.repository.sample.basic.BasicUserRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration
@Transactional
public class TransactionalRepositoryTest {

    @Configuration
    @EnableRequeryRepositories(basePackageClasses = { BasicUserRepository.class })
    @EnableTransactionManagement
    static class TestConfiguration extends RequeryTestConfiguration {

        @Bean
        @Override
        public DelegatingTransactionManager transactionManager(@Nonnull final EntityDataStore<Object> entityDataStore) {
            return new DelegatingTransactionManager(super.transactionManager(entityDataStore));
        }
    }

    @Autowired BasicUserRepository repository;
    @Autowired DelegatingTransactionManager transactionManager;
    @Autowired RequeryOperations operations;

    @Before
    public void setup() {
        transactionManager.resetCount();
    }

    @Test
    public void transaction_rollback() {
        try {
            runForRollback();
        } catch (Exception e) {
            log.error("Should rollback !!!");
        }
        assertThat(repository.findAll().size()).isZero();
    }

    @Transactional
    void runForRollback() {
        repository.save(RandomData.randomUser());
        repository.save(null);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    public void requery_transaction_rollback() {
        EntityDataStore<Object> dataStore = operations.getDataStore();

        try {
            dataStore.runInTransaction(() -> {
                dataStore.insert(RandomData.randomUser());
                dataStore.insert((BasicUser) null);
                return null;
            });
        } catch (Exception e) {
            log.warn("Rollbacked!!!");
        }

        assertThat(dataStore.count(BasicUser.class).get().value()).isEqualTo(0);
    }

    @Test
    public void simpleManipulatingOperation() {
        repository.save(RandomData.randomUser());
        assertThat(transactionManager.getTransactionRequests()).isGreaterThan(0);
    }

    @Test
    public void unannotatedFinder() {
        repository.findByEmail("foo@bar.kr");
        assertThat(transactionManager.getTransactionRequests()).isGreaterThan(0);
    }

    @Test
    public void invokeTransactionalFinder() {
        repository.findByAnnotatedQuery("foo@bar.kr");
        assertThat(transactionManager.getTransactionRequests()).isGreaterThan(0);
    }

    @Test
    public void invokeRedeclaredMethod() {
        repository.findById(1L);
        assertThat(transactionManager.getDefinition().isReadOnly()).isTrue();
    }

    @Getter
    @Slf4j
    public static class DelegatingTransactionManager implements PlatformTransactionManager {

        private final PlatformTransactionManager txManager;
        private int transactionRequests;
        private TransactionDefinition definition;
        private TransactionStatus status;

        public DelegatingTransactionManager(PlatformTransactionManager txManager) {
            this.txManager = txManager;
        }

        @Override
        @Nonnull
        public TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException {
            this.transactionRequests++;
            this.definition = definition;

            log.info("Get transaction. transactionRequests={}, definition={}", transactionRequests, definition);

            status = txManager.getTransaction(definition);
            return status;
        }

        @Override
        public void commit(@Nonnull final TransactionStatus status) throws TransactionException {
            DefaultTransactionStatus txStatus = (DefaultTransactionStatus) status;
//            RequeryTransactionObject txObject = (RequeryTransactionObject)txStatus.getTransaction();

            log.warn("Commit transaction. status transaction={}, isNewTransaction={}", txStatus.getTransaction(), txStatus.isNewTransaction());
            txManager.commit(status);
        }

        @Override
        public void rollback(@Nonnull final TransactionStatus status) throws TransactionException {
            DefaultTransactionStatus txStatus = (DefaultTransactionStatus) status;
//            RequeryTransactionObject txObject = (RequeryTransactionObject)txStatus.getTransaction();
            log.warn("Rollback transaction. status transaction={}, isNewTransaction={}", txStatus.getTransaction(), txStatus.isNewTransaction());
            txManager.rollback(status);
        }

        public void resetCount() {
            log.info("Reset transaction request.");
            this.transactionRequests = 0;
            this.definition = null;
        }
    }
}
