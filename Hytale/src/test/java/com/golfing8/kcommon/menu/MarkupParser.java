package com.golfing8.kcommon.menu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A strict parser for the subset of Hytale UI markup that menus write, so tests can prove the
 * document is well formed. Values come back as {@link String}s holding the literal, or maps for tuples.
 */
final class MarkupParser {
    private final String text;
    private int pos;

    private MarkupParser(String text) {
        this.text = text;
    }

    static final class Node {
        final String type;
        final String id;
        final Map<String, Object> properties = new LinkedHashMap<>();
        final List<Node> children = new ArrayList<>();

        Node(String type, String id) {
            this.type = type;
            this.id = id;
        }

        List<Node> all() {
            List<Node> nodes = new ArrayList<>();
            nodes.add(this);
            for (Node child : children) {
                nodes.addAll(child.all());
            }
            return nodes;
        }
    }

    static Node parse(String text) {
        MarkupParser parser = new MarkupParser(text);
        Node root = parser.element();
        parser.skip();
        if (parser.pos != text.length())
            throw parser.error("expected the end of the document");
        return root;
    }

    private Node element() {
        skip();
        String type = name();
        skip();
        expect('#');
        Node node = new Node(type, name());
        skip();
        expect('{');
        while (true) {
            skip();
            if (peek() == '}') {
                pos++;
                return node;
            }
            int start = pos;
            String name = name();
            skip();
            if (peek() == ':') {
                pos++;
                Object value = value();
                skip();
                expect(';');
                if (node.properties.put(name, value) != null)
                    throw error("property " + name + " set twice");
            } else {
                pos = start;
                node.children.add(element());
            }
        }
    }

    private Object value() {
        skip();
        char c = peek();
        if (c == '"')
            return string();
        if (c == '(')
            return tuple();
        if (c == '#') {
            int start = pos++;
            for (int i = 0; i < 6; i++) {
                if (Character.digit(text.charAt(pos++), 16) < 0)
                    throw error("bad color");
            }
            if (peek() == '(') {
                while (text.charAt(pos) != ')')
                    pos++;
                pos++;
            }
            return text.substring(start, pos);
        }
        int start = pos;
        while (pos < text.length() && (Character.isLetterOrDigit(peek()) || peek() == '-' || peek() == '.'))
            pos++;
        if (start == pos)
            throw error("expected a value");
        return text.substring(start, pos);
    }

    private Map<String, Object> tuple() {
        expect('(');
        Map<String, Object> values = new LinkedHashMap<>();
        skip();
        if (peek() == ')') {
            pos++;
            return values;
        }
        while (true) {
            skip();
            String name = name();
            skip();
            expect(':');
            values.put(name, value());
            skip();
            if (peek() == ',') {
                pos++;
            } else {
                expect(')');
                return values;
            }
        }
    }

    private String string() {
        expect('"');
        StringBuilder out = new StringBuilder("\"");
        while (true) {
            char c = text.charAt(pos++);
            if (c == '\\') {
                char escaped = text.charAt(pos++);
                if (escaped != '"' && escaped != '\\')
                    throw error("bad escape");
                out.append(escaped);
            } else if (c == '"') {
                return out.append('"').toString();
            } else if (c == '\n' || Character.isISOControl(c)) {
                throw error("control character in a string");
            } else {
                out.append(c);
            }
        }
    }

    private String name() {
        int start = pos;
        while (pos < text.length() && Character.isLetterOrDigit(peek()))
            pos++;
        if (start == pos || !Character.isLetter(text.charAt(start)))
            throw error("expected a name");
        return text.substring(start, pos);
    }

    private void expect(char c) {
        if (peek() != c)
            throw error("expected '" + c + "'");
        pos++;
    }

    private char peek() {
        if (pos >= text.length())
            throw error("unexpected end");
        return text.charAt(pos);
    }

    private void skip() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
            pos++;
    }

    private IllegalStateException error(String message) {
        return new IllegalStateException(message + " at " + pos + ": ..." + text.substring(Math.max(0, pos - 40), Math.min(text.length(), pos + 40)));
    }
}
