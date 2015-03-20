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
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.TableDesignerPanel = function(config) { 

	var defaultSettings = {
		title: LN('sbi.worksheet.designer.tabledesignerpanel.title')
	};
		
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.tableDesignerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.tableDesignerPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.addEvents("attributeDblClick", "attributeRemoved");
	
	this.tableDesigner = new Sbi.worksheet.designer.QueryFieldsCardPanel({ddGroup: this.ddGroup});
	// propagate events
	this.tableDesigner.on(
		'attributeDblClick' , 
		function (thePanel, attribute) { 
			this.fireEvent("attributeDblClick", this, attribute); 
		}, 
		this
	);
	this.tableDesigner.on(
		'attributeRemoved' , 
		function (thePanel, attribute) { 
			this.fireEvent("attributeRemoved", this, attribute); 
		}, 
		this
	);
	
	c = {
		layout: 'fit',
		height: 350,
		items: [new Ext.Panel({items:[this.tableDesigner], border: false, bodyStyle: 'width: 100%; height: 100%'})]
	};
	
	Sbi.worksheet.designer.TableDesignerPanel.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.worksheet.designer.TableDesignerPanel, Ext.Panel, {
	tableDesigner: null,
	
	getFormState: function() {
		var state = {};
		state.designer = 'Table';
		state.visibleselectfields = this.tableDesigner.tableDesigner.getContainedValues();
		return state;
	}
	
	, setFormState: function(state) {
		if(state.visibleselectfields!=undefined && state.visibleselectfields!=null){
			this.tableDesigner.tableDesigner.setValues(state.visibleselectfields);
		}
	}
	/* tab validity: rules are
	 * - at least one measure or attribute is in
	 */

	, validate: function(validFields){
		
		var valErr = ''+this.tableDesigner.validate(validFields);

		if(valErr!= ''){
			valErr = valErr.substring(0, valErr.length - 1)
			return LN("sbi.worksheet.designer.validation.invalidFields")+valErr;
		}
		
		var vals = this.tableDesigner.tableDesigner.getContainedValues();
		if (vals && vals.length> 0) {return} // OK
		else {
				return LN("sbi.designertable.tableValidation.noElement");
		} // ERROR MESSAGE
	}
	
	, containsAttribute: function (attributeId) {
		return this.tableDesigner.containsAttribute(attributeId);
	}
	
	
	
});
