// Copyright 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;

import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Tests related to the {@link Zone} component.
 */
public class ZoneTests extends TapestryCoreTestCase
{
    /**
     * TAP5-138
     */
    @Test
    public void select_zone()
    {
        clickThru("Select Zone Demo");

        select("carMaker", "Bmw");

        waitForElementToAppear("carModelContainer");

        click(SUBMIT);

        String condition = String.format("window.$$(\"%s\")", "t-error-popup");

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);

        assertText(String.format("//div[@class='%s']/span", "t-error-popup"), "You must provide a value for Car Model.");

        String selectLocator = "//div[@id='modelZone']//select";

        select(selectLocator, "7 Series");

        clickAndWait(SUBMIT);

        assertTextPresent("Car Maker: BMW");

        assertTextPresent("Car Model: 7 Series");

        select("carMaker", "Mercedes");

        waitForElementToAppear("carModelContainer");

        select(selectLocator, "E-Class");

        clickAndWait(SUBMIT);

        assertTextPresent("Car Maker: MERCEDES");

        assertTextPresent("Car Model: E-Class");
    }

    @Test
    public void zone_updates()
    {
        clickThru("Zone Demo");

        assertTextPresent("No name has been selected.");

        // Hate doing this, but selecting by the text isn't working, perhaps
        // because of the
        // HTML entities.
        click("select_0");

        // And that's as far as we can go currently, because
        // of limitations in Selenium 0.8.3 and bugs in Selenium 0.9.2.

        // assertTextPresent("Selected: Mr. &lt;Roboto&gt;");

        click("link=Direct JSON response");
    }

    /**
     * TAP5-187
     */
    @Test
    public void zone_redirect_by_class()
    {
        clickThru("Zone Demo");

        clickAndWait("link=Perform a redirect to another page");

        assertText("activePageName", "nested/AssetDemo");
    }

    /**
     * TAP5-108
     */
    @Test
    public void update_multiple_zones_at_once()
    {
        clickThru("Multiple Zone Update Demo");

        String now = getText("now");

        assertText("wilma", "Wilma Flintstone");

        click("update");

        waitForElementToAppear("fredName");

        assertText("fredName", "Fred Flintstone");
        assertText("dino", "His dog, Dino.");
        assertText("wilma", "His Wife, Wilma.");

        // Ideally, we'd add checks that the JavaScript for the Palette in the
        // Barney Zone was
        // updated.

        // Make sure it was a partial update
        assertText("now", now);
    }

    /**
     * TAP5-573
     */
    @Test
    public void zone_namespace_interaction_fixed()
    {
        clickThru("Zone/Namespace Interaction");

        String outerNow = getText("outernow");
        String innerNow = getText("innernow");

        // If we're too fast that innernow doesn't change because its all within
        // a single second.

        sleep(1050);

        click(SUBMIT);

        waitForElementToAppear("message");

        // Make sure it was just an Ajax update.
        assertEquals(getText("outernow"), outerNow);

        assertFalse(getText("innernow").equals(innerNow));
    }

    @Test
    public void zone_updated_event_triggered_on_client()
    {
        clickThru("Zone Demo");

        assertText("zone-update-message", "");

        click("link=Direct JSON response");

        // Give it some time to process.

        sleep(100);

        assertText("zone-update-message", "Zone updated.");
    }

    /**
     * TAP5-389
     */
    @Test
    public void link_submit_inside_form_that_updates_a_zone()
    {
        clickThru("LinkSubmit inside Zone");

        String now = getText("now");

        waitForElementToAppear("mySubmit");

        click("//a[@id='mySubmit']");

        waitForElementToAppear("value:errorpopup");

        type("value", "robot chicken");

        click("//a[@id='mySubmit']");

        waitForElementToAppear("outputvalue");

        assertText("outputvalue", "robot chicken");

        assertText("eventfired", "true");

        // Make sure it was a partial update
        assertText("now", now);
    }

    @Test
    public void zone_inject_component_from_template()
    {
        clickThru("Inject Component Demo");

        assertTextPresent(Form.class.getName() + "[form--form]");
    }

    /**
     * TAP5-707
     */
    @Test
    public void zone_fade_back_backgroundcolor()
    {
        clickThru("Form Zone Demo");

        type("longValue", "12");

        click(SUBMIT);

        click(SUBMIT);

        // wait some time to let the fade go away
        sleep(4050);

        // will only work in firefox.
        String color = getEval("selenium.browserbot.getCurrentWindow().getComputedStyle(this.page().findElement(\"xpath=//div[@id='valueZone']\"),'').getPropertyValue('background-color').toLowerCase()");

        assertEquals(color, "rgb(255, 255, 255)");
    }

    /** TAP5-1084 */
    @Test
    public void update_zone_inside_form()
    {
        clickThru("Zone/Form Update Demo");

        click("link=Update the form");

        waitForElementToAppear("updated");

        type("//INPUT[@type='text']", "Tapestry 5.2");

        clickAndWait(SUBMIT);

        assertText("output", "Tapestry 5.2");
    }

    /** TAP5-1109 */
    @Test
    public void update_to_zone_inside_form()
    {
        clickThru("MultiZone Update inside a Form");

        select("selectValue1", "3 pre ajax");

        waitForElementToAppear("select2ValueZone");

        select("//div[@id='select2ValueZone']//select", "4 post ajax");
    }

}
