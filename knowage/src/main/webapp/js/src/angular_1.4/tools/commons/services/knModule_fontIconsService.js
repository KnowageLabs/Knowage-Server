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
				{"name":"fontawesome","className":"fa",
					"icons":[
						{"label":"map marker","className":"fa fa-map-marker","unicode":"\uf041"},{"label":"map pin","className":"fa fa-map-pin","unicode":"\uf276"},{"label":"map","className":"fa fa-map","unicode":"\uf279"},
						{"label":"street view","className":"fa fa-street-view","unicode":"\uf21d"},{"label":"map 2","className":"fa fa-map-o","unicode":"\uf278"},{"label":"location arrow","className":"fa fa-location-arrow","unicode":"\uf124"},
						{"label":"map signs","className":"fa fa-map-signs","unicode":"\uf277"},{"label":"globe","className":"fa fa-globe","unicode":"\uf0ac"},
						{"label":"motorcycle","className":"fa fa-motorcycle","unicode":"\uf21c"},{"label":"car","className":"fa fa-car","unicode":"\uf1b9"},{"label":"bus","className":"fa fa-bus","unicode":"\uf207"},
						{"label":"shopping cart","className":"fa fa-shopping-cart","unicode":"\uf07a"},{"label":"adjust","className":"fa fa-adjust","unicode":"\uf042"},{"label":"anchor","className":"fa fa-anchor","unicode":"\uf13d"},
						{"label":"archive","className":"fa fa-archive","unicode":"\uf187"},{"label":"area chart","className":"fa fa-area-chart","unicode":"\uf1fe"},{"label":"bidirectional arrows","className":"fa fa-arrows","unicode":"\uf047"},
						{"label":"horizontal arrows","className":"fa fa-arrows-h","unicode":"\uf07e"},{"label":"vertical arrows","className":"fa fa-arrows-v","unicode":"\uf07d"},{"label":"asterisk","className":"fa fa-asterisk","unicode":"\uf069"},
						{"label":"address at","className":"fa fa-at","unicode":"\uf1fa"},{"label":"bolt","className":"fa fa-bolt","unicode":"\uf0e7"},{"label":"circle","className":"fa fa-plus-circle","unicode":"\uf055"},
						{"label":"breafcase","className":"fa fa-briefcase","unicode":"\uf0b1"}]
				},{"name":"appliances","className":"ai",
					"icons":[
						{"label":"refrigerator","className":"ai-fridge","unicode":"\61"},{"label":"iron","className":"ai-iron","unicode":"\62"},{"label":"microwave","className":"ai-microwave","unicode":"\63"},
						{"label":"oven","className":"ai-oven","unicode":"\64"},{"label":"toaster","className":"ai-toaster","unicode":"\65"},{"label":"tv","className":"ai-tv","unicode":"\66"},
						{"label":"washer","className":"ai-washer","unicode":"\67"},{"label":"washdrier","className":"ai-washdrier","unicode":"\68"},{"label":"lightbulb","className":"ai-lightbulb","unicode":"\69"},
						{"label":"hairdyer","className":"ai-hairdyer","unicode":"\6a"},{"label":"coffee","className":"ai-coffee","unicode":"\6b"},{"label":"cappa","className":"ai-cappa","unicode":"\6c"},
						{"label":"boiler","className":"ai-boiler","unicode":"\6d"}]
				}
			];
		}
	
	/*					"fa fa-at","fa fa-automobile","fa fa-balance-scale","fa fa-ban","fa fa-bank" ,
						"fa fa-bar-chart","fa fa-bar-chart-o","fa fa-barcode","fa fa-bars","fa fa-battery-empty","fa fa-battery-full","fa fa-battery-half","fa fa-battery-quarter","fa fa-battery-three-quarters","fa fa-bed",
						"fa fa-beer","fa fa-bell","fa fa-bell-o","fa fa-bell-slash","fa fa-bell-slash-o","fa fa-bicycle","fa fa-binoculars","fa fa-birthday-cake","fa fa-bolt","fa fa-bomb","fa fa-book","fa fa-bookmark","fa fa-bookmark-o","fa fa-briefcase",
						"fa fa-bug","fa fa-building","fa fa-building-o","fa fa-bullhorn","fa fa-bullseye","fa fa-bus","fa fa-calculator","fa fa-calendar","fa fa-calendar-check-o","fa fa-calendar-minus-o","fa fa-calendar-o","fa fa-calendar-plus-o",
						"fa fa-calendar-times-o","fa fa-camera","fa fa-camera-retro","fa fa-car","fa fa-caret-square-o-down","fa fa-caret-square-o-left","fa fa-caret-square-o-right","fa fa-caret-square-o-up","fa fa-cart-arrow-down",
						"fa fa-cart-plus","fa fa-cc","fa fa-certificate","fa fa-check","fa fa-check-circle","fa fa-check-circle-o","fa fa-check-square","fa fa-check-square-o","fa fa-child","fa fa-circle","fa fa-circle-o","fa fa-circle-o-notch"*/
}());