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

# ## core/forms
#
# Defines handlers for HTML forms and HTML field elements, specifically to control input validation.

define ["core/events", "core/spi", "core/builder", "_"],
  (events, spi, builder, _) ->

    # Meta-data name that indicates the next submission should skip validation (typically, because
    # the form was submitted by a "cancel" button).
    SKIP_VALIDATION = "t5:skip-validation"

    clearSubmittingHidden = (form) ->
      hidden = form.findFirst "[name='t:submit']"

      # Clear if found
      hidden and hidden.value null

      form.meta SKIP_VALIDATION, null

      return

    setSubmittingHidden = (form, submitter) ->

      mode = submitter.attribute "data-submit-mode"
      isCancel = mode and mode isnt "normal"
      if isCancel
        form.meta SKIP_VALIDATION, true

      hidden = form.findFirst "[name='t:submit']"

      unless hidden
        firstHidden = form.findFirst "input[type=hidden]"
        hidden = builder "input", type:"hidden", name:"t:submit"
        firstHidden.insertBefore hidden

      # TODO: Research why we need id and name and get rid of one if possible.
      value = Object.toJSON [ submitter.element.id, isCancel ? "cancel" : submitter.element.name ]

      hidden.value value

      return

    # Passed the element wrapper for a form element, returns a map of all the values
    # for all non-disabled fields (including hidden fields, select, textarea). This is primarily
    # used when assembling an Ajax request for a form submission.
    gatherParameters = (form) ->
      result = {}

      fields = form.find "input, select, textarea"

      _.each fields, (field) ->
          return if field.attribute "disabled"

          type = field.element.type

          # Ignore types file and submit; file doesn't make sense for Ajax, and submit
          # is handled by keeping a hidden field active with the data Tapestry needs
          # on the server.
          return if type is "file" || type is "submit"

          value = field.value()

          return if value is null

          name = field.element.name

          existing = result[name]

          if _.isArray existing
            existing.push value
            return

          if existing
            result[name] = [existing, value]
            return

          result[name] = value

      return result


    defaultValidateAndSubmit = ->

      if ((this.attribute "data-validate") is "submit") and
         (not this.meta SKIP_VALIDATION)

        this.meta SKIP_VALIDATION, null

        memo = error: false

        # This will become more relevant shortly, when field validation is modernized:
        for field in this.find "[data-validation]"
           field.trigger events.field.validate, memo

        # Only do form validation if all individual field validation
        # was successful.
        this.trigger events.form.validate, memo unless memo.error

        if memo.error
          clearSubmittingHidden this
          return false

      # Allow certain types of elements to do last-moment set up. Basically, this is for
      # FormFragment, or similar, to make their hidden field enabled or disabled to match
      # their UI's visible/hidden status. This is assumed to work or throw an exception; there
      # is no memo.
      this.trigger events.form.prepareForSubmit, this


      # Otherwise, the event is good, there are no validation problems, let the normal processing commence.
      # Possibly, the document event handler in core/zone will intercept form submission if this
      # is an Ajax submission.
      return

    spi.onDocument "submit", "form", defaultValidateAndSubmit

    # On any click on a submit or image, update the containing form to indicate that the element
    # was responsible for the eventual submit; this is very important to Ajax updates, otherwise the
    # information about which control triggered the submit gets lost.
    spi.onDocument "click", "input[type=submit], input[type=image]", ->
      setSubmittingHidden (spi this.element.form), this
      return

    # Support for link submits. `data-submit-mode` will be non-null, possibly "cancel".
    # Update the hidden field, but also cancel the default behavior for the click.
    spi.onDocument "click", "a[data-submit-mode]", ->
      form = this.findContainer "form"

      unless form
        console.error "Submitting link element not contained inside a form element."
        return false

      setSubmittingHidden form, this

      form.element.submit()

      # And cancel the default behavior for the original click event
      return false

    exports =
      gatherParameters: gatherParameters

      setSubmittingElement: setSubmittingHidden

      # Sets a flag on the form to indicate that client-side validation should be bypassed.
      # This is typically associated with submit buttons that "cancel" the form.
      skipValidation: (form) ->
        form.meta SKIP_VALIDATION, true