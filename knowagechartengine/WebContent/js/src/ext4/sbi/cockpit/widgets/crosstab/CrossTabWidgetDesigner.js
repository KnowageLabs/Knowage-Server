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

Ext.ns("Sbi.cockpit.widgets.crosstab");

Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner = function(config) {

	var defaultSettings = {
		name: 'crosstablWidgetDesigner'
	};

	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.crosstab && Sbi.settings.cockpit.widgets.crosstab.crosstabWidgetDesigner) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.crosstab.crosstabWidgetDesigner);
	}
	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c); // this operation should overwrite this.crosstabTemplate content, that is the definition of the crosstab

	this.init();

	c = {
		items: [this.crosstabDefinitionPanel]
		,title: LN('sbi.crosstab.crosstabdefinitionpanel.title')
		,border: false
	};

	Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner.superclass.constructor.call(this, c);

	this.columnsContainerPanel.on(	'beforeAddAttribute', this.checkIfAttributeIsAlreadyPresent, this);
	this.rowsContainerPanel.on(		'beforeAddAttribute', this.checkIfAttributeIsAlreadyPresent, this);

	this.columnsContainerPanel.on(
		'render' ,
		function (thePanel, attribute) {
			if(Sbi.isValorized(config)) {
				this.setDesignerState({rows: config.rows, columns: config.columns, measures: config.measures});
			}
		},
		this
	);
};

Ext.extend(Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner, Sbi.cockpit.core.WidgetDesigner, {

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
    // -----------------------------------------------------------------------------------------------------------------

	crosstabTemplate: {}
	, isStatic: false
	, crosstabDefinitionPanel: null
	, columnsContainerPanel: null
	, rowsContainerPanel: null
	, measuresContainerPanel: null
	, ddGroup: null // must be provided with the constructor input object

	, getCrosstabDefinition: function() {
		var crosstabDef = {};
		crosstabDef.rows = this.rowsContainerPanel.getContainedAttributes();
		crosstabDef.columns = this.columnsContainerPanel.getContainedAttributes();
		crosstabDef.measures = this.measuresContainerPanel.getContainedMeasures();
		crosstabDef.config = this.measuresContainerPanel.getCrosstabConfig();
		crosstabDef.config.type = 'pivot';
		return crosstabDef;
	}

	, getDesignerState: function() {
		Sbi.trace("[CrossTabWidgetDesigner.getDesignerState]: IN");
		Sbi.trace("[CrossTabWidgetDesigner.getDesignerState]: " + Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner.superclass.getDesignerState);

		var state = Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner.superclass.getDesignerState(this);

		state.rows = this.rowsContainerPanel.getContainedAttributes();
		state.columns = this.columnsContainerPanel.getContainedAttributes();
		state.measures = this.measuresContainerPanel.getContainedMeasures();
		state.config = this.measuresContainerPanel.getCrosstabConfig();
		state.config.type = 'pivot';

		Sbi.trace("[CrossTabWidgetDesigner.getDesignerState]: OUT");

		return state;
	}

	, setDesignerState: function(state) {
		Sbi.trace("[CrossTabWidgetDesigner.setDesignerState]: IN");
		Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner.superclass.setDesignerState(this, state);

		if (state !== undefined && state !== null) {
			if (state.rows) this.rowsContainerPanel.setAttributes(state.rows);
			if (state.columns) this.columnsContainerPanel.setAttributes(state.columns);
			if (state.measures) this.measuresContainerPanel.setMeasures(state.measures);
		}

		Sbi.trace("[CrossTabWidgetDesigner.setDesignerState]: OUT");
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


	// -----------------------------------------------------------------------------------------------------------------
    // init methods
    // -----------------------------------------------------------------------------------------------------------------
	, init: function() {

		this.columnsContainerPanel = new Sbi.cockpit.widgets.crosstab.AttributesContainerPanel({
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

		this.rowsContainerPanel = new Sbi.cockpit.widgets.crosstab.AttributesContainerPanel({
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

		this.measuresContainerPanel = new Sbi.cockpit.widgets.crosstab.MeasuresContainerPanel({
			title: LN('sbi.crosstab.crosstabdefinitionpanel.measures')
			, width: 400
			, initialData: this.crosstabTemplate.measures
			, crosstabConfig: this.crosstabTemplate.config
			, ddGroup: this.ddGroup
			, isStatic: this.isStatic
		});

		this.crosstabDefinitionPanel = new Ext.Panel({
			baseCls:'x-plain'
			, padding: '10 10 10 30'
			, layout: {
				type: 'table',
				columns:2
			}
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

	, onAfterLayout: function() {
		Sbi.trace("[CrossTabWidgetDesigner.onAfterLayout][" + this.getId() + "]: IN");
		var selections = this.getWidgetManager().getWidgetSelections(this.getId());

		// TODO: reselect rows in a selective way
		this.fireSelectionEvent = true;
		Sbi.trace("[CrossTabWidgetDesigner.onAfterLayout][" + this.getId() + "]: OUT");
	}
});
