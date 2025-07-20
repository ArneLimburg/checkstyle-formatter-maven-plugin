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
        switch (key) {
            case "ws.notFollowed":
                return line.substring(0, column + 1) + " " + line.substring(column + 1);
            case "ws.notPreceded":
                return line.substring(0, column) + " " + line.substring(column);
            case "ws.followed":
                return line.substring(0, column + 1) + line.substring(column + 2);
            case "ws.preceded":
                return line.substring(0, column - 1) + line.substring(column);
            default:
                return line;
        }
    }
}
