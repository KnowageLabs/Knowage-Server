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
  * - Antonella Giachino (antonella.giachino@eng.it)
  */

Ext.define('Sbi.exception.ExceptionHandler', {
	statics: {
		init : function () {
			//alert("init");
		},
		handleFailure : function (response, options) {
        	var errMessage = null;
        	if(response !== undefined) {        		
        		if(response.responseText !== undefined) {
        			var content = Ext.util.JSON.decode( response.responseText );
        			if(content.errors !== undefined && content.errors.length > 0) {
        				errMessage = '';
        				for(var i = 0; i < content.errors.length; i++) {
        					errMessage += content.errors[i].message + '<br>';
        				}
        			} 
        		} 
        		if(errMessage === null)	errMessage = 'An unspecified error occurred on the server side';
        	} else {
        		errMessage = 'Request has been aborted due to a timeout trigger';
        	}
        		
        	errMessage = errMessage || 'An error occurred while processing the server error response';
        	
        	Sbi.exception.ExceptionHandler.showErrorMessage(errMessage, 'Service Error');
       	
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
        },
        
        showWarningMessage : function(errMessage, title) {
        	var m = errMessage || 'Generic warning';
        	var t = title || 'Warning';
        	
        	Ext.MessageBox.show({
           		title: t
           		, msg: m
           		, buttons: Ext.MessageBox.OK     
           		, icon: Ext.MessageBox. WARNING
           		, modal: false
       		});
        }
        
       , onStoreLoadException : function(proxy, type, action, options, response, arg) {
        	Sbi.exception.ExceptionHandler.handleFailure(response, options);
        }
	}//statics
});