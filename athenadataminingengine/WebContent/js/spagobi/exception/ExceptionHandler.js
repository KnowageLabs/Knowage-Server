/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2008 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/
 
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


Ext.ns("Sbi.exception.ExceptionHandler");

Sbi.exception.ExceptionHandler = function(){
	// do NOT access DOM from here; elements don't exist yet
 
    // private variables
 
    // public space
	return {
	
		init : function() {
			//alert("init");
		},
		
		
        handleFailure : function(response, options) {
        	
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

	};
}();