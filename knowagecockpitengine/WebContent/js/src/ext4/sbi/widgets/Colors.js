/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

Ext.ns("Sbi.widgets");

Sbi.widgets.Colors = function(){

	return {

		defaultColors: ['#4572A7', '#DB843D', '#56AFC7', '#80699B', '#89A54E', '#AA4643', '#50B432'
		                , '#1EA6E0', '#DDDF00', '#ED561B', '#64E572', '#9C9C9C', '#4EC0B1', "#C3198E"
						, "#6B976B", "#B0AF3D", "#E7913A", "#82AEE9", "#7C3454", "#A08C1F", "#84D3D1", "#586B8A", "#B999CC"]

		, getRandomColor: function() {
			var chars = "0123456789ABCDEF";
			var string_length = 6;
			var randomstring = '';
			for (var i=0; i<string_length; i++) {
				var rnum = Math.floor(Math.random() * chars.length);
				randomstring += chars.substring(rnum,rnum+1);
			}
			return "#" + randomstring;
		}
	}

}();
