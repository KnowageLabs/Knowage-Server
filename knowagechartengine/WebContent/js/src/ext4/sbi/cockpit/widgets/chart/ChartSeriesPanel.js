/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

var selected_value="";

Ext.ns("Sbi.cockpit.widgets.chart");

Sbi.cockpit.widgets.chart.ChartSeriesPanel = function(config) {

	var defaultSettings = {
		title: LN('sbi.cockpit.widgets.chartseriespanel.title')
		, frame: true
		, emptyMsg: LN('sbi.cockpit.widgets.chartseriespanel.emptymsg')
	};

	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.chart && Sbi.settings.cockpit.widgets.chart.chartSeriesPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.chart.chartSeriesPanel);
	}

	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.designer && Sbi.settings.cockpit.designer.common) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.designer.common);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	this.init(c);

	c = Ext.apply(c, {
		items: [
		        this.emptyMsgPanel
		        ,this.grid
		        ]
		, layout: 'card'
		, activeItem: 0
		, tools: [
	          {
	        	  id: 'close'
  	        	, handler: this.removeAllMeasures
  	          	, scope: this
  	          	, qtip: LN('sbi.cockpit.widgets.chartseriespanel.tools.tt.removeall')
	  	      }
		]
	});

	// constructor
	Sbi.cockpit.widgets.chart.ChartSeriesPanel.superclass.constructor.call(this, c);

    this.on('render', this.initDropTarget, this);
    this.on('afterLayout', this.setActiveItem, this);

    thisPanel = this;

};

