<p align="center">
<img src="https://www.knowage-suite.com/site/wp-content/uploads/2016/03/KNOWAGE_logo_color.png">
</p>

[![License: APGL](https://img.shields.io/github/license/KnowageLabs/Knowage-Server.svg)](https://opensource.org/licenses/AGPL-3.0)
[![Docker badge](https://img.shields.io/docker/pulls/knowagelabs/knowage-server-docker.svg)](https://hub.docker.com/r/knowagelabs/knowage-server-docker/)
<br>
[![Documentation badge](https://img.shields.io/readthedocs/knowage.svg)](https://knowage.rtfd.io/)

KNOWAGE is the open source analytics and business intelligence suite that allows you to combine traditional data and big/cloud data sources into valuable and meaningful information. Its features, such as data
federation, mash-up, data/text mining and advanced data visualization, give
comprehensive support to rich and multi-source data analysis.


|  :books: [Documentation](https://knowage-suite.rtfd.io/) | :page_facing_up: [Site](https://www.knowage-suite.com/site/home/) | :whale: [Docker Hub](https://hub.docker.com/r/knowagelabs/knowage-server-docker/) | :dart: [Roadmap](https://github.com/KnowageLabs/Knowage-Server/blob/master/ROADMAP.md) |


## Contents

-   [Main functionlities](#main-functionlities)
-   [Editions](#editions)
-   [Install](#install)
-   [Usage](#usage)
-   [Contributions](#contributions)
-   [Documentation](#documentation)
-   [More](#More)
-   [Testing](#testing)
-   [License](#license)


## Main functionlities

| Name                   | Description                                                                                                              |
 ---------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| On line Dashboarding    | The dashboard is an interactive tool designed for visualizing information retrieved from data sets |
| Reporting   | The advanced reporting capabilities, enabling users to create, customize, and distribute interactive reports based on diverse data sources.    |
| OLAP  | This function allow users to explore multidimensional data interactively, enabling drill-down, slice-and-dice, and pivot operations for in-depth analysis   |
| KPI | Knowage enables KPI management by defining, monitoring, and visualizing key performance indicators to track business objectives and performance trends   |
| Data Preparation       | Knowage provides data preparation tools to clean, transform, and enrich raw data, ensuring quality and consistency for advanced analytics and reporting.    |
| Python integration             | Knowage supports Python integration, allowing users to execute scripts, apply advanced analytics, and embed custom algorithms directly within the BI environment     |
| Dossier             | Knowage allows users to create dossiers by combining multiple reports and documents into a single, organized and interactive view for comprehensive analysis     |

KNOWAGE supports a modern vision of the data analytics, providing new
self-service capabilities that give autonomy to the end-user, now able to build
his own analysis and explore his own data space, also combining data that come
from different sources.

## Editions

KNOWAGE is available on two versions:

-   the community edition, with the whole set of analytical capabilities, it is
    part of the software stack managed by [OW2](https://www.ow2.org/) as SpagoBI was;
-   the enterprise edition, provided and guaranteed directly from Engineering
    Group - the leading Italian software and services company - with a
    commercial offering and some facilities for the administrator.

This repository contains the source code of the Community Edition.

## Install

Information about how to install KNOWAGEis available on official documentation on [Read the Docs](http://knowage-suite.readthedocs.io/) within Installation & Administration Manuals.

A `Dockerfile` is also available for your use - further information can be found [here](https://github.com/KnowageLabs/Knowage-Server-Docker).

## Usage

Information about how to use KNOWAGE is available on official documentation on [Read the Docs](http://knowage-suite.readthedocs.io/) within User Guide and Functionalities sections.

## Contributions

KNOWAGE is open to external contributions. You can submit your contributions into this repository through pull requests.
Before starting, here there are a few things you must be aware of: 

-   This project is released with a [Contributor Code of Conduct](./CODE_OF_CONDUCT.md). By participating in this
    project, you agree to abide by its terms.
-   When you open a pull request, you must sign the
    [Individual Contributor License Agreement](./CLA.md) by stating in a comment 
	_"I have read the CLA Document and I hereby sign the CLA"_
-   Please ensure that your contribution passes all tests. If there are test failures, you will need to address them
    before we can merge your contribution.

## Documentation

The official documentation is available at
[Read the Docs](http://knowage-suite.readthedocs.io/).

## More

Please visit [the project website](https://www.knowage-suite.com) for information
about the Enterprise Edition.

## Build

To build KNOWAGE you need:
- JDK 17
- [Maven 3](https://maven.apache.org/)
- [NodeJS 22 or greater](https://nodejs.org/)

N.B.: You need to add the paths containing your ``java``, ``node`` and ``npm`` commands to your ``PATH`` environment variable: see the official documentation of your operating system.

The main project is ``knowage-ce-parent`` and from within that directory you need to launch:

```console
mvn package
```

At the end of the build you will find all the WAR files at the following paths:
 - ``knowage/target/knowage.war``
 - ``knowage-api/target/knowage-api.war``
 - ``knowage-vue/target/knowage-vue.war``
 - ``knowagebirtreportengine/target/knowagebirtreportengine.war``
 - ``knowagecockpitengine/target/knowagecockpitengine.war``
 - ``knowagejasperreportengine/target/knowagejasperreportengine.war``
 - ``knowagekpiengine/target/knowagekpiengine.war``
 - ``knowagemeta/target/knowagemeta.war``
 - ``knowageqbeengine/target/knowageqbeengine.war``
 - ``knowagetalendengine/target/knowagetalendengine.war``
 - ``knowagewhatifengine/target/knowagewhatifengine.war``

There is no need to do anything in order to build KNOWAGE-Python module: you can just take the source code and run it as a standalone program (refer to the official docs: https://knowage-suite.readthedocs.io/en/7.4/installation-guide/python-installation.html).

## Testing

To run tests, type

```console
mvn test -DskipTests=false
```

from ``knowage-ce-parent`` folder.

This project is tested with Browserstack.

## License

[AGPL](LICENSE) © 2021 Engineering Ingegneria Informatica S.p.A.
