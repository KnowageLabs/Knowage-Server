/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.core");

/**
 * @class Sbi.cockpit.core.WidgetContainerComponent
 * @extends Ext.Window
 *
 * bla bla bla bla bla ...
 */

/**
 * @cfg {Object} config
 * ...
 */
Sbi.cockpit.core.WidgetContainerComponent = function(config) {

	Sbi.trace("[WidgetContainerComponent.costructor]: IN");

	this.adjustConfigObject(config);
	this.validateConfigObject(config);

	// init properties...
	var defaultSettings = {
		//title : this.getTitle(config)
	    bodyBorder: true
	    , frame: true
	    , shadow: false
	    , plain : true
	    , constrain: true
	    , layout: {
	        type: 'vbox',
	        align : 'stretch'
	    }
	    , width:653
	    , height:332
	    , x: 100
	    , y: 100
	    , maximizable: true
	    , resizable: true
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.core.WidgetContainerComponent', defaultSettings);

	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);
	Sbi.trace("[WidgetContainerComponent.costructor]: region is equal to [" + Sbi.toSource(this.region) + "]");

	Ext.apply(c, this.region);
	delete this.region;

	// init events...
	this.addEvents('performaction', 'move', 'resize');

	this.initServices();
	this.init();
	
	//Condition to modify the GUI for the visualization mode
	//if(Sbi.config.docAuthor != '' && Sbi.user.userId != Sbi.config.docAuthor)
	//---> now we check the environment, not the doc author for visualization mode
	if(Sbi.config.environment === 'DOCBROWSER' && Sbi.config.documentMode === 'VIEW')
	{
		
		var visualizaionConfig = Ext.apply(this.adjustLayoutVisualizationMode(), config || {});
		Ext.apply(c, visualizaionConfig);
	}		

	if(this.widget) {
//		this.items = [this.widget];
		this.widget.setParentComponent(this);
		
		this.initRegionPanels();
		
		this.items = [this.northPanel, this.centerPanel];
		
	} else {
		this.html = "Please configure the widget";
	}

	this.closable = Sbi.config.docAuthor == '' || Sbi.user.userId == Sbi.config.docAuthor;


	// constructor
	Sbi.cockpit.core.WidgetContainerComponent.superclass.constructor.call(this, c);

	Sbi.trace("[WidgetContainerComponent.costructor]: OUT");
};

