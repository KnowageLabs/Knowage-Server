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


Ext.define
(
	"Sbi.behavioural.lov.LOVDetailPanel",
	
	{
		extend: "Ext.form.Panel",
		
		config: 
		{
	    	//frame: true,
	    	bodyPadding: '5 5 0',
	    	
	    	defaults: 
	    	{
	            width: 400
	        }, 
	        
	        fieldDefaults: 
	        {
	            labelAlign: 'right',
	            msgTarget: 'side'
	        },
	        
	        border: false,
			
	        services:[]
	    },
	    
	    
	    constructor: function(config)
    	{	    	
	    	this.initConfig(config);
	    	
	    	this.initFields();	
	    	
	    	this.items = [ this.panel1, this.panel2 ];
	    	
	    	this.tbar = Sbi.widget.toolbar.StaticToolbarBuilder.buildToolbar
	    	(
    			{ 
    				items:
    				[ 
    				 	{name:'->'},
    				 	{name:'test'},
    				 	{name:'save'} 
				 	] 
    			}, 
    			
    			this
			);	

	    	this.tbar.on
	    	(
    			"save",
    			
    			function()
    			{
					if(true)
					{	
						this.fireEvent("save", this.getValues());
					}
					else
					{
						Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.datasource.validation.error'),LN('sbi.generic.validationError'));
					}				
    			},
    			
    			this
			);
	    	
	    	this.tbar.on
	    	(
    			"test",
    			
    			function()
    			{
    				this.fireEvent("test", this.getValues());
    			},
    			
    			this
			);
	    	
	    	this.callParent(arguments);
	    	
	    	this.on
	    	(
    			"render",
    			
    			function()
    			{
    				this.hide();
				},
				
				this
			);
    	},
    	
    	
    	initFields: function()
    	{
    		var globalScope = this;
    		
    		this.lovId = Ext.create
    		(
    			"Ext.form.field.Text",
    			
    			{
    				name: "LOV_ID",
    				id: "LovId",
    				readOnly: true
    			}
    		);
    		
    		this.lovLabel = Ext.create
    		(
				"Ext.form.field.Text",
				{
	    			name: "LOV_LABEL",
	    			allowBlank: false,
	    			fieldLabel: LN('sbi.behavioural.lov.details.label'),
	    			width: 400,
	    			padding: '10 0 0 0' 
				}
			);
    		
    		this.lovName = Ext.create
    		(
				"Ext.form.field.Text",
				
				{
	    			name: "LOV_NAME",
	    			allowBlank: false,
	    			fieldLabel: LN('sbi.behavioural.lov.details.name'),
	    			width: 400,
	    			padding: '10 0 0 0' 
				}
			);
    		
    		this.lovDescription = Ext.create
    		(
				"Ext.form.field.Text",
				
				{
	    			name: "LOV_DESCRIPTION",
	    			fieldLabel: LN('sbi.behavioural.lov.details.description'),
	    			width: 400,
	    			padding: '10 0 0 0' 
				}
			);    		
    		
    		this.lovProvider = Ext.create
    		(
    			"Ext.form.field.TextArea",
    			
    			{
    				name: "LOV_PROVIDER",
    				width: 500,
    				height: 150,
    				readOnly: true
    			}
    		);    
    		    		
    		this.lovSelectionType = Ext.create
    		(
    			"Ext.form.field.Hidden",
    			
    			{
    				name: "SELECTION_TYPE"
    			}
    		);
    		
    		
    		Ext.define
    		(
				"InputTypeModel", 
				
				{
					extend: 'Ext.data.Model',
					fields: [ "VALUE_NM", "VALUE_DS", "VALUE_ID", "VALUE_CD" ]
				}
			);
        	
    	
    		var inputTypeStore = Ext.create
        	(
    			'Ext.data.Store',
	    		
    			{
	        		model: "InputTypeModel",
	        		autoLoad: true,
	        		
	        		proxy: 
	        		{
	        			type: 'rest',
	        			
	        			extraParams : { DOMAIN_TYPE: "INPUT_TYPE" },
	        			
	        			url: globalScope.services['getDomains'],
	        			
	        			reader: 
	        			{
	        				type:"json"
	        			}
	        		}
	        	}
			);
    		
    		inputTypeStore.on
        	(
    			"load", 
    			
    			function(inputTypeStore)
    			{ 
    			}
			);
    		
    		this.lovInputTypeCombo = new Ext.create
    		(
				'Ext.form.ComboBox', 
	    		
				{
	    			fieldLabel: LN('sbi.behavioural.lov.details.inputType'),
	    			name: "I_TYPE_CD",
	    	        store: inputTypeStore,
	    	        id: "INPUT_TYPE_COMBO",
	    	        displayField:'VALUE_NM',
	    	        valueField:'VALUE_CD',
	    	        editable: false,
	    	        allowBlank: false,
	    	        padding: "10 0 10 0",
	    	        
	    	        listeners: 
			        {
			            change: function() 
			            {
			                globalScope.selectedInputType(this.getValue());
			            }
			        }
	    	    }
			);
    		
    		this.panel1 = Ext.create
    		(
    			"Ext.panel.Panel",
    			
    			{
    				width: "100%",
    				
    				items: 
					[ this.lovId, this.lovLabel, this.lovName, this.lovDescription, 
					  this.lovProvider, this.lovInputTypeCombo, this.lovInputTypeCd,
					  this.lovInputTypeId, this.lovSelectionType ]    				
    			}
    		);
    		
    		
    		Ext.define
    		(
				"DataSourceModel", 
				
				{
					extend: 'Ext.data.Model',
					fields: [ "DESCRIPTION", "DATASOURCE_LABEL", "JNDI_URL", "DATASOURCE_ID" ] // fields (labels) from JSON that comes from server that we call
				}
			);
        	
    	
    		var dataSourceStore = Ext.create
        	(
    			'Ext.data.Store',
	    		
    			{
	        		model: "DataSourceModel",
	        		autoLoad: true,
	        		
	        		proxy: 
	        		{
	        			type: 'rest',	        			
	        			//extraParams : { DOMAIN_TYPE: "INPUT_TYPE" },
	        			
	        			url:  this.services["getDataSources"],	
	        			
	        			reader: 
	        			{
	        				type:"json",
	        				root: "root"
	        			}
	        		}
	        	}
			);
        	
    		dataSourceStore.on
        	(
    			'load', 
    			
    			function(dataSourceStore)
    			{ 
    			}
			);
    		
    		this.dataSourceCombo = new Ext.create
    		(
				'Ext.form.ComboBox', 
	    		
				{
	    			fieldLabel: LN('sbi.behavioural.lov.details.dataSourceLabel'),
	    	        store: dataSourceStore,
	    	        name: "DATASOURCE_ID",
	    	        id: "DATA_SOURCE_COMBO",
	    	        displayField:'DATASOURCE_LABEL',
	    	        valueField:'DESCRIPTION',
	    	        padding: "10 0 0 0",
	    	        editable: false,
	    	        allowBlank: false
	    	    }
			); 
    		
    		this.dataSourceQuery = Ext.create
    		(
				"Ext.form.field.TextArea",
				
				{
					id: "DATA_SOURCE_QUERY",
					fieldLabel: LN('sbi.behavioural.lov.details.queryDescription'), 
					height: 100,
					width: 500,
					padding: '10 0 10 0'
				}
    		);
    		
    	
    		this.panel2 = Ext.create
    		(
    			"Ext.panel.Panel",
    			
    			{
    				width: "100%",
    				padding: "10 0 0 0",
    				
    				items: 
					[ this.dataSourceCombo, this.dataSourceQuery ]    				
    			}
    		);
    		
    		    		
    		Ext.define
    		(
				"ScriptTypeModel", 
				
				{
					extend: 'Ext.data.Model',
					fields: [ "VALUE_NM", "VALUE_DS", "VALUE_ID" ]
				}
			);
        	
    		
        	var scriptTypeStore = Ext.create
        	(
    			'Ext.data.Store',
	    		
    			{
	        		model: "ScriptTypeModel",
	        		autoLoad: true,
	        		
	        		proxy: 
	        		{
	        			type: 'rest',   			
	        			extraParams : { DOMAIN_TYPE: "SCRIPT_TYPE" },
	        			
	        			url:  this.services['getDomains'],	
	        			
	        			reader: 
	        			{
	        				type:"json"
	        			}
	        		}
	        	}
			);
        	
        	scriptTypeStore.on
        	(
    			"load", 
    			
    			function(scriptTypeStore)
    			{ 
    			}
			);
    		
        	var globalScope = this;
        	
    		this.scriptTypeCombo = new Ext.create
    		(
				'Ext.form.ComboBox', 
	    		
				{
	    			fieldLabel: LN('sbi.behavioural.lov.details.scriptType'),
	    	        store: scriptTypeStore,
	    	        id: "SCRIPT_TYPE_COMBO",
	    	        displayField:'VALUE_NM',
	    	        valueField:'VALUE_CD',
	    	        editable: false,
	    	        allowBlank: false,
	    	        
	    	        listeners: 
			        {
			            change: function() 
			            {
			            }
			        }
	    	    }
			);
    		
    		this.scriptQuery = Ext.create
    		(
				"Ext.form.field.Text",
				
				{
					id: "SCRIPT_QUERY",
					height: 100,
					width: 200,
					padding: '0 100 50'
				}
    		);
    	},
    	
    	
    	selectedInputType: function(inputTypeCD)
		{    			
			var type = -1;
			
			if (inputTypeCD == "QUERY")
			{
				this.panel2.show();
								
				if (this.lovId.value == 0)
				{					
//					this.dataSourceCombo.setValue("");
//					this.dataSourceQuery.setValue("");
				}
			}
			else
			{
				this.panel2.hide();
			}
			
		},
    	
    	getValues: function()
    	{
    		var values = this.callParent();
    		return values;
    	},    
    	
    	setFormState: function(values)
    	{
    		var v = values;
    		console.log("PROVERA ID-eva");
			console.log(values.LOV_ID);
			console.log(values.I_TYPE_ID);
    		/* *** 	Problem solved: Keeping panel 2 details (combo and text area) from 
    		 * 		the old record in the new one. */
    		// Needed for differentiating between old (existing) record and the new one
    		if (values.LOV_ID != 0)
    		{   
    			this.lovInputTypeCombo.setValue(values.I_TYPE_CD);
    			    			
    			var query = values.LOV_PROVIDER;
    			
    			if (values.I_TYPE_CD == "QUERY") 
    			{
        			var startIndex = query.indexOf("<CONNECTION>");
        			var endIndex = query.indexOf("</CONNECTION>");
        			
        			this.dataSourceCombo.setValue(query.substring(startIndex + "<CONNECTION>".length,endIndex));
    			}  
        		
        		if (values.I_TYPE_CD == "QUERY") 
    			{
        			var startIndex = query.indexOf("<STMT>");
        			var endIndex = query.indexOf("</STMT>");
        			
        			this.dataSourceQuery.setValue(query.substring(startIndex + "<STMT>".length,endIndex));
    			}        		
        		
        		//this.selectedInputType(values.I_TYPE_CD);				
    		}
    		else
			{
    			this.lovInputTypeCombo.setValue(values.I_TYPE_CD);
    			this.dataSourceCombo.setValue("");
    			this.dataSourceQuery.setValue("");
			}
    		
    		this.getForm().setValues(values);    		
    	},
    	
    	getFormState: function()
    	{    		
    		var getLovId = this.lovId.value;
    		var getLovName = this.lovName.value;
    		var getLovDescription = this.lovDescription.value;
    		var getLovLabel = this.lovLabel.value;
    		
    		if (this.lovInputTypeCombo.getValue() == "QUERY")
			{    			
    			var getLovProvider = "<QUERY>" + "<CONNECTION>" + this.dataSourceCombo.getValue() + "</CONNECTION>" + "<STMT>" + this.dataSourceQuery.getValue() + "</STMT>" + "<VALUE-COLUMN>"
				+ "region_id" + "</VALUE-COLUMN>" + "<DESCRIPTION-COLUMN>" + "region_id" + "</DESCRIPTION-COLUMN>"
				+ "<VISIBLE-COLUMNS>" + "region_id" + "</VISIBLE-COLUMNS>" + "<INVISIBLE-COLUMNS>"
				+ "region_id" + "</INVISIBLE-COLUMNS>" + "<LOVTYPE>" + "simple"
				+ "</LOVTYPE>" + "<TREE-LEVELS-COLUMNS>" + "sales_city,sales_state_province,sales_district,sales_region,sales_country,sales_district_id" + "</TREE-LEVELS-COLUMNS>"
				+ "</QUERY>";
    			
    			var getInputTypeCd = "QUERY";
    			var getInputTypeId = 1; 
			}    			
    		else if (this.lovInputTypeCombo.getValue() == "SCRIPT")
			{
    			var getInputTypeCd = "SCRIPT";
    			var getInputTypeId = 2; 
    			var getLovProvider = "";
			}    			
    		else if (this.lovInputTypeCombo.getValue() == "FIX_LOV")
			{
    			var getInputTypeCd = "FIX_LOV";
    			var getInputTypeId = 3; 
    			var getLovProvider = "";
			}    			
    		else if (this.lovInputTypeCombo.getValue() == "JAVA_CLASS")
			{
    			var getInputTypeCd = "JAVA_CLASS";
    			var getInputTypeId = 4; 
    			var getLovProvider = "";
			}    			
    		else if (this.lovInputTypeCombo.getValue() == "DATASET")
			{
    			var getInputTypeCd = "DATASET";
    			var getInputTypeId = 5; 
    			var getLovProvider = "";
			}    			
    		
    		var getSelectionType = "";
    		
    		var model1 = Ext.create
			(
				"Sbi.behavioural.lov.LOVModel",
				
				{
					 LOV_ID: getLovId,
			         LOV_NAME: getLovName,
			         LOV_DESCRIPTION: getLovDescription,
			         LOV_PROVIDER: getLovProvider,
			         I_TYPE_CD: getInputTypeCd,
			         I_TYPE_ID: getInputTypeId,
			         LOV_LABEL: getLovLabel,
			         SELECTION_TYPE: getSelectionType
				}
			);    		
    		
    		return model1;
    	}
    	
	}
	
);