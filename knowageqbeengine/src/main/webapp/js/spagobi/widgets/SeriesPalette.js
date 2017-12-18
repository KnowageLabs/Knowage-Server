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

Ext.ns("Sbi.widgets");

Sbi.widgets.SeriesPalette = function(config) {

	var defaultSettings = {
		title: 'Palette'
		, frame: true
	};
		
	if (Sbi.settings && Sbi.settings && Sbi.settings.widgets && Sbi.settings.widgets.seriesPalette) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.widgets.seriesPalette);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.init(c);
	
	c = Ext.apply(c, {
		xtype: 'panel'
		, frame: true
		, border: false
		, autoScroll: true
		, layout:'fit'
        , items:[this.grid]
	});
	
	// constructor
    Sbi.widgets.SeriesPalette.superclass.constructor.call(this, c);
    
};

Ext.extend(Sbi.widgets.SeriesPalette, Ext.Window, {

	grid : null
	, colorFieldEditor : null
	, colorColumn : null
	, defaultColors : Sbi.widgets.Colors.defaultColors
	, init: function(c) {
		this.initStore(c);
		this.initColumnModel(c);
		this.initGrid(c);
	}

	, initStore: function(c) {
		this.store =  new Ext.data.ArrayStore({
	        fields: ['color']
		});
		this.setColors(this.defaultColors);
	}
	
	, initColumnModel: function(c) {
		
		this.colorColumn = new Ext.grid.Column({
			header: ''
			, width: 60
			, dataIndex: 'color'
			, editor: new Ext.form.TextField({}) // only in order to make the column editable: the editor is built 
												 // on the grid's beforeedit event 
			, renderer : function(v, metadata, record) {
				metadata.attr = ' style="background:' + v + ';"';
				return '';  
	       }
		});
	    this.cm = new Ext.grid.ColumnModel([this.colorColumn]);
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
		    , viewConfig: {
		    	forceFit: true
		    }
	        , listeners: {
				render: function(grid) { // hide the grid header
					grid.getView().el.select('.x-grid3-header').setStyle('display', 'none');
	    		}
	        	, beforeedit: {
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
			}
		});
	}
	
	, getColors: function() {
		var colors = [];
		for(i = 0; i < this.store.getCount(); i++) {
			var record = this.store.getAt(i);
			var color = record.data.color;
			colors.push(color);
		}
		return colors;
	}
	
	, setColors: function(colors) {
		var array = [];
		for (var i = 0; i < colors.length; i++) {
			array.push([colors[i]]);
		}
		this.store.loadData(array);
	}

});