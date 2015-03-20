/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
/**
  * Object name 
  * 
  * Singleton object that handle all errors generated on the client side
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


Ext.ns("Sbi.cookies.Cookies");

Sbi.cookies.Cookies = function() {

	return {
	
		getCookie : function (key) {
			var name = key + "=";
			var ca = document.cookie.split(';');
			for(var i=0; i<ca.length; i++) 
			  {
			  var c = ca[i].trim();
			  if (c.indexOf(name)==0) return c.substring(name.length,c.length);
			}
			return null;
		}
		
		,
		setCookie : function (key, value, expiration) {  // expiration must be set in milliseconds
			var d = new Date();
			d.setTime( d.getTime() + expiration );
			var expires = "expires="+d.toGMTString();
			document.cookie = key + "=" + value + "; " + expires;
		}
		
		,
		deleteCookie : function(name){
			Sbi.cookies.Cookies.setCookie(name, "-", -1);
		}

	};
}();