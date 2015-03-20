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

Sbi.crosstab.MeasuresContainerPanel = function(config) {
	
	var defaultSettings = {
	};
	
	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.measuresContainerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.measuresContainerPanel);
	}
	
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.common) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.common);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	this.initialData = c.initialData;		// initial grid's content
	this.crosstabConfig = c.crosstabConfig;	// initial crosstab configuration
	
	this.hasMandatoryMeasure = false;
	
	delete c.initialData;
	delete c.crosstabConfig; // deleting those properties is necessary, because otherwise default values are lost, most likely 
							 // when the super constructor is called: may be it invokes Ext.apply(this, c) and a undefined property
							 // overrides a non-undefined property with the same name
	
	// default value for crosstabConfig
	if (this.crosstabConfig === undefined) {
		this.crosstabConfig = {measureson: "columns"};
	}
	
	Ext.apply(this, c);
	
	this.init(c);
			
	Ext.apply(c, {
        store: this.store
        , cm: this.cm
        , enableDragDrop: true
        , ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
	    , layout: 'fit'
	    , viewConfig: {
	    	forceFit: true
	    }
		, tools: [
	          {
	        	  id: 'help'
	        	, handler: this.openDetailsWizard
	          	, scope: this
	          	, qtip: LN('sbi.crosstab.measurescontainerpanel.tools.tt.showdetailswizard')
	          }
	          , {
	        	  id: 'close'
  	        	, handler: this.removeAllMeasures
  	          	, scope: this
  	          	, qtip: LN('sbi.crosstab.measurescontainerpanel.tools.tt.removeall')
	  	      }
		]
        , listeners: {
			render: function(grid) { // hide the grid header
				grid.getView().el.select('.x-grid3-header').setStyle('display', 'none');
    		}
        	, keydown: function(e) { 
        		if (e.keyCode === 46) {
        			this.removeSelectedMeasures();
      	      	}      
      	    }
        	, mouseover: function(e, t) {
        		this.targetRow = t; // for Drag&Drop
        	}
        	, mouseout: function(e, t) {
        		this.targetRow = undefined;
        	}
        	, rowdblclick: function(theGrid, rowIndex, e) {
        		var theRow = this.store.getAt(rowIndex);
				var aWindow = new Sbi.crosstab.ChooseAggregationFunctionWindow({
					behindMeasure: Ext.apply({}, theRow.data) // creates a clone
        	  	});
        	  	aWindow.show();
        	  	aWindow.on('apply', function(modifiedMeasure, theWindow) {this.modifyMeasure(theRow, new this.Record(modifiedMeasure));}, this);
        	}
		}
        , scope: this
        , type: 'measuresContainerPanel'
	});	
	
	// constructor
    Sbi.crosstab.MeasuresContainerPanel.superclass.constructor.call(this, c);
    
    this.on('render', this.initDropTarget, this);
    
};

