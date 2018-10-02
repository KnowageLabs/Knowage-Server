# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security


## [6.2.0] - 2018-07-19
### Added
- New cockpit features (new selector/HTML widgets, dynamic text, chart switching and drilling, widgets cloning and others);
- Improvements on Meta designer: capability to exploit DBMS custom functions on calculated fields;
- New options for datasets on self service section;
- I18n support within cockpit and other user functionalities;
- Improvements on collaboration & sharing: user can easily get the direct link to an analysis or the HTML code to embed it.

### Fixed
- Many bugs fixed.

### Security
- New SSO mechanism based on a JWT token, enabled in default configuration;
- Fixed some XSS vulnerabilities.


## [6.1.1] - 2017-11-10
### Added
- [Docker](https://github.com/KnowageLabs/Knowage-Server-Docker/tree/master/6.1.1) image released
- [Chef](https://github.com/KnowageLabs/Knowage-Server-Chef/tree/master/6.1.1) deployment script released

### Fixed
- ChartJS and D3 now works with NGSI dataset inside dashboard
- Readthedocs typos


## [6.1.0] - 2017-10-25
### Added
- Cockpit features and general improvements
   - move a widget from tab to tab 
   - manage the look&feel 
   - 3d options for charts
   - new grouping options
   - new table widget with more options
   - new filter section on widget
   - selector widget has more style options
- Release of Dossier engine. The Dossier feature allows to automatically build a presentation with predefined reports
- New Real Time features on NGSI Data Set, which enables user to build real-time console by using the Cockpit engine

### Changed
- General improvements on Report Engine

### Fixed
- The installer now correctly creates the foodmart_demo database 
- Export of charts and dashboard to PDF and XLS 
- Removed dataset with parameters while using federator designer
-	Creation of gauge chart
- Restored correct credential for default login page
- Other minor bug fixes


## 6.0.0 - 2017-06-30

[Unreleased]: https://github.com/KnowageLabs/Knowage-Server/compare/v6.1.1...HEAD
[6.2.0]: https://github.com/KnowageLabs/Knowage-Server/compare/6.1.1...6.2.0
[6.1.1]: https://github.com/KnowageLabs/Knowage-Server/compare/6.1.0...6.1.1
[6.1.0]: https://github.com/KnowageLabs/Knowage-Server/compare/6.0.0...6.1.0
