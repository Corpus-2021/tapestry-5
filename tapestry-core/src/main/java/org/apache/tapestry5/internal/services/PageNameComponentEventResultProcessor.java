// Copyright 2006, 2007, 2008, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Traditional;

import java.io.IOException;

/**
 * Used when a component event handler returns a string value. The value is interpreted as the logical name of a page. A
 * link to the page will be sent as a redirect.
 */
public class PageNameComponentEventResultProcessor implements ComponentEventResultProcessor<String>
{
    private final LinkSource linkSource;

    private final ComponentEventResultProcessor primary;

    public PageNameComponentEventResultProcessor(LinkSource linkSource,
                                                 @Traditional @Primary
                                                 ComponentEventResultProcessor primary)
    {
        this.linkSource = linkSource;
        this.primary = primary;
    }

    public void processResultValue(String value) throws IOException
    {
        Link link = linkSource.createPageRenderLink(value, false);

        primary.processResultValue(link);
    }
}
