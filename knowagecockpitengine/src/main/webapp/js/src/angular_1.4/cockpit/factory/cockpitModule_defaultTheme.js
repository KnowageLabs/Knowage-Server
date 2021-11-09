/*
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
 */

angular.module('cockpitModule').factory('cockpitModule_defaultTheme',function(sbiModule_config,sbiModule_translate){
	var alternatedBackgroundColor = 'rgb(228, 232, 236)';
	return{
		cockpit: {
			"style": {
			    "titles": false,
			    "title": {
			      "font": {
			        "color": "rgb(59, 103, 140)",
			        "text-align": "center",
			        "font-weight": "bold",
			        "font-size": "16px"
			      }
			    },
			    "borders": true,
			    "border": {
			      "border-color": "rgb(212, 212, 212)",
			      "border-width": "1px",
			      "border-style": "solid"
			    },
			    "shadows": true,
			    "shadow": {
			      "box-shadow": "0px 2px 3px #ccc"
			    }
			  }
		},
		table:{
			"settings": {
				"pagination" : {
					'enabled': true,
					'itemsNumber': 10,
					'frontEnd': false
				},
				"alternateRows": {
				      "enabled": true,
				      "evenRowsColor": alternatedBackgroundColor
				    },
				"page":1
			},
			"style": {
				"title": {
			        "label": "Table Widget"
			      },
			      "th": {
			        "enabled": true,
			        "background-color": "rgb(137, 158, 175)",
			        "color": "rgb(255, 255, 255)",
			        "justify-content": "center",
			        "font-size": "14px"
			      },
			      "tr": {},
			      "padding": {
			        "enabled": true,
			        "padding-left": "5px",
			        "padding-bottom": "5px",
			        "padding-right": "5px",
			        "padding-top": "",
			        "unlinked": true
			      }
			}
		},
		pivotTable:{
			"style": {
			    "showAlternateRows": true,
			    "measuresRow": {
			      "odd-background-color": alternatedBackgroundColor,
			      "even-background-color": "",
			      "border-color": "rgb(137, 158, 175)",
			      "border-width": "0.1em",
			      "border-style": "solid"
			    },
			    "showGrid": true
			  }
		}
	}
});