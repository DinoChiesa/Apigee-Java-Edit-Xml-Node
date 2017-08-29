package com.dinochiesa.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;

public class VariableRefResolver {
    private static final String variableReferencePatternString = "(.*?)\\{([^\\{\\} ]+?)\\}(.*?)";
    private static final Pattern variableReferencePattern = Pattern.compile(variableReferencePatternString);

    public interface VariableResolver {
        public String get(String name);
    }

    // If the value of a property contains a pair of curlies,
    // eg, {apiproxy.name}, then "resolve" the value by de-referencing
    // the context variable whose name appears between the curlies.
    public static String resolve(String spec, VariableResolver map) {
        Matcher matcher = variableReferencePattern.matcher(spec);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
            sb.append(matcher.group(1));
            sb.append((String) map.get(matcher.group(2)));
            sb.append(matcher.group(3));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
