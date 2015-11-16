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
  * - Andrea Gioia (andrea.gioia@eng.it)
  */


Ext.ns("Sbi.commons.ExceptionHandler");

Sbi.commons.ExceptionHandler = function(){
	// do NOT access DOM from here; elements don't exist yet
 
    // private variables
 
    // public space
	return {
	
		init : function() {
			//alert("init");
		},
		
		
        handleFailure : function(response, options) {
        	
        	var errMessage = ''
        	if(response !== undefined) {
        		
        		if(response.responseText !== undefined) {
        			var content = Ext.util.JSON.decode( response.responseText );
        			if (content.errors !== undefined) {
    					for (var count = 0; count < content.errors.length; count++) {
    						var anError = content.errors[count];
		        			if (anError.localizedMessage !== undefined && anError.localizedMessage !== '') {
		        				errMessage += anError.localizedMessage;
		        			} else if (anError.message !== undefined && anError.message !== '') {
		        				errMessage += anError.message;
		        			}
		        			if (count < content.errors.length - 1) {
		        				errMessage += '<br/>';
		        			}
    					}
    				}
        		} else {
        			errMessage = 'Generic error';
        		}
        	}
        	
        	Sbi.commons.ExceptionHandler.showErrorMessage(errMessage, 'Service Error');
       	
        },
        
        showErrorMessage : function(errMessage, title) {
        	var m = errMessage || 'Generic error';
        	var t = title || 'Error';
        	
        	Ext.MessageBox.show({
           		title: t
           		, msg: m
           		, buttons: Ext.MessageBox.OK     
           		, icon: Ext.MessageBox.ERROR
           		, modal: false
       		});
        }
        
        
        
	};
}();