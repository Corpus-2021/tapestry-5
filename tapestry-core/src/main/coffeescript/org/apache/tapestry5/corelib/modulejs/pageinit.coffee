# Copyright 2012 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# Module that defines functions used for page initialization.
# The initialize function is passed an array of initializers; each initializer is itself
# an array. The first value in the initializer defines the name of the module to invoke.
# The module name may also indicate the function exported by the module, as a suffix following a colon:
# e.g., "my/module:myfunc".
# Any additional values in the initializer are passed to the function. The context of the function (this) is null.
define ->
  invokeInitializer = (qualifiedName, initArguments...) ->

    [moduleName, functionName] = qualifiedName.split ':'

    require [moduleName], (moduleLib) ->
      fn = if functionName? then moduleLib[functionName] else moduleLib
      fn.apply null, initArguments


  # Exported function passed a list of initializers.
  initialize : (inits) ->
    # apply will split the first value from the rest for us, implicitly.
    invokeInitializer.apply null, init for init in inits

