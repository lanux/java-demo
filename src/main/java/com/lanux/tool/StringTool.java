package com.lanux.tool;

/**
 * Created by lanux on 2017/9/18.
 */
public class StringTool {
    public static String maxString(String value, int length) {
        if (value == null || value.length() <= length) {
            return value;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(value.substring(0, length / 2));
        sb.append("……");
        sb.append(value.substring(value.length() - length / 2, value.length()));
        return sb.toString();
    }
}
