package org.gate.gui.graph.elements.sampler.protocol.selenium.util;


import org.gate.gui.tree.test.elements.config.SeleniumDefaults;

public interface SeleniumConstantsInterface {

    //name for context
    String Selenium = "selenium";
    String DefaultConfigName = SeleniumDefaults.class.getName();
    String PN_MethodSuppliersName = "method";

    //Name of elements
    String PN_LocatorType  = "locator_type";
    String Locator_ClassName = "className";
    String Locator_CssSelector = "cssSelector";
    String Locator_Id = "id";
    String Locator_LinkText = "linkText";
    String Locator_Name = "name";
    String Locator_PartialLinkText = "partialLinkText";
    String Locator_TagName = "tagName";
    String Locator_XPath = "xpath";

    String[] LocatorTypes = {Locator_XPath, Locator_ClassName, Locator_CssSelector, Locator_Id, Locator_LinkText, Locator_Name, Locator_PartialLinkText, Locator_TagName};
    String PN_LocatorCondition = "locator_condition";
    String PN_AttributeName = "attribute_name";
    String PN_AttributeValue = "attribute_value";
    String PN_URL = "url";
    String PN_Selected ="selected";
    String PN_Text ="text";
    String PN_JavaScript ="javaScript";
    String PN_Number ="number";
    String PN_Regex ="regex";
    String PN_Title ="title";

    // names in SeleniumDefaults
    String BrowserName_Chrome = "chrome";
    String BrowserName_Safari = "safari";
    String BrowserName_FireFox = "firefox";
    String BrowserName_InternetExplorer = "ie";
    String BrowserName_Edge = "edge";
    String BrowserName_Opera = "opera";
    String[] BrowserNames = {BrowserName_Chrome, BrowserName_FireFox, BrowserName_InternetExplorer, BrowserName_Edge, BrowserName_Opera};
    String PN_DriverId = "driver_id";
    String PN_BrowserName = "browser_name";
    String PN_BrowserVersion = "browser_version";
    String PN_GridHubUrl = "grid_hub_url";

    String PN_Platform = "platform";
    String PN_JavascriptEnabled = "javascript_enabled";

    String PN_WaitExpectedCondition = "wait_expected_condition"; //action timeout
    String[] WaitExpectedConditionsForElements = {"presenceOfElementLocated", "elementToBeClickable", "visibilityOfElementLocated"};
    String PN_WaitPollingInterval = "wait_polling_interval"; //action polling frequency
    String PN_WaitTimeOut = "wait_timeout"; //action timeout

    //  Screen shot
    String ScreenShotConditionPropName = "gate.selenium.screenshot.condition";
    String ScreenShotConditionAlways = "always";
    String ScreenShotConditionNever = "never";
    String ScreenShotConditionFail = "fail";
    String ScreenShotLocationPropName = "selenium.screenshot.location";
}
