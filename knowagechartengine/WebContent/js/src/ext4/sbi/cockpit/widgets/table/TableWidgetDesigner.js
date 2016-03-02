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

Ext.ns("Sbi.cockpit.widgets.table");

Sbi.cockpit.widgets.table.TableWidgetDesigner = function(config) {

	var defaultSettings = {
		name: 'tableWidgetDesigner',
		title: LN('sbi.cockpit.widgets.table.tableWidgetDesigner.title')
	};

	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.table && Sbi.settings.cockpit.widgets.table.tableWidgetDesigner) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.table.tableWidgetDesigner);
	}
	var c = Ext.apply(defaultSettings, config || {});
	Ext.apply(this, c);

	this.addEvents("attributeDblClick", "attributeRemoved");

	this.tableDesigner = new Sbi.cockpit.widgets.table.QueryFieldsCardPanel({
		ddGroup: this.ddGroup
	});

	// propagate events
	this.tableDesigner.on(
		'attributeDblClick' ,
		function (thePanel, attribute) {
			//this.fireEvent("attributeDblClick", this, attribute);
			this.attributeDblClickHandler(attribute, thePanel);
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

	this.tableDesigner.on(
		'render' ,
		function (thePanel, attribute) {
			if(Sbi.isValorized(this.visibleselectfields)) {
				this.setDesignerState({visibleselectfields: this.visibleselectfields});
			}
		},
		this
	);

	c = {
		layout: 'fit',
		height: 350,
		items: [
		        new Ext.Panel({
		        	border: false
		        	, bodyStyle: 'width: 100%; height: 100%'
		        	, items:[this.tableDesigner]
		        	//, html: "tableDesigner"
		        })
		]
	};

	Sbi.cockpit.widgets.table.TableWidgetDesigner.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.cockpit.widgets.table.TableWidgetDesigner, Sbi.cockpit.core.WidgetDesigner, {
	tableDesigner: null

	, getDesignerState: function(running) {
		Sbi.trace("[TableWidgetDesigner.getDesignerState]: IN");

		var state = Sbi.cockpit.widgets.table.TableWidgetDesigner.superclass.getDesignerState(this);
		state.wtype = 'table';
		if(this.tableDesigner.rendered === true) {
			state.visibleselectfields = this.tableDesigner.tableDesigner.getContainedValues();
		} else {
			state.visibleselectfields =  this.visibleselectfields;
		}

		// if all measures are aggregate set category and series: category are attributes, seriesare measure with aggregation function
		var atLeastOneAggregate = false;
		var areAllMeasureAggregate = true;
		var measureNumber = 0;

		for (var i = 0; i < state.visibleselectfields.length; i++) {
			var  field = state.visibleselectfields[i];
			if(field.nature == 'measure'){
				measureNumber++;
				if(field.funct != null && field.funct != 'NaN' && field.funct != '' ){
					atLeastOneAggregate = true;
				}
				if(field.funct == null || field.funct == 'NaN' || field.funct == ''){
					areAllMeasureAggregate = false;
				}
			}
		}

		if(running != undefined && running === true){
			if(atLeastOneAggregate == true && areAllMeasureAggregate==false){
				Sbi.exception.ExceptionHandler.showWarningMessage(LN("sbi.cockpit.TableWidgetDesigner.notAllMeasureAggregated"), "Warning");
				throw new Error(LN("sbi.cockpit.TableWidgetDesigner.notAllMeasureAggregated"));
			}
		}

		var toAggregate = false;
		if(measureNumber > 0 && areAllMeasureAggregate == true){
			toAggregate = true;
			state.category = new Array();
			state.series = new Array();

			// calculate category and series
			for (var i = 0; i < state.visibleselectfields.length; i++) {
				var  field = state.visibleselectfields[i];
				if(field.nature == 'attribute' || field.nature == 'segment_attribute'){
					state.category.push(field);
				}
				else if(field.nature == 'measure'){
					state.series.push(field);
				}
			}
		}


		Sbi.trace("[TableWidgetDesigner.getDesignerState]: OUT");
		return state;
	}

	, setDesignerState: function(state) {
		Sbi.trace("[TableWidgetDesigner.setDesignerState]: IN");
		Sbi.cockpit.widgets.table.TableWidgetDesigner.superclass.setDesignerState(this, state);
		if(state.visibleselectfields!=undefined && state.visibleselectfields!=null){
			Sbi.trace("[TableWidgetDesigner.setDesignerState]: there are [" + state.visibleselectfields.length + "] fields slected");
			this.tableDesigner.tableDesigner.setValues(state.visibleselectfields);
		} else {
			Sbi.trace("[TableWidgetDesigner.setDesignerState]: no fields selected");
		}
		Sbi.trace("[TableWidgetDesigner.setDesignerState]: OUT");
	}

	/* tab validity: rules are
	 * - at least one measure or attribute is in
	 */
	, validate: function(validFields){

		var valErr = Sbi.cockpit.widgets.table.TableWidgetDesigner.superclass.validate(this, validFields);
		if(valErr!= ''){
			return varErr;
		}

		valErr = ''+this.tableDesigner.validate(validFields);

		if(valErr!= ''){
			valErr = valErr.substring(0, valErr.length - 1);
			return LN("sbi.cockpit.widgets.table.validation.invalidFields")+valErr;
		}

		var vals = this.tableDesigner.tableDesigner.getContainedValues();
		if (vals && vals.length> 0) {return;} // OK
		else {
				return LN("sbi.designertable.tableValidation.noElement");
		}
	}

	, containsAttribute: function (attributeId) {
		return this.tableDesigner.containsAttribute(attributeId);
	}

	, attributeDblClickHandler : function (thePanel, attribute, theSheet) {

	}


});
