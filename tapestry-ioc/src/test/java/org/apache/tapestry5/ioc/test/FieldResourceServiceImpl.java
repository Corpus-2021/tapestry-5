// Copyright 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.test;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.annotations.InjectResource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FieldResourceServiceImpl implements FieldResourceService
{
    @InjectResource
    private ServiceResources resources;

    @InjectResource
    private Collection<String> configuration;

    @Override
    public String getServiceId()
    {
        return resources.getServiceId();
    }

    @Override
    public List<String> getLabels()
    {
        List<String> result = CollectionFactory.newList(configuration);

        Collections.sort(result);

        return result;
    }
}
