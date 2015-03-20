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
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.DesignToolsPallettePanel = function(config) { 

	var defaultSettings = {
			border: false
	};

	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.designToolsPallettePanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.designToolsPallettePanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	c = this.initPanel();
	Sbi.worksheet.designer.DesignToolsPallettePanel.superclass.constructor.call(this, c);	 		

};

Ext.extend(Sbi.worksheet.designer.DesignToolsPallettePanel, Ext.Panel, {
	
	initPanel:function(){

		var store = new Ext.data.ArrayStore({
			fields: ['name', 'url'],
			data   : this.getAvailablePallettes()
		});

		this.tpl = new Ext.Template(
				'<tpl for=".">',

				'<div  style="float: left; clear: left; padding-bottom: 10px;">',
					'<div style="float: left;"><img src="{0}" title="{1}" width="40"></div>',
					'<div style="float: left; padding-top:10px; padding-left:10px;">{1}</div>',
				'</div>',
	
				'</tpl>'
		);
		this.tpl.compile();
	    var fieldColumn = new Ext.grid.Column({
	    	width: 300
	    	, dataIndex: 'name'
	    	, hideable: false
	    	, hidden: false	
	    	, sortable: false
	   	    , renderer : function(value, metaData, record, rowIndex, colIndex, store){
	        	return this.tpl.apply(	
	        			[record.json.url, record.json.name]	);
	    	}
	        , scope: this
	    });
	    this.cm = new Ext.grid.ColumnModel([fieldColumn]);

		var conf ={
				title: 'Palette',	
				autoScroll : true,
				border: false,
				items: [
				        new Ext.Panel({
				        	height:342,
				        	border: false,
				        	style: 'padding-top: 0px; padding-left: 0px',
				        	items:[
				        	       new Ext.grid.GridPanel({
				        	    	   ddGroup: 'paleteDDGroup',
				        	    	   type: 'palette',
				        	    	   header: false,
				        	    	   hideHeaders : true,
				        	    	   enableDragDrop: true,
				        	    	   cm:this.cm,
				        	    	   store: store,
				        	    	   autoHeight: true
				        	       })]
				        })]
		};
	    
		return conf;

	},
	
	
	getAvailablePallettes:function(){
		var pallette = [];
		pallette.push({name: 'Bar Chart', url:'../img/worksheet/palette_bar_chart.png'});
		pallette.push({name: 'Pie Chart', url:'../img/worksheet/palette_pie_chart.png'});
		pallette.push({name: 'Line Chart', url:'../img/worksheet/palette_line_chart.png'});
		pallette.push({name: 'Table', url:'../img/worksheet/palette_table.png'});
		pallette.push({name: 'Pivot Table', url:'../img/worksheet/palette_crosstab.png'});	
		pallette.push({name: 'Static Pivot Table', url:'../img/worksheet/palette_crosstab.png'});
		return pallette;
	}

	
});