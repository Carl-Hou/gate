package org.gate.gui.graph.elements.extractor;

public interface ExtractorConstantsInterface  {

    String SourceType_Response = "Response";
    String SourceType_Variable = "Variable";
    String SourceType = "Source type";
    String[] SourceTypes = {SourceType_Variable, SourceType_Response};
    String PN_Variable_Name = "Variable Name";
    String PN_DefaultValue = "Default Value";
    String Extractor_Type = "Extractor Type";
    String PN_MatchNo = "Match No. (0 for Random)";
    // JSON Extractor
    String JSON_ExtractorType_JSONPath = "JSONPath";
    String JSON_ExtractorType_JMESPath = "JMESPath";
    String[] JSON_ExtractorTypes = {JSON_ExtractorType_JSONPath, JSON_ExtractorType_JMESPath};
    // Text Extractor
    String TEXT_ExtractorType_Regex = "Regex";
    String TEXT_Extractor_Template = "Template";
    // XML Extractor
    String PN_XML_Validate = "valid xml";
    String PN_XML_IgnoreWhiteSpace = "ignore white space";
    String PN_XML_Fragment = "entire xpath fragment"; // $NON-NLS-1$
    String XML_ExtractorType_XPATH = "XPath";



}
