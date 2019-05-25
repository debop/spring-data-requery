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

package org.springframework.data.requery.repository.query;

import org.springframework.data.repository.query.ParameterAccessor;

import javax.annotation.Nonnull;

/**
 * Interface to access method parameters. Allows dedicated access to parameters of special types
 *
 * @author debop
 * @since 18. 6. 8
 */
public interface RequeryParameterAccessor extends ParameterAccessor {

    @Nonnull
    RequeryParameters getParameters();

}
