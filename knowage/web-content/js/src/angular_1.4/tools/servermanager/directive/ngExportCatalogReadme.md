# ngExportCatalog
Directive to show catalogs' elements available for export functionality

## Dependencies
```
angular.js
```


##Setup
Import the directive, there is no need to inject in controller.

##Settings
> 



###Customization
> type-catalog

This variable sets the type of the catalog managed. At the moment are available 'Dataset', 'BusinessModel',
'MondrianSchema','SVG','Layer', 'AnalyticalDrivers'

> path-catalog

This variable sets the final path to define the url for calling REST services. 
Example with "catalog" value the url will be: /1.0/serverManager/importExport/catalog.

> catalog-data

This variable sets the content of the list

> catalog-selected

This variable will contains all the selected elements of the list

## Authors

* **Antonella Giachino** - *Knowage* - [sbigiachino](https://github.com/sbigiachino)

##License

Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.