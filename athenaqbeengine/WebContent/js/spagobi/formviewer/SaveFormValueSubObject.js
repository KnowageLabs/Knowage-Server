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
  * - Alberto Ghedin (alberto.ghedin@eng.it)
  */

Ext.ns("Sbi.formviewer");

Sbi.formviewer.SaveFormValueSubObject = function(){
	
    // public space
	return {
		
		 saveSubObject: function(formValues, meta, ref){
			 
			//define the callback function. 
			//definitely for the metadata
	    	this.save(formValues, meta, ref, function(response, options) {
	    		// for old gui
	    		try {
					var content = Ext.util.JSON.decode( response.responseText );
					content.text = content.text || "";
					parent.loadSubObject(window.name, content.text);
				} catch (ex) {}
				// for new gui
				// build a JSON object containing message and ID of the saved  object
				
				try {
					// get the id of the subobject just inserted, decode string, need to call metadata window
					var responseJSON = Ext.util.JSON.decode( response.responseText )
					var id = responseJSON.text;
					var msgToSend = 'Sub Object Saved!!';
					
					//sendMessage({'id': id, 'meta' : meta.metadata, 'msg': msgToSend},'subobjectsaved');
					//alert('id '+id+' message '+msgToSend);
					sendMessage({'id': id, 'msg': msgToSend},'subobjectsaved');
				} catch (ex) {}
				// show only if not showing metadata windows
				/*if( meta.metadata == false ){
				Ext.Msg.show({
					   title:LN('sbi.qbe.queryeditor.querysaved'),
					   msg: LN('sbi.qbe.queryeditor.querysavedsucc'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.INFO
				});
			}*/
			}, this);
	  	}
	  	
		// perform the save action
		, save: function(formValues, formState, ref, callback){
			
			var saveFormStateService =  Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'SAVE_FORM_VALUES_SUB_OBJECT_ACTION'
				, baseParams: {MESSAGE_DET: "SAVE_SUB_OBJECT"}
			});
			
			var doSave = function() {
				Ext.Ajax.request({
					url: saveFormStateService,
					params: {'name': formState.name,'description': formState.description, 'scope': formState.scope,  'formState': Ext.encode(formValues)},
					method: 'POST',
					success: callback,
					failure: function() {
						Ext.MessageBox.show({
							title: LN('sbi.generic.error'),
							msg: LN('sbi.generic.savingItemError'),
							width: 150,
							buttons: Ext.MessageBox.OK
						});
					}
					,scope: this
			
				});
			};
			
			doSave();
					
		}
    
	};
}();