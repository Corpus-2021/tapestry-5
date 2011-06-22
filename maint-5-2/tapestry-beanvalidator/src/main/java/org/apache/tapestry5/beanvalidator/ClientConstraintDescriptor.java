// Copyright 2009 The Apache Software Foundation
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
package org.apache.tapestry5.beanvalidator;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newSet;

import java.util.Set;

import org.apache.tapestry5.json.JSONObject;

/**
 * Describes a single client-side constraint.
 *
 */
public final class ClientConstraintDescriptor
{
   private final Class annotationClass;
   private final String validatorName;
   private final Set<String> attributes;

   /**
    * Creates a {@link ClientConstraintDescriptor}.
    * 
    * @param annotationClass Type of the constraint annotation
    * @param validatorName Name of the client-side validator
    * @param attributes Attribute names of the constraint annotation to be passed (along with their values) to the JavaScript validator 
    * function as an {@link JSONObject}.
    */
   public ClientConstraintDescriptor(final Class annotationClass,
         final String validatorName, final String... attributes) 
   {
     this.annotationClass = annotationClass;
     this.validatorName = validatorName;
     this.attributes = newSet(attributes);
   }
   
   /**
    * Returns the annotation describing the constraint declaration.
    */
   public Class getAnnotationClass() 
   {
     return this.annotationClass;
   }

   /**
    * Returns the name of the client-side validator.
    */
   public String getValidatorName() 
   {
     return this.validatorName;
   }

   /**
    * Attribute names of the constraint annotation to be passed (along with their values) to the JavaScript validator 
    * function as an {@link JSONObject}. 
    */
   public Set<String> getAttributes() 
   {
     return this.attributes;
   }
}