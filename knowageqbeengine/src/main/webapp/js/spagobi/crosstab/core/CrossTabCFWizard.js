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
Ext.ns("Sbi.crosstab.core");

Sbi.crosstab.core.CrossTabCFWizard = function(config) {

	this.baseNode = config.baseNode;
	this.activeLevel = this.baseNode.level;
	this.horizontal = this.baseNode.horizontal;
	
	this.modality = config.modality; // new (for a new calculated field definition) or edit (for an existing calculated field modification)
	
	this.initMainPanel();

	var c = {
			title: LN('sbi.crosstab.calculatefieldwizard.title'),
			layout: 'fit',
			width: 350,
			height: 250,
			items:[this.mainPanel],
		    buttons: [{
				text: LN('sbi.crosstab.calculatefieldwizard.ok'),
			    handler: function(){
		    		var expression= this.getExpression();
		    		var cfName= this.cfNameField.getValue();
		    		if(expression!=null && expression!="" && cfName!=null && cfName!="" && this.validate(false)){
		    			if(this.modality == 'edit'){
		    				this.fireEvent('modifyCalculatedField', this.baseNode, this.activeLevel, this.horizontal, expression, cfName);
		    			}else{
		    				this.fireEvent('applyCalculatedField', this.baseNode, this.activeLevel, this.horizontal, expression, cfName);
		    			}
		    			this.close();
		    		}
	        	}
	        	, scope: this
		    }], 
		    tools: [{
	          id: 'help',
	          handler: function(event, toolEl, panel) {
	        	  var aWindow = new Ext.Window({
	        		  width: 300,
	        		  style: 'padding: 5px;',
	        		  items: [{
	        			  xtype: 'panel',
	        			  html: LN('sbi.crosstab.calculatefieldwizard.info')
	        		  }]
	        	  });
	        	  aWindow.show();
	          },
	          scope: this
		    }]
	};
	
	Sbi.crosstab.core.CrossTabCFWizard.superclass.constructor.call(this, c);
	
};
	
Ext.extend(Sbi.crosstab.core.CrossTabCFWizard, Ext.Window, {
	baseNode: null
	,textField: null
	,activeLevel: null
	,horizontal: null
	,cfNameField: null
	,mainPanel: null
	
	
	,addField: function(text, level, horizontal){
		if(this.activeLevel==null){
			this.activeLevel = level;
		}
		if(this.horizontal==null){
			this.horizontal = horizontal;
		}
		if(this.activeLevel != level || this.horizontal!=horizontal){
			return;
		}
		this.textField.insertAtCursor(text); 
	}


	,isActiveLevel: function(level, horizontal){
		if(this.activeLevel==null){
			return true;
		}else{
			return (this.activeLevel == level) && (this.horizontal == horizontal);
		}
	}
	
	, getExpression: function() {
		var expression;
		if(this.textField) {
	  		expression = this.textField.getValue();
	  		expression = Ext.util.Format.stripTags( expression );
	  		expression = expression.replace(/&nbsp;/g," ");
	  		expression = expression.replace(/\u200B/g,"");
	  		expression = expression.replace(/&gt;/g,">");
	  		expression = expression.replace(/&lt;/g,"<");
		}
		return expression;
	}

	,validate: function(showSuccess){
		var error = Sbi.crosstab.core.ArithmeticExpressionParser.module.validateCrossTabCalculatedField(this.getExpression());
		if(error==""){
			if(showSuccess){
				Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.qbe.calculatedFields.validationwindow.success.text'), LN('sbi.qbe.calculatedFields.validationwindow.success.title'));
			}
			return true;
		}else{
			Sbi.exception.ExceptionHandler.showWarningMessage(error, LN('sbi.qbe.calculatedFields.validationwindow.fail.title'));
			return false;
		}
	}
	
	
	
	,initTextField: function(){

		var buttonclear = new Ext.Button({
		    text: LN('sbi.crosstab.calculatefieldwizard.clear'),
		    icon: 'null',
		    iconCls:'remove'
		});
		buttonclear.addListener('click', function(){this.textField.reset();}, this);

		
		var buttonvalidate = new Ext.Button({
		    text: LN('sbi.crosstab.calculatefieldwizard.validate'),
		    icon: 'null',
		    iconCls:'option'
		});
		buttonvalidate.addListener('click', function(){this.validate(true);}, this);

	
		this.textField = new Ext.form.HtmlEditor({
    		name:'expression',
    	    enableAlignments : false,
    	    enableColors : false,
    	    enableFont :  false,
    	    enableFontSize : false, 
    	    enableFormat : false,
    	    enableLinks :  false,
    	    enableLists : false,
    	    enableSourceEdit : false,
    	    listeners:{
		    	'render': function(editor){
					var tb = editor.getToolbar();
					tb.add(buttonclear);
					tb.add(buttonvalidate);
		        },
    	        'initialize': {
		        	fn: function(){
						this.onFirstFocus();
	    	        } 
    	        } 
    	    }
    	});
		
		// in case of modifying an existing CF, putting the initial value info CF name field
		if (this.modality == 'edit' && this.baseNode.type == 'CF') {
			this.textField.setValue(this.baseNode.cfExpression);
		}
	}
	
	, initMainPanel: function() {
		
		this.initTextField();
		
		this.cfNameField = new Ext.form.TextField({
			name:'name',
			allowBlank: false, 
			fieldLabel: 'Nome'
		});
		
		// in case of modifying an existing CF, putting the initial value info CF name field
		if (this.modality == 'edit' && this.baseNode.type == 'CF') {
			this.cfNameField.setValue(this.baseNode.name);
		}
		
		this.mainPanel = new Ext.Panel({
			layout: 'border',
		    items: [
		             new Ext.form.FormPanel({
		     	    	region:'north',
		     	    	height: 30,
		    		    border: true,
		    		    frame: false, 
			            items: [this.cfNameField],
			            bodyStyle: "background-color: transparent; border-color: transparent; padding-top: 2px; padding-left: 10px;"
			         }),
		             new Ext.Panel({
			            region:'center',
			            layout: "fit",
			    		border: true,
			    		frame: false, 
			            items: [this.textField]
			         })
		           ]
		 });
    }
});