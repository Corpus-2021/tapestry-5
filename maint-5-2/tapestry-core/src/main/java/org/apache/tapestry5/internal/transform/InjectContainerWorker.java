// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.TransformField;

/**
 * Identifies the {@link org.apache.tapestry5.annotations.InjectContainer} annotation and adds code
 * to initialize it to
 * the core component.
 */
public class InjectContainerWorker implements ComponentClassTransformWorker
{
    private final ComponentClassCache cache;

    public InjectContainerWorker(ComponentClassCache cache)
    {
        this.cache = cache;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (final TransformField field : transformation.matchFieldsWithAnnotation(InjectContainer.class))
        {
            transformField(model, field);
        }
    }

    private void transformField(MutableComponentModel model, TransformField field)
    {
        InjectContainer annotation = field.getAnnotation(InjectContainer.class);

        field.claim(annotation);

        ComponentValueProvider<FieldValueConduit> provider = createFieldValueConduitProvider(field);

        field.replaceAccess(provider);
    }

    private ComponentValueProvider<FieldValueConduit> createFieldValueConduitProvider(TransformField field)
    {

        final String fieldName = field.getName();

        final String fieldTypeName = field.getType();

        return new ComponentValueProvider<FieldValueConduit>()
        {
            public FieldValueConduit get(final ComponentResources resources)
            {
                final Class fieldType = cache.forName(fieldTypeName);

                return new ReadOnlyFieldValueConduit(resources, fieldName)
                {

                    public Object get()
                    {
                        Component container = resources.getContainer();

                        if (!fieldType.isInstance(container))
                        {
                            String message = String.format(
                                    "Component %s is not assignable to field %s.%s (of type %s).", container
                                            .getComponentResources().getCompleteId(), resources.getComponentModel()
                                            .getComponentClassName(), fieldName, fieldTypeName);

                            throw new RuntimeException(message);
                        }

                        return container;
                    }
                };
            }
        };
    }
}
