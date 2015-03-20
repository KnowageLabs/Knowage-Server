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

Ext.ns("Sbi.execution.toolbar");

Sbi.execution.toolbar.NotesWindow = function(config) {
	
	// always declare exploited services first!
	var msg = ( config.MESSAGE == null)?'GET_DETAIL_NOTE':config.MESSAGE;
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null, MESSAGE: msg};
	
	if (msg != 'INSERT_NOTE'){
    this.idNote = config.REC.id;
  	this.owner = config.REC.owner;
  	this.creationDate = config.REC.creationDate;
	  this.lastModificationDate = config.REC.lastModificationDate;
  }else{
    this.idNote = '';
  	this.owner = Sbi.user.userId;
  	this.creationDate ='';
  	this.lastModificationDate ='';
  }
	this.services = new Array();
	this.services['getNotesService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_NOTES_ACTION'
		, baseParams: params
	});
	this.services['saveNotesService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SAVE_NOTES_ACTION'
		, baseParams: params
	});
	
	this.previousNotes = undefined;
	
	this.SBI_EXECUTION_ID = config.SBI_EXECUTION_ID;
	this.buddy = undefined;
	
 
  this.editor = new Ext.form.HtmlEditor({
        frame: true
        ,value: ''
        //,bodyStyle:'padding:5px 5px 0'
        //,width:'100%'
        ,width:690
        ,disabled: true
	      ,height: 265
	      ,enableSourceEdit: false
	      //,autoWidth : true
	      , autoHeight : true
        ,id:'notes'        
  }); 
    
  var scopeComboBoxData = [
    		['PUBLIC','Public', 'Everybody can view this note'],
    		['PRIVATE', 'Private', 'The saved note will be visible only to you']
    	];
    		
    	var scopeComboBoxStore = new Ext.data.SimpleStore({
    		fields: ['value', 'field', 'description'],
    		data : scopeComboBoxData 
    	});    		    
 		    
    	this.scopeField = new Ext.form.ComboBox({
    	   	tpl: '<tpl for="."><div ext:qtip="{field}: {description}" class="x-combo-list-item">{field}</div></tpl>',	
    	   	editable  : false,
    	   	fieldLabel : 'Scope',
    	   	forceSelection : true,
    	   	mode : 'local',
    	   	name : 'scope',
    	   	store : scopeComboBoxStore,
    	   	displayField:'field',
    	    valueField:'value',
    	    emptyText:'Select scope...',
    	    typeAhead: true,
    	    triggerAction: 'all',
    	    selectOnFocus:true
  });
	var c = Ext.apply({}, config, {
	   title: LN('sbi.execution.notes.notes')
		,width:700
		,height:300
		,items: [this.editor]
		,tbar: new Ext.Toolbar({enableOverflow: false
	            ,items: [ LN('sbi.execution.notes.owner') + ': ' + this.owner
                      , LN('sbi.execution.notes.creationDate') + ': ' + this.creationDate
                      , LN('sbi.execution.notes.lastModificationDate') + ': ' + this.lastModificationDate]	
        })
		,bbar:  new Ext.Toolbar({
	             enableOverflow: false
	            ,items: [this.scopeField]	
	     })
	,buttons: [
		          {
		            id: 'save'
		        	  ,text: LN('sbi.execution.notes.savenotes') 
		        	  ,scope: this
		        	  ,handler: this.saveNotes
		        	  ,disabled: (this.owner != Sbi.user.userId)
		          }
		          ,{
		            id: 'back'
		        	  ,text: LN('sbi.execution.notes.goBack')
		        	  ,scope: this
		        	  ,handler: this.goBack
		           }
		]
	});   
	
	this.loadNotes();
	
	// constructor
    Sbi.execution.toolbar.NotesWindow.superclass.constructor.call(this, c);
    
    if (this.buddy === undefined) {
    	this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
    }
    
};

Ext.extend(Sbi.execution.toolbar.NotesWindow, Ext.Window, {
	
	loadNotes: function () {
		Ext.Ajax.request({
	        url: this.services['getNotesService'],
	        params: {SBI_EXECUTION_ID: this.SBI_EXECUTION_ID, NOTE_ID: this.idNote, OWNER: this.owner},
	        success : function(response, options) {
	      		if(response !== undefined && response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if (content !== undefined) {
	      			  
	      				this.previousNotes = content.notes;
	      				this.editor.setValue(Ext.util.Format.htmlDecode(content.notes));
	      				if (options.params.OWNER == Sbi.user.userId){		      				  
	      				  //this.editor.enable();
	      				   this.editor.setDisabled(false);
	      				}
	      				else{
	      				  this.editor.setDisabled(true);
	      				}
	      				this.scopeField.setValue((content.visibility==true)?'PUBLIC':'PRIVATE');		      			
	      			} 
	      		} else {
	      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	      		}
	        },
	        scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure      
		});
	}

	, saveNotes: function () {
		Ext.Ajax.request({
	        url: this.services['saveNotesService'],
	        params: {'SBI_EXECUTION_ID': this.SBI_EXECUTION_ID, 
					 'PREVIOUS_NOTES': this.previousNotes, 'NOTES':  this.checkEmptyValues(this.editor.getValue()), 'VISIBILITY': this.scopeField.getValue()},
			success: function(response, options) {
	      		if(response !== undefined && response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if (content !== undefined) {
	      				//if (content.result === 'conflict') {
	      				//	Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.execution.notes.notesConflict'), 'Service Error');
	      				//} else {
			      			Ext.MessageBox.show({
			      				title: 'Status',
			      				msg: LN('sbi.execution.notes.notedSaved'),
			      				modal: false,
			      				buttons: Ext.MessageBox.OK,
			      				width:300,
			      				icon: Ext.MessageBox.INFO,
			      				animEl: 'root-menu'        			
			      			});
			      			this.previousNotes = this.editor.getValue();
	      				//}
	      			}
	      		} else {
	      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	      		}
	        },
	        scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure      
		});
		
	}
	
		, goBack: function () {
    this.hide();
		this.win_notes = new Sbi.execution.toolbar.ListNotesWindow({'SBI_EXECUTION_ID': this.SBI_EXECUTION_ID});
		this.win_notes.show();
	}
		
	,checkEmptyValues: function(val){
	     var expression = Ext.util.Format.stripTags( val );
	     expression = expression.replace(/&nbsp;/g," ");
	     if(expression.trim() == ""){
	     	val = "";
	     }
	     return val;
	}	
});