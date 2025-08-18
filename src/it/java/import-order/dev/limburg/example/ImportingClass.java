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
package dev.limburg.example;

import static org.slf4j.LoggerFactory.getLogger;import org.slf4j.Logger;
 import java. io. IOException; // line comment
import java. /* here is a comment */io.InputStream; import java.nio.file.Path;
/*
import java.nio.file.Path;
 */
import static java.nio.file.Files.newInputStream; import // line comment
static org.apache.commons.lang3.Validate.notEmpty; /*
multi line comment
*/ import org.apache.commons.lang3.Validate;
/* comment at beginning */ import java.lang.String;

public class ImportingClass {

    private static final Logger LOG = getLogger(App.class);

    public static void main(String[] args) {
        Validate.noNullElements(args);
        notEmpty(args);
        try (InputStream in = newInputStream(Path.of(args[0]))) {
            in.read();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
