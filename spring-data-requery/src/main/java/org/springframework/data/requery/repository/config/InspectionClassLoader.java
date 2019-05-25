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

import org.springframework.instrument.classloading.ShadowingClassLoader;

import javax.annotation.Nonnull;

/**
 * Disposable {@link ClassLoader} used to inspect user-code classes within an isolated class loader without preventing
 * class transformation at a later time.
 *
 * @author debop
 * @since 18. 6. 6
 */
public class InspectionClassLoader extends ShadowingClassLoader {

    public InspectionClassLoader(@Nonnull final ClassLoader parent) {
        super(parent, true);
        excludePackage("org.springframework.");
    }
}
