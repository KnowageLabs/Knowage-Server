/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**

  * Authors
  *
  * - Antonella Giachino (antonella.giachino@eng.it)
  */

Ext.ns("Sbi.cockpit.editor.widget");

Sbi.cockpit.editor.widget.WidgetEditorFieldPalette = function(config) {

	var defaultSettings = {
		title: LN('sbi.cockpit.queryfieldspanel.title')
		, displayRefreshButton : false
		, border: true
		, bodyStyle:'padding:3px'
      	, layout: 'fit'
	};
	var settings = Sbi.getObjectSettings('Sbi.cockpit.editor.widget.WidgetEditorFieldPalette', defaultSettings);

	var c = Ext.apply(settings, config || {});

	Ext.apply(this, c);

	this.initServices();
	this.init();

	c.items = [this.grid];
	if(this.displayRefreshButton === true) {
		c.tools = [{
		    type:'refresh',
		    qtip: LN('sbi.formbuilder.queryfieldspanel.tools.refresh'),
		    handler: function(){
      			this.refreshFieldsList(null);
		    }
		    , scope: this
      	}];
	}

	// constructor
	Sbi.cockpit.editor.widget.WidgetEditorFieldPalette.superclass.constructor.call(this, c);

	this.addEvents("validateInvalidFieldsAfterLoad");
};

