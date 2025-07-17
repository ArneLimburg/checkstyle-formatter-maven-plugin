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

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.util.List;

import com.puppycrawl.tools.checkstyle.api.Violation;

public interface LineFormatter {

    List<String> format(Violation violation, List<String> lines);

    default String getLine(Violation violation, List<String> lines) {
        if (isEmpty(lines)) {
            return "";
        }
        return lines.get(violation.getLineNo() - 1);
    }

    default int getColumnNumber(Violation violation) {
        return violation.getColumnNo() - 1;
    }

    default int getLineNumber(Violation violation) {
        return violation.getLineNo() - 1;
    }
}
