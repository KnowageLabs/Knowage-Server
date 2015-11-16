/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
/**
  * ServiceRegistry - short description
  * 
  * Object documentation ...
  * 
  * by Chiara Chiarelli 
  */

Ext.ns("Sbi.commons");

Sbi.commons.ExecutionPanel = function(config) {

	var params = {
        LIGHT_NAVIGATOR_DISABLED : 'TRUE'
    };
    
    this.services = new Array();
    this.services['getStartService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'START_WORK'
		, baseParams: params
	});
	
	this.services['getStopService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'STOP_WORK'
		, baseParams: params
	});

	this.services['getStatusService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'STATUS_WORK'
		, baseParams: params
	});

	
	this.document_id = config.document_id;

	this.parameters=config.parameters;
				
	this.buttons = [];
    this.buttons.push({
        text : 'Start'
        , scope : this
        , handler : this.startProcess
    });
    this.buttons.push({
        text : 'Stop'
        , scope : this
        , handler : this.stopProcess
        //,disabled: true
    });
    
    this.infoStore ='undefined';
    
    this.infoStore = new Ext.data.SimpleStore({
        fields : ['meta_id','meta_name', 'meta_content' ],
        data : [
                ['ProcessId', 'Status', 'Data']
        ]        
    });
    
    this.tabInfo = new Ext.grid.GridPanel({
            store : this.infoStore,
            autoHeight : true,
			mode: 'local',
            columns : 
            [ {
                header : 'ProcessId',
                width : 150,
                dataIndex : 'meta_id'
            },
            {
                header : 'Status',
                width : 150,
                dataIndex : 'meta_name'
            }, {
                header : 'Time',
                width : 200,
                dataIndex : 'meta_content'
            } ],
            viewConfig : {
                forceFit : false,
                scrollOffset : 2
            // the grid will never have scrollbars
            }
        });
 
   	 var c = Ext.apply( {}, config, {
        title : 'Process Execution',
        layout : 'fit',
        collapsible : false,
        collapsed : false,
        buttons: this.buttons,
        buttonAlign: 'left',
        items: [this.tabInfo],
        autoWidth : true,
        autoHeight : true,
        region: 'center'
    });
	
	// constructor
    Sbi.commons.ExecutionPanel.superclass.constructor.call(this,c);
    
    
};

Ext.extend(Sbi.commons.ExecutionPanel, Ext.Panel, {
    
    // static contens and methods definitions
    services : null
    ,document_id : null
    ,buttons: null
    ,tabInfo: null
    ,infoStore:null
    ,status:null
    ,pid:null
    // public methods
    ,monitorStatus: function(){
    
    }
    
    ,startProcess : function() {
    
    //alert(this.parameters);
    var parJSON=Ext.urlDecode(this.parameters);
    //alert(parJSON.toSource());
    
    	var params = {
        'DOCUMENT_ID' : this.document_id
    	};
    	
    	var applied=Ext.apply(parJSON,params);
    //alert(applied.toSource());
    
       Ext.Ajax.request({
	        url: this.services['getStartService'],
	        //params: {'DOCUMENT_ID' : this.document_id },
	        params:  applied ,
	        success : function(response, options) {
	      		if(response !== undefined && response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if (content !== undefined) {
	      			//this.setTitle(content.status);
	      				record=this.tabInfo.store.getAt(0);
	      				record.set('meta_id',content.pid);
	      				record.set('meta_name',content.status);
	      				record.set('meta_content',content.time);
				      	this.pid=content.pid;
	      			this.buttons[0].setDisabled(true);			      			
	      			this.buttons[1].setDisabled(false);		
	      			} else {
	      				Sbi.commons.ExceptionHandler.showErrorMessage('Server response cannot be decoded', 'Service Error');
	      			}
	      		} else {
	      				Sbi.commons.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	      		}
	        },
	        scope: this,
			failure: Sbi.commons.ExceptionHandler.handleFailure      
	   });	
	       }
    , stopProcess : function() {
     	Ext.Ajax.request({
	        url: this.services['getStopService'],
	        params: {'DOCUMENT_ID' : this.document_id, 'PROCESS_ID' : this.pid },
	        success : function(response, options) {
	      		if(response !== undefined && response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if (content !== undefined) {
	  	      			this.buttons[0].setDisabled(false);		
	      				this.buttons[1].setDisabled(true);		
	      				record=this.tabInfo.store.getAt(0);
	      				record.set('meta_id',content.pid);
	      				record.set('meta_name',content.status);
	      				record.set('meta_content',content.time);
	      				//this.pid=null;
	      			} else {
	      				Sbi.commons.ExceptionHandler.showErrorMessage('Server response cannot be decoded', 'Service Error');
	      			}
	      		} else {
	      			Sbi.commons.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	      		}
	        },
	        scope: this,
			failure: Sbi.commons.ExceptionHandler.handleFailure      
	   });
    }

    ,statusProcess : function() {
    
       Ext.Ajax.request({
	        url: this.services['getStatusService'],
	        params: {'DOCUMENT_ID' : this.document_id, 'PROCESS_ID' : this.pid },
	        success : function(response, options) {
	      		if(response !== undefined && response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if (content !== undefined) {
						if(content.status_code==0){ // not started, enable start button
	  	      			this.buttons[0].setDisabled(false);		
	      				this.buttons[1].setDisabled(true);								
						}
						else if(content.status_code==4){ // completed, disable both buttons
	  	      			this.buttons[0].setDisabled(true);		
	      				this.buttons[1].setDisabled(true);								
						}
						else if(content.status_code==2){ // rejected, disable both buttons
	  	      			this.buttons[0].setDisabled(false);		
	      				this.buttons[1].setDisabled(true);								
						}
						else if(content.status_code==1){ // accepted,disable start button
	  	      			this.buttons[0].setDisabled(true);		
	      				this.buttons[1].setDisabled(true);								
						}												
						else{ 	// started: disable start button, enable stop button
	  	      			this.buttons[0].setDisabled(true);		
	      				this.buttons[1].setDisabled(false);								
						}
	      				//this.setTitle(content.status);
	      				record=this.tabInfo.store.getAt(0);
	      				record.set('meta_id',content.pid);
	      				record.set('meta_name',content.status);
	      				record.set('meta_content',content.time);
	      				this.status=content.status_code;
	      			} else {
	      				Sbi.commons.ExceptionHandler.showErrorMessage('Server response cannot be decoded', 'Service Error');
	      			}
	      		} else {
	      			Sbi.commons.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	      		}
	        },
	        scope: this,
			failure: Sbi.commons.ExceptionHandler.handleFailure      
	   });
	   
    }
    

});