Ext.extend(Sbi.cockpit.core.WidgetContainerComponent, Ext.Window, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null

	/**
     * @property {Sbi.cockpit.core.WidgetContainer} parentContainer
     * The parent container
     */
	, parentContainer: null

	/**
     * @property {Sbi.cockpit.core.Widget} widget
     * The wrapped widget object
     */
	, widget: null
	
	, northPanel: null
	, centerPanel: null

    /**
     * Last cache date retrieved from datastore, it is memorized because in case of re-execution without changes data store is not reloaded
     *  and in case of widget change widget cache date information would be lost
     */
	, cacheDate: null

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	/**
	 * @method
	 *
	 * Controls that the configuration object passed in to the class constructor contains all the compulsory properties.
	 * If it is not the case an exception is thrown. Use it when there are properties necessary for the object
	 * construction for whom is not possible to find out a valid default value.
	 *
	 * @param {Object} the configuration object passed in to the class constructor
	 *
	 * @return {Object} the config object received as input
	 */
	, validateConfigObject: function(config) {
		return config;
	}

	/**
	 * @method
	 *
	 * Modify the configuration object passed in to the class constructor adding/removing properties. Use it for example to
	 * rename a property or to filter out not necessary properties.
	 *
	 * @param {Object} the configuration object passed in to the class constructor
	 *
	 * @return {Object} the modified version config object received as input
	 *
	 */
	, adjustConfigObject: function(config) {
		return config;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getTitle: function(config) {

		var title = config.widget ? config.widget.getTitle() : 'Widget';

		return title;
	}

	, refreshTitle: function() {
		var config = this.getWidgetConfiguration();

		var buildTitle = config.wgeneric.title;

		if(this.cacheDate != null){
			if(buildTitle != ''){
				this.northPanel.setVisible(true);
				this.northPanel.update(buildTitle);
				
				var widgetTitlePerc = this.getWidgetConfiguration().wgeneric.titlePerc;
				
				if(widgetTitlePerc !== undefined && widgetTitlePerc !== null && widgetTitlePerc !== ''){
					
					this.northPanel.flex = (widgetTitlePerc/100);
				} else{
					this.northPanel.flex = 0.1;
				}
				
//				buildTitle += ' - '+LN('sbi.cockpit.window.toolbar.cacheDate');				
			}
			else{
//				buildTitle += LN('sbi.cockpit.window.toolbar.cacheDate');
				this.northPanel.setVisible(false);
			}
			buildTitle = LN('sbi.cockpit.window.toolbar.cacheDate');

//			var dateObjF =  Sbi.locale.formats['timestamp'].dateFormat;
//			var dateTs =Sbi.commons.Format.date(this.cacheDate, dateObjF);
//			buildTitle += dateTs;
			buildTitle += this.cacheDate;

		}

		this.setTitle(buildTitle);
		
		this.doLayout();
	}

	/**
	 * @method
	 *
	 * @return {boolean} false if there is a wrapped widget; true otherwise
	 */
	, isEmpty: function() {
		return (this.getWidget() === null);
	}

	, isNotEmpty: function() {
		return (this.isEmpty() === false);
	}

	/**
	 * @method
	 *
	 * Replace the old embedded widget with the new one passed as argument
	 * @param {Object} the configuration object passed in to the class constructor
	 */
	, setWidget: function(widget) {
		Sbi.trace("[WidgetContainerComponent.setWidget]: IN");
		this.removeAll(true);
		Sbi.trace("[WidgetContainerComponent.setWidget]: removed component content");

		if(Sbi.isValorized(widget)) {
			// TODO check if widget is an instance of widget
			this.widget = widget;
			this.widget.setParentComponent(this);
			try {
				
				this.initRegionPanels();
				
				this.add(this.northPanel);
				this.add(this.centerPanel);

			} catch(e) {
				alert("An error occured while adding widget [" + this.widget + "] to containe: " + e);
			}
			Sbi.trace("[WidgetContainerComponent.setWidget]: widget added");
		} else {
			this.widget = widget;
		}
		this.doLayout();

		this.refreshTitle();

		Sbi.trace("[WidgetContainerComponent.setWidget]: layout refreshed");

		Sbi.trace("[WidgetContainerComponent.setWidget]: OUT");
	}

	/**
	 * @method
	 *
	 * @return {Sbi.cockpit.core.Widget} the wrapped widget. null if there is widget wrapped
	 */
	, getWidget: function() {
		var w = null;
		if(Sbi.isValorized(this.widget)) {
			w = this.widget;
		}
		return w;
	}

	, getWidgetId: function(){
		var id = null;
		if(Sbi.isValorized(this.widget)) {
			id = this.widget.getId();
		}
		return id;
	}



	, setWidgetConfiguration: function(widgetConf) {
		Sbi.trace("[WidgetContainerComponent.setWidgetConfiguration]: IN");
		var widget;

		if(this.isEmpty()) {
			widget = Sbi.cockpit.core.WidgetExtensionPointManager.getWidgetRuntime(widgetConf);
			this.setWidget(widget);
		} else {
			widget = this.getWidget();
			widget.setConfiguration(widgetConf);
		}
		Sbi.trace("[WidgetContainerComponent.setWidgetConfiguration]: widgetConf is equal to [" + Sbi.toSource(widgetConf) + "]");

		Sbi.trace("[WidgetContainerComponent.setWidgetConfiguration]: OUT");
	}

	, getWidgetConfiguration: function(widgetConf) {
		var widgetConf = null;
		if(Sbi.isValorized(this.widget)) {
			widgetConf = this.widget.getConfiguration();
		}
		return widgetConf;
	}

	, getParentContainer: function() {
		return this.parentContainer;
	}

    , setParentContainer: function(container) {
    	Sbi.trace("[WidgetContainerComponent.setParentContainer]: IN");
		this.parentContainer = container;
		Sbi.trace("[WidgetContainerComponent.setParentContainer]: OUT");
	}
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------
	, onShowWidgetConfiguration: function() {
		this.fireEvent('performaction', this, 'showConfiguration');
    }

    , onShowWidgetEditor: function() {
    	this.fireEvent('performaction', this, 'showEditor');
    }

    , onWidgetRefresh: function() {
    	if(this.isNotEmpty()) {
    		var widget = this.getWidget();
    		widget.refresh();
    	} else {
    		Ext.Msg.alert('Message', 'No widget to refresh.');
    	}
    }

    , onWidgetClone: function() {
    	this.fireEvent('performaction', this, 'cloneWidget');
    }
    
    ,adjustLayoutVisualizationMode: function()
    {     	
    	
    	var visualizationSettings = 
		{
			frame: false
		    , shadow: false
		    , resizable: false
		};
    	
    	var viewModeTabLayout = Sbi.storeManager.getLayout("viewModeLayouts");
    	
    	if(Sbi.isValorized(viewModeTabLayout)){
    		if(viewModeTabLayout.showHeader !== undefined && viewModeTabLayout !== null && viewModeTabLayout.showHeader){
    			visualizationSettings.header = {xtype: 'header', height: 1};
    			visualizationSettings.tools = 
    			[
//    			 	{ type:'maximize', hidden:true, scope:this }
    			]
    		}else{
    			visualizationSettings.header = false;
    			visualizationSettings.border = false;
    			visualizationSettings.style = 
    			{
    					borderColor: 'transparent', 
    					borderStyle:'solid', 
    					borderWidth:'0px', 
    					backgroundColor: 'transparent', 
    					borderRadius:'0px'
    			};
    			visualizationSettings.padding = 0;
    			visualizationSettings.maximizable = false;

    		}
    	} else {
			visualizationSettings.header = false;
			visualizationSettings.border = false;
			visualizationSettings.style = 
			{
					borderColor: 'transparent', 
					borderStyle:'solid', 
					borderWidth:'0px', 
					backgroundColor: 'transparent', 
					borderRadius:'0px'
			};
			visualizationSettings.padding = 0;
			visualizationSettings.maximizable = false;
    	}
    	
    	return visualizationSettings;
    }
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 *
	 * Initialize the following services exploited by this component:
	 *
	 *    - none
	 */
	, initServices: function() {

	}


	/**
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function() {
		
		this.tools =  [{
			type:'gear',
			tooltip: LN('sbi.cockpit.window.toolbar.editor'),
    		handler: this.onShowWidgetEditor,
    		scope: this,
    		hidden: Sbi.config.docAuthor != '' && Sbi.user.userId != Sbi.config.docAuthor
    	},
    	/*{
    		type:'help',
    		tooltip: LN('sbi.cockpit.window.toolbar.configuration'),
            handler: this.onShowWidgetConfiguration,
    		scope: this
        }, */
        {
        	type:'refresh',
        	tooltip: LN('sbi.cockpit.window.toolbar.refresh'),
        	handler: this.onWidgetRefresh,

    		scope: this
        }, {
        	type:'plus',
        	tooltip: LN('sbi.cockpit.window.toolbar.clone'),
        	handler: this.onWidgetClone,
        	scope:this,
        	hidden: Sbi.config.docAuthor != '' && Sbi.user.userId != Sbi.config.docAuthor
        }];
	}
	
	, initRegionPanels: function(){
		
		this.centerPanel = Ext.create('Ext.panel.Panel', {
			xtype: 'panel', 
			layout: 'fit', 
			flex: 1, 
			region:'center', 
			header:false, 
			border:false,
			items:[this.widget]
		});
		
		this.northPanel = Ext.create('Ext.panel.Panel', {
			xtype: 'panel',
			maxHeight: 65,
			flex: 0.1, 
			region:'center', 
			header:false,
			border:false
		});
		
		var widgetTitle = this.getWidgetConfiguration().wgeneric.title;
		var widgetTitlePerc = this.getWidgetConfiguration().wgeneric.titlePerc;
		
		if(widgetTitle !== undefined && widgetTitle !== null && widgetTitle !== ''){
			
			this.northPanel.hidden = false;				
		} else{
			this.northPanel.hidden = true;
		}
		
		if(widgetTitlePerc !== undefined && widgetTitlePerc !== null && widgetTitlePerc !== ''){
			
			this.northPanel.flex = (widgetTitlePerc/100);
		} else{
			this.northPanel.flex = 0.1;
		}
		
	}

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================
	/**
	 * @method
	 *
	 * Override of Ext.window.Window.maximize method to handle resizing of charts
	 */
	, maximize: function() {
		var o = this.callParent(arguments);
		var widget = this.getWidget();
		widget.maximize();
		return o;
	}

	/**
	 * @method
	 *
	 * Override of restore method to handle resizing of charts
	 */
	, restore: function() {
		var o = this.callParent(arguments);
		var widget = this.getWidget();
		widget.restore();
		return o;
	}

	/**
	 * @method
	 *
	 * Override of resize method to handle resizing of charts
	 */
	, resize: function() {
		var o = this.callParent(arguments);
		var widget = this.getWidget();
		widget.resize();
		return o;
	}
});