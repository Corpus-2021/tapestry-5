// Copyright 2010 The Apache Software Foundation
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

import java.util.Locale;
import java.util.Map;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;

/**
 * Allows for injection of the global application message catalog into services. The injected value
 * is, in fact, a proxy. Each method access of the proxy will determine the current thread's locale, and delegate
 * to the actual global message catalog for that particular locale. There's caching to keep it reasonably
 * efficient.
 * 
 * @since 5.2.0
 * @see ComponentMessagesSource#getApplicationCatalog(Locale)
 */
public class ApplicationMessageCatalogObjectProvider implements ObjectProvider, InvalidationListener
{
    private final ObjectLocator objectLocator;

    private ComponentMessagesSource messagesSource;

    private ThreadLocale threadLocale;

    private final Map<Locale, Messages> localeToMessages = CollectionFactory.newMap();

    private Messages proxy;

    private class ApplicationMessagesObjectCreator implements ObjectCreator
    {
        public Object createObject()
        {
            Locale locale = threadLocale.getLocale();

            Messages messages = localeToMessages.get(locale);

            if (messages == null)
            {
                messages = messagesSource.getApplicationCatalog(locale);
                localeToMessages.put(locale, messages);
            }

            return messages;
        }
    };

    public ApplicationMessageCatalogObjectProvider(ObjectLocator locator)
    {
        this.objectLocator = locator;
    }

    /**
     * Because this is an ObjectProvider and is part of the MasterObjectProvider pipeline, it has to
     * be careful to not require further dependencies at construction time. This means we have to "drop out"
     * of normal IoC dependency injection and adopt a lookup strategy based on the ObjectLocator. Further,
     * we have to be careful about multi-threading issues.
     */
    private synchronized Messages getProxy()
    {
        if (proxy == null)
        {
            this.messagesSource = objectLocator.getService(ComponentMessagesSource.class);
            this.threadLocale = objectLocator.getService(ThreadLocale.class);

            ClassFactory classFactory = objectLocator.getService("ClassFactory", ClassFactory.class);

            proxy = classFactory.createProxy(Messages.class, new ApplicationMessagesObjectCreator(),
                    "<ApplicationMessagesProxy>");

            // Listen for invalidations; clear our cache of localized Messages bundles when
            // and invalidation occurs.

            messagesSource.getInvalidationEventHub().addInvalidationListener(this);
        }

        return proxy;
    }

    public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
    {
        if (objectType.equals(Messages.class))
            return objectType.cast(getProxy());

        return null;
    }

    public void objectWasInvalidated()
    {
        localeToMessages.clear();
    }

}
