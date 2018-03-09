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
(function() {
	angular
		.module("knModule")
		.service("knModule_fontIconsService",knModule_fontIconsService)
		
		function knModule_fontIconsService(){
			var self=this;
			self.icons=[
				{"name":"font-awesome","className":"fa",
					"list":[
						"fa-adjust","fa-anchor","fa-archive","fa-area-chart","fa-arrows","fa-arrows-h","fa-arrows-v","fa-asterisk","fa-at","fa-automobile","fa-balance-scale","fa-ban","fa-bank" ,
						"fa-bar-chart","fa-bar-chart-o","fa-barcode","fa-bars","fa-battery-empty","fa-battery-full","fa-battery-half","fa-battery-quarter","fa-battery-three-quarters","fa-bed",
						"fa-beer","fa-bell","fa-bell-o","fa-bell-slash","fa-bell-slash-o","fa-bicycle","fa-binoculars","fa-birthday-cake","fa-bolt","fa-bomb","fa-book","fa-bookmark","fa-bookmark-o","fa-briefcase",
						"fa-bug","fa-building","fa-building-o","fa-bullhorn","fa-bullseye","fa-bus","fa-calculator","fa-calendar","fa-calendar-check-o","fa-calendar-minus-o","fa-calendar-o","fa-calendar-plus-o",
						"fa-calendar-times-o","fa-camera","fa-camera-retro","fa-car","fa-caret-square-o-down","fa-caret-square-o-left","fa-caret-square-o-right","fa-caret-square-o-up","fa-cart-arrow-down",
						"fa-cart-plus","fa-cc","fa-certificate","fa-check","fa-check-circle","fa-check-circle-o","fa-check-square","fa-check-square-o","fa-child","fa-circle","fa-circle-o","fa-circle-o-notch"
						]
				},
				/*
				 * JUST ADD FONT ICONS FAMILIES LIKE THAT
				 {"name":"otherFamily","className":"fa",
					"list":['fa-th','fa-cell', 'fa-dot', 'fa-times']
				}*/
			];
		}
}());