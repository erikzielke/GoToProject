# GoToProject

Plugin for IntelliJ Idea

Install it via IntelliJs plugin system or via the plugin store https://plugins.jetbrains.com/plugin/7359-go-to-project

## Development

### Code Quality

This project uses [detekt](https://github.com/detekt/detekt) for static code analysis of Kotlin code, [Kover](https://github.com/Kotlin/kotlinx-kover) for code coverage reporting, and [Spotless](https://github.com/diffplug/spotless) with [ktlint](https://github.com/pinterest/ktlint) for code formatting.

#### Running Detekt Locally

To run detekt locally and generate reports:

```bash
./gradlew detektAll
```

This will:
- Run detekt analysis on the whole project
- Generate HTML and SARIF reports in `build/reports/detekt/`
- Use the configuration defined in `config/detekt/detekt.yml`

#### Code Coverage with Kover

This project uses Kover to measure code coverage of Kotlin code. The minimum required coverage is set to 10%.

To run tests with coverage and generate reports:

```bash
./gradlew koverHtmlReport   # Generates HTML report
./gradlew koverXmlReport    # Generates XML report
./gradlew koverVerify       # Verifies coverage meets minimum threshold
```

Reports are generated in `build/reports/kover/`.

#### Code Formatting with Spotless

This project uses Spotless with ktlint to enforce consistent code formatting across the codebase. The formatting rules are applied to all Kotlin files and Gradle Kotlin script files.

To check if your code follows the formatting rules:

```bash
./gradlew spotlessCheck
```

To automatically fix formatting issues:

```bash
./gradlew spotlessApply
```

#### GitHub Integration

The project is configured to run detekt analysis and Spotless checks on GitHub via GitHub Actions:
- Analysis runs on push to master, pull requests, and on a weekly schedule
- Results are uploaded to GitHub and visible in the Security tab
- The same configuration is used for both local and GitHub runs
- Code formatting is checked as part of the CI process
