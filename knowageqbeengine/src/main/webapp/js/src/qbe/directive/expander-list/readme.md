# ExpanderList
Directive used to create a customizable expander list with 3 levels.
```
|-- Main level
|   |-- Expandable Secondary level
|       |-- Leaf third level
```
## Dependencies
```
angular.js
ngDraggable.js
```
in npm
```
npm install angular
npm install ngdraggable
```

## Setup
Import the directive, inject the module "expander_list" in the controller .
Use the directive inserting the following tag:
```
<expander-list 
    ng-model="model"
    colors="colors"
    font-icons="iconLibrary"
    drop-function="droppedFunction(data)"  
    entities-actions="entitiesFunctions" 
    fields-actions="fieldsFunctions" >
</expander-list>
```
The container of the expander cannot have the css property _overflow: hidden;_ or _overflow: auto;_. In this case the elements will not be draggable.

## Settings
> ng-model : \<object\>

This attribute is mandatory and it should contain the model for the list.
The structure should be similar to the following one. 
Each key declared will be the first level menu of the view. 

```
 {'entities': [{
    "id": "entity1",
    "text": "entity1",
    "iconCls": "cube",
    "children": [
        { 
            "id": "field1", 
            "text": "field1", 
            "iconCls": "measure"
        }  
    ]},
    'subqueries': [{...}]
 }
```
- _id_ is the identifier for the element.
- _text_ is the label that will be shown for the element.
- _iconCls_ is the class that will represent the icon for that element. Use any fonticons classname you have declared inside the css.
- _children_ is the array containing the children, which will have the same structure. This name can be different if specified in the "children-name" attribute

***
> children-name: "string"

This attribute is optional and contains the name of the object property in ngModel to identify the children element.
The default value is "children".

***
> colors : \[array\] 

This attribute is optional and contains an array of hexadecimal colors to use as palette for the level 2 elements.
If no attribute is given the colored square will not be visible.
***
> font-icons: "string"

This attribute is optional if you want to use icons.
To use a specific font-icons library insert the library css identifier (ie for FontAwesome is "fa").
The single icon classes must be identified somewhere in che css if different from the defaults for that library (ie FontAwesome fa-list or his classname if remapped)
***
> drop-function : function(data)

This attribute is optional, it gives the chance to the parent scope to receive a callback when something inside the directive is dropped.
The function will take data as argument, containing the dragged element.
If no function is given no function will be launched and the items inside the directive will not be draggable.
***
> entities-actions : \[\<objectsArray\>\]

This attribute is optional, it renders some button icons at the right side of a 2nd level item.
The structure of the object should be like the following:
```
[{
    "label": "add calculated field",
    "icon": "fa fa-calculator",
    "action": function(item, event) {
        CONTROLLER FUNCTION
    }
},{...}];
```
If no object is given the item will not have any icon button.
***
> fields-actions : \[\<objectsArray\>\]

This attribute is optional. Has the same behaviour of entities-actions but renders icon buttons at the right side of a 3rd level item.

### Customization

It's possible to change the behaviour of the directive changing the class ".expanderList" inside _qbeEngine.scss.
All the icons are updateable from the model, except for the chevron expanding or collapsing icons.

## Authors

* **Davide Vernassa** - *Knowage* - [Redjaw](https://github.com/Redjaw)

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