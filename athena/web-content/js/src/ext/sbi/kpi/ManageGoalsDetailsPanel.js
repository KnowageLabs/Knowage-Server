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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.ManageGoalsDetailsPanel = function(config, ref) { 
	this.configurationObject={};
	var paramsList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "GOAL_INSERT"};
	this.configurationObject.saveGrantService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_GOALS_ACTION'
			, baseParams: paramsList
	});
	var c = this.initForm(config);
	this.addEvents();
	this.referencedCmp = ref;
	Sbi.kpi.ManageGoalsDetailsPanel.superclass.constructor.call(this, c);	 	
};

Ext.extend(Sbi.kpi.ManageGoalsDetailsPanel, Ext.FormPanel, {

	 detailFieldLabel: null 
	,detailFieldName: null  
	,detailFieldDescr: null
    ,detailFieldFrom: null
    ,detailFieldTo: null
    ,detailFieldGrant: null
    ,goalId: null
	
	,initForm: function(config){
		var thisPanel = this;
		
		//fileds of the detail panel
		this.detailFieldLabel = new Ext.form.TextField({
			minLength:1,
			fieldLabel:LN('sbi.generic.label'),
			allowBlank: false,
			//validationEvent:true,
			name: 'label'
		});	  
	
		this.detailFieldName = new Ext.form.TextField({
			maxLength:100,
			minLength:1,
			fieldLabel: LN('sbi.generic.name'),
			allowBlank: false,
			name: 'name'
		});

		this.detailFieldDescr = new Ext.form.TextArea({
			maxLength:400,
			width : 250,
			height : 80,
			fieldLabel: LN('sbi.generic.descr'),
			name: 'description',
			allowBlank: false
		});
	
		this.detailFieldFrom = new Ext.form.DateField({
			id: 'from',
			name: 'from',
			fieldLabel: LN('sbi.generic.from'),
			format: 'd/m/Y',
			allowBlank: false
		});
	
		this.detailFieldTo = new Ext.form.DateField({
			id: 'to',
			name: 'to',
			fieldLabel: LN('sbi.generic.to'),
			format: 'd/m/Y',
			allowBlank: false
		});
		 
	
		var baseConfig = {drawFilterToolbar:false}; 
	
	
		var grantStore = new Ext.data.JsonStore({
			url: config.manageGrantListService,
			root: 'rows',
			fields: ['id','label','name','description','modelinstance']
		});
	
		this.detailFieldGrant = new Sbi.widgets.LookupField(Ext.apply( baseConfig, {
			name: 'name',
			valueField: 'id',
			displayField: 'name',
			descriptionField: 'description',
			fieldLabel: LN('sbi.goal.grant'),
			store: grantStore,
			singleSelect: true,
			allowBlank: false,
			cm: new Ext.grid.ColumnModel([
			                              new Ext.grid.RowNumberer(),
			                              {   header: LN('sbi.generic.label'),
			                            	  dataIndex: 'label',
			                            	  width: 75
			                              },
			                              {   header: LN('sbi.generic.name'),
			                            	  dataIndex: 'name',
			                            	  width: 75
			                              },
			                              {   header: LN('sbi.generic.descr'),
			                            	  dataIndex: 'description',
			                            	  width: 75
			                              }
			                              ])
		}));
	
		var tbSave2 = new Ext.Toolbar( {
			buttonAlign : 'right',
			items : [ 
			         new Ext.Toolbar.Button( {
			        	 text : LN('sbi.generic.update'),
			        	 iconCls : 'icon-save',
			        	 handler : this.save,
			        	 width : 30,
			        	 scope : thisPanel
			         })
			         ]
		});
	
		
		var conf = {
       	 title: LN('sbi.generic.details')
    	 , itemId: 'detail'
    	 , tbar: tbSave2
    	 , width: 430
    	 , items: [{
    		 id: 'items-detail',   	
    		 itemId: 'items-detail',               
    		 columnWidth: 2,
    		 xtype: 'fieldset',
    		 labelWidth: 150,
    		 defaults: {width: 200, border:false},    
    		 defaultType: 'textfield',
    		 autoHeight: true,
    		 autoScroll  : true,
    		 bodyStyle: Ext.isIE ? 'padding:15 0 5px 10px;' : 'padding:10px 15px;',
    		 border: false,
    		 style: {
    			 "margin-left": "10px", 
    			 "margin-right": Ext.isIE6 ? (Ext.isStrict ? "-10px" : "-13px") : "0"  
    		 },
    		 items: [this.detailFieldLabel, this.detailFieldName,  this.detailFieldDescr,
    		        this.detailFieldFrom, this.detailFieldTo, this.detailFieldGrant]
    	 }]};

		return conf;
	}

	, save: function(){
		var thisPanel = this;
		if(		(this.detailFieldLabel.getValue()==null || this.detailFieldLabel.getValue()=='') ||
				(this.detailFieldName.getValue()==null || this.detailFieldName.getValue()=='') ||
				(this.detailFieldDescr.getValue()==null || this.detailFieldDescr.getValue()=='') ||
				(this.detailFieldFrom.getValue()==null || this.detailFieldFrom.getValue()=='') ||
				(this.detailFieldTo.getValue()==null || this.detailFieldTo.getValue()=='') ||
				(this.detailFieldGrant.getValue()==null || this.detailFieldGrant.getValue()=='')
				){
			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.goal.insert.all.data'), LN('sbi.goal.insert.all.data'));
			return false;
		}
				
		var goal = {
			id: this.goalId,
			label: this.detailFieldLabel.getValue(), 
			name: this.detailFieldName.getValue(),  
			description: this.detailFieldDescr.getValue(),
			startdate: this.detailFieldFrom.getValue(), 
			enddate: this.detailFieldTo.getValue(),
			grant: this.detailFieldGrant.getValue()
		};
		
		var goalE = Ext.encode(goal);
		
		Ext.Ajax.request({
			url: this.configurationObject.saveGrantService,
			params: {'goal': goalE},
			method: 'POST',
			success: function(response, options) {
				if (response !== undefined) {
					Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.generic.resultMsg'),'');
					thisPanel.fireEvent('saved');
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.savingItemError'), LN('sbi.goal.insert.all.data'));
				}
			},
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
		return true;
	}
});


