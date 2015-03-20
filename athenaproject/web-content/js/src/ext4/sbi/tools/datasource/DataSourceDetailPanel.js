/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */
 
  
Ext.define('Sbi.tools.datasource.DataSourceDetailPanel', {
    extend: 'Ext.form.Panel'

    ,config: {
    	//frame: true,
    	bodyPadding: '5 5 0',
    	defaults: {
            width: 400
        },        
        fieldDefaults: {
            labelAlign: 'right',
            msgTarget: 'side'
        },
        border: false,
        services:[]
    }

	, constructor: function(config) {
		
		//this.isSuperadmin = config.isSuperadmin;
		
		this.initConfig(config);
		this.initFields();
		this.items=[this.dataSourceId, this.dataSourceDialectId, this.dataSourceLabel , this.dataSourceDescription, this.dataSourceDialect, this.dataSourceMultischema , this.dataSourceMultischemaAttribute, this.dataSourceReadOnly, this.dataSourceReadWrite, this.dataSourceWriteDefault, this.dataSourceTypeJdbc, this.dataSourceTypeJndi,this.dataSourceJndiName, this.dataSourceJdbcUrl, this.dataSourceJdbcUser, this.dataSourceJdbcPassword ,this.dataSourceDriver]
		
		this.addEvents('save');
		this.tbar = Sbi.widget.toolbar.StaticToolbarBuilder.buildToolbar({items:[{name:'->'},{name:'test'},{name:'save'}]},this);
		this.tbar.on("save",function(){
			if(this.validateForm()){
				this.fireEvent("save", this.getValues());
			}else{
				Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.datasource.validation.error'),LN('sbi.generic.validationError'));
			}
			
		},this);
		this.tbar.on("test",function(){
			this.fireEvent("test", this.getValues());
		},this);
		this.callParent(arguments);
		this.on("render",function(){this.hide()},this);
    }

	, initFields: function(){
		this.dataSourceId = Ext.create("Ext.form.field.Hidden",{
			name: "DATASOURCE_ID"
		});
		this.dataSourceLabel = Ext.create("Ext.form.field.Text",{
			name: "DATASOURCE_LABEL",
			fieldLabel: LN('sbi.datasource.label'),
			allowBlank: false
		});
		this.dataSourceDescription = Ext.create("Ext.form.field.Text",{
			name: "DESCRIPTION",
			fieldLabel: LN('sbi.datasource.description')
		});

		Ext.create("Ext.form.field.Text",{
			name: "DIALECT_NAME",
			fieldLabel: LN('sbi.datasource.dialect'),
			allowBlank: false
		});	   
    	
    	Ext.define("DialectModel", {
    		extend: 'Ext.data.Model',
            fields: ["VALUE_NM","VALUE_DS","VALUE_ID"]
    	});
    	
    	var dialectStore=  Ext.create('Ext.data.Store',{
    		model: "DialectModel",
    		proxy: {
    			type: 'ajax',
    			extraParams : {DOMAIN_TYPE:"DIALECT_HIB"},
    			url:  this.services['getDialects'],
    			reader: {
    				type:"json"
    			}
    		}
    	});
    	dialectStore.load();
		this.dataSourceDialect = new Ext.create('Ext.form.ComboBox', {
			fieldLabel: LN('sbi.datasource.dialect'),
	        store: dialectStore,
	        name: "DIALECT_ID",
	        displayField:'VALUE_DS',
	        valueField:'VALUE_ID',
	        allowBlank: false
	    });
    	
		this.dataSourceMultischema = Ext.create("Ext.form.Checkbox",{
	        fieldLabel: LN('sbi.datasource.multischema'),
	        name: "MULTISCHEMA",
	        value: false
		});
		
		this.dataSourceMultischemaAttribute = Ext.create("Ext.form.field.Text",{
			name: "SCHEMA",
			fieldLabel: LN('sbi.datasource.multischema.attribute'),
			allowBlank: true,
			hidden: true
		});
		
		this.dataSourceMultischema.on("change", function(field, newValue, oldValue, eOpts){
			if(newValue){
				this.dataSourceMultischemaAttribute.show();
			}else{
				this.dataSourceMultischemaAttribute.hide();
			}
		},this);
			
		this.dataSourceReadWrite = Ext.create("Ext.form.field.Radio",{
            hideEmptyLabel: false,
            //fieldLabel: LN('sbi.datasource.readonly'),
			boxLabel: LN('sbi.datasource.readwrite'), 
			checked : true,
			name: 'READ_WRITE' , 
			inputValue:'readwrite'
		});
				
		this.dataSourceReadOnly = Ext.create("Ext.form.field.Radio",{
			fieldLabel: LN('sbi.datasource.readonly'),
			boxLabel: LN('sbi.datasource.readonly'), 
			name: 'READ_WRITE' , 
			inputValue:'readonly'
		})
		this.dataSourceReadOnly.addListener('change', this.readOnlyCheck, this);
	
		this.dataSourceWriteDefault = Ext.create("Ext.form.Checkbox",{
	        fieldLabel: LN('sbi.datasource.writedefault'),
	        name: "WRITE_DEFAULT",
	        value: false
		});
		this.dataSourceWriteDefault.addListener('change', this.writeDefaultCheck, this);
			
		this.dataSourceTypeJdbc = Ext.create("Ext.form.field.Radio",{
			fieldLabel: LN('sbi.datasource.type'),
			boxLabel: LN('sbi.datasource.type.jdbc'), 
			name: 'TYPE' , 
			inputValue:'jdbc'
		})

		this.dataSourceTypeJndi = Ext.create("Ext.form.field.Radio",{
            hideEmptyLabel: false,
			boxLabel: LN('sbi.datasource.type.jndi'), 
			checked : true,
			name: 'TYPE' , 
			inputValue:'jndi'
		});

		var thisPanel = this;
		this.dataSourceJndiName = Ext.create('Ext.form.field.Trigger', {
	    	triggerCls:'x-form-question-trigger',
			name: "JNDI_URL",
			fieldLabel: LN('sbi.datasource.type.jndi.name'),
			allowBlank: false,
	    	onTriggerClick: function(e) {
		        
	    		if(!thisPanel.win){
		        	
		        	var p = new Ext.Panel({
		        		bodyStyle: 'background-color: white;',
		        		html:LN('sbi.datasource.jndiname.info')
		        	})
		        	
		        	thisPanel.win = new Ext.Window({
		                layout:'fit',
		                bodyStyle: 'background-color: white;',
		                width:300,
		                height:150,
		                closeAction:'hide',
		                plain: true,
		                items: [p],
		                buttons: [{
		                    text: 'Close',
		                    handler: function(){
		                    	thisPanel.win.hide();
		                    }
		                }]
		            });
		        }
	    		thisPanel.win.show(this);
			}
	    	
	    });
		
		 if (Sbi.user.isSuperAdmin !== 'true'){

			 this.dataSourceTypeJndi.disable();
			 this.dataSourceJndiName.disable();
		 } 
		
		this.dataSourceJdbcUser = Ext.create("Ext.form.field.Text",{
			name: "USER",
			fieldLabel: LN('sbi.datasource.type.jdbc.user'),
			hidden: true
		});
		
		this.dataSourceJdbcUrl = Ext.create("Ext.form.field.Text",{
			name: "CONNECTION_URL",
			fieldLabel: LN('sbi.datasource.type.jdbc.url'),
			allowBlank: false,
			hidden: true
		});
		
		this.dataSourceJdbcPassword = Ext.create("Ext.form.field.Text",{
			name: "PASSWORD",
			inputType: 'password',
			fieldLabel: LN('sbi.datasource.type.jdbc.password'),
			hidden: true
			
		});
		
		this.dataSourceDriver = Ext.create("Ext.form.field.Text",{
			name: "DRIVER",
			fieldLabel: LN('sbi.datasource.driver'),
			allowBlank: false,
			hidden: true
		});	

		this.dataSourceTypeJdbc.on("change", function(field, newValue, oldValue, eOpts){
			if(newValue){
				this.dataSourceJdbcPassword.show();
				this.dataSourceJdbcUrl.show();
				this.dataSourceJdbcUser.show();
				this.dataSourceDriver.show();
			}else{
				this.dataSourceJdbcPassword.hide();
				this.dataSourceJdbcUrl.hide();
				this.dataSourceJdbcUser.hide();
				this.dataSourceDriver.hide();
			}
		},this);
		
		this.dataSourceTypeJndi.on("change", function(field, newValue, oldValue, eOpts){
			if(newValue){
				this.dataSourceJndiName.show();
			}else{
				this.dataSourceJndiName.hide();

			}
		},this);
	}
	
	, setFormState: function(values){
		var v = values;
		if(v.JNDI_URL){
			v.TYPE='jndi';
		}else{
			v.TYPE='jdbc';
		}
		if(v.SCHEMA && v.SCHEMA!=""){
			v.MULTISCHEMA='on';
		}
		
		// convert for radio button
		if(v.READ_ONLY != true)
		{
			v.READ_WRITE = 'readwrite';	
		}
		else{
			v.READ_WRITE = 'readonly';	
		}
		
		this.getForm().setValues(v);
		

		
		if (Sbi.user.isSuperAdmin != 'true' && (this.dataSourceTypeJndi.getValue() || (v.USERIN!="" && v.USERIN != Sbi.user.userId))){
			 
			 //set all fields readonly
			 this.dataSourceLabel.disable();
			 this.dataSourceDescription.disable();
			 this.dataSourceDialect.disable();
			 this.dataSourceMultischema.disable();
			 this.dataSourceReadOnly.disable();
			 this.dataSourceReadWrite.disable();
			 this.dataSourceWriteDefault.disable();
			 this.dataSourceTypeJdbc.disable();
			 
			 this.dataSourceTypeJndi.disable();
			 this.dataSourceJndiName.disable();
			 
			 this.dataSourceJdbcPassword.disable();
			 this.dataSourceJdbcUrl.disable();
			 this.dataSourceJdbcUser.disable();
			 this.dataSourceDriver.disable();
		 } 
		 else{
			 
			 this.dataSourceLabel.enable();
			 this.dataSourceDescription.enable();
			 this.dataSourceDialect.enable();
			 this.dataSourceMultischema.enable();
			 this.dataSourceReadOnly.enable();
			 this.dataSourceReadWrite.enable();
			 this.dataSourceWriteDefault.enable();
			 this.dataSourceTypeJdbc.enable();			 
			 //this.dataSourceTypeJndi.enable();
			 //this.dataSourceJndiName.enable();
			 this.dataSourceJdbcPassword.enable();
			 this.dataSourceJdbcUrl.enable();
			 this.dataSourceJdbcUser.enable();
			 this.dataSourceDriver.enable();
		 }
		
		if(v.READ_ONLY == true)
		{
			this.dataSourceWriteDefault.disable();	
		}

	}
	
	, getValues: function(){
		var values = this.callParent();
		if(values.TYPE=='jdbc'){
			values.JNDI_URL="";
		}
        
        var readOnly =  this.dataSourceReadOnly.getValue();
        var readWrite =  this.dataSourceReadWrite.getValue();
		if(readOnly == true){
			values.READ_ONLY=true;
		}
		else{
			values.READ_ONLY = false;
		}
		
        var writeDefault = this.dataSourceWriteDefault.getValue();
        values.WRITE_DEFAULT = writeDefault;
		
		return values;
	}
	
	, validateForm: function(){
		var valid = true;
		var v = this.getValues();

		valid = valid && (v.DIALECT_ID!=null && v.DIALECT_ID!=undefined &&  v.DIALECT_ID!="");
		valid = valid && (v.DATASOURCE_LABEL!=null && v.DATASOURCE_LABEL!=undefined &&  v.DATASOURCE_LABEL!="");
		if(v.TYPE == 'jndi'){
			valid = valid && (v.JNDI_URL!=null && v.JNDI_URL!=undefined &&  v.JNDI_URL!="");
		}else{
			valid = valid && (v.CONNECTION_URL!=null && v.CONNECTION_URL!=undefined &&  v.CONNECTION_URL!="");
			//valid = valid && (v.USER!=null && v.USER!=undefined &&  v.USER!="");
			valid = valid && (v.DRIVER!=null && v.DRIVER!=undefined &&  v.DRIVER!="");
		}
		return valid;
	}
	
	,
	writeDefaultCheck : function(check, checked) {
		// var persistSelected = newValue;
		var writeSelected = checked;
		if (writeSelected != null && writeSelected == true) {
			
	        this.dataSourceReadOnly.setValue(false);
	        this.dataSourceReadWrite.setValue(true);
	        this.dataSourceReadOnly.disable();
		} else {
			this.dataSourceReadOnly.enable();
		
		}
	}
	
	,
	readOnlyCheck : function(check, checked) {
		// var persistSelected = newValue;
		var readOnlySel = checked;
		if (readOnlySel != null && readOnlySel == true) {
			
	        this.dataSourceWriteDefault.setValue(false);
	        this.dataSourceWriteDefault.disable();
		} else {
			this.dataSourceWriteDefault.enable();
		
		}
	}
	
});
    