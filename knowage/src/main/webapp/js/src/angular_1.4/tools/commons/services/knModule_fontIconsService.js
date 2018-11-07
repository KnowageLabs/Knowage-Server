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
						{"label":"archive","className":"fa fa-archive","unicode":"\uf187"},{"label":"area chart","className":"fa fa-area-chart","unicode":"\uf1fe"},{"label":"bar chart","className":"fa fa-bar-chart","unicode":"\uf080"},
						{"label":"pie chart","className":"fa fa-pie-chart","unicode":"\uf200"},{"label":"line chart","className":"fa fa-line-chart","unicode":"\uf201"},{"label":"bidirectional arrows","className":"fa fa-arrows","unicode":"\uf047"},
						{"label":"horizontal arrows","className":"fa fa-arrows-h","unicode":"\uf07e"},{"label":"vertical arrows","className":"fa fa-arrows-v","unicode":"\uf07d"},{"label":"asterisk","className":"fa fa-asterisk","unicode":"\uf069"},
						{"label":"address at","className":"fa fa-at","unicode":"\uf1fa"},{"label":"bolt","className":"fa fa-bolt","unicode":"\uf0e7"},{"label":"circle","className":"fa fa-plus-circle","unicode":"\uf055"},
						{"label":"breafcase","className":"fa fa-briefcase","unicode":"\uf0b1"},{"label":"check","className":"fa fa-check","unicode":"\uf00c"},{"label":"times","className":"fa fa-times","unicode":"\uf00d"},
						{"label":"warning","className":"fa fa-warning","unicode":"\uf071"},{"label":"exclamation","className":"fa fa-exclamation-circle","unicode":"\uf06a"},
						{"label":"plus","className":"fa fa-plus","unicode":"\uf067"},{"label":"bars","className":"fa fa-bars","unicode":"\uf0c9"},{"label":"minus","className":"fa fa-minus","unicode":"\uf068"},
						{"label":"down","className":"fa fa-chevron-down","unicode":"\uf078"},{"label":"up","className":"fa fa-chevron-up","unicode":"\uf077"},
						{"label":"arrow down","className":"fa fa-arrow-down","unicode":"\uf063"},{"label":"arrow up","className":"fa fa-arrow-up","unicode":"\uf062"},{"label":"plane","className":"fa fa-plane","unicode":"\uf072"},
						{"label":"thumbs up","className":"fa fa-thumbs-up","unicode":"\uf164"},{"label":"thumbs down","className":"fa fa-thumbs-down","unicode":"\uf165"},{"label":"folder open","className":"fa fa-folder-open","unicode":"\uf07c"},
						{"label":"filter","className":"fa fa-filter","unicode":"\uf0b0"},{"label":"external link","className":"fa fa-external-link","unicode":"\uf08e"}]
				},{"name":"Appliances","className":"ai",
					"icons":[
						{"label":"boiler","className":"ai-boiler","unicode":"\6d"},{"label":"aways on","className":"ai-alwayson","unicode":"\6e"},{"label":"home","className":"ai-home","unicode":"\6f"},
						{"label":"conditioning","className":"ai-conditioning","unicode":"\70"},{"label":"fan","className":"ai-fan","unicode":"\71"},{"label":"vacuum","className":"ai-vacuum","unicode":"\72"},
						{"label":"dishwasher","className":"ai-dishwasher","unicode":"\73"},{"label":"mixer","className":"ai-mixer","unicode":"\74"},{"label":"mixedwasher","className":"ai-mixedwasher","unicode":"\75"},
						{"label":"magnet","className":"ai-magnet","unicode":"\76"},{"label":"battery","className":"ai-battery","unicode":"\77"},{"label":"resistor","className":"ai-resistor","unicode":"\78"}]
				}
			];
		}
}());