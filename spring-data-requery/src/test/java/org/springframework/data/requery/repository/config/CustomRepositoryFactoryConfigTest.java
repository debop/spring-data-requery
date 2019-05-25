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

package org.springframework.data.requery.repository.config;

import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.requery.configs.RequeryTestConfiguration;
import org.springframework.data.requery.repository.custom.CustomGenericRequeryRepository;
import org.springframework.data.requery.repository.custom.CustomGenericRequeryRepositoryFactoryBean;
import org.springframework.data.requery.repository.custom.UserCustomExtendedRepository;
import org.springframework.data.requery.repository.support.TransactionalRepositoryTest.DelegatingTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CustomRepositoryFactoryConfigTest
 *
 * @author debop
 * @since 18. 6. 14
 */

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CustomRepositoryFactoryConfigTest {

    @Configuration
    @EnableRequeryRepositories(//basePackages = { "com.coupang.springframework.data.requery.repository.custom" },
                               basePackageClasses = { CustomGenericRequeryRepository.class },
                               repositoryFactoryBeanClass = CustomGenericRequeryRepositoryFactoryBean.class)
    static class TestConfiguration extends RequeryTestConfiguration {
        @Bean
        @Override
        public DelegatingTransactionManager transactionManager(@Nonnull final EntityDataStore<Object> entityDataStore) {
            return new DelegatingTransactionManager(super.transactionManager(entityDataStore));
        }
    }

    @Autowired(required = false) UserCustomExtendedRepository userRepository;

    @Autowired DelegatingTransactionManager transactionManager;

    @Before
    public void setup() {
        transactionManager.resetCount();
    }

    /**
     * repositoryFactoryBeanClass 를 지정해줘야 UserCustomExtendedRepository 가 제대로 injection이 된다.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testCustomFactoryUsed() {
        userRepository.customMethod(1L);
    }

    @Test
    public void reconfiguresTransactionalMethodWithoutGenericParameter() {

        userRepository.findAll();
        // NOTE: Not support timeout in requery transaction.
//        assertThat(transactionManager.getDefinition().getTimeout()).isEqualTo(100);
        assertThat(transactionManager.getDefinition().isReadOnly()).isTrue();
    }

    @Test
    public void reconfiguresTransactionalMethodWithGenericParameter() {

        userRepository.findById(1L);
        // NOTE: Not support timeout in requery transaction.
//        assertThat(transactionManager.getDefinition().getTimeout()).isEqualTo(10);
        assertThat(transactionManager.getDefinition().isReadOnly()).isTrue();
    }
}
