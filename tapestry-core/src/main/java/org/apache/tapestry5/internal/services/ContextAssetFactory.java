// Copyright 2006, 2007, 2008, 2009, 2010, 2013 The Apache Software Foundation
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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.AssetPathConverter;
import org.apache.tapestry5.services.Context;
import org.apache.tapestry5.services.assets.AssetPathConstructor;

import java.io.IOException;

/**
 * Implementation of {@link AssetFactory} for assets that are part of the web application context.
 *
 * @see org.apache.tapestry5.internal.services.ContextResource
 */
public class ContextAssetFactory extends AbstractAssetFactory
{
    private final AssetPathConstructor assetPathConstructor;

    private final Resource rootResource;

    private final AssetPathConverter converter;

    private final boolean invariant;

    public ContextAssetFactory(AssetPathConstructor assetPathConstructor, Context context,

                               AssetPathConverter converter)
    {
        this.assetPathConstructor = assetPathConstructor;
        this.converter = converter;

        rootResource = new ContextResource(context, "/");
        invariant = this.converter.isInvariant();
    }

    public Asset createAsset(Resource resource)
    {
        if (invariant)
        {
            return createInvariantAsset(resource);
        }

        return createVariantAsset(resource);
    }

    private String defaultPath(Resource resource)
    {
        try
        {
            return assetPathConstructor.constructAssetPath(RequestConstants.CONTEXT_FOLDER, resource.getPath(), resource);
        } catch (IOException ex)
        {
            throw new RuntimeException(String.format("Unable to construct asset path for %s: %s",
                    resource, InternalUtils.toMessage(ex)), ex);
        }
    }

    private Asset createInvariantAsset(final Resource resource)
    {
        return new AbstractAsset(true)
        {
            private final Invokable<String> clientURL = recomputable.create(new Invokable<String>()
            {
                @Override
                public String invoke()
                {
                    return converter.convertAssetPath(defaultPath(resource));
                }
            });

            public Resource getResource()
            {
                return resource;
            }

            public String toClientURL()
            {
                return clientURL.invoke();
            }
        };
    }

    private Asset createVariantAsset(final Resource resource)
    {
        return new AbstractAsset(false)
        {
            private final Invokable<String> defaultPath = recomputable.create(new Invokable<String>()
            {
                @Override
                public String invoke()
                {
                    return defaultPath(resource);
                }
            });

            public Resource getResource()
            {
                return resource;
            }

            public String toClientURL()
            {
                return converter.convertAssetPath(defaultPath.invoke());
            }
        };
    }

    /**
     * Returns the root {@link org.apache.tapestry5.internal.services.ContextResource}.
     */
    public Resource getRootResource()
    {
        return rootResource;
    }
}
