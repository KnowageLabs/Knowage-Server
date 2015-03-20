/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.editor.widget");

Sbi.cockpit.editor.widget.WidgetEditorDesignerPalette = function(config) {

	var defaultSettings = {
		border: false,
		layout: "fit"
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.editor.widget.WidgetEditorDesignerPalette', defaultSettings);

	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	c = this.initPanel();
	Sbi.cockpit.editor.widget.WidgetEditorDesignerPalette.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.cockpit.editor.widget.WidgetEditorDesignerPalette, Ext.Panel, {

	initPanel:function(){

//		var store = new Ext.data.ArrayStore({
//			fields: ['type', 'name', 'description', 'icon'],
//			data   : this.getAvailablePallettes()
//		});

		Ext.create('Ext.data.Store', {
		    storeId:'widgetDesignerStore',
		    fields: ['type', 'name', 'description', 'icon'],
		    data:{'items': this.getAvailablePallettes()},
		    proxy: {
		        type: 'memory',
		        reader: {
		            type: 'json',
		            root: 'items'
		        }
		    }
		});

		this.tpl = new Ext.Template(
			'<tpl for=".">',

			'<div  style="float: left; clear: left; padding-bottom: 10px;">',
				'<div style="float: left;"><img src="{3}" title="{1}" width="40"></div>',
				'<div style="float: left; padding-top:10px; padding-left:10px;">{1}</div>',
			'</div>',

			'</tpl>'
		);
		this.tpl.compile();

		var gridPanel = Ext.create('Ext.grid.Panel', {
			id: 'designer-grid', // used to detect drag source in WidgetEditorMainPanel drop area
		    store: Ext.data.StoreManager.lookup('widgetDesignerStore'),
		    columns: [
		        {
		        	width: 255
		        	, dataIndex: 'name'
		        	, hideable: false
		        	, hidden: false
		        	, sortable: false
		        	, renderer : function(value, metaData, record, rowIndex, colIndex, store){
		        		return this.tpl.apply(
		        				[record.get('type'), record.get('name'), record.get('description'), record.get('icon')]
		        		);
		        	}
		        	, scope: this
		        }
		    ],
			viewConfig: {
				plugins: {
					ptype: 'gridviewdragdrop',
		            dragText: 'Drag and drop to reorganize',
		            ddGroup : 'paleteDDGroup',
		            enableDrop: false
		        }
			},
			header : false,
			hideHeaders : true,
			autoHeight : true
		});


		var conf = {
			title : 'Visualization',
			autoScroll : true,
			border : false,
			items : [ new Ext.Panel({
					//height : 342,
					layout: "fit",
					border : false,
					style : 'padding-top: 0px; padding-left: 0px',
					items : [gridPanel]
				}) ]
		};

		return conf;

	},


	getAvailablePallettes:function(){
		Sbi.trace("[WidgetEditorDesignerPalette.getAvailablePallettes]: IN");

		var pallette = new Array();

		Sbi.cockpit.core.WidgetExtensionPointManager.forEachWidget(function(wtype, wdescriptor) {
			if(wtype === 'selection') return;
			pallette.push({
				type: wtype
				, name: wdescriptor.name
				, description:wdescriptor.description
				, icon: Sbi.config.serviceRegistry.getResourceUrl(wdescriptor.icon)
			});
		}, this);

		Sbi.trace("[WidgetEditorDesignerPalette.getAvailablePallettes]: IN");

		return pallette;
	}


});