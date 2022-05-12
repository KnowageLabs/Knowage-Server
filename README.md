<p align="center">
<img src="https://www.knowage-suite.com/site/wp-content/uploads/2016/03/KNOWAGE_logo_color.png">
</p>

[![License: APGL](https://img.shields.io/github/license/KnowageLabs/Knowage-Server.svg)](https://opensource.org/licenses/AGPL-3.0)
[![Docker badge](https://img.shields.io/docker/pulls/knowagelabs/knowage-server-docker.svg)](https://hub.docker.com/r/knowagelabs/knowage-server-docker/)
<br>
[![Documentation badge](https://img.shields.io/readthedocs/knowage.svg)](https://knowage.rtfd.io/)
[![Build Status](https://travis-ci.com/KnowageLabs/Knowage-Server.svg?branch=master)](https://travis-ci.com/KnowageLabs/Knowage-Server)

KNOWAGE is the open source analytics and business intelligence suite that allows you to combine traditional data and big/cloud data sources into valuable and meaningful information. Its features, such as data
federation, mash-up, data/text mining and advanced data visualization, give
comprehensive support to rich and multi-source data analysis. The suite is
composed of two main modules and four additional plugins that can be combined to ensure full coverage of user’ requirements.


KNOWAGE is now available on [FIWARE Marketplace](https://marketplace.fiware.org/) 
as FIWARE-ready software enabler, being fully compliant with [FIWARE](https://www.fiware.org/) 
architecture and GEs. For more information check the FIWARE Marketplace entry 
for [KNOWAGE](https://marketplace.fiware.org/pages/solutions/59611fb5573b7cb51c44ef68).

|  :books: [Documentation](https://knowage-suite.rtfd.io/) | :page_facing_up: [Site](https://www.knowage-suite.com/site/home/) | :whale: [Docker Hub](https://hub.docker.com/r/knowagelabs/knowage-server-docker/) | :dart: [Roadmap](https://github.com/KnowageLabs/Knowage-Server/blob/master/ROADMAP.md) |


## Contents

-   [Modules and plugins available](#modules-and-plugins-available)
-   [Editions](#editions)
-   [Install](#install)
-   [Usage](#usage)
-   [Contributions](#contributions)
-   [Documentation](#documentation)
-   [More](#More)
-   [Testing](#testing)
-   [License](#license)


## Modules and plugins available

|                                                   | Name                   | Description                                                                                                              |
| ------------------------------------------------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| ![SI](/images/modules/SI-40x40.jpg?raw=true "SI") | Smart Intelligence     | The usual business intelligence on structured data, but more oriented to self-service capabilities and agile prototyping |
| ![ER](/images/modules/ER-40x40.jpg?raw=true "ER") | Enterprise Reporting   | To produce and distribute static reports                                                                                 |
| ![LI](/images/plugins/LI-40x40.png?raw=true "LI") | Location Intelligence  | To relate business data with spatial or geographical information                                                         |
| ![PM](/images/plugins/PM-40x40.png?raw=true "PM") | Performance Management | To manage KPIs and organize scorecards, to monitor your business in real-time                                            |
| ![CA](/images/plugins/CA-40x40.png?raw=true "CA") | Custom Analytics       | To add what-if capabilities e take full advantage of R/python embedding possibilities                                    |
| ![SD](/images/plugins/SD-40x40.png?raw=true "SD") | Smart Data             | To combine Solr index with other data sources and provide faceted views and full text search                             |

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

An installer for Windows and Linux environments is available on [KNOWAGE website](https://www.knowage-suite.com) within the download area.

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
- JDK 8
- [Maven 3](https://maven.apache.org/)
- [NodeJS 8 or greater](https://nodejs.org/)

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
 - ``knowagecommonjengine/target/knowagecommonjengine.war``
 - ``knowagedataminingengine/target/knowagedataminingengine.war``
 - ``knowagegeoreportengine/target/knowagegeoreportengine.war``
 - ``knowagejasperreportengine/target/knowagejasperreportengine.war``
 - ``knowagekpiengine/target/knowagekpiengine.war``
 - ``knowagemeta/target/knowagemeta.war``
 - ``knowageqbeengine/target/knowageqbeengine.war``
 - ``knowagesdk/target/knowagesdk.war``
 - ``knowagesvgviewerengine/target/knowagesvgviewerengine.war``
 - ``knowagetalendengine/target/knowagetalendengine.war``
 - ``knowagewhatifengine/target/knowagewhatifengine.war``

There is no need to do anything in order to build KNOWAGE-Python module: you can just take the source code and run it as a standalone program (refer to the official docs: https://knowage-suite.readthedocs.io/en/7.4/installation-guide/python-installation.html).

## Testing

To run tests, type

```console
mvn test -DskipTests=false
```

from ``knowage-ce-parent`` folder.

## License

[AGPL](LICENSE) © 2021 Engineering Ingegneria Informatica S.p.A.
