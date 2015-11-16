/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *     
 *  @author
 *  Monica Franceschini
 */
 
  
Ext.define('Sbi.datamining.FillVariablesPanel', {
	extend: 'Ext.panel.Panel',
	layout: {
        type: 'fit',
        align: 'left'
    },
	
	config:{
		padding: 5
		, border: 0
	},
	
	variablesForm: null,
	caller: 'command',
	callerName: null,
	itsParent: null,
	
	constructor : function(config) {
		
		this.initConfig(config||{});
		this.caller = config.caller;
		this.callerName = config.callerName;
		this.itsParent = config.itsParent;

		var wd = 500;
		if(this.caller == 'output'){
			this.border=1;
			this.style="background: #fff0aa;";
			buttonAlign = 'right';
			wd = 800;
		}
		var service = Ext.create("Sbi.service.RestService",{
			url: this.caller
			,method: "POST"
			,subPath: "setVariables"
			,pathParams: this.callerName
		});
		

		
		this.variablesForm = Ext.create('Ext.form.Panel', {
		    bodyPadding: 5,
		    width: wd,
		    // Fields will be arranged vertically, stretched to full width
		    layout: 'anchor',
		    defaults: {
		        anchor: '100%'		        
		    },
		    border: 0,
		    // The fields
		    defaultType: 'textfield',
		    items: [],
		    dockedItems: [{
		        xtype: 'toolbar',
		        dock: 'top',
		        items:  [{
			        text: LN('sbi.dm.execution.reset.btn'),
			        handler: function() {
			            this.up('form').getForm().reset();
			        }
			    },{
			        text: LN('sbi.dm.execution.run.text'),
				    scope: this,
				    iconCls: 'run',
				    scale: 'medium',	
				    margin: 5,
				    style: {
			            background: '#fff0aa;'
			        },
			        handler: function() {
			        	this.setVariables(this.variablesForm.getForm())	;	        	
			        },
			        listeners:{
			        	click:{
			        		fn: function(){
			        			this.refreshParentPanelActions();
			        			if(this.itsParent.resultPanel !== undefined && this.itsParent.resultPanel != null){
			        				this.itsParent.resultPanel.getResult(true);	
			        			}else{
			        				this.itsParent.getActiveTab().getActiveTab().resultPanel.getResult(true);
			        			}
			        							        			
			        		}
			        	},scope: this
			        },
			        scope: this

			    }]
		    }],
            scope: this
		});
		
		var url = service.getRestUrlWithParameters(true);
		var urlParams = service.getParameters(url);
		for (var key in urlParams){
			var hiddenInput4Param = Ext.create('Ext.form.field.Hidden',{
			        xtype: 'hiddenfield',
			        name: key,
			        value: urlParams[key]
			});
			this.variablesForm.add(hiddenInput4Param);
		}
		
		
		this.addEvents('hasVariables');
		this.callParent(arguments);
	},

	initComponent: function() {
		Ext.apply(this, {
			items: [this.variablesForm]
		});
		this.getVariablesFileds();		
		this.callParent();
	}
	

	
	
	,getVariablesFileds: function(){
		
		var thisPanel = this;
		
		var service = Ext.create("Sbi.service.RestService",{
			url: this.caller			
			,subPath: "getVariables"
			,pathParams: this.callerName
		});
		
		var functionSuccess = function(response){
			var thisPanel = this;
			if(response != null && response.responseText !== undefined && response.responseText !== null && response.responseText !== ''){
				var res = Ext.decode(response.responseText);
				
				if(res && Array.isArray(res)){

					for (var i=0; i< res.length; i++){
						var variable = res[i];
						var valueOfVar = variable.value;
						if(valueOfVar == undefined || valueOfVar == null){
							valueOfVar = variable.defaultVal
						}
						var varField= Ext.create("Ext.form.field.Text",{
					        value: valueOfVar,
					        name: variable.name,
					        fieldLabel: variable.name,
					        labelWidth: 150,
					        msgTarget: 'side',
					        allowBlank: false,
					        anchor: '100%',
					        border: 0
					    });
						this.variablesForm.add(varField);
					}
					this.fireEvent('hasVariables', true);
				}
			}
			else{
				if(this.caller =='command'){
					var emptyField =Ext.create("Ext.form.field.Display", {
				        xtype: 'displayfield',
				        fieldLabel: 'SpagoBI Dataset label',
				        labelStyle: 'font-weight: bold; color: #28596A;',
				        labelWidth: 150,
				        name: dataset.label,
				        value: 'no variables to display'
				    });
					this.variablesForm.add(emptyField);
					this.add(this.variablesForm);
				}
			}
			
		};
		service.callService(this, functionSuccess);
	}
	, setVariables : function(form){
		var thisPanel = this;
		
		var service = Ext.create("Sbi.service.RestService",{
			url: this.caller
			,method: "POST"
			,subPath: "setVariables"
			,pathParams: this.callerName
		});
		
        form.submit({
            url: service.getRestUrlWithParameters(), // a multipart form cannot contain parameters on its main URL;
            												   // they must POST parameters
            waitMsg: LN('sbi.dm.execution.loading'),
            success: function(form, action) {
    			Ext.Msg.show({
 				   title : LN('sbi.dm.execution.msg'),
 				   msg: LN('sbi.dm.execution.ok'),
 				   buttons: Ext.Msg.OK
 				});

 			
            },
            failure : function (form, action) {
    			Ext.Msg.show({
    				title : LN('sbi.dm.execution.msg'),
  				   msg: action.result.msg,
  				   buttons: Ext.Msg.OK
  				});
            },
            scope : this
        });
		//service.callService(this);
	}
	,refreshParentPanelActions: function(){
	/*
		if(this.caller == 'command'){			
			this.itsParent.getActiveTab().getActiveTab().resultPanel.getResult(true);
			this.itsParent.varWin.hide();	
			this.itsParent.getActiveTab().getActiveTab().executeScriptBtn.show();
		}else{
			this.itsParent.resultPanel.getResult(true);
			this.itsParent.executeScriptBtn.show();
		}*/
		if(this.caller == 'command'){
			//this.itsParent.getActiveTab().getActiveTab().executeScriptBtn.show();
			this.itsParent.varWin.hide();	
		}
	}
});