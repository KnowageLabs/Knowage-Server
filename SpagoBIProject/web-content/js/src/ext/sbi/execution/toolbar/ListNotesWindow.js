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
  * - Antonella Giachino (antonella.giachino@eng.it)
  */

Ext.ns("Sbi.execution.toolbar");

Sbi.execution.toolbar.ListNotesWindow = function(config) {
	// always declare exploited services first!
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: config.SBI_EXECUTION_ID, MESSAGE:'GET_LIST_NOTES'};
	
	this.services = new Array();
	this.services['getNotesService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_NOTES_ACTION'
		, baseParams: params
	});
	this.services['deleteNotesService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DELETE_NOTES_ACTION'
		, baseParams: params
	});
	this.services['printNotesService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'PRINT_NOTES_ACTION'
		, baseParams: params
	});		
	this.previousNotes = undefined;
	this.SBI_EXECUTION_ID = config.SBI_EXECUTION_ID;
	
	this.buddy = undefined;
	this.lookup = {};
	this.notesStore = new Ext.data.JsonStore({
        root: 'results'
        , idProperty: 'id'
        , fields: ['id', 'owner', 'notes', 'visibility', 'deletable','biobjId', 'creationDate','lastModificationDate'
           //        {name:'creationDate', type:'date', dateFormat: Sbi.config.clientServerTimestampFormat}, 
           //        {name:'lastModificationDate', type:'date', dateFormat: Sbi.config.clientServerTimestampFormat}
                  ]
		, url: this.services['getNotesService']
    }); 
		
  this.tpl = new Ext.XTemplate(
	      '<tpl for=".">'
	         ,'<div class="group-header">'
	    /*
	         ,'<div class="action-detail-note">'
	         ,    '<a href="javascript:void(0);">Owner: <b>{owner}</b></a>'
	         ,'</div>'
	      */   
	         ,'<i>'+LN('sbi.execution.notes.owner')+' </i><b>{owner}</b>'
	         ,'<i>      '+LN('sbi.execution.notes.creationDate')+' </i><b>{creationDate}</b>'
           ,'<i>      '+LN('sbi.execution.notes.modificationDate')+' </i>&nbsp<b>{lastModificationDate}</b>' 
           ,'<br>'
	         , '{[Ext.util.Format.ellipsis(Ext.util.Format.stripTags(values.notes), 100)]}' 	        
	         ,'<tpl if="values.deletable == true ">'
	         ,   '<div class="button"><img class="action-delete-note" title="'+LN('sbi.execution.notes.deleteNote')+'" src="' + Ext.BLANK_IMAGE_URL + '"/></div>'
	         ,'</tpl>'
           , '<div class="button"><img class="action-detail-note" title="'+LN('sbi.execution.notes.detailNote')+'" src="' + Ext.BLANK_IMAGE_URL + '"/></div>'
           ,'</div>'
           ,'<br>'
           ,'<hr  style="border:solid #E0E0E0 1px;">'         
	      ,'</tpl>'
	      ,'<div class="x-clear"></div>'
	  );


//	this.notesStore.load();
this._toolbar =  new Ext.Toolbar({items:[]});

this.notesStore.on('load' 
		  				,function() {                                                        
                             var viewBtnInsert = (this.notesStore.find('owner',Sbi.user.userId)< 0)?true:false; 
                             //if there aren't notes, it goes in detail note directly
                                                                          
                             if (this.notesStore.getCount() == 0){                            
                               	this.hide();
                              	this.win_notes = new Sbi.execution.toolbar.NotesWindow({'SBI_EXECUTION_ID': this.SBI_EXECUTION_ID, 'MESSAGE': 'INSERT_NOTE'});
                              	this.win_notes.show();
                              
                             }
                             else{
                                 if (viewBtnInsert){                  
                                   var ttbarInsertNoteButton = new Ext.Toolbar.Button({
                                      text: LN('sbi.execution.notes.insertNotes')
                                    	,tooltip: LN('sbi.execution.notes.insertNotes')
                                		  ,iconCls:'icon-insert'
                                		  ,listeners: {
                                			'click': {
                                          		fn: this.addNote
                                          		,scope: this
                                       } 
                                  		}
                                    });                                
                                   this._toolbar.addButton(ttbarInsertNoteButton) ;                            
                                  }
                              }
                           var ttbarPrintNoteButton = new Ext.Toolbar.Button({
                                      text: LN('sbi.execution.notes.printnotesPDF')
                                    	,tooltip: LN('sbi.execution.notes.printnotesPDF')
                                		  ,iconCls:'icon-pdf'
                                		  ,listeners: {
                                			'click': {
                                          		fn: this.printNotesPDF
                                          		,scope: this
                                       } 
                                  		}
                                    });                                
                                   this._toolbar.addButton(ttbarPrintNoteButton) ;   
                           var ttbarPrint2NoteButton = new Ext.Toolbar.Button({
                                      text: LN('sbi.execution.notes.printnotesRTF')
                                    	,tooltip: LN('sbi.execution.notes.printnotesRTF')
                                		  ,iconCls:'icon-rtf'
                                		  ,listeners: {
                                			'click': {
                                          		fn: this.printNotesRTF
                                          		,scope: this
                                       } 
                                  		}
                                    });                                
                                   this._toolbar.addButton(ttbarPrint2NoteButton) ;                                                               
                          }
                          , this);        
  this.notesStore.load();
  
   this.listPanel = new Ext.Panel({        
        height:300
        ,autoScroll:true
        ,items: new Ext.DataView({
            tpl: this.tpl
            ,store:this.notesStore
            ,itemSelector: 'div.group-header'
            ,listeners: {
               'click': {
                   fn: this.onClick
                 , scope: this
               }
             }
        })
        ,tbar: this._toolbar
        ,bbar: new Ext.PagingToolbar({
            store: this.notesStore
            ,pageSize: 20
            ,displayInfo: true
            ,displayMsg: 'Notes {0} - {1} of {2}'
            ,emptyMsg: "No notes to display"
        })
       
    });

   //notesStore.load();
	var c = Ext.apply({}, config, {
		title: LN('sbi.execution.notes.notes')
		,width:700
		,height:300
		,items: [this.listPanel]
	});   
	 
	// constructor
    Sbi.execution.toolbar.ListNotesWindow.superclass.constructor.call(this, c);
  	this.buddy = new Sbi.commons.ComponentBuddy({
  		buddy : this
  	});


};

