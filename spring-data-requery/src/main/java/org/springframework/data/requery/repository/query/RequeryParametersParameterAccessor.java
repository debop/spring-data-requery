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

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.ParametersParameterAccessor;

import javax.annotation.Nonnull;

/**
 * This class provides access to parameters of a user-defined queryMethod. It wraps ParametersParameterAccessor which catches
 * special parameters Sort and Pageable, and catches Arango-specific parameters e.g. AqlQueryOptions.
 *
 * @author debop
 * @since 18. 6. 8
 */
@Slf4j
public class RequeryParametersParameterAccessor extends ParametersParameterAccessor implements RequeryParameterAccessor {

    @Nonnull
    private final RequeryParameters parameters;

    public RequeryParametersParameterAccessor(@Nonnull final RequeryQueryMethod method,
                                              @Nonnull final Object[] values) {
        this(method.getParameters(), values);
    }

    public RequeryParametersParameterAccessor(@Nonnull final RequeryParameters parameters,
                                              @Nonnull final Object[] values) {
        super(parameters, values);
        this.parameters = parameters;
    }

    @Override
    @Nonnull
    public RequeryParameters getParameters() {
        return parameters;
    }
}

