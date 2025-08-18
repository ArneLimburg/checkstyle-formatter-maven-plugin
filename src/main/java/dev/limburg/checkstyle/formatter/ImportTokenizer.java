/*
 * Copyright 2025 Arne Limburg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.limburg.checkstyle.formatter;

import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

public class ImportTokenizer {

    private List<String> lines;

    ImportTokenizer(List<String> lines) {
        this.lines = requireNonNull(lines);
    }

    static boolean everyImportOnSeparateLine(List<Token> tokens) {
        for (int i = 1; i < tokens.size(); i++) {
            if (tokens.get(i - 1).endLine() == tokens.get(i).startLine()) {
                return false;
            }
        }
        return true;
    }

    List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        Location start = new Location(0, 0);
        boolean hasImportToken = false;
        do {
            Location end = nextDelimiter(start);
            String currentLine = lines.get(end.line());
            while (currentLine.charAt(end.column()) == '/') {
                if (currentLine.charAt(end.column() + 1) == '/') {
                    end = nextDelimiter(new Location(end.line() + 1, 0));
                } else {
                    Location multiLineEnd = findMultiLineEnd(end);
                    end = nextDelimiter(new Location(multiLineEnd.line(), multiLineEnd.column() + 2));
                }
                currentLine = lines.get(end.line());
            }
            end = includeLineCommendIfPresent(end);
            Token token = new Token(start, end);
            if (token.isImportToken()) {
                hasImportToken = true;
            } else if (hasImportToken) {
                // Class body is the last token
                end = new Location(lines.size() - 1, lines.get(lines.size() - 1).length() - 1);
                tokens.add(new Token(start, end));
                return tokens;
            }
            tokens.add(token);
            if (end.column() < currentLine.length() - 1) {
                start = new Location(end.line(), end.column() + 1);
            } else {
                start = new Location(end.line() + 1, 0);
            }
        } while (true);
    }

    private Location nextDelimiter(Location location) {
        int lineIndex = location.line();
        String currentLine = lines.get(lineIndex);
        int lineComment = currentLine.indexOf("//", location.column());
        int multiLineComment = currentLine.indexOf("/*", location.column());
        int endColumn = currentLine.indexOf(';', location.column());
        while (endColumn < 0 && lineComment < 0 && multiLineComment < 0) {
            lineIndex++;
            currentLine = lines.get(lineIndex);
            lineComment = currentLine.indexOf("//");
            multiLineComment = currentLine.indexOf("/*");
            endColumn = currentLine.indexOf(';');
        }
        if (endColumn < 0) {
            endColumn = Integer.MAX_VALUE;
        }
        if (lineComment < 0) {
            lineComment = Integer.MAX_VALUE;
        }
        if (multiLineComment < 0) {
            multiLineComment = Integer.MAX_VALUE;
        }
        return new Location(lineIndex, min(endColumn, min(lineComment, multiLineComment)));
    }

    private Location findMultiLineEnd(Location location) {
        int lineIndex = location.line();
        String currentLine = lines.get(lineIndex);
        int column = currentLine.indexOf("*/", location.column());
        while (column < 0) {
            lineIndex++;
            currentLine = lines.get(lineIndex);
            column = currentLine.indexOf("*/");
        }
        return new Location(lineIndex, column);
    }

    private Location includeLineCommendIfPresent(Location end) {
        String currentLine = lines.get(end.line());
        Location potentialLineComment = nextDelimiter(new Location(end.line(), end.column() + 1));
        if (potentialLineComment.line() == end.line()
            && potentialLineComment.column() + 1 < currentLine.length()
            && "//".equals(currentLine.substring(potentialLineComment.column(), potentialLineComment.column() + 2))) {
            String inBetween = currentLine.substring(end.column() + 1, potentialLineComment.column());
            if (inBetween.isBlank()) {
                return new Location(end.line(), currentLine.length() - 1);
            }
        }
        return end;
    }

    class Token {
        private Location startLocation;
        private Location endLocation;

        Token(Location start, Location end) {
            startLocation = start;
            endLocation = end;
        }

        int startLine() {
            return startLocation.line();
        }

        int startColumn() {
            return startLocation.column();
        }

        int endLine() {
            return endLocation.line();
        }

        int endColumn() {
            return endLocation.column();
        }

        boolean isImportToken() {
            String line = toString().trim();
            if (line.startsWith("/*")) {
                line = line.substring(line.indexOf("*/") + 2).trim();
            }
            return line.startsWith("import") && isWhitespace(line.charAt("import".length()));
        }

        boolean isBlank() {
            return toString().isBlank();
        }

        public String toString() {
            StringBuilder line = new StringBuilder();
            for (int currentLine = startLocation.line(); currentLine <= endLocation.line(); currentLine++) {
                int startIndex = currentLine == startLocation.line() ? startLocation.column() : 0;
                int endIndex = currentLine == endLocation.line() ? endLocation.column() : lines.get(currentLine).length() - 1;
                line.append(lines.get(currentLine).substring(startIndex, endIndex + 1));
                line.append('\n');
            }
            line.deleteCharAt(line.length() - 1);
            return line.toString();
        }
    }

    class Location {
        int line;
        int column;

        Location(int line, int column) {
            this.line = line;
            this.column = column;
        }

        int line() {
            return line;
        }

        int column() {
            return column;
        }
    }
}
