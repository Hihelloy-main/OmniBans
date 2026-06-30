package com.hihelloy.work.omnibans.text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class SimpleJsonParser {

    private final String input;
    private int index;

    private SimpleJsonParser(String input) {
        this.input = input;
        this.index = 0;
    }

    static Object parse(String input) {
        SimpleJsonParser parser = new SimpleJsonParser(input);
        return parser.parseValue();
    }

    private Object parseValue() {
        skipWhitespace();
        char current = peek();
        if (current == '{') {
            return parseObject();
        }
        if (current == '[') {
            return parseArray();
        }
        if (current == '"') {
            return parseString();
        }
        if (current == 't' || current == 'f') {
            return parseBoolean();
        }
        if (current == 'n') {
            index += 4;
            return null;
        }
        return parseNumber();
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> result = new LinkedHashMap<>();
        index++;
        skipWhitespace();
        if (peek() == '}') {
            index++;
            return result;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            index++;
            Object value = parseValue();
            result.put(key, value);
            skipWhitespace();
            char next = input.charAt(index);
            index++;
            if (next == '}') {
                break;
            }
        }
        return result;
    }

    private List<Object> parseArray() {
        List<Object> result = new ArrayList<>();
        index++;
        skipWhitespace();
        if (peek() == ']') {
            index++;
            return result;
        }
        while (true) {
            Object value = parseValue();
            result.add(value);
            skipWhitespace();
            char next = input.charAt(index);
            index++;
            if (next == ']') {
                break;
            }
        }
        return result;
    }

    private String parseString() {
        StringBuilder builder = new StringBuilder();
        index++;
        while (true) {
            char current = input.charAt(index);
            index++;
            if (current == '"') {
                break;
            }
            if (current == '\\') {
                appendEscaped(builder);
                continue;
            }
            builder.append(current);
        }
        return builder.toString();
    }

    private void appendEscaped(StringBuilder builder) {
        char escaped = input.charAt(index);
        index++;
        if (escaped == 'u') {
            String hex = input.substring(index, index + 4);
            index += 4;
            builder.append((char) Integer.parseInt(hex, 16));
            return;
        }
        builder.append(unescapeSingle(escaped));
    }

    private char unescapeSingle(char escaped) {
        switch (escaped) {
            case 'n':
                return '\n';
            case 't':
                return '\t';
            case 'r':
                return '\r';
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            default:
                return escaped;
        }
    }

    private Boolean parseBoolean() {
        if (peek() == 't') {
            index += 4;
            return Boolean.TRUE;
        }
        index += 5;
        return Boolean.FALSE;
    }

    private Double parseNumber() {
        int start = index;
        while (index < input.length() && isNumberChar(input.charAt(index))) {
            index++;
        }
        return Double.parseDouble(input.substring(start, index));
    }

    private boolean isNumberChar(char character) {
        return Character.isDigit(character) || character == '-' || character == '+' || character == '.' || character == 'e' || character == 'E';
    }

    private void skipWhitespace() {
        while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
            index++;
        }
    }

    private char peek() {
        return input.charAt(index);
    }

}
