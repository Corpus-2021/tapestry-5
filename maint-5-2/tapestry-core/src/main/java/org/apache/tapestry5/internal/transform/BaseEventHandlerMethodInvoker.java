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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.MethodAccess;
import org.apache.tapestry5.services.MethodInvocationResult;
import org.apache.tapestry5.services.TransformMethod;

/**
 * Base class for invoking event handler methods that also serves when invoking an
 * event handler method that takes no parameters.
 * 
 * @since 5.2.0
 */
public class BaseEventHandlerMethodInvoker implements EventHandlerMethodInvoker
{
    private final MethodAccess access;

    private final String identifier;

    private final String eventType;

    private final String componentId;

    public BaseEventHandlerMethodInvoker(TransformMethod method, String eventType, String componentId)
    {
        this.eventType = eventType;
        this.componentId = componentId;

        access = method.getAccess();
        identifier = method.getMethodIdentifier();
    }

    public void invokeEventHandlerMethod(ComponentEvent event, Object instance)
    {
        event.setMethodDescription(identifier);

        MethodInvocationResult result = access.invoke(instance, constructParameters(event));

        result.rethrow();

        event.storeResult(result.getReturnValue());
    }

    public String getComponentId()
    {
        return componentId;
    }

    public String getEventType()
    {
        return eventType;
    }

    /**
     * Returns 0 (the event method takes no parameters). Subclasses should override.
     */
    public int getMinContextValueCount()
    {
        return 0;
    }

    /** Overridden in subclasses to provide the actual values to be passed to the method. */
    protected Object[] constructParameters(ComponentEvent event)
    {
        return InternalConstants.EMPTY_STRING_ARRAY;
    }
}
