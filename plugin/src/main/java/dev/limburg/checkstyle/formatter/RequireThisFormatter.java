/*
 * Copyright 2025 Steffen Pieper, Arne Limburg.
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

public class RequireThisFormatter implements LineFormatter {
    @Override
    public List<String> format(Violation violation, List<String> lines) {
        List<String> modifiableLines = new ArrayList<>(lines);
        String lineToChange = getLine(violation, lines);
        int columnNo = getColumnNumber(violation);

        String formattedLine = lineToChange.substring(0, columnNo) + "this." + lineToChange.substring(columnNo);
        modifiableLines.set(getLineNumber(violation), formattedLine);
        return modifiableLines;
    }
}
