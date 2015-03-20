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
 * - Davide Zerbetto (davide.zerbetto@eng.it)
 */

Ext.ns("Sbi.crosstab");

Sbi.crosstab.CrosstabDefinitionPanel = function(config) {

	var defaultSettings = {
			title: LN('sbi.crosstab.crosstabdefinitionpanel.title')
	};
	if(Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.crosstabDefinitionPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.crosstabDefinitionPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c); // this operation should overwrite this.crosstabTemplate content, that is the definition of the crosstab

	this.addEvents("attributeDblClick");
	
	this.init(c);

	c = Ext.apply(c, {
		items: [this.crosstabDefinitionPanel]
		        , autoScroll: true
		        , tools: this.tools || []
	});

	// constructor
	Sbi.crosstab.CrosstabDefinitionPanel.superclass.constructor.call(this, c);

	this.columnsContainerPanel.on(	'beforeAddAttribute', this.checkIfAttributeIsAlreadyPresent, this);
	this.rowsContainerPanel.on(		'beforeAddAttribute', this.checkIfAttributeIsAlreadyPresent, this);
	
};

Ext.extend(Sbi.crosstab.CrosstabDefinitionPanel, Ext.Panel, {

	crosstabTemplate: {}
	, isStatic: false
	, crosstabDefinitionPanel: null
	, columnsContainerPanel: null
	, rowsContainerPanel: null
	, measuresContainerPanel: null
	, ddGroup: null // must be provided with the constructor input object
	
	, init: function(c) {
	
		this.columnsContainerPanel = new Sbi.crosstab.AttributesContainerPanel({
			title: LN('sbi.crosstab.crosstabdefinitionpanel.columns')
			, width: 400
			, initialData: this.crosstabTemplate.columns
			, ddGroup: this.ddGroup
		});
		// propagate events
		this.columnsContainerPanel.on(
			'attributeDblClick' , 
			function (thePanel, attribute) { 
				this.fireEvent("attributeDblClick", this, attribute); 
			}, 
			this
		);
		this.columnsContainerPanel.on(
			'attributeRemoved' , 
			function (thePanel, attribute) { 
				this.fireEvent("attributeRemoved", this, attribute); 
			}, 
			this
		);
	
		this.rowsContainerPanel = new Sbi.crosstab.AttributesContainerPanel({
			title: LN('sbi.crosstab.crosstabdefinitionpanel.rows')
			, width: 200
			, initialData: this.crosstabTemplate.rows
			, ddGroup: this.ddGroup
		});
		// propagate events
		this.rowsContainerPanel.on(
			'attributeDblClick' , 
			function (thePanel, attribute) { 
				this.fireEvent("attributeDblClick", this, attribute); 
			}, 
			this
		);
		this.rowsContainerPanel.on(
			'attributeRemoved' , 
			function (thePanel, attribute) { 
				this.fireEvent("attributeRemoved", this, attribute); 
			}, 
			this
		);
	
		this.measuresContainerPanel = new Sbi.crosstab.MeasuresContainerPanel({
			title: LN('sbi.crosstab.crosstabdefinitionpanel.measures')
			, width: 400
			, initialData: this.crosstabTemplate.measures
			, crosstabConfig: this.crosstabTemplate.config
			, ddGroup: this.ddGroup
			, isStatic: this.isStatic
		});
	
		this.crosstabDefinitionPanel = new Ext.Panel({
			layout: 'table'
				, baseCls:'x-plain'
					, padding: '30 30 30 100'
						, layoutConfig: {columns:2}
		// applied to child components
		, defaults: {height: 150}
		, items:[
		         {
		        	 border: false
		         }
		         , this.columnsContainerPanel
		         , this.rowsContainerPanel
		         , this.measuresContainerPanel
		         ]
		});
	
		
	}

	, getCrosstabDefinition: function() {
		var crosstabDef = {};
		crosstabDef.rows = this.rowsContainerPanel.getContainedAttributes();
		crosstabDef.columns = this.columnsContainerPanel.getContainedAttributes();
		crosstabDef.measures = this.measuresContainerPanel.getContainedMeasures();
		crosstabDef.config = this.measuresContainerPanel.getCrosstabConfig();
		crosstabDef.config.type = 'pivot';
		return crosstabDef;
	}
	
	, getFormState: function() {
		var crosstabDefinition = this.getCrosstabDefinition();
		var state = {
				'designer':'Pivot Table',
				'crosstabDefinition': crosstabDefinition
		};
		return state;
	}

	, setFormState: function(state) {
		if (state !== undefined && state !== null && state.crosstabDefinition !== undefined && state.crosstabDefinition !== null) {
			var crosstabDefinition = state.crosstabDefinition;
			if (crosstabDefinition.rows) this.rowsContainerPanel.setAttributes(crosstabDefinition.rows);
			if (crosstabDefinition.columns) this.columnsContainerPanel.setAttributes(crosstabDefinition.columns);
			if (crosstabDefinition.measures) this.measuresContainerPanel.setMeasures(crosstabDefinition.measures);
			if (crosstabDefinition.config) this.measuresContainerPanel.setCrosstabConfig(crosstabDefinition.config);
		}
	}




	/* check cross tab validity: rules are
	 * - at least one measure
	 * - at least one attribute in rows or in columns
	 * - if there is segment attribute it must be used
	 * - if ther is mandatory measure it must be used
	 */

	, validate: function(validFields){
		var valErr='';

		valErr+= ''+this.columnsContainerPanel.validate(validFields);
		valErr+= ''+this.rowsContainerPanel.validate(validFields);
		valErr+= ''+this.measuresContainerPanel.validate(validFields);
		
		if(valErr != ''){
			valErr = valErr.substring(0, valErr.length - 1)
			return LN("sbi.worksheet.designer.validation.invalidFields")+valErr;
		}
		
		var crossTabDef = this.getCrosstabDefinition();
	
		// at least one measure
		if(crossTabDef.measures.length<1){
			return LN('sbi.crosstab.crossTabValidation.noMeasure');
	
		}
	
		// at least one row or one column
		else if(crossTabDef.columns.length<1 && crossTabDef.rows.length<1){
			return LN('sbi.crosstab.crossTabValidation.noAttribute');
		} 
	
		// if there is mandatoryField it must have been inserted
		if(this.measuresContainerPanel.hasMandatoryMeasure === true){
			var isMandatory = this.isThereMandatoryMeasure(crossTabDef);
			if(isMandatory === false){
				return LN('sbi.crosstab.crossTabValidation.noMandatoryMeasure');
			}
	
		}

		// if there is segmentAttribute it must have been inserted in columns or rows
		if(this.rowsContainerPanel.hasSegmentAttribute === true || this.columnsContainerPanel.hasSegmentAttribute === true){
			var isSegment = this.isThereSegmentAttribute(crossTabDef);
			if(isSegment === false){
				return LN('sbi.crosstab.crossTabValidation.noSegmentAttribute');
			}
		}
		return null;	
	}

	, isThereSegmentAttribute: function(crossTabDef){
		var isThereSegment = false;
		for (var i = 0; i < crossTabDef.rows.length && isThereSegment === false; i++) {
			var row = crossTabDef.rows[i];
			if(row.nature === 'segment_attribute'){
				isThereSegment = true;
			}
		}
	
		for (var i = 0; i < crossTabDef.columns.length && isThereSegment === false; i++) {
			var row = crossTabDef.columns[i];
			if(row.nature === 'segment_attribute'){
				isThereSegment = true;
			}
		}
		return isThereSegment;
	}
	
	, isThereMandatoryMeasure: function(crossTabDef){
		var isThereMandatory = false;
		for (var i = 0; i < crossTabDef.measures.length && isThereMandatory === false; i++) {
			var measure = crossTabDef.measures[i];
			if(measure.nature === 'mandatory_measure'){
				isThereMandatory = true;
			}
		}
		return isThereMandatory;
	}

	, checkIfAttributeIsAlreadyPresent: function(aPanel, attribute) {
		var attributeId = attribute.id;
		var alreadyPresent = this.containsAttribute(attributeId);
		if (alreadyPresent) {
			Ext.Msg.show({
				   title: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.title'),
				   msg: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.attributealreadypresent'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
			return false;
		}
		return true;
	}
	
	, containsAttribute: function (attributeId) {
		var storeRows = this.rowsContainerPanel.store;
		var storeColumns = this.columnsContainerPanel.store;
		if (storeRows.findExact('id', attributeId) !== -1) {
			return true;
		}
		if (storeColumns.findExact('id', attributeId) !== -1) {
			return true;
		}
		return false;
	}
	
});