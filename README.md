# GoToProject

Plugin for IntelliJ Idea

Install it via IntelliJs plugin system or via the plugin store https://plugins.jetbrains.com/plugin/7359-go-to-project

## Development

### Code Quality

This project uses [detekt](https://github.com/detekt/detekt) for static code analysis of Kotlin code.

#### Running Detekt Locally

To run detekt locally and generate reports:

```bash
./gradlew detektAll
```

This will:
- Run detekt analysis on the whole project
- Generate HTML and SARIF reports in `build/reports/detekt/`
- Use the configuration defined in `config/detekt/detekt.yml`

#### GitHub Integration

The project is configured to run detekt analysis on GitHub via GitHub Actions:
- Analysis runs on push to master, pull requests, and on a weekly schedule
- Results are uploaded to GitHub and visible in the Security tab
- The same configuration is used for both local and GitHub runs
