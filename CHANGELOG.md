# Go To Project Changelog

## [Unreleased]

### Fixed

- Opening a recent project from the Search Everywhere "Projects" tab now behaves like the IDE's own Recent Projects
  list.
- Entries in the recent projects list that aren't regular projects no longer break the popup.

## [1.6.0] - 2026-07-18

### Added

- Project lists show the current git branch and mark linked worktrees.

### Changed

- Matching in the Search Everywhere "Projects" tab is more fuzzy.

## [1.5.0] - 2026-05-02

### Fixed

- Matched characters are highlighted correctly in the Search Everywhere "Projects" tab.

## [1.4.1] - 2026-02-26

### Fixed

- Removed the use of internal Search Everywhere statistics API.

## [1.4.0] - 2026-02-22

Thanks to [ChrisCarini](https://github.com/ChrisCarini).

### Added

- Option for showing a "Projects" tab in Search Everywhere, and for opening it by default from the "Go to Project"
  action.

### Changed

- "Go to Last Project" is disabled when there is no previous project.

[Unreleased]: https://github.com/erikzielke/GoToProject/compare/v1.6.0...HEAD
[1.6.0]: https://github.com/erikzielke/GoToProject/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/erikzielke/GoToProject/compare/v1.4.1...v1.5.0
[1.4.1]: https://github.com/erikzielke/GoToProject/compare/v1.4.0...v1.4.1
[1.4.0]:https://github.com/erikzielke/GoToProject/commits/v1.4.0
