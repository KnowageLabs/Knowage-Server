<p align="center">
<img src="https://www.knowage-suite.com/site/wp-content/uploads/2016/03/KNOWAGE_logo_color.png">
</p>

[![License: APGL](https://img.shields.io/github/license/KnowageLabs/Knowage-Server.svg)](https://opensource.org/licenses/AGPL-3.0)
[![Docker badge](https://img.shields.io/docker/pulls/knowagelabs/knowage-server-docker.svg)](https://hub.docker.com/r/knowagelabs/knowage-server-docker/)
<br>
[![Documentation badge](https://img.shields.io/readthedocs/knowage.svg)](https://knowage.rtfd.io/)
[![Build Status](https://travis-ci.com/KnowageLabs/Knowage-Server.svg?branch=master)](https://travis-ci.com/KnowageLabs/Knowage-Server)

Knowage is the full capabilities open source suite for modern business analytics
over traditional sources and big data systems. Its features, such as data
federation, mash-up, data/text mining and advanced data visualization, give
comprehensive support to rich and multi-source data analysis. The suite is
composed of several modules, each one conceived for a specific analytical
domain. They can be used individually as complete solution for a certain task,
or combined with one another to ensure full coverage of user’ requirements.


Knowage is now available on [FIWARE Marketplace](https://marketplace.fiware.org/) 
as FIWARE-ready software enabler, being fully compliant with [FIWARE](https://www.fiware.org/) 
architecture and GEs. For more information check the FIWARE Marketplace entry 
for [Knowage](https://marketplace.fiware.org/pages/solutions/59611fb5573b7cb51c44ef68).

|  :books: [Documentation](https://knowage-suite.rtfd.io/) | :page_facing_up: [Site](https://www.knowage-suite.com/site/home/) | :whale: [Docker Hub](https://hub.docker.com/r/knowagelabs/knowage-server-docker/) | :dart: [Roadmap](https://github.com/KnowageLabs/Knowage-Server/blob/master/ROADMAP.md) |


## Contents

-   [Modules available](#modules-available)
-   [Editions](#editions)
-   [Install](#install)
-   [Usage](#usage)
-   [Contributions](#contributions)
-   [Documentation](#documentation)
-   [More](#More)
-   [Testing](#testing)
-   [License](#license)


## Modules available

|                                                   | Name                   | Description                                                                                                              |
| ------------------------------------------------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| ![BD](/images/modules/BD-40x40.jpg?raw=true "BD") | Big Data               | To analyse data stored on big data clusters or NoSQL databases                                                           |
| ![SI](/images/modules/SI-40x40.jpg?raw=true "SI") | Smart Intelligence     | The usual business intelligence on structured data, but more oriented to self-service capabilities and agile prototyping |
| ![ER](/images/modules/ER-40x40.jpg?raw=true "ER") | Enterprise Reporting   | To produce and distribute static reports                                                                                 |
| ![LI](/images/modules/LI-40x40.jpg?raw=true "LI") | Location Intelligence  | To relate business data with spatial or geographical information                                                         |
| ![PM](/images/modules/PM-40x40.jpg?raw=true "PM") | Performance Management | To manage KPIs and organize scorecards, to monitor your business in real-time                                            |
| ![PA](/images/modules/PA-40x40.jpg?raw=true "PA") | Predictive Analysis    | To perform advanced analyses for forecasting and prescriptive purposes                                                   |

Knowage supports a modern vision of the data analytics, providing new
self-service capabilities that give autonomy to the end-user, now able to build
his own analysis and explore his own data space, also combining data that come
from different sources.

## Editions

Knowage is available on two versions:

-   the community edition, with the whole set of analytical capabilities, it is
    part of the software stack managed by [OW2](https://www.ow2.org/) as SpagoBI was;
-   the enterprise edition, provided and guaranteed directly from Engineering
    Group - the leading Italian software and services company - with a
    commercial offering and some facilities for the administrator.

This repository contains the source code of the Community Edition.

## Install

Information about how to install Knowage is available on official documentation on [Read the Docs](http://knowage-suite.readthedocs.io/) within Installation & Administration Manuals.

An installer for Windows and Linux environments is available on [Knowage website](https://www.knowage-suite.com) within the download area.

A `Dockerfile` is also available for your use - further information can be found [here](https://github.com/KnowageLabs/Knowage-Server-Docker).

## Usage

Information about how to use Knowage is available on official documentation on [Read the Docs](http://knowage-suite.readthedocs.io/) within User Guide and Functionalities sections.

## Contributions

Knowage is open to external contributions. You can submit your contributions into this repository through pull requests.
Before starting, here there are a few things you must be aware of: 

-   This project is released with a [Contributor Code of Conduct](./CODE_OF_CONDUCT.md). By participating in this
    project, you agree to abide by its terms.
-   When you open a pull request, you must sign the
    [Individual Contributor License Agreement](./CLA.md) by stating in a comment 
	_"I have read the CLA Document and I hereby sign the CLA"_
-   Please ensure that your contribution passes all tests. If there are test failures, you will need to address them
    before we can merge your contribution.

## Documentation

The official documentantion is available at
[Read the Docs](http://knowage-suite.readthedocs.io/).

## More

Please visit [the project website](http://www.knowage-suite.com) for information
about the Enterprise Edition.

## Testing

To run tests, type

```console
mvn test -DskipTests=false
```

from knowage-ce-parent folder.

## License

[AGPL](LICENSE) © 2021 Engineering Ingegneria Informatica S.p.A.
