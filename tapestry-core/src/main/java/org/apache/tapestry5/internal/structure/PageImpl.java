// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.structure;

import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.services.PersistentFieldManager;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.PersistentFieldBundle;
import org.slf4j.Logger;

public class PageImpl implements Page
{
    private final String name;

    private final Locale locale;

    private final PersistentFieldManager persistentFieldManager;

    private ComponentPageElement rootElement;

    private final List<PageLifecycleListener> lifecycleListeners = CollectionFactory.newThreadSafeList();

    private final List<PageResetListener> resetListeners = CollectionFactory.newList();

    private final PerThreadValue<Integer> dirtyCount;

    private boolean loadComplete;

    private final OneShotLock lock = new OneShotLock();

    /**
     * Obtained from the {@link org.apache.tapestry5.internal.services.PersistentFieldManager} when
     * first needed,
     * discarded at the end of the request.
     */
    private final PerThreadValue<PersistentFieldBundle> fieldBundle;

    /**
     * @param name
     *            canonicalized page name
     * @param locale
     *            locale for page and all components
     * @param persistentFieldManager
     *            for access to cross-request persistent values
     * @param perThreadManager
     *            for managing per-request mutable state
     * @param pooled
     *            if pooling enabled, or is this page a singleton
     */
    public PageImpl(String name, Locale locale, PersistentFieldManager persistentFieldManager,
            PerthreadManager perThreadManager, boolean pooled)
    {
        this.name = name;
        this.locale = locale;
        this.persistentFieldManager = persistentFieldManager;

        if (pooled)
        {
            dirtyCount = perThreadManager.createValue();
        }
        else
        {
            dirtyCount = null;
        }

        fieldBundle = perThreadManager.createValue();
    }

    @Override
    public String toString()
    {
        return String.format("Page[%s %s]", name, locale);
    }

    public ComponentPageElement getComponentElementByNestedId(String nestedId)
    {
        assert nestedId != null;
        ComponentPageElement element = rootElement;

        if (InternalUtils.isNonBlank(nestedId))
        {
            for (String id : nestedId.split("\\."))
                element = element.getEmbeddedElement(id);
        }

        return element;
    }

    public Locale getLocale()
    {
        return locale;
    }

    public void setRootElement(ComponentPageElement component)
    {
        lock.check();

        rootElement = component;
    }

    public ComponentPageElement getRootElement()
    {
        return rootElement;
    }

    public Component getRootComponent()
    {
        return rootElement.getComponent();
    }

    public void addLifecycleListener(PageLifecycleListener listener)
    {
        lock.check();

        lifecycleListeners.add(listener);
    }

    public void removeLifecycleListener(PageLifecycleListener listener)
    {
        lock.check();

        lifecycleListeners.remove(listener);
    }

    public boolean detached()
    {
        boolean result = dirtyCount != null && dirtyCount.exists() && dirtyCount.get() > 0;

        for (PageLifecycleListener listener : lifecycleListeners)
        {
            try
            {
                listener.containingPageDidDetach();
            }
            catch (RuntimeException ex)
            {
                getLogger().error(StructureMessages.detachFailure(listener, ex), ex);
                result = true;
            }
        }

        return result;
    }

    public void loaded()
    {
        lock.check();

        for (PageLifecycleListener listener : lifecycleListeners)
            listener.containingPageDidLoad();

        loadComplete = true;

        lock.lock();
    }

    public void attached()
    {
        if (dirtyCount != null && dirtyCount.exists() && !dirtyCount.get().equals(0))
            throw new IllegalStateException(StructureMessages.pageIsDirty(this));

        for (PageLifecycleListener listener : lifecycleListeners)
            listener.restoreStateBeforePageAttach();

        for (PageLifecycleListener listener : lifecycleListeners)
            listener.containingPageDidAttach();
    }

    public Logger getLogger()
    {
        return rootElement.getLogger();
    }

    public void persistFieldChange(ComponentResources resources, String fieldName, Object newValue)
    {
        if (!loadComplete)
            throw new RuntimeException(StructureMessages.persistChangeBeforeLoadComplete());

        persistentFieldManager.postChange(name, resources, fieldName, newValue);
    }

    public Object getFieldChange(String nestedId, String fieldName)
    {
        if (!fieldBundle.exists())
            fieldBundle.set(persistentFieldManager.gatherChanges(name));

        return fieldBundle.get().getValue(nestedId, fieldName);
    }

    public void decrementDirtyCount()
    {
        if (dirtyCount != null)
        {
            int newCount = dirtyCount.get() - 1;

            dirtyCount.set(newCount);
        }
    }

    public void discardPersistentFieldChanges()
    {
        persistentFieldManager.discardChanges(name);
    }

    public void incrementDirtyCount()
    {
        if (dirtyCount != null)
        {
            int newCount = dirtyCount.get(0) + 1;

            dirtyCount.set(newCount);
        }
    }

    public String getName()
    {
        return name;
    }

    public void addResetListener(PageResetListener listener)
    {
        assert listener != null;
        lock.check();

        resetListeners.add(listener);
    }

    public void pageReset()
    {
        for (PageResetListener l : resetListeners)
        {
            l.containingPageDidReset();
        }
    }

    public boolean hasResetListeners()
    {
        return !resetListeners.isEmpty();
    }

}
