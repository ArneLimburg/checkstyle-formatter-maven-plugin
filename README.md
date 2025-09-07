[![maintained](https://img.shields.io/badge/Maintained-yes-brightgreen.svg)](https://github.com/ArneLimburg/checkstyle-formatter-maven-plugin/graphs/commit-activity)
[![Maven Central](https://img.shields.io/maven-central/v/dev.limburg.checkstyle/checkstyle-formatter-maven-plugin.svg)](https://search.maven.org/artifact/dev.limburg.checkstyle/checkstyle-formatter-maven-plugin)
![build](https://github.com/ArneLimburg/checkstyle-formatter-maven-plugin/workflows/build/badge.svg) 

# Checkstyle Formatter Maven Plugin

This is a maven plugin to format code with checkstyle rules.

## Features

Currently the following rules are formatted automatically:
- [Final Parameters](https://checkstyle.sourceforge.io/checks/misc/finalparameters.html)
- [Import Order](https://checkstyle.sourceforge.io/checks/imports/importorder.html)
- [Indentation](https://checkstyle.sourceforge.io/checks/misc/indentation.html)
- [File Tab Character](https://checkstyle.sourceforge.io/checks/whitespace/filetabcharacter.html)
- [Generic Whitespace](https://checkstyle.sourceforge.io/checks/whitespace/genericwhitespace.html)
- [NoWhitespaceBefore](https://checkstyle.sourceforge.io/checks/whitespace/nowhitespacebefore.html)
- [NoWhitespaceAfter](https://checkstyle.sourceforge.io/checks/whitespace/nowhitespaceafter.html)
- [WhitespaceAfter](https://checkstyle.sourceforge.io/checks/whitespace/whitespaceafter.html)
- [WhitespaceAround](https://checkstyle.sourceforge.io/checks/whitespace/whitespacearound.html)

## Configuration

The plugin is configured like the checkstyle plugin (from which it is derived).
See in their documentation for configuration options: [Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/).
There is one more configuration option: You can configure, which new line character will be chosen when writing the files. You can do this with the `checkstyleFormatter.lineEnding`property.

## Reporting a bug

When you report a bug, please come up with a pull-request that demonstrates the bug with a failing test:

1. Create a source tree in `src/it/java` with the code that is not formatted correctly
and another source tree with the code with the expected formatting result.

2. Create a test that references your source tree:

```
import org.junit.Rule;
import org.junit.Test;

import dev.limburg.checkstyle.CheckstyleFormatterRule;

public class YourTest {

    @Rule
    public CheckstyleFormatterRule rule = new CheckstyleFormatterRule();

    @Test
    public void formatFinalParameter() throws Exception {
        rule.given("src/it/java/your/source/tree/with/the/failing/code");
        rule.whenExecuteFormatting();
        rule.thenResultIsSameAs("src/it/java/your/source/tree/with/the/expected/result");
    }
}
```

The created test should fail. This demonstrates the bug.

## Contributing

We always implement formatters test-driven.
So when you want to contribute a formatter, please first create a failing test like described in "Reporting a bug".
Then implement your formatter by implementing the interface `LineFormatter` 
and register it in the class FileFormatter with the corresponding key of the formatted checkstyle rule.
