/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 
/**
  * Object name 
  * 
  * [description]
  * 
  * 
  * Public Properties
  * 
  * [list]
  * 
  * 
  * Public Methods
  * 
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Davide Zerbetto (davide.zerbetto@eng.it)
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
					