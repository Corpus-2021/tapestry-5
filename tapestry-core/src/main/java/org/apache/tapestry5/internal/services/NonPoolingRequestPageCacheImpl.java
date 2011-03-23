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

import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.slf4j.Logger;

public class NonPoolingRequestPageCacheImpl implements RequestPageCache, ThreadCleanupListener
{
    private final Logger logger;

    private final ComponentClassResolver resolver;

    private final PageSource pageSource;

    private final ThreadLocale threadLocale;

    private final Map<String, Page> cache = CollectionFactory.newMap();

    public NonPoolingRequestPageCacheImpl(Logger logger, ComponentClassResolver resolver, PageSource pageSource,
            ThreadLocale threadLocale)
    {
        this.logger = logger;
        this.resolver = resolver;
        this.pageSource = pageSource;
        this.threadLocale = threadLocale;
    }

    public void threadDidCleanup()
    {
        for (Page page : cache.values())
        {
            try
            {
                page.detached();
            }
            catch (Throwable t)
            {
                logger.error(String.format("Error detaching page %s: %s", page, InternalUtils.toMessage(t)), t);
            }
        }
    }

    public Page get(String pageName)
    {
        String canonical = resolver.canonicalizePageName(pageName);

        Page page = cache.get(canonical);

        if (page == null)
        {
            Locale locale = threadLocale.getLocale();

            page = pageSource.getPage(canonical, locale);

            try
            {
                page.attached();
            }
            catch (Throwable t)
            {
                throw new RuntimeException(String.format("Unable to attach page %s (%s): %s", canonical, locale,
                        InternalUtils.toMessage(t)), t);
            }

            cache.put(canonical, page);
        }

        return page;
    }
}
