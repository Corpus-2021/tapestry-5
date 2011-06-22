// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.assets.AssetPathConstructor;

public class ClasspathAssetAliasManagerImpl implements ClasspathAssetAliasManager
{
    private final AssetPathConstructor assetPathConstructor;

    /**
     * Map from alias to path.
     */
    private final Map<String, String> aliasToPathPrefix = CollectionFactory.newMap();

    /**
     * Map from path to alias.
     */
    private final Map<String, String> pathPrefixToAlias = CollectionFactory.newMap();

    private final List<String> sortedAliases;

    private final List<String> sortedPathPrefixes;

    /**
     * Configuration is a map of aliases (short names) to complete names. Keys and values should end with a slash, but
     * one will be provided as necessary, so don't both.
     */
    public ClasspathAssetAliasManagerImpl(AssetPathConstructor assetPathConstructor,

    Map<String, String> configuration)
    {
        this.assetPathConstructor = assetPathConstructor;

        for (Map.Entry<String, String> e : configuration.entrySet())
        {
            String alias = withOutSlash(e.getKey());

            if (alias.contains("/"))
                throw new RuntimeException(String.format(
                        "Virtual folder names (for component libraries) may no longer contain slashes as of Tapestry 5.2. "
                                + "You must change the ComponentClassAsssetAliasManager contribution for '%s'.", alias));

            String path = withOutSlash(e.getValue());

            aliasToPathPrefix.put(alias, path);
            pathPrefixToAlias.put(path, alias);

        }

        Comparator<String> sortDescendingByLength = new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                return o2.length() - o1.length();
            }
        };

        sortedAliases = CollectionFactory.newList(aliasToPathPrefix.keySet());
        Collections.sort(sortedAliases, sortDescendingByLength);

        sortedPathPrefixes = CollectionFactory.newList(aliasToPathPrefix.values());
        Collections.sort(sortedPathPrefixes, sortDescendingByLength);
    }

    private String withOutSlash(String input)
    {
        if (input.endsWith("/"))
            return input.substring(0, input.length() - 1);

        return input;
    }

    public String toClientURL(String resourcePath)
    {
        for (String pathPrefix : sortedPathPrefixes)
        {
            if (resourcePath.startsWith(pathPrefix))
            {
                String virtualFolder = pathPrefixToAlias.get(pathPrefix);

                String virtualPath = resourcePath.substring(pathPrefix.length() + 1);

                return assetPathConstructor.constructAssetPath(virtualFolder, virtualPath);
            }
        }

        // This is a minor misuse of the UnknownValueException but the exception reporting
        // is too useful to pass up.

        throw new UnknownValueException(
                String
                        .format(
                                "Unable to create a client URL for classpath resource %s: The resource path was not within an aliased path.",
                                resourcePath), new AvailableValues("Aliased paths", aliasToPathPrefix.values()));
    }

    public String toResourcePath(String clientURL)
    {
        // Include the slash in the base path

        String basePath = clientURL.substring(RequestConstants.ASSET_PATH_PREFIX.length());

        for (String alias : sortedAliases)
        {
            if (basePath.startsWith(alias)) { return aliasToPathPrefix.get(alias) + "/"
                    + basePath.substring(alias.length() + 1); }
        }

        return basePath;
    }

    public Map<String, String> getMappings()
    {
        return Collections.unmodifiableMap(aliasToPathPrefix);
    }

}
