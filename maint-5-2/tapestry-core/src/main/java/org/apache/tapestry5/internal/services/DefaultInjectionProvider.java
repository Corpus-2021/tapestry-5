// Copyright 2006, 2007, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.services.MasterObjectProvider;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.InjectionProvider;
import org.apache.tapestry5.services.TransformField;

/**
 * Worker for the {@link org.apache.tapestry5.ioc.annotations.Inject} annotation that delegates out to the master
 * {@link org.apache.tapestry5.ioc.services.MasterObjectProvider} to access the value. This worker must be scheduled
 * after certain other workers, such as {@link BlockInjectionProvider} (which is keyed off a combination of type and
 * the Inject annotation).
 * 
 * @see org.apache.tapestry5.ioc.services.MasterObjectProvider
 */
public class DefaultInjectionProvider implements InjectionProvider
{
    private final MasterObjectProvider masterObjectProvider;

    private final ObjectLocator locator;

    public DefaultInjectionProvider(MasterObjectProvider masterObjectProvider, ObjectLocator locator)
    {
        this.masterObjectProvider = masterObjectProvider;
        this.locator = locator;
    }

    @SuppressWarnings("unchecked")
    public boolean provideInjection(String fieldName, Class fieldType, ObjectLocator locator,
            final ClassTransformation transformation, MutableComponentModel componentModel)
    {
        // I hate special cases, but we have a conflict between the ObjectProvider contributed so as to inject
        // the global application messages into services, and the injection of per-component Messages into components.
        // For yet other reasons, this InjectionProvider gets invoked before CommonResources, and will attempt
        // to inject the wrong Messages (the global application messages, not the component messages) ... so we
        // make a special check here.

        if (fieldType.equals(Messages.class))
            return false;

        TransformField field = transformation.getField(fieldName);

        Object injectionValue = masterObjectProvider.provide(fieldType, field, this.locator, false);

        // Null means that no ObjectProvider could provide the value. We have set up the chain of
        // command so that InjectResources can give it a try next. Later, we'll try to match against
        // a service.

        if (injectionValue != null)
        {
            field.inject(injectionValue);
            return true;
        }

        return false;
    }
}
