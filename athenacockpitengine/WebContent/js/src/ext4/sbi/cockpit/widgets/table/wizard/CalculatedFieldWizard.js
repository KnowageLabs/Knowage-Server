/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. *
 */
 
 
/**
  * CalculatedFieldWizard - short description
  * 
  * Object documentation ...
  * 
  * @author Andrea Gioia (andrea.gioia@eng.it)
  * @author Benedetto Milazzo
  */

Ext.ns("Sbi.cockpit.widgets.table.wizard");

Sbi.cockpit.widgets.table.wizard.CalculatedFieldWizard = function(config) {	
	
	var c = Ext.apply({}, config || {}, {
		title: 'Expression wizard ...'
		, width: 700
		, minWidth: 400
		, height: 350
		, minHeight: 300
		, hasBuddy: false	
		, constrainHeader : true
	});

	Ext.apply(this, c);
	
	this.initMainPanel(c);	
	this.initButtonsConfig(c);
	
	// constructor
	Sbi.cockpit.widgets.table.wizard.SaveWindow.superclass.constructor.call(this, {
		layout: 'fit',
		width: this.width,
		height: this.height,
//		closeAction: 'hide',
		closeAction: 'destroy',
		plain: true,
		title: this.title,
		buttonAlign : 'center',
	    buttons: this.buttonsConfig,
		items: [this.mainPanel]
    });
	
	if(c.hasBuddy === 'true') {
		this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
	}
		
	this.addEvents('apply');    
};

Ext.extend(Sbi.cockpit.widgets.table.wizard.CalculatedFieldWizard, Ext.Window, {
	
	hasBuddy: null
    , buddy: null
   
    , mainPanel: null 
    , buttonsConfig: null

    
    , setExpItems: function(itemGroupName, items) {
    	this.mainPanel.setExpItems(itemGroupName, items);
    }

	, setTargetRecord: function(record) {
		this.mainPanel.setTargetRecord(record);
	}

	, setTargetNode: function(node) {
		this.mainPanel.setTargetNode(node);
	}
	
	, initMainPanel: function(c) {
		this.mainPanel = new Sbi.cockpit.widgets.table.wizard.CalculatedFieldEditorPanel({
			expItemGroups: c.expItemGroups
			, fields: c.fields
			, functions: c.functions
			, aggregationFunctions: c.aggregationFunctions
			, arithmeticFunctions : c.arithmeticFunctions
			, groovyFunctions : c.groovyFunctions
			, dateFunctions: c.dateFunctions
			, expertMode: c.expertMode
			, scopeComboBoxData: c.scopeComboBoxData   		
			, validationService: c.validationService
		});
    }

	, initButtonsConfig: function(c) {
		var okButtonConfig = {
			text: LN('sbi.cockpit.widgets.table.calculatedFields.buttons.text.ok'),
		    handler: function(){

		   	    var emptyAlias = (this.mainPanel.inputFields.alias.getValue()==null) 
		   	    	|| (this.mainPanel.inputFields.alias.getValue().trim() == "");
//		   	    var emptyType = (this.mainPanel.inputFields.type.getValue()==null) 
//		   	    	|| (this.mainPanel.inputFields.type.getValue().trim() == "");

		   	    if(emptyAlias){
		    	   	this.mainPanel.inputFields.alias.focus();
//		    	} else if(emptyType){
//		    	  	this.mainPanel.inputFields.type.focus();
		    	} else {
		    		var fieldType = this.mainPanel.expertMode? 
		    				Sbi.commons.Constants.NODE_TYPE_CALCULATED_FIELD: 
		    					Sbi.commons.Constants.NODE_TYPE_INLINE_CALCULATED_FIELD;
			    	this.fireEvent('apply', this, this.mainPanel.getFormState(), this.mainPanel.target, fieldType);
		           	this.hide();
		    	}
		    }
	       	, scope: this
		};
		
		var koButtonConfig = {
		    text: LN('sbi.cockpit.widgets.table.calculatedFields.buttons.text.cancel'),
		    handler: function(){
//		    	this.hide();
	           	this.close();
	      	}
	       	, scope: this
		}
		
		this.buttonsConfig = [okButtonConfig, koButtonConfig];
	}

	, getCalculatedFieldFormula: function() {
		return this.mainPanel.getExpression();
	}
	
});

//--------------------------------------------------------------------------------------------
// static methods
// --------------------------------------------------------------------------------------------
Sbi.cockpit.widgets.table.wizard.CalculatedFieldWizard.getUsedItemSeeds = function(itemGroupName, expression) {
	 var pattern = new RegExp(/dmFields\['[^\']+'\]/g);
     var patternSeed = new RegExp(/'.*'/);
     var token = null;
     var seeds = new Array();
     while( (token = pattern.exec(expression)) !== null) {
         token = patternSeed.exec(token); 
         token = new String(token);
         token = token.substring(1, token.length - 1);
         seeds.push(token);
     } 
     return seeds;
}