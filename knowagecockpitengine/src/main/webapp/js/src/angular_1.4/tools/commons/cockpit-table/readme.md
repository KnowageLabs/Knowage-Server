# CockpitTable
Directive used to create a custom table with multiple different option and settings for the Knowage Cockpit.

## Dependencies
```
angular.js
angular-material.js
```
in npm
```
npm install angular
npm install angular-material
```

## Setup
Import the directive, inject the module "cockpitTable" in the main module declaration.
Use the directive inserting the following tag:
```
<cockpit-table
	model=itemList
	columns=ngModel.content.columnSelectedOfDataset
	click-function = "selectRow(row,column,evt)"
	settings=ngModel.settings
	styledata=ngModel.style
></cockpit-table>
```

## Settings
> model : [< objectsArray >]

This attribute is mandatory and it should contain the model for the table rows.
The structure should be similar to the following one where the key is the same as column.name.

```
 [{
    "column_a": "value_a",
    "column_b": "value_b",
    "column_c": "value_c",
    "column_d": "value_d",
    "column_e": "value_e",
    },
]
```

***
> columns: \[ < objectsArray \>\]

This attribute is mandatory and contains the descriptor for the table columns.
The structure should be similar to this:
```
[{
	name: "2010",
	alias : "2010",
	aliasToShow : "2010",
	barchart : {
		enabled : true,
		maxValue : 1000, 
		minValue : 0,
		style : {
			background-color : "rgb(180, 70, 237)"
		chartLength : 200 
	}
	fieldType : "MEASURE",
	ranges : [{
		background-color : "rgb(235, 189, 189)",
		color : "rgb(167, 84, 84)",
		icon : "fa fa-bell",
		operator : "<",
		value : 45
	},{...}},
	style : {
		background-color : "rgb(176, 97, 97)",
		color : "rgb(255, 255, 255)",
		font-size : "8px",
		font-weight : "bold",
		td :{
			justify-content: "center"
		}
		width : "200px"
	},
	text : {
		enabled : true;
		format : "#,###.##",
		maxChars : 5,
		precision : 2,
		prefix : "a",
		suffix : "s",
	}
}
,{...}]

```
- **name** (*string, mandatory*) The name of the column, this string will be used by the rows to identify the column value.
-  **barchart** (*object*) This object describes the composition of the barchart. If enabled is set to false the barchart will not be visible.
 - **style** (*object*) This object contains the css style of the barchart.
-  **ranges** (*objectArray*) This objects array describes the composition of the different threshold ranges. 
 - **icon** (*string*) the font-icon class related to the icon to show.
 -	**operator** (*string*) the operation to apply in the range eval.
 - **value** (*number*) the value for the confrontation. 
- **style** (*object*) This object contains the css style of the column.
- **text** (*object*) This object describes the composition of the text element. In enabled is set to false the text will not appear. All the settings related to the text except for those related to the css are inside this object. 
***
> click-function : function(row,column,evt)

This attribute is optional and contains the function that will be triggered in the parent when a click event happens on a cell.
The arguments returned are:
- **row** is the object containing the clicked cell row.
- **column** is the object containing the clicked cell column.
- **evt** is the mouse event. 

***
> settings: < object \>

This attribute is optional if no customization or functionality are needed. It contains the descriptor for the customization of the whole table and functionalities like the pagination. It's highly recomended to us it.
The structure should be similar to this:
```
{
	"alternateRows": {
		"enabled": false,
		"evenRowsColor": "rgb(240, 10, 10)",
		"oddRowsColor": "rgb(28, 14, 255)"
	},
	"autoRowsHeight": false,
	"modalSelectionColumn": "Country Name",
	"multiselectable": true,
	"pagination": {
	  "enabled": true,
	  "frontend": true,
	  "itemsNumber": 10
	},
	"showGrid": false,
	"sortingColumn": "Continent",
	"sortingOrder": "DESC",
	"summary": {
	  "enabled": true
	}
}
```
***
> style: < object \>

```
{
    "backgroundColor": "rgb(229, 16, 16)",
    "borders": true,
    "border": {
      "enabled": true,
      "border-color": "rgb(242, 13, 13)",
      "border-style": "dashed",
      "border-width": "0.7em"
    },
    "shadows": true,
    "shadow": {
      "enabled": true,
      "box-shadow": "0px 4px 5px #ccc"
    },
    "td": {
      "border-style": "solid",
      "border-width": "2px",
      "border-color": "rgb(57, 218, 215)"
    },
    "title": {
      "enabled": true
    },
    "th": {
      "background-color": "rgb(142, 133, 220)",
      "color": "rgb(255, 255, 255)",
      "font-family": "Roboto",
      "font-size": "10px",
      "font-weight": "bold",
	  "text-align": "right"
    },
    "tr": {
      "height": "50px"
    },
    "summary": {
      "background-color": "rgb(20, 44, 150)",
      "color": "rgb(255, 0, 0)",
      "font-family": "Gungsuh",
      "font-size": "24px",
      "font-weight": "bold"
    }
 }
```


***
### Customization

It's possible to change the behaviour of the directive changing the settings object in the scope. 
The css behaviour is present in the cockpitEngine.scss inside the cockpitTable selector.

## Authors

* **Davide Vernassa** - *Knowage* - [Redjaw](https://github.com/Redjaw)
* **Francesco Lucchi** - *Knowage* - [Fralucch](https://github.com/orgs/KnowageLabs/people/fralucch)

## License

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