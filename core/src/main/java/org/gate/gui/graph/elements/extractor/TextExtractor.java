/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.gate.gui.graph.elements.extractor;

import org.apache.oro.text.regex.*;
import org.gate.gui.common.TestElement;
import org.gate.gui.graph.elements.extractor.gui.TextExtractorGui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TextExtractor extends AbstractExtractor {

    public TextExtractor(){
        addProp(TestElement.NS_DEFAULT, TEXT_Extractor_Template, "");
        addProp(TestElement.NS_DEFAULT, Extractor_Type, TEXT_ExtractorType_Regex);
    }

    @Override
    protected List<String> extract(String regex, String content) {
        Perl5Matcher matcher = new Perl5Matcher();
        Perl5Compiler compiler = new Perl5Compiler();

        List<String> extractedValues = new LinkedList<>();
        PatternMatcherInput matcherInput = new PatternMatcherInput(content);
        String rawTemplate = getRunTimeProp(NS_DEFAULT, TEXT_Extractor_Template).trim();
        if(rawTemplate.isEmpty()){
            throw new IllegalArgumentException("Template is empty.(e,g $0$, $1$ ...");
        }
        List<Object> template = null;
        Pattern pattern = null;
        try {
            template = initTemplate(rawTemplate);
            pattern = compiler.compile(regex, Perl5Compiler.READ_ONLY_MASK);
        } catch (MalformedPatternException e) {
            log.info(e);
            throw new IllegalArgumentException(e);
        }

        int found = 0;
        while (matcher.contains(matcherInput, pattern)) {
            found++;
            MatchResult result = matcher.getMatch();
            extractedValues.add(generateResult(result, template));
            for(int i = 0; i < result.groups(); i++) {
                System.out.printf("Found: %s, $%s$ = %s\n", found, i, result.group(i));
            }
        }
        return extractedValues;
    }

    @Override
    public String getGUI() {
        return TextExtractorGui.class.getName();
    }

    @Override
    public String getStaticLabel() {
        return "Text Extractor";
    }

    String generateResult(MatchResult match, List<Object> template) {
        StringBuilder result = new StringBuilder();
        for (Object obj : template) {
            if (obj instanceof Integer) {
                result.append(match.group(((Integer) obj).intValue()));
            } else {
                result.append(obj);
            }
        }

        return result.toString();
    }

    List<Object> initTemplate(String rawTemplate) throws MalformedPatternException {
        Perl5Matcher matcher = new Perl5Matcher();
        Perl5Compiler compiler = new Perl5Compiler();

        // Contains Strings and Integers
        List<Object> combined = new ArrayList<>();


//        Pattern templatePattern = JMeterUtils.getPatternCache().getPattern("\\$(\\d+)\\$"  // $NON-NLS-1$
//                , Perl5Compiler.READ_ONLY_MASK
//                        & Perl5Compiler.SINGLELINE_MASK);
        org.apache.oro.text.regex.Pattern templatePattern = compiler.compile("\\$(\\d+)\\$", Perl5Compiler.READ_ONLY_MASK & Perl5Compiler.SINGLELINE_MASK);
        int beginOffset = 0;
        MatchResult currentResult;
        PatternMatcherInput pinput = new PatternMatcherInput(rawTemplate);
        while(matcher.contains(pinput, templatePattern)) {
            currentResult = matcher.getMatch();
            final int beginMatch = currentResult.beginOffset(0);
            if (beginMatch > beginOffset) { // string is not empty
                combined.add(rawTemplate.substring(beginOffset, beginMatch));
            }
            combined.add(Integer.valueOf(currentResult.group(1)));// add match as Integer
            beginOffset = currentResult.endOffset(0);
        }

        if (beginOffset < rawTemplate.length()) { // trailing string is not empty
            combined.add(rawTemplate.substring(beginOffset, rawTemplate.length()));
        }

        return combined;
    }
}
