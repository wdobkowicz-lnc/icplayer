package com.lorepo.icplayer.client.model.alternativeText;



import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.lorepo.icf.utils.StringUtils;
import com.lorepo.icf.utils.TextToSpeechVoice;
import com.lorepo.icplayer.client.utils.DomElementManipulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlternativeTextService {
    public static String getVisibleText(String source) {
        List<IToken> tokens = parseAltText(source);
        StringBuilder builder = new StringBuilder();

        for (IToken token : tokens) {
            builder.append(token.getVisibleText());
        }

        return builder.toString();
    }

    public static List<TextToSpeechVoice> getReadableText(String source) {
        List<IToken> tokens = parseAltText(source);
        List<TextToSpeechVoice> textToSpeechVoices = new ArrayList<>();

        for (IToken token : tokens) {
            TextToSpeechVoice ttsv;

            if (token.getLanguage() != null) {
                ttsv = TextToSpeechVoice.create(token.getReadableText(), token.getLanguage());
            } else {
                ttsv = TextToSpeechVoice.create(token.getReadableText());
            }

            textToSpeechVoices.add(ttsv);
        }

        return textToSpeechVoices;
    }

    public static String getAltTextAsHtml(String source) {
        List<IToken> tokens = parseAltText(source);
        StringBuilder builder = new StringBuilder();

        for (IToken token : tokens) {

            if (token.isAlt()) {
                String code = StringUtils.unescapeXML(
                        getAltTextElement(
                                token.getVisibleText(),
                                token.getReadableText(),
                                token.getLanguage()
                        ).getHTMLCode()
                );
                builder.append(
                    code
                );
            } else {
                // it doesn't matter which text we will get as they are the same
                builder.append(token.getReadableText());
            }

            builder.append(token.getVisibleText());
        }

        return builder.toString();
    }

    private static List<IToken> parseAltText(String srcText) {
        final String pattern = "\\\\alt\\{";
        String input = srcText;
        int index = -1;
        RegExp regExp = RegExp.compile(pattern);
        MatchResult matchResult;

        List<IToken> altTokens = new ArrayList<>();

        while ((matchResult = regExp.exec(input)) != null) {
            if (matchResult.getGroupCount() <= 0) {
                break;
            }

            String group = matchResult.getGroup(0);
            altTokens.add(new Token(input.substring(0, matchResult.getIndex())));

            input = input.substring(matchResult.getIndex() + group.length());
            index = findClosingBracket(input);

            String expression = input.substring(0, index);
            input = input.substring(index + 1);
            Map<String, String> gapOptions = getGapOptions(input);
            input = removeGapOptions(input);
            int separatorIndex = expression.indexOf("|");
            if (separatorIndex > 0) {
                String visibleText = expression.substring(0, separatorIndex);
                String readableText = expression.substring(separatorIndex + 1);
                if (gapOptions != null && gapOptions.containsKey("lang")) {
                    altTokens.add(new AltToken(visibleText, readableText, gapOptions.get("lang")));
                } else {
                    altTokens.add(new AltToken(visibleText, readableText));
                }
            } else {
                altTokens.add(new AltToken("#ERR", "#ERR"));
            }
        }

        if (input.length() > 0) {
            altTokens.add(new AltToken(input, input));
        }

        return altTokens;
    }

    public static Map<String,String> getGapOptions(String expression) {
        final String pattern = 	"^\\[[a-zA-Z0-9_\\- ]*?\\]";
        Map<String,String> result = new HashMap<String,String>();
        RegExp regExp = RegExp.compile(pattern);
        MatchResult matchResult;

        while ((matchResult = regExp.exec(expression)) != null) {
            if (matchResult.getGroupCount() <= 0) {
                break;
            }

            String group = matchResult.getGroup(0);
            expression = expression.replaceFirst(pattern, "");
            group = group.replaceAll("[\\[\\]]", "");
            String[] values = group.split(" ");
            if(values.length==2) {
                result.put(values[0], values[1]);
            }
        }
        return result;
    };

    public static String removeGapOptions(String expression) {
        final String pattern = 	"^\\[[a-zA-Z0-9_\\- ]*?\\]";
        RegExp regExp = RegExp.compile(pattern);
        MatchResult matchResult;

        while ((matchResult = regExp.exec(expression)) != null) {
            if (matchResult.getGroupCount() <= 0) {
                break;
            }

            String group = matchResult.getGroup(0);
            group = group.replaceAll("[\\[\\]]", "");
            String[] values = group.split(" ");
            if(values.length!=2) {
                break;
            }
            expression = expression.replaceFirst(pattern, "");
        }
        return expression;
    };

    public static DomElementManipulator getAltTextElement(String visibleText, String altText, String langTag) {
        DomElementManipulator wrapper = new DomElementManipulator("span");
        wrapper.setHTMLAttribute("aria-label", altText);
        if (langTag != null && langTag.length() > 0) {
            wrapper.setHTMLAttribute("lang", langTag);
        }
        DomElementManipulator visibleTextElement = new DomElementManipulator("span");
        visibleTextElement.setHTMLAttribute("aria-hidden", "true");
        visibleTextElement.setInnerHTMLText(visibleText);

        wrapper.appendElement(visibleTextElement);
        return wrapper;

    }

    public static int findClosingBracket(String input) {

        int counter = 0;

        for (int index = 0; index < input.length(); index++) {

            if (input.charAt(index) == '{') {
                counter++;
            } else if (input.charAt(index) == '}') {
                counter--;
                if (counter < 0) {
                    return index;
                }
            }
        }

        return -1;
    }

//    private static String escapeAltTextInTag(String srcText){
//        String parsedText = srcText;
//        String oldParsedText = "";
//        String pattern = "<([^>]*?)\\\\alt\\{([^<]*?)>";
//        while(!oldParsedText.equals(parsedText)){ //it is possible that there will be multiple alt texts inside one tag, all must be escaped
//            oldParsedText = parsedText;
//            parsedText =  parsedText.replaceAll(pattern, "<$1\\\\altEscaped\\{$2>");
//        }
//        return parsedText;
//    }
//
//    private static String unescapeAltTextInTag(String srcText){
//        String pattern = "<([^>]*?)\\\\altEscaped\\{([^<]*?)>";
//        String parsedText = srcText;
//        String oldParsedText = "";
//        while(!oldParsedText.equals(parsedText)){ //it is possible that there will be multiple alt texts inside one tag, all must be unescaped
//            oldParsedText = parsedText;
//            parsedText =  parsedText.replaceAll(pattern, "<$1\\\\alt\\{$2>");
//        }
//        return parsedText;
//    }
}
