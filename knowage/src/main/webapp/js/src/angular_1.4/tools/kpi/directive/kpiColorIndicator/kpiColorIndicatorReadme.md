# KpiColorIndicator
Directive used to create and to present scorecard KPI representations.

## Dependencies
```
angular.js
```

## Setup
Import the directive, there is no need to inject in controller.
Use the directive inserting the following tag:
```
<kpi-color-indicator perspectives="perspectiveObject" definition="true" criterion="criterionObject"></kpi-color-indicator>
```

## Settings
> perspectives : <object>

this attribute is mandatory and it should contain the perspectives and relative targets object model.

```
{"perspectives":[
    {
        "id": int
        "name": string
        "criterion": <object>,
        "groupedKpis": Array[<object>],
        "options": <object>,
        "status": string ["grey","green", "yellow","red"],
        "targets": Array[<object>]
    },
    {...}
]}
```

> definition : boolean 

This attribute is mandatory and is TRUE if the directive is a scorecard definition, FALSE if is a scorecard visualization.
The definition visualization permit the user to create new perspective, populate targets and put new kpis inside. In the definition visualization is possible to remove and rename elements too with the input-rename inner directive.
> criterion : <object>

This attribute is mandatory, it's the list of all the criterion items that will be available to the perspectives and kpi.
This attribute is needed because every criterion has a different dynamic id in the db, so they will be correctly connected.

###Customization
It's possible to change the behaviour of the directive changing the sass component _scorecardKpiDefinition.scss.

The icons are set inside the template and are the ones from [font-awesome](http://fontawesome.io/icons/).
We used the escaped unicode value inside the template navigator object.

## Authors

* **Davide Vernassa** - *Knowage* - [Redjaw](https://github.com/Redjaw)
* **Alessandro Portosa** - *Knowage* - [aportosa](https://github.com/aportosa)

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