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

Ext.define('Sbi.cockpit.widgets.chart.SeriesPalette', {
	extend: 'Ext.Window'
	, layout:'fit'

	, config:{
		title: 'Palette'
	  , width: 300
	  , height: 250
	  , closeAction: 'hide'
	 // , frame: true
	  //, xtype: 'panel'
	  //, border: false
	  //, autoScroll: true
	}

	, grid : null
	, colorFieldEditor : null
	, colorColumn : null
	, defaultColors : Sbi.widgets.Colors.defaultColors


	, constructor : function(config) {
		Sbi.trace("[SeriesPalette.constructor]: IN");
		this.initConfig(config);
		this.initStore();
		this.init(config);
		this.callParent(arguments);
		Sbi.trace("[SeriesPalette.constructor]: OUT");
	}

	, initComponent: function() {

		 Ext.apply(this, {
	            items: [this.grid]
	     });

        this.callParent();
    }

	, init: function(c) {
		this.initStore(c);
		this.initGrid(c);
	}

	, initStore: function(c) {
		this.store =  new Ext.data.ArrayStore({
	        fields: ['color']
		});
		this.setColors(this.defaultColors);
	}


	, initGrid: function (c) {
		var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
	        clicksToEdit: 1
	    });
		this.grid = new Ext.grid.Panel({
	        store: this.store
	        , border: false
	        , columns: [{
	               header: ''
				, width: '100%'
				, dataIndex: 'color'
				, renderer : function(v, metadata, record) {
					metadata.attr = ' style="background:' + v + ';"';
					return '';
		          }
	           , getEditor: function(record){
		            if( Sbi.isValorized(record) ){
		            	var rowIndex = record.store.indexOf(record);
		            	var editorField = new Ext.ux.ColorField({ value: record.data.color, msgTarget: 'qtip', fallback: true});
//		            	var editorField = new Ext.picker.Color({ value: record.data.color, msgTarget: 'qtip', fallback: true});
		            	editorField.on('change', function(f, val) {
		            		record.store.getAt(rowIndex).set('color', '#'+val);
	        			}, this);

		                return Ext.create('Ext.grid.CellEditor', {field: editorField});
		            } else return false;
		        }
	        }]
			, flex: 1
			, selModel: new Ext.selection.RowModel({})
			, plugins: [cellEditing]
	        , enableDragDrop: true
	        , ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
		    , viewConfig: {
		    	forceFit: true
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