/*
 * Copyright 2025 Steffen Pieper.
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

public class ImportGroupsFormatter implements LineFormatter {

    // Applies only, when imports are already ordered.
    @Override
    public boolean canApply(Violation violation, List<Violation> violations) {
        return violations.stream().map(Violation::getKey).noneMatch("import.ordering"::equals);
    }

    @Override
    public List<String> format(Violation violation, List<String> lines) {
        List<String> modifiableList = new ArrayList<>(lines);
        int lineNo = violation.getLineNo() - 2;
        String wrongImportStatement = lines.get(lineNo);
        if (wrongImportStatement.isBlank()) {
            modifiableList.remove(lineNo);
        }
        return modifiableList;
    }
}
