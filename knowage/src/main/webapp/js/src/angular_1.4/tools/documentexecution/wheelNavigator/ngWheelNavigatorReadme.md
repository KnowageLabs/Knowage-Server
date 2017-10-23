# ngWheelNavigator
Directive to internally wrap in angular the  [wheelnav from Softwaretailoring](https://github.com/softwaretailoring/wheelnav/)
This directive is intended for the Pont-MO Environment only.
To match with other environments some adjustment can be needed.

## Dependencies
```
angular.js
raphael.js 
wheelnav.js
```
or with Bower
```
bower -install -- save wheelnav
```

##Setup
Import the directive, there is no need to inject in controller.

##Settings
> _CURRENTCONTEXTURL

this global variable is inherited from the environment. It is used to find the correct link to the template.

> scope.baseUrl = 

This variable gives the base url for the links of the navigator.
> scope.params =

This variable is concatenated to the base url and gives the parameters to be passed in the link of the navigator.

> scope.pieItems = [{"id": INT, "link":STRING},{...}]

This object contains the array of elements to be show on the navigator and the final part of the link.
The total number of the elements should be 7. For different numbers see the customization section.

###Customization
It's possible to change the number of items and the behaviour of the navigator using the code generated from this site.
http://pmg.softwaretailoring.net/

It is possible to change the colors and position of the navigator changing the sass file related _wheelNavigator.scss.

The icons are set inside the template and are the ones from [font-awesome](http://fontawesome.io/icons/).
We used the escaped unicode value inside the template navigator object.

## Authors

* **Davide Vernassa** - *Knowage* - [Redjaw](https://github.com/Redjaw)
* **Marco Cortella** - *Knowage* - [mcortella](https://github.com/mcortella)

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