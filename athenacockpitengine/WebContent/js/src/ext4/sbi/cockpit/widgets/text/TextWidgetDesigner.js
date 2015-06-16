/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


Ext.ns("Sbi.cockpit.widgets.text");

Sbi.cockpit.widgets.text.TextWidgetDesigner = function(config) {

	var defaultSettings = {
		name: 'textWidgetDesigner',
		title: LN('sbi.cockpit.widgets.text.textWidgetDesigner.title')
	};

	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.text && Sbi.settings.cockpit.widgets.text.textWidgetDesigner) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.text.textWidgetDesigner);
	}
	var c = Ext.apply(defaultSettings, config || {});
	Ext.apply(this, c);

	this.initTextPanel();
	
	c = {
		layout: 'fit',
		height: 350,
		items: [
		        new Ext.Panel({
		        	border: false
		        	, bodyStyle: 'width: 100%; height: 100%'
		        	, items:[this.textPanel]
		        })
		]
	};

	Sbi.cockpit.widgets.text.TextWidgetDesigner.superclass.constructor.call(this, c);

	this.on(
			'beforerender' ,
			function (thePanel, attribute) {
				var state = {};
				state.textValue = thePanel.textValue;
				state.wtype = 'text';
				this.setDesignerState(state);
			},
			this
	);
};

Ext.extend(Sbi.cockpit.widgets.text.TextWidgetDesigner, Sbi.cockpit.core.WidgetDesigner, {
	textDesigner: null

	, getDesignerState: function(running) {
		Sbi.trace("[TextWidgetDesigner.getDesignerState]: IN");

		var state = Sbi.cockpit.widgets.text.TextWidgetDesigner.superclass.getDesignerState(this);
		state.designer = 'Text Designer';
		state.wtype = 'text';
		
		state.textValue = this.textField.getValue();
		
		Sbi.trace("[TextWidgetDesigner.getDesignerState]: OUT");
		return state;
	}

	, setDesignerState: function(state) {
		Sbi.trace("[TextWidgetDesigner.setDesignerState]: IN");
		Sbi.cockpit.widgets.text.TextWidgetDesigner.superclass.setDesignerState(this, state);
		if(state.textValue) this.textField.setValue(state.textValue);
		
		Sbi.trace("[TextWidgetDesigner.setDesignerState]: OUT");
	}

	/* tab validity: rules are
	 * - at least one measure or attribute is in
	 */
	, validate: function(validFields){

		var valErr = Sbi.cockpit.widgets.text.TextWidgetDesigner.superclass.validate(this, validFields);
		if(valErr!= ''){
			return varErr;
		}

		valErr = ''+this.textDesigner.validate(validFields);

		if(valErr!= ''){
			valErr = valErr.substring(0, valErr.length - 1);
			return LN("sbi.cockpit.widgets.text.validation.invalidFields")+valErr;
		}

		var vals = this.textDesigner.textDesigner.getContainedValues();
		if (vals && vals.length> 0) {return;} // OK
		else {
				return LN("sbi.designertext.textValidation.noElement");
		}
	}

	, containsAttribute: function (attributeId) {
		return this.textDesigner.containsAttribute(attributeId);
	}

	, attributeDblClickHandler : function (thePanel, attribute, theSheet) {

	}

	, initTextPanel: function (){
	   this.textField = Ext.create('Ext.form.HtmlEditor', {
			width: 525,
		    height: 100,
//		    labelWidth: 50,
		    fieldLabel: 'Text',
		    name: 'textField',
		    enableLinks: false,
		    enableSourceEdit: false,
		    enableLists: false
		});
		this.textPanel = Ext.create('Ext.form.Panel', {
	        border: false,
	        fieldDefaults: {
	            labelWidth: 55
	        },
	        /*url: 'save-form.php',*/
	        defaultType: 'textfield',
	        bodyPadding: 5,

	        items: [this.textField/*this.fontTypeCombo, this.rowsFontSizeCombo, this.rowsFontColorText, this.rowsFontWeightCombo, this.rowsFontDecorationCombo*/]
	    });
	}

});
