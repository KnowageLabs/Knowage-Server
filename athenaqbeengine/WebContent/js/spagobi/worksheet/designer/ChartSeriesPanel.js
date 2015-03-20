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

Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.ChartSeriesPanel = function(config) {

	var defaultSettings = {
		title: LN('sbi.worksheet.designer.chartseriespanel.title')
		, frame: true
		, emptyMsg: LN('sbi.worksheet.designer.chartseriespanel.emptymsg')
	};
		
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.chartSeriesPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.chartSeriesPanel);
	}
	
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.common) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.common);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.init(c);
	
	c = Ext.apply(c, {
		items: [this.emptyMsgPanel, this.grid]
		, layout: 'card'
		, activeItem: 0
		, tools: [
	          {
	        	  id: 'close'
  	        	, handler: this.removeAllMeasures
  	          	, scope: this
  	          	, qtip: LN('sbi.worksheet.designer.chartseriespanel.tools.tt.removeall')
	  	      }
		]
	});
	
	// constructor
    Sbi.worksheet.designer.ChartSeriesPanel.superclass.constructor.call(this, c);
    
    this.on('render', this.initDropTarget, this);
    this.on('afterLayout', this.setActiveItem, this);
    
};

Ext.extend(Sbi.worksheet.designer.ChartSeriesPanel, Ext.Panel, {
	
	emptyMsg : null
	, emptyMsgPanel : null
	, targetRow: null
	, detailsWizard: undefined
	, grid: null
	, currentRowEdit : null
	, displayColorColumn : true // to display or not the color column, default is true
	, colorColumn : null
	, validFields: null
	
	// static members
	, Record: Ext.data.Record.create([
	      {name: 'id', type: 'string'}
	      , {name: 'alias', type: 'string'}
	      , {name: 'funct', type: 'string'}
	      , {name: 'iconCls', type: 'string'}
	      , {name: 'nature', type: 'string'}
	      , {name: 'seriename', type: 'string'}
	      , {name: 'color', type: 'string'}
	      , {name: 'showcomma', type: 'bool'}
	      , {name: 'precision', type: 'int'}
	      , {name: 'suffix', type: 'string'}
	])
	
	, aggregationFunctionsStore:  new Ext.data.ArrayStore({
		 fields: ['funzione', 'nome', 'descrizione'],
	     data : [
	        ['SUM', LN('sbi.qbe.selectgridpanel.aggfunc.name.sum'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.sum')],
	        ['AVG', LN('sbi.qbe.selectgridpanel.aggfunc.name.avg'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.avg')],
	        ['MAX', LN('sbi.qbe.selectgridpanel.aggfunc.name.max'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.max')],
	        ['MIN', LN('sbi.qbe.selectgridpanel.aggfunc.name.min'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.min')],
	        ['COUNT', LN('sbi.qbe.selectgridpanel.aggfunc.name.count'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.count')],
	        ['COUNT_DISTINCT', LN('sbi.qbe.selectgridpanel.aggfunc.name.countdistinct'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.countdistinct')]
	     ] 
	 })
	
	
	, init: function(c) {
		this.initEmptyMsgPanel();
		this.initStore(c);
		this.initColumnModel(c);
		this.initGrid(c);
	}
	
	, initEmptyMsgPanel: function() {
		this.emptyMsgPanel = new Ext.Panel({
			html: this.emptyMsg
			, height: 40
		});
	}

	, initStore: function(c) {
		this.store =  new Ext.data.ArrayStore({
	        fields: ['id', 'alias', 'funct', 'iconCls', 'nature', 'seriename', 'color', 'showcomma', 'precision', 'suffix', 'valid']
		});
	}
	
	, initColumnModel: function(c) {
		
	    var serieNameColumn = new Ext.grid.Column({
	    	header: LN('sbi.worksheet.designer.chartseriespanel.columns.seriename')
	    	, dataIndex: 'seriename'
	    	, hideable: false
	    	, sortable: false
	    	, editor: new Ext.form.TextField({})
		, renderer : function(v, metadata, record) {
		  	
			if(record.data.valid != undefined && !record.data.valid){
				metadata.attr = ' style="color:#ff0000; text-decoration:line-through;';	   	    		
				
			}
			else{
				metadata.attr = ' style="background:' + v + ';"';	   	    							
			}
			
			return v; 
		}
	    });
		
	    var fieldColumn = new Ext.grid.Column({
	    	header: LN('sbi.worksheet.designer.chartseriespanel.columns.queryfield')
	    	, dataIndex: 'alias'
	    	, hideable: false
	    	, sortable: false
	        , scope: this

	    });
	    
	    var aggregatorColumn = new Ext.grid.Column({
	    	 header: LN('sbi.qbe.selectgridpanel.headers.function')
	         , dataIndex: 'funct'
	         , editor: new Ext.form.ComboBox({
		         allowBlank: true,
		         editable: false,
		         store: this.aggregationFunctionsStore,
		         displayField: 'nome',
		         valueField: 'funzione',
		         typeAhead: true,
		         mode: 'local',
		         triggerAction: 'all',
		         autocomplete: 'off',
		         emptyText: LN('sbi.qbe.selectgridpanel.aggfunc.editor.emptymsg'),
		         selectOnFocus: true
	         })
		     , hideable: true
		     , hidden: false
		     , width: 50
		     , sortable: false
	    });
		
		this.colorColumn = new Ext.grid.Column({
			header: LN('sbi.worksheet.designer.chartseriespanel.columns.color')
			, width: 60
			, dataIndex: 'color'
			, editor: new Ext.form.TextField({}) // only in order to make the column editable: the editor is built 
			 									 // on the grid's beforeedit event 
			, renderer : function(v, metadata, record) {
				metadata.attr = ' style="background:' + v + ';"';	   	
				return v;  
	       }
		});
		
	    var showCommaCheckColumn = new Ext.grid.CheckColumn({
    		header: LN('sbi.worksheet.designer.chartseriespanel.columns.showcomma')
    		, tooltip: LN('sbi.worksheet.designer.chartseriespanel.columns.showcomma')
    		, dataIndex: 'showcomma'
    		, hideable: false
    		, hidden: false	
    		, width: 30
    		, sortable: false
    	});
	    
	    var precisionColumn = new Ext.grid.Column({
	    	header: LN('sbi.worksheet.designer.chartseriespanel.columns.precision')
	    	, tooltip: LN('sbi.worksheet.designer.chartseriespanel.columns.precision')
	    	, dataIndex: 'precision'
	    	, hideable: false
	    	, sortable: false
	    	, width: 30
	        , editor: new Ext.form.NumberField({
	        	value: 2
	        	, minValue: 0
	        	, maxValue: 10
	        })
	    });
		
	    var suffixColumn = new Ext.grid.Column({
	    	header: LN('sbi.worksheet.designer.chartseriespanel.columns.suffix')
	    	, tooltip: LN('sbi.worksheet.designer.chartseriespanel.columns.suffix')
	    	, dataIndex: 'suffix'
	    	, hideable: false
	    	, sortable: false
	    	, width: 30
	        , editor: new Ext.form.TextField({})
	    });
	    
		var columns = [serieNameColumn, fieldColumn, aggregatorColumn];
		if (this.displayColorColumn)  {
			columns.push(this.colorColumn);
		}
		columns.push(showCommaCheckColumn);
		columns.push(precisionColumn);
		columns.push(suffixColumn);
	    
		this.plgins = [showCommaCheckColumn];
		
	    this.cm = new Ext.grid.ColumnModel(columns);
	}
	
	, initGrid: function (c) {
		this.grid = new Ext.grid.EditorGridPanel({
	        store: this.store
	        , border: false
	        , cm: this.cm
	        , sm: new Ext.grid.RowSelectionModel()
	        , enableDragDrop: true
	        , ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
		    , layout: 'fit'
		    , cls: 'chart-series-panel'
		    , viewConfig: {
		    	forceFit: true
		    }
			, plugins: this.plgins
	        , listeners: {
	        	beforeedit: {
	        		fn : function (e) {
	        	    	var t = Ext.apply({}, e);
	        			this.currentRowRecordEdited = t.row;
	        			var color = this.store.getAt(this.currentRowRecordEdited).data.color;
	        			var colorFieldEditor = new Ext.ux.ColorField({ value: color, msgTarget: 'qtip', fallback: true});
	        			colorFieldEditor.on('select', function(f, val) {
	        				this.store.getAt(this.currentRowRecordEdited).set('color', val);
	        			}, this);
	        			this.colorColumn.setEditor(colorFieldEditor);
	        		}
	        		, scope : this
	        	}
	        	, keydown: {
	        		fn: function(e) {
		        		if (e.keyCode === 46) {
		        			this.removeSelectedMeasures();
		      	      	}      
		      	    }
	        		, scope: this
	        	}
	        	, mouseover: {
	        		fn: function(e, t) {
		        		this.targetRow = t; // for Drag&Drop
			        }
	        		, scope: this
		        }
	        	, mouseout: {
	        		fn: function(e, t) {
	        			this.targetRow = undefined;
			        }
        			, scope: this
	        	}
	        	, refresh: {
	          		fn: function(e, t) {
	          			var gridView = this.grid.getView();
	          		
	          		}
        			, scope: this
	        	}
			}
	        , type: 'measuresContainerPanel'
		});
	}
	
	, initDropTarget: function() {
		this.removeListener('render', this.initDropTarget, this);
		var dropTarget = new Sbi.widgets.GenericDropTarget(this, {
			ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
			, onFieldDrop: this.onFieldDrop
		});
	}

	, onFieldDrop: function(ddSource) {
		
		if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'queryFieldsPanel') {
			// dragging from QueryFieldsPanel
			this.notifyDropFromQueryFieldsPanel(ddSource);
		} else if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'measuresContainerPanel') {
			// dragging from MeasuresContainerPanel
			this.notifyDropFromMeasuresContainerPanel(ddSource);
		} else if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'attributesContainerPanel') {
			Ext.Msg.show({
				   title: LN('sbi.worksheet.designer.chartseriespanel.cannotdrophere.title'),
				   msg: LN('sbi.worksheet.designer.chartseriespanel.cannotdrophere.attributes'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		}
		
	}
	
	, notifyDropFromQueryFieldsPanel: function(ddSource) {
		var rows = ddSource.dragData.selections;
		var i = 0;
		for (; i < rows.length; i++) {
			var aRow = rows[i];
			// if the field is an attribute show a warning
			if (aRow.data.nature === 'attribute' || aRow.data.nature === 'segment_attribute') {
				Ext.Msg.show({
					   title: LN('sbi.worksheet.designer.chartseriespanel.cannotdrophere.title'),
					   msg: LN('sbi.worksheet.designer.chartseriespanel.cannotdrophere.attributes'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
				return;
			}
			// if the field is a postLineCalculated show an error
			if (aRow.data.nature === 'postLineCalculated') {
				Ext.Msg.show({
					   title: LN('sbi.worksheet.designer.chartseriespanel.cannotdrophere.title'),
					   msg: LN('sbi.worksheet.designer.chartseriespanel.cannotdrophere.postlinecalculated'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
				});
				return;
			}
			// if the measure is missing the aggregation function, user must select it
			
			if(this.defaultAggregationFunction){
					var measure = Ext.apply({}, aRow.data) ;
					measure.funct = this.defaultAggregationFunction;
					this.addMeasure(new this.Record(measure));
			}else{
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
				rowIndex = this.grid.getView().findRowIndex( this.targetRow );
			}
			if (rowIndex === undefined || rowIndex === false) {
				rowIndex = undefined;
			}
	           
         	var rowData = this.store.getById(row.id);
        	this.store.remove(this.store.getById(row.id));
            if (rowIndex !== undefined) {
            	this.store.insert(rowIndex, rowData);
            } else {
            	this.store.add(rowData);
            }
	         
	         this.grid.getView().refresh();
		}
	}

	, getContainedMeasures: function () {
		var measures = [];
		for(i = 0; i < this.store.getCount(); i++) {
			var record = this.store.getAt(i);
			measures.push(record.data);
		}
		return measures;
	}
	
	, removeSelectedMeasures: function() {
        var sm = this.grid.getSelectionModel();
        var rows = sm.getSelections();
        this.store.remove( rows );
        if (this.store.getCount() === 0) {
        	this.getLayout().setActiveItem( 0 );
        }
	}
	
	, addMeasure: function(record) {
		var data = Ext.apply({}, record.data, {
			seriename: record.data.alias
			, color: Sbi.widgets.Colors.defaultColors[this.store.getCount()]
			, showcomma: true
			, precision: 2
			, suffix: '' 
		});
		var theRecord = new this.Record(data);
		// if the measure is already present, does not insert it 
		if (this.containsMeasure(theRecord)) {
			Ext.Msg.show({
				   title: LN('sbi.worksheet.designer.chartseriespanel.cannotdrophere.title'),
				   msg: LN('sbi.worksheet.designer.chartseriespanel.cannotdrophere.measurealreadypresent'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		} else {
			this.store.add(theRecord);
			this.getLayout().setActiveItem( 1 );
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
		if (this.rendered) {
			this.getLayout().setActiveItem( 0 );
		}
	}

	, setMeasures: function(measures) {
		this.removeAllMeasures();
		if (measures !== undefined && measures !== null && measures.length > 0) {
			var i = 0;
			for (; i < measures.length; i++) {
	  			var measure = measures[i];
	  			var record = new this.Record(measure);
	  			this.store.add(record); 
	  		}
			if (this.rendered) {
				this.getLayout().setActiveItem( 1 );
			}
		}
	}
	
	, setActiveItem: function() {
		this.un('afterLayout', this.setActiveItem, this);
    	if (this.store.getCount() > 0) {
    		this.getLayout().setActiveItem( 1 );
    	} else {
    		this.getLayout().setActiveItem( 0 );
    	}
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