Ext.extend(Sbi.cockpit.widgets.chart.ChartSeriesPanel, Ext.Panel, {

	  emptyMsg : null
	, emptyMsgPanel : null
	, targetRow: null
	, detailsWizard: undefined
	, grid: null
	, currentRowEdit : null
	, displayColorColumn : true // to display or not the color column, default is true
	, colorColumn : null
	, validFields: null
	, defaultAggregationFunction: 'SUM'

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
	      , {name: 'sortMeasure', type: 'bool'}
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
			id: 'ChartSeriesPanelStoreId',
	        fields: ['id', 'alias', 'funct', 'iconCls', 'nature', 'seriename', 'color', 'showcomma', 'precision', 'suffix', 'valid', 'sortMeasure']
		});
	}

	, initGrid: function (c) {

		var serieNameColumn =  {
            	text: LN('sbi.cockpit.widgets.chartseriespanel.columns.seriename')
            	,dataIndex: 'seriename'
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
             };

		var fieldColumn = {
		    	text: LN('sbi.cockpit.widgets.chartseriespanel.columns.queryfield')
		    	, dataIndex: 'alias'
		    	, hideable: false
		    	, sortable: false
		        , scope: this

		};

		var aggregatorColumn = {
		    	 text: LN('sbi.qbe.selectgridpanel.headers.function')
		         , dataIndex: 'funct'
		         , editor: new Ext.form.ComboBox({
			         allowBlank: true,
			         editable: false,
			         store: this.aggregationFunctionsStore,
			         displayField: 'nome',
			         valueField: 'funzione',
			         typeAhead: true,
			         queryMode: 'local',
			         triggerAction: 'all',
			         autocomplete: 'off',
			         emptyText: LN('sbi.qbe.selectgridpanel.aggfunc.name.none'),
			         selectOnFocus: true
		         })
			     , hideable: true
			     , hidden: false
			     , width: 50
			     , sortable: false
		    };

		this.colorColumn = {
				text: LN('sbi.cockpit.widgets.chartseriespanel.columns.color')
				, width: 60
				, dataIndex: 'color'
				, editor: new Ext.form.TextField({}) // only in order to make the column editable: the editor is built
				 									 // on the grid's beforeedit event
				, renderer : function(v, metadata, record) {
					metadata.attr = ' style="background:' + v + ';"';
					return v;
		       }
			};

		var sortByThisMeasureRadioColumn = {
			header: LN('sbi.cockpit.widgets.chartseriespanel.columns.sort'),
			tooltip: LN('sbi.cockpit.widgets.chartseriespanel.columns.sort'),
			hideable: false,
			renderer: this.renderRadioBox,
			width: 30
		};

		var showCommaCheckColumn = {
				xtype: 'checkcolumn',
	    		text: LN('sbi.cockpit.widgets.chartseriespanel.columns.showcomma')
	    		, tooltip: LN('sbi.cockpit.widgets.chartseriespanel.columns.showcomma')
	    		, dataIndex: 'showcomma'
	    		, hideable: false
	    		, hidden: false
	    		, width: 30
	    		, sortable: false
	    	};

	    var precisionColumn = {
		    	text: LN('sbi.cockpit.widgets.chartseriespanel.columns.precision')
		    	, tooltip: LN('sbi.cockpit.widgets.chartseriespanel.columns.precision')
		    	, dataIndex: 'precision'
		    	, hideable: false
		    	, sortable: false
		    	, width: 50
		        , editor: new Ext.form.NumberField({
		        	value: 2
		        	, minValue: 0
		        	, maxValue: 10
		        })
		    };

	    var suffixColumn = {
		    	text: LN('sbi.cockpit.widgets.chartseriespanel.columns.suffix')
		    	, tooltip: LN('sbi.cockpit.widgets.chartseriespanel.columns.suffix')
		    	, dataIndex: 'suffix'
		    	, hideable: false
		    	, sortable: false
		    	, width: 40
		        , editor: new Ext.form.TextField({})
		    };


		this.gridColumns = [serieNameColumn, fieldColumn, aggregatorColumn];
		if (this.displayColorColumn)  {
			this.gridColumns.push(this.colorColumn);
		}
		//this.gridColumns.push(showCommaCheckColumn);
		if ((c.parent != undefined) && ((c.parent == 'barchart') || (c.parent == 'linechart')  )){
			this.gridColumns.push(sortByThisMeasureRadioColumn);
		}
		this.gridColumns.push(precisionColumn);
		this.gridColumns.push(suffixColumn);

		var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
	        clicksToEdit: 1
	    });


		this.grid = new Ext.grid.Panel({
			 store: this.store
			 , border: false
			 , enableDragDrop: true
		     , ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
			 , layout: 'fit'
			 , cls: 'chart-series-panel'
			 , viewConfig: {
				  forceFit: true
				  ,listeners : {
	                   'itemkeydown' : function(view, record, item, index, key) {
	                       if (key.getKey() == 46) {//the delete button
	                          thisPanel.removeSelectedMeasures();
	                       }
	                   }
	               }
			 }
        	 , selModel: Ext.selection.RowModel()
        	 , columns: this.gridColumns
 			 , plugins: [cellEditing]
        	 , listeners: {
 	        	beforeedit: {
 	        		fn : function (editor, e) {
 	        	    	var t = Ext.apply({}, e);
 	        	    	if (t.field === 'color'){
 	 	        			this.currentRowRecordEdited = t.rowIdx;
 	 	        			var color = this.store.getAt(this.currentRowRecordEdited).data.color;

 	 	        			var colorFieldEditor = new Ext.ux.ColorField({ value: color, msgTarget: 'qtip', fallback: true});
 	 	        			colorFieldEditor.on('colorUpdate', function(f, val) {
 	 	        				var colorExe = (f.indexOf('#')>= 0) ? f: '#'+f;
 	 	        				this.store.getAt(this.currentRowRecordEdited).set('color',colorExe);
 	 	        			}, this);
 	 	        			e.column.setEditor(colorFieldEditor);
 	        	    	}

 	        		}
 	        		, scope : this
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
	, renderRadioBox: function (val, meta, record, rowIndex, colIndex, store){
		var sortMeasure = record.data.sortMeasure;
		if (sortMeasure){
			selected_value = "radio"+rowIndex;
		}
		var myRadio = '<input '+(sortMeasure==true?'checked=checked':'')+' type= "radio" value="radio'+rowIndex+'" name="radiogroup" id="sortRadio'+rowIndex+'"  onclick="setSortMeasure('+rowIndex+',\''+store.storeId+'\');" />';
		return myRadio;
	}

	, initDropTarget: function() {
		this.removeListener('render', this.initDropTarget, this);
		this.dropTarget = new Sbi.widgets.GenericDropTarget(this, {
			ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
			, onFieldDrop: this.onFieldDrop
		});
	}

	, onFieldDrop: function(ddSource) {
		Sbi.trace("[ChartSeriesPanel.onFieldDrop]: IN");
		if (ddSource.id === "field-grid-body") {
			this.notifyDropFromQueryFieldsPanel(ddSource);
		} else {
			alert('Unknown drag source [' + ddSource.id + ']');
		}

		Sbi.trace("[ChartSeriesPanel.onFieldDrop]: OUT");

	}

	, notifyDropFromQueryFieldsPanel: function(ddSource) {
		var rows = ddSource.dragData.records;
		var i = 0;
		for (; i < rows.length; i++) {
			var aRow = rows[i];
			// if the field is an attribute show a warning
			if (aRow.data.nature === 'attribute' || aRow.data.nature === 'segment_attribute') {
				Ext.Msg.show({
					   title: LN('sbi.cockpit.widgets.chartseriespanel.cannotdrophere.title'),
					   msg: LN('sbi.cockpit.widgets.chartseriespanel.cannotdrophere.attributes'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
				return;
			}
			// if the field is a postLineCalculated show an error
			if (aRow.data.nature === 'postLineCalculated') {
				Ext.Msg.show({
					   title: LN('sbi.cockpit.widgets.chartseriespanel.cannotdrophere.title'),
					   msg: LN('sbi.cockpit.widgets.chartseriespanel.cannotdrophere.postlinecalculated'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
				});
				return;
			}
			// if the measure is missing the aggregation function, user must select it

			var measure = Ext.apply({}, aRow) ;

			if(this.defaultAggregationFunction){
				measure.data.funct = this.defaultAggregationFunction;
			}else{
				// if default aggregation function is missing, use the first occurrence of the arrayStore aggregationFunctionsStore
				measure.data.funct = this.aggregationFunctionsStore.first().get('funzione');
			}

			this.addMeasure(measure);

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
		for(var i = 0; i < this.store.getCount(); i++) {
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
				   title: LN('sbi.cockpit.widgets.chartseriespanel.cannotdrophere.title'),
				   msg: LN('sbi.cockpit.widgets.chartseriespanel.cannotdrophere.measurealreadypresent'),
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

//Pure javascript functions
function setSortMeasure(rowIdx,storeId){

	var store = Ext.getStore(storeId);

	var value = document.getElementById('sortRadio'+rowIdx).value;

	if (selected_value == value){
		//uncheck same radio button
		document.getElementById('sortRadio'+rowIdx).checked = false;
	    store.data.items[rowIdx].set('sortMeasure',false);
	    selected_value = "";
	} else {
		//check radio button (and uncheck all the rest)
	    for(var i=0;i<store.getCount();i++)	    {
	        if(store.data.items[i].data.sortMeasure==true){
	        	store.data.items[i].set('sortMeasure',false);
	    		document.getElementById('sortRadio'+i).checked = false;
	        }
	    }
	    store.data.items[rowIdx].set('sortMeasure',true);
		document.getElementById('sortRadio'+rowIdx).checked = true;
		selected_value = document.getElementById('sortRadio'+rowIdx).value;
	}
}