Ext.extend(Sbi.cockpit.editor.widget.WidgetEditorFieldPalette, Ext.Panel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
    services: null

    , grid: null

    , store: null

	 , renderTpl1: [
	                '<a id="button-{id}" class="x-btn x-unselectable x-btn-default-medium x-icon-text-left x-btn-icon-text-left x-btn-default-medium-icon-text-left" tabindex="0" unselectable="on" hidefocus="on" role="button">',
	                '<span id="{id}-btnWrap" class="{baseCls}-wrap',
	                     '<tpl if="splitCls"> {splitCls}</tpl>',
	                     '{childElCls}" unselectable="on">',
	                     '<span id="{id}-btnEl" class="{baseCls}-button">',
	                         '<span id="{id}-btnInnerEl" class="{baseCls}-inner {innerCls}',
	                             '{childElCls}" unselectable="on">',
	                             '{text}',
	                         '</span>',
	                         '<span role="img" id="{id}-btnIconEl" class="{baseCls}-icon-el {iconCls}',
	                             '{childElCls} {glyphCls}" unselectable="on" style="',
	                             '<tpl if="iconUrl">background-image:url({iconUrl});</tpl>',
	                             '<tpl if="glyph && glyphFontFamily">font-family:{glyphFontFamily};</tpl>">',
	                             '<tpl if="glyph">&#{glyph};</tpl><tpl if="iconCls || iconUrl">&#160;</tpl>',
	                         '</span>',
	                     '</span>',
	                 '</span>',
	                 // if "closable" (tab) add a close element icon
	                 '<tpl if="closable">',
	                     '<span id="{id}-closeEl" class="{baseCls}-close-btn" title="{closeText}" tabIndex="0"></span>',
	                 '</tpl>'
	                 , '</a>'

	    ]

	  	, renderTpl2: ['<table id="{id}" cellspacing="0" class="x-btn x-btn-text-icon"><tbody class="x-btn-small x-btn-icon-small-left">',
	  	                '<tr><td class="x-btn-tl"><i>&#160;</i></td><td class="x-btn-tc"></td><td class="x-btn-tr"><i>&#160;</i></td></tr>',
	  	                '<tr><td class="x-btn-ml"><i>&#160;</i></td><td class="x-btn-mc"><button type="button" class=" x-btn-text {iconCls}"></button>{text}</td><td class="x-btn-mr"><i>&#160;</i></td></tr>',
	  	                '<tr><td class="x-btn-bl"><i>&#160;</i></td><td class="x-btn-bc"></td><td class="x-btn-br"><i>&#160;</i></td></tr>',
	  	                '</tbody></table>']

	 	, templateArgs: {
	         innerCls : '',
	         splitCls : '',
	         baseCls : Ext.baseCSSPrefix + 'btn',
	         //iconUrl  : me.icon,
	         iconCls  : '',//me.iconCls,
	         //glyph: glyph,
	         glyphCls: '',
	         glyphFontFamily: Ext._glyphFontFamily,
	         text     : '&#160;'
	     }

    , displayRefreshButton: null  // if true, display the refresh button



    // =================================================================================================================
	// METHODS
	// =================================================================================================================


    , refreshFieldsList: function(datasetLabel) {
    	Sbi.trace("[WidgetEditorFieldPalette.refreshFieldsList]: IN");

    	Sbi.trace("[WidgetEditorFieldPalette.refreshFieldsList]: input parameter datasetLabel is equal to [" + datasetLabel + "]");

		if (datasetLabel) {
			this.dataset = datasetLabel;

			this.store.getProxy().url  = Sbi.config.serviceReg.getServiceUrl("loadDataSetField", {
				pathParams: {datasetLabel: datasetLabel}
			});

			Sbi.trace("[WidgetEditorFieldPalette.refreshFieldsList]: url: " + this.store.getProxy().url);
		}
		this.store.load();

		Sbi.trace("[WidgetEditorFieldPalette.refreshFieldsList]: OUT");
	}



    // -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 *
	 * Initialize the following services exploited by this component:
	 *
	 */
    , initServices: function(){
    	var baseParams = {};
    	if (this.dataset) {
    		baseParams.dataset = this.dataset;
    	}

    	this.services = this.services || new Array();
    }

	/**
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function() {
		this.initGrid();
	}

	, initStore: function() {
		Sbi.trace("[WidgetEditorFieldPalette.initStore]: IN");

		// Set up a model to use in our Store
		Ext.define('Field', {
		    extend: 'Ext.data.Model',
		    idProperty : 'alias',
		    fields: [
		        {name: 'id', type: 'string'},
		        {name: 'alias',  type: 'string'},
		        {name: 'funct',  type: 'int'},
		        {name: 'iconCls', type: 'string'},
		        {name: 'nature',  type: 'string'},
		        {name: 'values',  type: 'string'},
		        {name: 'options',  type: 'string'}
		    ]
		});

		this.store = Ext.create('Ext.data.Store', {
		    model: 'Field',
		    proxy: {
		        type: 'ajax',
		        url: Sbi.config.serviceReg.getServiceUrl("loadDataSetField", {
					pathParams: {datasetLabel: this.dataset}
				}),
		        reader: {
		            type: 'json',
		            root: 'results'
		        }
		    },
		    autoLoad: (this.dataset)?true:false
		});


		this.store.on('loadexception', function(store, options, response, e){
			Sbi.exception.ExceptionHandler.handleFailure(response, options);
		}, this);

		this.store.on('load', function(){
			Sbi.trace("[WidgetEditorFieldPalette.onLoad]: store loaded");
			this.fireEvent("validateInvalidFieldsAfterLoad", this);
		}, this);

		Sbi.trace("[WidgetEditorFieldPalette.initStore]: OUT");
	}




    , initGrid: function() {
    	var c = this.gridConfig;

    	this.initStore();

    	this.template = new Ext.XTemplate(this.renderTpl1);
        this.template.compile();

		this.grid = new Ext.grid.GridPanel(Ext.apply(c || {}, {
			id: 'field-grid',
	        store: this.store,
	        hideHeaders: true,
	        autoScroll: false,
            viewConfig: {
				plugins: {
					ptype: 'gridviewdragdrop',
		            dragText: 'Drag and drop to reorganize',
		            ddGroup: 'worksheetDesignerDDGroup',
		            enableDrop: false
		        }
			},
	        columns: [{
	        	id:'alias'
            	, header: LN('sbi.formbuilder.queryfieldspanel.fieldname')
            	, flex: 1
            	, sortable: true
            	, dataIndex: 'alias'
            	, renderer : function(value, metaData, record, rowIndex, colIndex, store) {
            		Sbi.trace("[WidgetEditorFieldPalette.renderGridRow]: IN");

            		var templateData = Ext.apply({}, {
	            		id: Ext.id()
	            		, text:  record.get("alias")
	            		, iconCls: record.get("iconCls")
	            	}, this.templateArgs);
            		var htmlFragment = this.template.apply(templateData);

            		//Sbi.trace("[WidgetEditorFieldPalette.renderGridRow]: htmlFragment ["  + htmlFragment + "]");
            		Sbi.trace("[WidgetEditorFieldPalette.renderGridRow]: OUT");
            		return htmlFragment;
		    	}
	            , scope: this
            }],
	        stripeRows: false
	    }));
    }


    // public methods



    , getFields : function () {
    	var fields = [];
    	var count = this.store.getCount();
    	for (var i = 0; i < count; i++) {
    		fields.push(this.store.getAt(i).data);
    	}
    	return fields;
    }

});