Ext.extend(Sbi.execution.toolbar.ListNotesWindow, Ext.Window, {

	  addNote: function () {
  	  	this.hide();
  		this.win_notes = new Sbi.execution.toolbar.NotesWindow({'SBI_EXECUTION_ID': this.SBI_EXECUTION_ID, 'MESSAGE': 'INSERT_NOTE'});
  		this.win_notes.show();
	  }
	  
	  ,loadNote: function (rec) {
		this.hide();
  		this.win_notes = new Sbi.execution.toolbar.NotesWindow({'SBI_EXECUTION_ID': this.SBI_EXECUTION_ID, 'REC':rec});
  		this.win_notes.show();
	  }
	
	, deleteNotes: function (rec) {
			Ext.Ajax.request({
		        url: this.services['deleteNotesService'],
		        params: {NOTE_ID:rec.id, OWNER:rec.owner},
		        callback : function(options , success, response) {
		  	  		if (success) {
			      		if(response !== undefined && response.responseText !== undefined) {
			      			var content = Ext.util.JSON.decode( response.responseText );
			      			if (content !== undefined && content.result == 'OK') {
			      			    // removes the note from the store				      					      		
                      var rec = this.getRecord(options.params.NOTE_ID);		                  
                      this.notesStore.remove(rec);
			      			   
					      			Ext.MessageBox.show({
					      				title: 'Status',
					      				msg: LN('sbi.execution.notes.noteDeleted'),
					      				modal: false,
					      				buttons: Ext.MessageBox.OK,
					      				width:300,
					      				icon: Ext.MessageBox.INFO,
					      				animEl: 'root-menu'        			
					      			});					 
					      			
					      			if (options.params.OWNER == Sbi.user.userId){
			      			         var ttbarInsertNoteButton = new Ext.Toolbar.Button({
                              text: LN('sbi.execution.notes.insertNotes')
                            	,tooltip: LN('sbi.execution.notes.insertNotes')
                        		  ,iconCls:'icon-insert'
                        		  ,listeners: {
                        			'click': {
                                  		fn: this.addNote
                                  		,scope: this
                               } 
                          		}
                            });                                
                           this._toolbar.addButton(ttbarInsertNoteButton) ;     
                      }
			      			}
			      		} else {
			      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
			      		}
		  	  		} else { 
		  	  			Sbi.exception.ExceptionHandler.showErrorMessage('Cannot delete notes', 'Service Error');
		  	  		}
		        },
		        scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure      
			});
			
		}
		
   ,onClick: function(dataview, i, node, e) {
    	var button = e.getTarget('div[class=button]', 20, true);
    	var action = null;
    	if(button) {
    		var buttonImg = button.down('img');
    		var startIndex = (' '+buttonImg.dom.className+' ').indexOf(' action-');
    		if(startIndex != -1) {
    			action = buttonImg.dom.className.substring(startIndex).trim().split(' ')[0];
    			action = action.split('-')[1];
    		}    		
    	}
  		var idx = 0; 
    	var records = this.notesStore.getRange(0, this.notesStore.getCount());
    
      for(var j = 0; j < records.length; j++) {
        this.lookup[idx++] = records[j].data;  
      }
    	var r = this.lookup[i];
    	
    	if(action !== null) {
    		if (action === 'detail'){
        		this.loadNote(r);
      	}
      	else if (action === 'delete'){
      		this.deleteNotes(r);
      	}
    	} 
    }
    
    ,getRecord : function(codId){
      var records = this.notesStore.getRange(0, this.notesStore.getCount());
    
      for(var j = 0; j < records.length; j++) {
         if (records[j].data.id == codId){			  	   
			  	    return records[j];
			  	  }
      }
      return null;
    }
	, printNotesPDF: function () {
		var urlPrint = this.services['printNotesService'];
		urlPrint+= '&SBI_OUTPUT_TYPE=PDF';
			window.open(urlPrint,'name','height=750,width=1000');
	}    
	, printNotesRTF: function () {
		var urlPrint = this.services['printNotesService'];
		urlPrint+= '&SBI_OUTPUT_TYPE=RTF';
		window.open(urlPrint,'name','height=750,width=1000');
	}    
});