Ext.extend(Sbi.crosstab.MeasuresContainerPanel, Ext.grid.GridPanel, {
	
	initialData: undefined
	, isStatic: false
	, targetRow: null
	, detailsWizard: undefined
	, crosstabConfig: undefined
	, validFields: null
	, Record: Ext.data.Record.create([
	      {name: 'id', type: 'string'}
	      , {name: 'alias', type: 'string'}
	      , {name: 'funct', type: 'string'}
	      , {name: 'iconCls', type: 'string'}
	      , {name: 'nature', type: 'string'}
	])

	, init: function(c) {
		this.initStore(c);
		this.initColumnModel(c);
	}
	
	, initStore: function(c) {
		this.store =  new Ext.data.SimpleStore({
	        fields: ['id', 'alias', 'funct', 'iconCls', 'nature', 'valid']
		});
		// if there are initialData, load them into the store
		if (this.initialData !== undefined) {
			for (i = 0; i < this.initialData.length; i++) {
				var record = new this.Record(this.initialData[i]);
	  			this.addMeasure(record);
			}
		}
	}
	
	, initColumnModel: function(c) {
        this.template = new Ext.Template( // see Ext.Button.buttonTemplate and Button's onRender method
        		// margin auto in order to have button center alignment
                '<table style="margin-left: auto; margin-right: auto;" id="{4}" cellspacing="0" class="x-btn {3} {6}"><tbody class="{1}">',
                '<tr><td class="x-btn-tl"><i>&#160;</i></td><td class="x-btn-tc"></td><td class="x-btn-tr"><i>&#160;</i></td></tr>',
                '<tr><td class="x-btn-ml"><i>&#160;</i></td><td class="x-btn-mc"><button type="{0}" class=" x-btn-text {5}"></button>{7} ({8})</td><td class="x-btn-mr"><i>&#160;</i></td></tr>',
                '<tr><td class="x-btn-bl"><i>&#160;</i></td><td class="x-btn-bc"></td><td class="x-btn-br"><i>&#160;</i></td></tr>',
                '</tbody></table>');
        
        this.template.compile();
		
	    var fieldColumn = new Ext.grid.Column({
	    	header:  ''
	    	, dataIndex: 'alias'
	    	, hideable: false
	    	, hidden: false	
	    	, sortable: false
	   	    , renderer : function(value, metaData, record, rowIndex, colIndex, store){

				if(record.data.valid != undefined && !record.data.valid){
	   	    		toReturn = this.template.apply(
		        			['button', 'x-btn-small x-btn-icon-small-left', '', 'x-btn-text-icon x-btn-invalid', Ext.id(), record.data.iconCls, record.data.iconCls+'_text', record.data.alias, record.data.funct]
		   	    		);		   	    		
				}
				else{
	   	    		toReturn = this.template.apply(
		        			['button', 'x-btn-small x-btn-icon-small-left', '', 'x-btn-text-icon', Ext.id(), record.data.iconCls, record.data.iconCls+'_text', record.data.alias, record.data.funct]		   	    		
		   	    		);  	    							
				}

   	    	return toReturn;
	   	    	
	   	    	
	    	}
	        , scope: this
	    });
	    this.cm = new Ext.grid.ColumnModel([fieldColumn]);
	}
	
	, initDropTarget: function() {
		this.removeListener('render', this.initDropTarget, this);
		var dropTarget = new Sbi.widgets.GenericDropTarget(this, {
			ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
			, onFieldDrop: this.onFieldDrop
		});
	}

	, onFieldDrop: function(ddSource) {
		
		if (ddSource.grid){
			var store = ddSource.grid.getStore();
			var index = store.find("nature","mandatory_measure");
			if(index == -1 )this.hasMandatoryMeasure = false;
			else this.hasMandatoryMeasure = true;
		}
		if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'queryFieldsPanel') {
			// dragging from QueryFieldsPanel
			this.notifyDropFromQueryFieldsPanel(ddSource);
		} else if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'measuresContainerPanel') {
			// dragging from MeasuresContainerPanel
			this.notifyDropFromMeasuresContainerPanel(ddSource);
		} else if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'attributesContainerPanel') {
			Ext.Msg.show({
				   title: LN('sbi.crosstab.measurescontainerpanel.cannotdrophere.title'),
				   msg: LN('sbi.crosstab.measurescontainerpanel.cannotdrophere.attributes'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		}
		
	}
	
	, notifyDropFromQueryFieldsPanel: function(ddSource) {
		var rows = ddSource.dragData.selections;
		for (var i = 0; i < rows.length; i++) {
			var aRow = rows[i];
			// if the field is an attribute show a warning
			if (aRow.data.nature === 'attribute' || aRow.data.nature === 'segment_attribute') {
				Ext.Msg.show({
					   title: LN('sbi.crosstab.measurescontainerpanel.cannotdrophere.title'),
					   msg: LN('sbi.crosstab.measurescontainerpanel.cannotdrophere.attributes'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
				return;
			}
			// if the field is a postLineCalculated show an error
			if (aRow.data.nature === 'postLineCalculated') {
				Ext.Msg.show({
					   title: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.title'),
					   msg: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.postlinecalculated'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
				});
				return;
			}
			if(this.defaultAggregationFunction){
				var measure = Ext.apply({}, aRow.data) ;
				measure.funct = this.defaultAggregationFunction;
				this.addMeasure(new this.Record(measure));
			}else{
				// if the measure is missing the aggregation function, user must select it
				if (aRow.data.funct === null || aRow.data.funct === '' || aRow.data.funct === 'NONE') {
					var aWindow = new Sbi.crosstab.ChooseAggregationFunctionWindow({
						behindMeasure: Ext.apply({}, aRow.data) // creates a clone
					});
					aWindow.show();
					aWindow.on('apply', function(modifiedMeasure, theWindow) {this.addMeasure(new this.Record(modifiedMeasure));}, this);
				} else {
					this.addMeasure(aRow);
				}
			}
			// register if there is a mandatory measure among set
			//this.hasMandatoryMeasure = aRow.data.mandatory_measure;
		}
	}
	
	, notifyDropFromMeasuresContainerPanel: function(ddSource) {
		// DD on the same MeasuresContainerPanel --> re-order the fields
		var rows = ddSource.dragData.selections;
		if (rows.length > 1) {
			Ext.Msg.show({
				   title:'Drop not allowed',
				   msg: 'You can move only one field at a time',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		} else {
			var row = rows[0];
			var rowIndex; // the row index on which the field has been dropped on
			if(this.targetRow) {
				rowIndex = this.getView().findRowIndex( this.targetRow );
			}
			if (rowIndex == undefined || rowIndex === false) {
				rowIndex = undefined;
			}
	           
         	var rowData = this.store.getById(row.id);
        	this.store.remove(this.store.getById(row.id));
            if (rowIndex != undefined) {
            	this.store.insert(rowIndex, rowData);
            } else {
            	this.store.add(rowData);
            }
	         
	         this.getView().refresh();
		}
	}
	
	, getCrosstabConfig: function() {
		return this.crosstabConfig;
	}
	
	, setCrosstabConfig: function(crosstabConfig) {
		this.crosstabConfig = crosstabConfig;
	}
	
	, getContainedMeasures: function () {
		var measures = [];
		for(i = 0; i < this.store.getCount(); i++) {
			var record = this.store.getAt(i);
			measures.push(record.data);
		}
		return measures;
	}
	
	, setMeasures: function (measures) {
		this.removeAllMeasures();
		for (var i = 0; i < measures.length; i++) {
  			var measure = measures[i];
  			var record = new this.Record(measure);
  			this.store.add(record); 
  		}
	}
	
	, removeSelectedMeasures: function() {
        var sm = this.getSelectionModel();
        var rows = sm.getSelections();
        this.store.remove( rows );
	}
	
	, openDetailsWizard: function(event, toolEl, panel) {
	  	if (this.detailsWizard === undefined) {
	  		this.detailsWizard = new Sbi.crosstab.CrosstabDetailsWizard({
	  			isStatic: this.isStatic
	  		});
	  		this.detailsWizard.on('apply', function(values, theWizard) {
	  			this.crosstabConfig = values;
	  		}, this);
	  	}
	  	this.detailsWizard.show();
	  	this.detailsWizard.setFormState(this.crosstabConfig);
  	}
	
	, addMeasure: function(record) {
		// if the measure is already present, does not insert it 
		if (this.containsMeasure(record)) {
			Ext.Msg.show({
				   title: LN('sbi.crosstab.measurescontainerpanel.cannotdrophere.title'),
				   msg: LN('sbi.crosstab.measurescontainerpanel.cannotdrophere.measurealreadypresent'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		} else {
			this.store.add(record);
		}
	}
	
	, containsMeasure: function(record) {
		if (this.store.findBy(function(aRecord) {
            	return aRecord.get("alias") === record.get("alias") && aRecord.get("funct") === record.get("funct");
        	}) === -1) {
			return false;
		} else {
			return true;
		}
	}
	
	, removeAllMeasures: function() {
		this.store.removeAll(false);
	}
	
	, modifyMeasure: function(recordToBeModified, newRecordsValues) {
		recordToBeModified.set("funct", newRecordsValues.get("funct")); // only the aggregation function must be modified
	}
	, validate: function (validFields) {

		this.validFields = validFields;
		var invalidFields = this.modifyStore(validFields);
		this.store.fireEvent("datachanged", this, null); 
		return invalidFields;
	}
	, modifyStore: function (validFields) {
		var invalidFields = '';

		var num = this.store.getCount();
		for(var i = 0; i < num; i++) {
			var record = this.store.getAt(i);
			var isValid = this.validateRecord(record,validFields);
			record.data.valid = isValid;
			if(isValid == false){
				invalidFields+=''+record.data.alias+',';
			}
		}
		return invalidFields;
	}
	, validateRecord: function (record, validFields) {
		var isValid = false;
		var i = 0;
		for(; i<validFields.length && isValid == false; i++){
			if(validFields[i].id == record.data.id){
			isValid = true;	
			}
		}
		return isValid;
	}

});