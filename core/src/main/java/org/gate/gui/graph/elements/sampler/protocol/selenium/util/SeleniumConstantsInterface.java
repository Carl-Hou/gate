package org.gate.gui.graph.elements.sampler.protocol.selenium.util;


import org.gate.gui.tree.test.elements.config.SeleniumDefaults;

public interface SeleniumConstantsInterface {

    //name for context
    String Selenium = "selenium";
    String DefaultConfigName = SeleniumDefaults.class.getName();
    String PN_MethodSuppliersName = "Method";

    //Name of elements

    String Element_Category = "Interaction Category";
    String PN_EC_Element = "Element";
    String PN_EC_Select = "Select";
    String[] Element_Categories = { PN_EC_Element, PN_EC_Select};


    String PN_VariableName = "Variable Name";
    String PN_VariableName_ReturnValue = "Variable Name (Return  Value)";
    String PN_VariableNamePrefix_ReturnValue = "Variable Name Prefix (Return Value)";
    String PN_LocatorType  = "Locator Type";
    String Locator_ClassName = "Class Name";
    String Locator_CssSelector = "Css Selector";
    String Locator_Id = "Id";
    String Locator_LinkText = "Link Text";
    String Locator_Name = "Name";
    String Locator_PartialLinkText = "Partial Link Text";
    String Locator_TagName = "Tag Name";
    String Locator_XPath = "XPath";

    String[] LocatorTypes = {Locator_XPath, Locator_ClassName, Locator_CssSelector, Locator_Id, Locator_LinkText, Locator_Name, Locator_PartialLinkText, Locator_TagName};
    String PN_LocatorCondition = "Locator Condition";
    String PN_AttributeName = "Attribute Name";
    String PN_AttributeValue = "Attribute Value";
    String PN_Selected ="Selected";
    String PN_Text ="Text";
    String PN_JavaScript ="Java Script";
    String PN_Number ="Number";
    String PN_Regex ="Regex";
    String PN_Title ="Title";

    String ElementInputType = "Element Input Type";
    String ElementInputType_Driver = "Driver";
    String ElementInputType_Locator = "Locator";
    String ElementInputType_Variable = "Variable";
    String[] ElementInputTypes = {ElementInputType_Locator, ElementInputType_Variable, ElementInputType_Driver};
    String[] SelectInputTypes = {ElementInputType_Locator, ElementInputType_Variable};
    // Names in SeleniumDefaults. Driver
    String BrowserName_Chrome = "Chrome";
    String BrowserName_Safari = "Safari";
    String BrowserName_FireFox = "Firefox";
    String BrowserName_InternetExplorer = "IE";
    String BrowserName_Edge = "Edge";
    String BrowserName_Opera = "Opera";
    String[] BrowserNames = {BrowserName_Chrome, BrowserName_FireFox, BrowserName_InternetExplorer, BrowserName_Edge, BrowserName_Opera};
    String PN_DriverId = "Driver Id";
    String PN_BrowserName = "Browser Name";
    String PN_BrowserVersion = "Browser Version";
    String PN_GridHubUrl = "Grid Hub Url";
    String PN_Platform = "Platform";
    String PN_JavascriptEnabled = "Java Script Enabled";
    String PN_DriverURL = "URL";
    String PN_ImplicitlyWaitTimeoutSeconds = "Implicitly Wait Timeout Seconds";
    String PN_PageLoadTimeoutSeconds = "Page Load Timeout Seconds";
    String PN_SetScriptTimeoutSeconds = "Script Timeout Seconds";
    String PN_CloseBrowserAfterTest = "Close Browser After Test";
    String TimeUnit_Seconds = "Seconds";
    String TimeUnit_Milliseconds = "Milliseconds";
    String TimeUnit_Minutes = "Minutes";
    String[] TimeUnites = {TimeUnit_Minutes, TimeUnit_Seconds, TimeUnit_Milliseconds};

    // Explicit Wait
    String ExplicitWaitType = "Explicit Wait Type";
    String ExplicitWait_Condition = "Explicit Wait for Condition";
    String ExplicitWait_Element = "Explicit Wait for Element";
    String[] ExplicitWaitTypes = {ExplicitWait_Condition, ExplicitWait_Element};

    String PN_ExplicitWaitPollingInterval = "Explicit Wait Polling Interval"; //action polling frequency
    String PN_ExplicitWaitTimeOut = "Explicit Wait Timeout"; //action timeout
    // Interactions
    String Interaction_Category = "Interaction Category";
    String PN_IC_Navigation = "Navigation";
    String PN_IC_Browser = "Browser";
    String PN_IC_Alerts = "Alerts";
    String PN_IC_Windows = "Windows";
    String PN_IC_Frames = "Frames";
    String[] Interaction_Categories = { PN_IC_Navigation, PN_IC_Browser,  PN_IC_Alerts, PN_IC_Frames, PN_IC_Windows};

    String PN_Position_X = "Variable Name of Position X";
    String PN_Position_Y = "Variable Name of Position Y";
    String PN_Size_Width = "Variable Name of Size Width";
    String PN_Size_Height = "Variable Name of Size Height";
    String PN_Position_X_ReturnValue = "Variable Name of Position X (Return Value)";
    String PN_Position_Y_ReturnValue = "Variable Name of Position Y (Return Value)";
    String PN_Size_Width_ReturnValue = "Variable Name of Size Width (Return Value)";
    String PN_Size_Height_ReturnValue = "Variable Name of Size Height (Return Value)";
    String PN_Index = "Index";
    String PN_NameOrID = "Name or ID";

    // Actions
    String Keyboard_KeysToSend = "Keys to Send";
    String Keyboard_Key = "Key";
    String PN_PAUSE = "Pause";
    String KK_Control = "Ctrl";
    String KK_ALT = "Alt";
    String KK_Shift = "Shift";
    String[] Keyboard_Keys = {KK_Control, KK_ALT, KK_Shift};

    //  Screenshot property
    String ScreenShotConditionPropName = "gate.selenium.screenshot.condition";
    String ScreenShotLocationPropName = "selenium.screenshot.location";
    String ScreenShotConditionAlways = "always";
    String ScreenShotConditionNever = "never";
    String ScreenShotConditionFail = "fail";

}
