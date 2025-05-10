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

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.Violation;

class AuditEventComparatorTest {

    @Test
    void compareTo() {
        // Given
        AuditEventComparator comparator = new AuditEventComparator();
        AuditEvent firstEvent = new AuditEvent(this, null,
            new Violation(1, 2, "bundle", "violation.key", new Object[0], "module", AuditEventComparatorTest.class, "message"));
        AuditEvent secondEvent = new AuditEvent(this, null,
            new Violation(1, 3, "bundle", "violation.key", new Object[0], "module", AuditEventComparatorTest.class, "message"));
        AuditEvent thirdEvent = new AuditEvent(this, null,
            new Violation(1, 1, "bundle", "violation.key", new Object[0], "module", AuditEventComparatorTest.class, "message"));
        AuditEvent fourthEvent = new AuditEvent(this, null,
            new Violation(2, 2, "bundle", "violation.key", new Object[0], "module", AuditEventComparatorTest.class, "message"));
        List<AuditEvent> events = new ArrayList<>(of(firstEvent, secondEvent, thirdEvent, fourthEvent));

        // When
        Collections.sort(events, comparator);

        // Then
        assertEquals(of(fourthEvent, secondEvent, firstEvent, thirdEvent), events);
    }
}
