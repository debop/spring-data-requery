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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.annotation.Nonnull;

/**
 * Requery DataStore를 Bean으로 등록한 후 후처리를 수행한다
 *
 * @author debop
 * @since 18. 6. 14
 */
public class DataStoreBeanDefinitionRegistrarPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(@Nonnull final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Nothing to do
    }
}
