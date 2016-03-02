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

        	var errorSeparator = "error.mesage.description.";

        	var errMessage = ''
        	if(response !== undefined) {
        		if (response.responseText !== undefined) {
        			try{
        				var content = Ext.JSON.decode( response.responseText );
        			}catch(e){
        				var content =Ext.JSON.decode( response.responseText );
        			}

        			if (content.errors !== undefined  && content.errors.length > 0) {
        				if (content.errors[0].message === 'session-expired') {
        					// session expired
        					errMessage = LN('sbi.qbe.sessionexpired.msg');
        				} else if (content.errors[0].message === 'not-enabled-to-call-service') {
        					Sbi.exception.ExceptionHandler.showErrorMessage(LN('not-enabled-to-call-service'), 'Service Error')
        				} else {
        					for (var count = 0; count < content.errors.length; count++) {
        						var anError = content.errors[count];
        						if (anError.message !== undefined && anError.message !== '' && anError.message.indexOf(errorSeparator)>=0) {
			        				errMessage += LN(anError.message);
			        			} else if (anError.localizedMessage !== undefined && anError.localizedMessage !== '') {
			        				errMessage += anError.localizedMessage;
			        			} else if (anError.message !== undefined && anError.message !== '') {
			        				errMessage += anError.message;
			        			}
			        			if (count < content.errors.length - 1) {
			        				errMessage += '<br/>';
			        			}
        					}
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
           		, icon: Ext.MessageBox.WARNING
           		, modal: false
       		});
        },

        showInfoMessage : function(errMessage, title, config) {
        	var m = errMessage || 'Info';
        	var t = title || 'Info';

        	Ext.MessageBox.show(Ext.apply({
           		title: t
           		, msg: m
           		, buttons: Ext.MessageBox.OK
           		, icon: Ext.MessageBox.INFO
           		, modal: false
       		},config||{}));
        }

	};
}();

// shortcuts

Sbi.showErrorMessage = Sbi.exception.ExceptionHandler.showErrorMessage;
Sbi.showWarningMessage = Sbi.exception.ExceptionHandler.showWarningMessage;
Sbi.showInfoMessage = Sbi.exception.ExceptionHandler.showInfoMessage;
