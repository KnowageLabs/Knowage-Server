<p align="center">
<img src="https://www.knowage-suite.com/site/wp-content/uploads/2016/03/KNOWAGE_logo_color.png">
</p>

[![FIWARE Visualization](https://nexus.lab.fiware.org/static/badges/chapters/visualization.svg)](https://www.fiware.org/developers/catalogue/)
[![License: APGL](https://img.shields.io/github/license/KnowageLabs/Knowage-Server.svg)](https://opensource.org/licenses/AGPL-3.0)
[![Docker badge](https://img.shields.io/docker/pulls/fiware/knowage-server-docker.svg)](https://hub.docker.com/r/fiware/knowage-server-docker)
[![Support badge](https://nexus.lab.fiware.org/static/badges/stackoverflow/knowage.svg)](https://stackoverflow.com/questions/tagged/fiware-knowage)
<br>
[![Documentation badge](https://img.shields.io/readthedocs/knowage.svg)](https://knowage.rtfd.io/)
[![Build Status](https://travis-ci.com/KnowageLabs/Knowage-Server.svg?branch=master)](https://travis-ci.com/KnowageLabs/Knowage-Server)
![Status](https://nexus.lab.fiware.org/static/badges/statuses/knowage.svg)

Knowage is the full capabilities open source suite for modern business analytics
over traditional sources and big data systems. Its features, such as data
federation, mash-up, data/text mining and advanced data visualization, give
comprehensive support to rich and multi-source data analysis. The suite is
composed of several modules, each one conceived for a specific analytical
domain. They can be used individually as complete solution for a certain task,
or combined with one another to ensure full coverage of user’ requirements.


This project is part of [FIWARE](https://www.fiware.org/). For more information
check the FIWARE Catalogue entry for
[Context Processing, Analysis and Visualization](https://github.com/Fiware/catalogue/tree/master/processing).

|  :books: [Documentation](http://knowage.rtfd.io/) | :page_facing_up: [Site](https://www.knowage-suite.com/site/home/) | :mortar_board: [Academy](https://fiware-academy.readthedocs.io/en/latest/processing/knowage) | :whale: [Docker Hub](https://hub.docker.com/r/fiware/knowage-server-docker/) | :dart: [Roadmap](https://github.com/KnowageLabs/Knowage-Server/blob/master/ROADMAP.md) |
|---|---|---|---|---|

## Modules available

|                                                                                                                    | Name                   | Description                                                                                                              |
| ------------------------------------------------------------------------------------------------------------------ | ---------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| <img src="http://www.knowage-suite.com/site/wp-content/uploads/2016/03/BD_txt-150x150.png" alt="BD" width="50px"/> | Big Data               | To analyse data stored on big data clusters or NoSQL databases                                                           |
| <img src="http://www.knowage-suite.com/site/wp-content/uploads/2016/03/SI_txt-150x150.png" alt="SI" width="50px"/> | Smart Intelligence     | The usual business intelligence on structured data, but more oriented to self-service capabilities and agile prototyping |
| <img src="http://www.knowage-suite.com/site/wp-content/uploads/2016/03/ER_txt-150x150.png" alt="ER" width="50px"/> | Enterprise Reporting   | To produce and distribute static reports                                                                                 |
| <img src="http://www.knowage-suite.com/site/wp-content/uploads/2018/09/LI_txt-150x150.png" alt="LI" width="50px"/> | Location Intelligence  | To relate business data with spatial or geographical information                                                         |
| <img src="http://www.knowage-suite.com/site/wp-content/uploads/2016/03/PM_txt-150x150.png" alt="PM" width="50px"/> | Performance Management | To manage KPIs and organize scorecards, to monitor your business in real-time                                            |
| <img src="http://www.knowage-suite.com/site/wp-content/uploads/2016/03/PA_txt-150x150.png" alt="PA" width="50px"/> | Predictive Analysis    | To perform advanced analyses for forecasting and prescriptive purposes                                                   |

Knowage supports a modern vision of the data analytics, providing new
self-service capabilities that give autonomy to the end-user, now able to build
his own analysis and explore his own data space, also combining data that come
from different sources.

## Editions

Knowage is available on two versions:

-   the community edition, with the whole set of analytical capabilities, it is
    part of the software stack managed by [OW2](https://www.ow2.org/) and the
    reference implementation of the Data Visualization GE in
    [FIWARE](https://www.fiware.org/) (see the
    [FIWARE catalogue entry](https://catalogue.fiware.org/enablers/data-visualization-knowage)
    for details), as SpagoBI was;
-   the enterprise edition, provided and guaranteed directly from Engineering
    Group - the leading Italian software and services company - with a
    commercial offering and some facilities for the administrator.

This repository contains the source code of the Community Edition.

## Contributions

Before start to contribute, please read and sign the
[Contributor License Agreement](https://www.clahub.com/agreements/KnowageLabs/Knowage-Server).

## Documentation

The official documentantion is available at
[Read the Docs](http://knowage-suite.readthedocs.io/).

## More

Please visit [the project website](http://www.knowage-suite.com) for information
about the Enterprise Edition.

## Quality Assurance

This project is part of [FIWARE](https://fiware.org/) and has been rated as
follows:

-   **Version Tested:**
    ![ ](https://img.shields.io/badge/dynamic/json.svg?label=Version&url=https://fiware.github.io/catalogue/json/knowage.json&query=$.version&colorB=blue)
-   **Documentation:**
    ![ ](https://img.shields.io/badge/dynamic/json.svg?label=Completeness&url=https://fiware.github.io/catalogue/json/knowage.json&query=$.docCompleteness&colorB=blue)
    ![ ](https://img.shields.io/badge/dynamic/json.svg?label=Usability&url=https://fiware.github.io/catalogue/json/knowage.json&query=$.docSoundness&colorB=blue)
-   **Responsiveness:**
    ![ ](https://img.shields.io/badge/dynamic/json.svg?label=Time%20to%20Respond&url=https://fiware.github.io/catalogue/json/knowage.json&query=$.timeToCharge&colorB=blue)
    ![ ](https://img.shields.io/badge/dynamic/json.svg?label=Time%20to%20Fix&url=https://fiware.github.io/catalogue/json/knowage.json&query=$.timeToFix&colorB=blue)
-   **FIWARE Testing:**
    ![ ](https://img.shields.io/badge/dynamic/json.svg?label=Tests%20Passed&url=https://fiware.github.io/catalogue/json/knowage.json&query=$.failureRate&colorB=blue)
    ![ ](https://img.shields.io/badge/dynamic/json.svg?label=Scalability&url=https://fiware.github.io/catalogue/json/knowage.json&query=$.scalability&colorB=blue)
    ![ ](https://img.shields.io/badge/dynamic/json.svg?label=Performance&url=https://fiware.github.io/catalogue/json/knowage.json&query=$.performance&colorB=blue)
    ![ ](https://img.shields.io/badge/dynamic/json.svg?label=Stability&url=https://fiware.github.io/catalogue/json/knowage.json&query=$.stability&colorB=blue)

---

## License

[AGPL](LICENSE) © 2018 Engineering Ingegneria Informatica S.p.A.
