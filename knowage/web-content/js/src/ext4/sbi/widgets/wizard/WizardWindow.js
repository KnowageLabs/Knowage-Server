/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**TODO aggiornare documentazione!!!
 * 
 * Wizard window base. It define a layout and provides the stubs methods for the wizard.
 * This methods should be overridden to define the logic. 
 * The configuration of the window must be passed to the object.
 * 
 * 
 * 		@example
 * 
 * @author
 * Antonella Giachino (antonella.giachino@eng.it)
 */

Ext.define('Sbi.widgets.wizard.WizardWindow', {
    extend: 'Ext.Window'
    
    ,config: {    	  	     	
    	width: 	700,
		height: 700,//500,
		hasBuddy: false,	
		modal: true,
		closeAction:'destroy',
		constrain: true,
		plain: true,
		title: '',
		buttonAlign : 'center',
		wizardPanel: null,		
		fieldMap: {},
		resizable: false,
		isTabbedPanel: null
    } 
   
	/**
	 * In this constructor you must pass configuration
	 */
	, constructor: function(config) {
		this.initTabPanel(config);		
		this.addEvents('cancel',		           
		  	 		   'navigate',
		  	 		   'confirm');

		this.callParent(arguments);
	}
	
	, initTabPanel: function(c){
		var localConf = {};		
		localConf.items = c.tabs;		
		localConf.activeTab = 0;
		localConf.bodyStyle = 'z-index:80000';
		localConf.height = this.height - 60; //360; 
		localConf.fieldDefaults= {
	            labelAlign: 'right',
	            msgTarget: 'side'
	        };
		localConf.border = false;
		
	    if (this.isTabbedPanel !== undefined && this.isTabbedPanel === true){	  
	    	this.wizardPanel = Ext.create('Ext.TabPanel', localConf);
	    }else{
	    	localConf.layout = 'card';
	    	this.wizardPanel = Ext.create('Ext.Panel', localConf);
	    }
		
		this.items = [this.wizardPanel];
	}
	
	, createWizardToolbar: function() {
		Sbi.debug('WizardWindow bulding the wizard toolbar...');
		var buttons = [];
		if(this.buttonsBar){
			for(var i=0; i<this.buttonsBar.length;i++){
				buttons.push(this.buttonsBar[i]);
			}
		}
		
		Sbi.debug('WizardWindow wizard toolbar created...');
		return buttons;
	}
	
	, navigate: function(panel, direction){
		this.fireEvent('navigate',panel, direction);

        // This routine could contain business logic required to manage the navigation steps.
        // It would call setActiveItem as needed, manage navigation button state, handle any
        // branching logic that might be required, handle alternate actions like cancellation
        // or finalization, etc.  A complete wizard implementation could get pretty
        // sophisticated depending on the complexity required, and should probably be
        // done as a subclass of CardLayout in a real-world implementation.
		 
	}
	
	, createStepFieldsGUI: function(fieldsConf) {		
		if (!fieldsConf) return;
		
		var fields = [];		
		
		
		for(var i=0; i<fieldsConf.length;i++){			
			var tmpField = this.createWizardField(fieldsConf[i]);			
			if (fieldsConf[i].hidden == undefined || fieldsConf[i].hidden == "false"){
				fields.push(tmpField);
			}
			this.fieldMap[fieldsConf[i].name] = tmpField;	
		}
		
		return fields;
	}
	
	, initWizardBar: function() {
		var thisPanel = this;
		var buttonsBar = [];
		buttonsBar.push('->');
		buttonsBar.push({ id: 'move-prev',
            text: LN('sbi.ds.wizard.back'),
            handler: function(btn) {
            	thisPanel.navigate(btn.up("panel"), "prev");
            }, 
            scope: this,
            disabled: true
        });
		
		buttonsBar.push({id: 'move-next',
            text:  LN('sbi.ds.wizard.next'),
            handler: function(btn) {
            	thisPanel.navigate(btn.up("panel"), "next");
            }, scope: this
        	});
		
		buttonsBar.push({id: 'confirm',
			hidden: true,
            text:  LN('sbi.ds.wizard.confirm'),
            handler: function(){
            	thisPanel.fireEvent('confirm', this);   
            }, scope: this            
        	});
		
		buttonsBar.push({id: 'cancel',
            text:  LN('sbi.ds.wizard.cancel'),
            handler: function(){
//            	thisPanel.hide();
            	thisPanel.fireEvent('cancel', this);   
            }, scope: this
        	});
		return buttonsBar;
	}

	, createWizardField: function (obj){
		var tmpField = null;
		if (obj.type === undefined ||obj.type == 'text'){        			
			//default is a text
			tmpField = this.createTextField(obj);        		          		
		}else if (obj.type == 'textarea'){  
			//textarea
			tmpField = this.createTextAreaField(obj);   
		} else if (obj.type == 'data'){  
			//data
			tmpField = this.createDataField(obj);   
		}  else if (obj.type == 'combo'){	
			//combobox     			
			tmpField = this.createComboField(obj);    			    					 
		} else if (obj.type == 'checkList'){
			//multivalue management
			tmpField = this.createCheckListField(obj);    	
			
		} else if (obj.type == 'lookup'){
			//singlevalue management
			tmpField = this.createListField(obj);        			
  		} else if (obj.type == 'group'){
			//group radio management
  			tmpField = this.createRadioGroupField(obj);        			
  		} else if (obj.type == 'checkbox'){
  			//single checkbox management
  			tmpField = this.createCheckBoxField(obj)
  		}
		
		return tmpField;
	}

	, createTextField: function(f){
		var field = new Ext.form.TextField({
  		  fieldLabel: f.label
  		  	  , id: f.id 
  		  	  , name: f.name
	          , width: 500 
	          //, height: 30
			  , activeError: this.isError(f)
			  , validationEvent: f.mandatory || false
			  , allowBlank : (f.mandatory !== undefined && f.mandatory==true)?false:true
			  , blankText: LN('sbi.ds.field.mandatory')		  
			  , margin: '5 0 0 10'
			  , maxLength: f.maxLength
			  , enforceMaxLength: (f.maxLength !== undefined)? true: false 
			  , msgTarget: f.msgTarget || 'qtip'
	          , readOnly: f.readOnly || false
//	          , labelStyle:'font-weight:bold;' //usare itemCls : <tagstyle>
	          , value: f.value || f.defaultValue || ""
	          , disabled: f.disabled || false
	          , fieldStyle: (f.hideBorder !== undefined && f.hideBorder == true)? "border:none 0px black; background-image:none;":""	          
	        });
		return field;
	}
	
	, createTextAreaField: function(f){
		var field = new Ext.form.TextArea({
  		  fieldLabel: f.label 
  		  	  , name: f.name
	          , width: 500 
	          , height: 80
//	          , vertical-align:top
			  , xtype : 'textarea'
			  , multiline: true
			  , maxLength: 250
			  , activeError:   this.isError(f)
			  , validationEvent: f.mandatory || false
			  , allowBlank : (f.mandatory !== undefined && f.mandatory==true)?false:true
	          , margin: '5 0 0 10'
	          , readOnly: f.readOnly || false
//	          , labelStyle:'font-weight:bold;' //usare itemCls : <tagstyle>
	          , value: f.value || f.defaultValue || ""
	        });
		return field;
	}
	
	
	, createDataField: function(f){
		this.dateFormat = f.values.format || 'd/m/Y';
		
		var field = new Ext.form.DateField({
		  fieldLabel: f.label 
		  , name: f.name
          , width: 250  
          , margin: '5 0 0 10'
          , validationEvent: f.mandatory || false
          , allowBlank : (f.mandatory !== undefined && f.mandatory==true)?false:true
    	  , activeError:   this.isError(f)
          , format: f.values.format || 'd/m/Y'
          , defaultValue: f.defaultValue 
        });
		
		return field;
	}
	
	, createComboField: function(f){
		
		var tmpStore = f.data; 
		var tmpValueField = f.valueCol;
		var tmpValueText = f.descCol;    	
		
		var field = new Ext.form.ComboBox({
			fieldLabel: f.label ,
			store :tmpStore,
			name : f.name,			
			width : 500,
			margin: '2 0 0 10',
			displayField : tmpValueText,
			valueField : tmpValueField,
			readOnly: f.readOnly || false,
//			labelStyle:'font-weight:bold;', 
			emptyText:'Select ...',
			typeAhead : true, forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, editable : false,
		    validationEvent: f.mandatory || false,
	        allowBlank : (f.mandatory !== undefined && f.mandatory==true)?false:true,
			activeError:  this.isError(f), 
			xtype : 'combo'	,
			value:  f.value
		});
		
		return field;
	}
		
	, createCheckBoxField: function(f){
		var field = new Ext.form.Checkbox({
			fieldLabel: f.label,
			name: f.name,
	        value: f.value,
	        margin: '2 0 0 10'
		})
		return field;
	}

	
	, getFormState: function() {
    	var state = {};
		var index = null;
    	for(f in this.fieldMap) {  
    		index = f;
			var tmpField = this.fieldMap[f];
			var tmpFieldValue = tmpField.getValue();
			var tmpDefaultValue = tmpField.defaultValue;    	
			//the field is correctly valorized:
			if (tmpField.getXTypes().indexOf('/datefield') >= 0){
				//if it's a date: sets its format
//    				state[index] = Sbi.console.commons.Format.date(tmpFieldValue , this.dateFormat);
			}else{	
				//sets the current value
				state[index] = tmpFieldValue;
			}	    		    		 	
    	}
    	return state;
    }
	
	, validateForm: function() {
		var toReturn = true;
    	for(f in this.fieldMap) {  
    		index = f;
			var tmpField = this.fieldMap[f];
			if (tmpField.validationEvent !== undefined && tmpField.validationEvent == true && 
				tmpField.allowBlank !== undefined && tmpField.allowBlank == false &&
				(tmpField.value == undefined || tmpField.value == "" || tmpField.validate() == false)){
				toReturn = false;
				break;
			}			   		 	
    	}
    	return toReturn;
    }
	
	, isError: function(f){
		var toReturn = false;
		if (f.mandatory == true && (f.value == undefined || f.value == null || f.value == ""))
			toReturn = true;
		else
			toReturn = false;

		return toReturn; 
	}
	
});