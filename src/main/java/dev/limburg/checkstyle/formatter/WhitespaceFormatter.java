/*
 * Copyright 2025 Arne Limburg, Steffen Pieper.
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

import java.util.ArrayList;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.Violation;

public class WhitespaceFormatter implements LineFormatter {

    @Override
    public List<String> format(Violation violation, List<String> content) {
        List<String> lines = new ArrayList<>(content);
        int lineNo = violation.getLineNo() - 1;
        int columnNo = violation.getColumnNo() - 1;
        String line = lines.get(lineNo);
        lines.set(lineNo, formatLine(violation.getKey(), line, columnNo));
        return lines;
    }

    private String formatLine(String key, String line, int column) {
        if ("ws.notFollowed".equals(key) && !hasWhitespaceAfter(line, column)) {
            return line.substring(0, column + 1) + " " + line.substring(column + 1);
        } else if ("ws.notPreceded".equals(key) && !hasWhitespaceBefore(line, column)) {
            return line.substring(0, column) + " " + line.substring(column);
        } else if ("ws.followed".equals(key) && hasWhitespaceAfter(line, column)) {
            return line.substring(0, column + 1) + line.substring(column + 2);
        } else if ("ws.preceded".equals(key) && hasWhitespaceBefore(line, column)) {
            return line.substring(0, column - 1) + line.substring(column);
        } else {
            // already corrected
            return line;
        }
    }

    private boolean hasWhitespaceBefore(String line, int column) {
        return column > 0 && isWhitespace(line.charAt(column - 1));
    }

    private boolean hasWhitespaceAfter(String line, int column) {
        return column + 1 < line.length() && isWhitespace(line.charAt(column + 1));
    }
}
