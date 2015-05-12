/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.core");

/**
 * @class Sbi.cockpit.core.WidgetContainer
 * @extends Ext.util.Observable
 *
 * It manage the widget layout. At the moment it support only white board layout.
 * In the future it should be extended in order to a support different layouts (ex.
 * table, portal, ecc ...). The layout should be managed as an extension point and new
 * layouts should be plugged at any time.
 */

/**
 * @cfg {Object} config The configuration object passed to the constructor
 */
Sbi.cockpit.core.WidgetContainer = function(config) {

	this.validateConfigObject(config);
	this.adjustConfigObject(config);


	// init properties...
	var defaultSettings = {
		// set default values here
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.core', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	this.widgetManager = this.widgetManager ||  new Sbi.cockpit.core.WidgetManager();

	this.init();


	// constructor
	Sbi.cockpit.core.WidgetContainer.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.cockpit.core.WidgetContainer, Sbi.cockpit.core.WidgetRuntime, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {Sbi.cockpit.core.WidgetManager} widgetManager
     * The container that manages the all the widgets rendered within this panel
     */
	widgetManager: null

	/**
	 * @property {Ext.Window} widgetEditorWizard
	 * The wizard that manages the single widget definition
	 */
	, widgetEditorWizard: null

	/**
	 * @property {Object} defaultRegion
	 * The region of the container to  which all new widgets will be added if not explicitly specified otherwise
	 */
	, defaultRegion: {
		width : 0.5
	   	, height : 0.5
	   	, x : 0.25
	   	, y: 0.25
	}

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

	}

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 *
	 * Returns the cockpit configuration. This object can be used as argument to the constructor
	 * of a new cockpit in order to clone the current one. It can also be passed to the
	 * #setConfiguration method at any time  roll back the configuration.
	 */
    , getConfiguration: function(){
    	Sbi.trace("[WidgetContainer.getConfiguration]: IN");

    	var conf = {};
    	conf.widgets = [];

    	var components = this.components.getRange();
    	Sbi.trace("[WidgetContainer.getConfiguration]: the container contains [" + components.length + "] component(s)");
    	for(var i = 0; i < components.length; i++) {
    		if(components[i].isNotEmpty()) {
    			conf.widgets.push( components[i].getWidgetConfiguration() );
    		} else {
    			Sbi.trace("[WidgetContainer.getConfiguration]: component [" + components.id + "] is empty");
    		}
    	}

    	Sbi.trace("[WidgetContainer.getConfiguration]: OUT");

    	return conf;
    }

    /**
     * @method
     *
     * Sets the configuration of this container.
     *
     * @param {Object} The configuration object.
     */
    , setConfiguration: function(configuration) {
    	Sbi.trace("[WidgetContainer.setConfiguration]: IN");
    	for(var i = 0; i < configuration.widgets.length; i++) {
			var widgetConf = configuration.widgets[i];
			var w = this.addWidget(widgetConf);
		}
    	Sbi.trace("[WidgetContainer.setConfiguration]: OUT");
    }

    /**
     * @method
     *
     * Resets the configuration of this container.
     */
    , resetConfiguration: function() {
    	Sbi.trace("[WidgetContainer.resetConfiguration]: IN");
    	this.removeAllWidgets();
    	Sbi.trace("[WidgetContainer.resetConfiguration]: OUT");
    }


    , getWidgetsCount: function() {
    	return this.getWidgetManager().getWidgets().length;
    }

	/**
	 * @method
	 *
	 * Adds the widget to the container using the passed in layout configuration to render it
	 * properly.
	 *
	 * @param {Sbi.cockpit.core.WidgetRuntime/Object} widget The widget to add or it configuration
	 * object.
	 * @parm {Object} The layout configuration to use in oreder to render properly yhe widhet into
	 * the container. It's opetional. If not provided the Sbi.cockpit.core.WidgetRuntime#wlayout proeprty of
	 * the widget will be used (see Sbi.cockpit.core.WidgetRuntime#getLayoutConfiguration).
	 *
	 * @return The added widget
	 */
    , addWidget: function(widget, layoutConf) {

		Sbi.trace("[WidgetContainer.addWidget]: IN");

		if(Sbi.isNotValorized(widget)) {
			Sbi.trace("[WidgetContainer.addWidget]: [widget] parameter is not defined. An empty component will be added to the container.");
		} else {
			widget = Sbi.cockpit.core.WidgetExtensionPointManager.getWidgetRuntime(widget);

	    	if(Sbi.isValorized(widget)) {
	    		this.getWidgetManager().register(widget);

	    		if(Sbi.isValorized(layoutConf)) {
	        		Sbi.trace("[WidgetContainer.addWidget]: Input parameter [layoutConf] is valorized");
	        		widget.setLayoutConfiguration(layoutConf);
	        	} else {
	        		Sbi.trace("[WidgetContainer.addWidget]: Input parameter [layoutConf] is not valorized so it will e replaced with the [wlayout] property of the widget]");
	        		layoutConf = widget.getLayoutConfiguration();
	        	}
	    	} else {
	    		Sbi.error("[WidgetContainer.addWidget]: Impossible to create a widget from [widget] parameter passed in as argument. An empty container will be added to the container.");
	    	}
		}



    	Sbi.trace("[WidgetContainer.addWidget]: [layoutConf] is equal to [" + Sbi.toSource(layoutConf) + "]");

    	var component = this.addComponent(widget, layoutConf);

		Sbi.trace("[WidgetContainer.addWidget]: OUT");

		return widget;
	}
    
    /**
	 * @method
	 *
	 * Adds the widget to the container using the passed in layout configuration to render it
	 * properly. TODO This method is a copy of the previous addWidget method, but it returns the component,
	 * not the widget. If there is an easier way to accomplish this goal, just remove it.
	 *
	 * @param {Sbi.cockpit.core.WidgetRuntime/Object} widget The widget to add or it configuration
	 * object.
	 * @parm {Object} The layout configuration to use in oreder to render properly yhe widhet into
	 * the container. It's opetional. If not provided the Sbi.cockpit.core.WidgetRuntime#wlayout proeprty of
	 * the widget will be used (see Sbi.cockpit.core.WidgetRuntime#getLayoutConfiguration).
	 *
	 * @return The added widget container component
	 */
    , addWidgetContainerComponent: function(widget, layoutConf) {

		Sbi.trace("[WidgetContainer.addWidgetContainerComponent]: IN");

		if(Sbi.isNotValorized(widget)) {
			Sbi.trace("[WidgetContainer.addWidgetContainerComponent]: [widget] parameter is not defined. An empty component will be added to the container.");
		} else {
			widget = Sbi.cockpit.core.WidgetExtensionPointManager.getWidgetRuntime(widget);

	    	if(Sbi.isValorized(widget)) {
	    		this.getWidgetManager().register(widget);

	    		if(Sbi.isValorized(layoutConf)) {
	        		Sbi.trace("[WidgetContainer.addWidgetContainerComponent]: Input parameter [layoutConf] is valorized");
	        		widget.setLayoutConfiguration(layoutConf);
	        	} else {
	        		Sbi.trace("[WidgetContainer.addWidgetContainerComponent]: Input parameter [layoutConf] is not valorized so it will e replaced with the [wlayout] property of the widget]");
	        		layoutConf = widget.getLayoutConfiguration();
	        	}
	    	} else {
	    		Sbi.error("[WidgetContainer.addWidgetContainerComponent]: Impossible to create a widget from [widget] parameter passed in as argument. An empty container will be added to the container.");
	    	}
		}



    	Sbi.trace("[WidgetContainer.addWidgetContainerComponent]: [layoutConf] is equal to [" + Sbi.toSource(layoutConf) + "]");

    	var component = this.addComponent(widget, layoutConf);

		Sbi.trace("[WidgetContainer.addWidgetContainerComponent]: OUT");

		return component;
	}


    , addSelectionWidget: function(selectionWidget, layoutConf) {

		Sbi.trace("[WidgetContainer.addSelectionWidget]: IN");

		if(Sbi.isNotValorized(selectionWidget)) {
			Sbi.trace("[WidgetContainer.addSelectionbWidget]: [selectionWidget] parameter is not defined. Create it");
		} else {
			selectionWidget = Sbi.cockpit.core.WidgetExtensionPointManager.getWidgetRuntime(selectionWidget);

	    	if(Sbi.isValorized(selectionWidget)) {
	    		this.getWidgetManager().register(selectionWidget);

	    		//selectionWidget.setConfiguration()

	    		if(Sbi.isValorized(layoutConf)) {
	        		Sbi.trace("[WidgetContainer.addSelectionWidget]: Input parameter [layoutConf] is valorized");
	        		selectionWidget.setLayoutConfiguration(layoutConf);
	        	} else {
	        		Sbi.trace("[WidgetContainer.addSelectionWidget]: Input parameter [layoutConf] is not valorized so it will e replaced with the [wlayout] property of the widget]");
	        		layoutConf = selectionWidget.getLayoutConfiguration();
	        	}
	    	} else {
	    		Sbi.error("[WidgetContainer.addSelectionWidget]: Impossible to create a widget from [widget] parameter passed in as argument. An empty container will be added to the container.");
	    	}
		}


    	var component = this.addSelectionComponent(selectionWidget, layoutConf);

		Sbi.trace("[WidgetContainer.addWidget]: OUT");

		return selectionWidget;
	}



    /**
     * @method
     *
     * Removes the specified widget
     *
     * @param {Sbi.cockpit.core.WidgetRuntime/Object} The widget to remove or its id
     */
    , removeWidget: function(widget) {
    	Sbi.trace("[WidgetContainer.removeWidget]: IN");
    	this.removeComponent(widget);
    	Sbi.trace("[WidgetContainer.removeWidget]: OUT");
    }

    /**
     * @method
     *
     * Remove all widgets contained in this container
     */
    , removeAllWidgets: function() {
    	Sbi.trace("[WidgetContainer.removeAllWidgets]: IN");
    	this.getWidgetManager().forEachWidget(function(widget, index, length) {
    		this.removeWidget(widget);
    	}, this);
    	if(this.components.getCount() > 0) { // there are some empty components (= not yet associated to any widget)
    		this.components.each(function(component, index, length) {
        		component.close();
        	}, this);
    		this.components.clear();
    	}
    	Sbi.trace("[WidgetContainer.removeAllWidgets]: OUT");
    }

    , getComponentRegion: function(component, relative) {
    	Sbi.trace("[WidgetContainer.getComponentRegion]: IN");
    	var region = null;
    	if( this.components.contains(component) ) {
    		var box = component.getBox();
    		region = {};
    		region.x = box.x;
    		region.y = box.y;
    		region.width = box.width;
    		region.height = box.height;

    		if(relative === true) {
    			region = this.convertToRelativeRegion(region);
    		}
    	}
    	Sbi.trace("[WidgetContainer.getComponentRegion]: OUT");
    	return region;
    }

    , setComponentRegion: function(component, region) {

    }

    , getWidgetManager: function() {
    	return this.widgetManager;
    }

    /**
     * TODO: integrate ace-extjs editor to have the configuration not only pretty printed
     * but also highlighted
     */
    , showWidgetConfiguration: function(component) {

    	Sbi.trace("[WidgetContainer.showWidgetConfiguration]: IN");

    	if( Sbi.isNotValorized(component) ) {
    		Sbi.trace("[WidgetContainer.showWidgetConfiguration]: component not defined");
    	}

    	var widget = component.getWidget();
    	if(widget) {
    		// to be sure to have the conf pretty printed also on old browser that dont support
        	// JSON object natively it is possible to include json2.jd by Douglas Crockford (
        	// https://github.com/douglascrockford/JSON-js)
        	var confStr = (typeof JSON === 'object')
        					? JSON.stringify(widget.getConfiguration(), null, 2)
        					: Ext.JSON.encode(widget.getConfiguration());



        	var win = new Ext.Window({
        		layout:'fit',
                width:500,
                height:300,
                //closeAction:'hide',
                plain: true,
                title: "Widget [" + component.getWidgetId() + "] configuration",
                items: new Ext.form.TextArea({
                	border: false
                	, value: confStr
                    , name: 'configuration'
                }),

                buttons: [
//              {
//                	text:'Copy to clipboard',
//                  	handler: function(){
//                    		...
//                		}
//              },
                {
                	text: 'Close',
                    handler: function(){
                    	win.close();
                    }
                }]
            });
        	win.show();
    	} else {
    		alert("widget not defined");
    	}



    	Sbi.trace("[WidgetContainer.showWidgetConfiguration]: OUT");
    }

    , showWidgetEditorWizard: function(component) {

    	Sbi.trace("[WidgetContainer.showWidgetEditorWizard]: IN");

    	var widget = component.getWidget();

    	if(this.widgetEditorWizard === null) {

    		Sbi.trace("[WidgetContainer.showWidgetEditorWizard]: instatiating the editor");

    		this.widgetEditorWizard = new Sbi.cockpit.editor.WidgetEditorWizard();
    		this.widgetEditorWizard.on("submit", this.onWidgetEditorWizardSubmit, this);
    		this.widgetEditorWizard.on("cancel", this.onWidgetEditorWizardCancel, this);
    		this.widgetEditorWizard.on("apply", this.onWidgetEditorWizardApply, this);

	    	Sbi.trace("[WidgetContainer.showWidgetEditorWizard]: editor succesfully instantiated");
    	}

    	var storeIds = Sbi.storeManager.getStoreIds();
    	Sbi.trace("[WidgetContainer.showWidgetEditorWizard]: used dataset ids [" + storeIds + "]");
    	this.widgetEditorWizard.getDatasetBrowserPage().setUsedDatasets( storeIds );
    	this.widgetEditorWizard.setWizardTargetComponent(component);

    	this.widgetEditorWizard.show();

    	Sbi.trace("[WidgetContainer.showWidgetEditorWizard]: OUT");
    }

    , hideWidgetEditorWizard: function() {
    	this.widgetEditorWizard.hide();
    }

    , applyWidgetEditorWizardState: function() {

    	Sbi.trace("[WidgetContainer.applyWidgetEditorWizardState]: IN");

    	var component = this.widgetEditorWizard.getWizardTargetComponent();

    	var wtype = "";

        /*
         * If I am changing type of widget or modifying existing one,
         * I store the type for further compare
         */
        var existingWidget = component.getWidget();

        if (existingWidget){
        	wtype = existingWidget.wtype;
        }

        // pass information that is moving towards runtime
		var wizardState = null;
		try{
			wizardState = this.widgetEditorWizard.getWizardState(true);
		}
		catch(e){
			Sbi.error("[WidgetContainer.applyWidgetEditorWizardState]: OUT because exception happened");
			return null;
		}

		// must select widget to confirm
		if(!wizardState.wconf){
			Sbi.trace("Must define widget on custom configuration before confirming");
			Ext.Msg.show({
				title: 'Warning',
				msg: LN('Sbi.cockpit.core.WidgetContainer.applyWidgetEditorWizardState'),
				buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.WARNING
			});
			return;
		}


		Sbi.trace("[WidgetContainer.applyWidgetEditorWizardState]: Title validation");

		var re = this.widgetEditorWizard.editorMainPanel.widgetEditorPage.widgetEditorPanel.mainPanel.genericConfPanel.re;

		var titleRegExp = new RegExp(re);
	    
	    var titleWithoutHtml = Ext.util.Format.stripTags(wizardState.wgeneric.title);

		if (!titleRegExp.test(titleWithoutHtml)){
			Ext.Msg.alert('Message', 'Title not valid');

			return false;
		}



		wizardState.storeId = wizardState.selectedDatasetLabel;

		if (wizardState.storeId != null){
			var storeConf = {storeId: wizardState.storeId};
			if(wizardState.wtype.indexOf("crosstab")>=0){
				storeConf.stype = "crosstab";
			}
			if(wizardState.wconf.series && wizardState.wconf.category) {
				var categories = [];

				// category may be nowan array (keep single name for compatibility)

				if(wizardState.wconf.category instanceof Array){

					for(var i = 0; i<wizardState.wconf.category.length;i++){
						var cat = wizardState.wconf.category[i];
						categories.push(cat);
					}

				}
				else{
					categories.push(wizardState.wconf.category);
				}

				if(wizardState.wconf.groupingVariable){categories.push(wizardState.wconf.groupingVariable);}

				var aggregations = {
					measures: wizardState.wconf.series,
					categories: categories
				};
				storeConf.aggregations = aggregations;
				wizardState.storeConf = storeConf;
				Sbi.trace("[WidgetContainer.applyWidgetEditorWizardState]: add store [" + wizardState.storeId + "] with aggregations");
			} else {
				Sbi.trace("[WidgetContainer.applyWidgetEditorWizardState]: add store [" + wizardState.storeId + "] without aggregations");
			}


			// the method addStore add the store only if it is not contained yet in the manager
			Sbi.storeManager.addStore(storeConf);
			Sbi.trace("[WidgetContainer.applyWidgetEditorWizardState]: selected store [" + wizardState.storeId + "] succesfully added to store manager");
		} else {
			Sbi.trace("[WidgetContainer.applyWidgetEditorWizardState]: no store selected");
		}

		var unselectedDatasetLabel = wizardState.unselectedDatasetLabel;
		delete wizardState.selectedDatasetLabel;
		delete wizardState.unselectedDatasetLabel;

		 /*
         * I compare the type to know if I was creating a new one, changing type or just modifying parameters
         */
        if ((wtype == wizardState.wtype) || (!existingWidget)){
			component.setWidgetConfiguration( wizardState );
			var widget = component.getWidget();
			this.getWidgetManager().register(widget);

        } else {
                /*
                 * If i was changing type of widget i have to
                 * remove the old one and create a new one
                 */
                var widget = Sbi.cockpit.core.WidgetExtensionPointManager.getWidgetRuntime(wizardState);

                component.setWidget(widget);

                this.removeWidget(existingWidget);

                this.addWidget(wizardState);
        }

		Sbi.trace("[WidgetContainer.applyWidgetEditorWizardState]: the list of widget registered in widget manager is equal to [" + this.getWidgetManager().getWidgetCount() + "]");

		this.applyStoreUnselection(unselectedDatasetLabel);

		Sbi.trace("[WidgetContainer.applyWidgetEditorWizardState]: OUT");

		return true;
    }

    , applyStoreUnselection: function(unselectedDatasetLabel) {

    	Sbi.trace("[WidgetContainer.applyStoreUnselection]: IN");

		if(Sbi.isValorized(unselectedDatasetLabel)) {
			Sbi.trace("[WidgetContainer.applyStoreUnselection]: removing from store manger unselected store [" + unselectedDatasetLabel + "] ...");
			if( this.getStoreManager().isStoreUsed(unselectedDatasetLabel) == false ) {
				Sbi.storeManager.removeStore(unselectedDatasetLabel);
				Sbi.trace("[WidgetContainer.applyStoreUnselection]: unselected store [" + unselectedDatasetLabel + "] succesfully removed from store manager");
			} else {
				Sbi.trace("[WidgetContainer.applyStoreUnselection]: unselected store [" + unselectedDatasetLabel + "] wont be used because other widgets are using it");
			}
		}

		Sbi.trace("[WidgetContainer.applyWidgetEditorWizardState]: the list of stores registered in store manager is equal to [" + Sbi.storeManager.getStoreIds().join(";") + "]");

		Sbi.trace("[WidgetContainer.applyStoreUnselection]: OUT");
    }

    // -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

    , getContainerSize: function() {
    	return Ext.getBody().getViewSize();
    }

    , getContainerWidth: function() {
    	return this.getContainerSize().width;
    }

    , getContainerHeight: function() {
    	return this.getContainerSize().height;
    }

    , convertToAbsoluteWidth: function(relativeWidth) {
    	return Math.ceil(this.getContainerWidth() * relativeWidth);
    }

    , convertToAbsoluteX: function(relativeX) {
    	return this.convertToAbsoluteWidth(relativeX);
    }

    , convertToAbsoluteHeight: function(relativeHeight) {
    	return Math.ceil(this.getContainerHeight() * relativeHeight);
    }

    , convertToAbsoluteY: function(relativeY) {
    	return this.convertToAbsoluteHeight(relativeY);
    }

    /**
     * @method
     * Returns the region received as argument with all measures converted from relative unit (i.e. %)) to absolute unit (i.e. %). The
     * original object is not modified
     *
     * @param {Objcet} relativeRegion The region to convert in relative units
     * @param {Number} relativeRegion.x The region x position in percentage
     * @param {Number} relativeRegion.y The region y position in percentage
     * @param {Number} relativeRegion.width The region width in percentage
     * @param {Number} relativeRegion.height The region height in percentage
     *
     * @return {Object} The region with all measure express in absolute units (i.e. px)
     */
    , convertToAbsoluteRegion: function(relativeRegion) {
    	var absoluteRegion = {};

    	Sbi.trace("[WidgetContainer.convertToAbsoluteRegion]: IN");

    	Sbi.trace("[WidgetContainer.convertToAbsoluteRegion]: Input relative region is equal to [" + Sbi.toSource(relativeRegion) + "]");

    	if(Sbi.isNotValorized(relativeRegion)) {
    		Sbi.trace("[WidgetContainer.convertToAbsoluteRegion]: Input parameter [relativeRegion] is not defined");
    		Sbi.trace("[WidgetContainer.convertToAbsoluteRegion]: OUT");
    		return null;
    	}

    	if(Sbi.isValorized(relativeRegion.width)) {
    		absoluteRegion.width = this.convertToAbsoluteWidth(relativeRegion.width);
    	} else {
    		Sbi.warn("[WidgetContainer.convertToAbsoluteRegion]: attribute [width] is not defined in the region to convert");
    	}

    	if(Sbi.isValorized(relativeRegion.height)) {
    		absoluteRegion.height = this.convertToAbsoluteHeight(relativeRegion.height);
    	} else {
    		Sbi.warn("[WidgetContainer.convertToAbsoluteRegion]: attribute [height] is not defined in the region to convert");
    	}

    	if(Sbi.isValorized(relativeRegion.x)) {
    		absoluteRegion.x = this.convertToAbsoluteX(relativeRegion.x);
    	} else {
    		Sbi.warn("[WidgetContainer.convertToAbsoluteRegion]: attribute [x] is not defined in the region to convert");
    	}


    	if(Sbi.isValorized(relativeRegion.y)) {
    		absoluteRegion.y = this.convertToAbsoluteY(relativeRegion.y);
    	} else {
    		Sbi.warn("[WidgetContainer.convertToAbsoluteRegion]: attribute [y] is not defined in the region to convert");
    	}


    	Sbi.trace("[WidgetContainer.convertToAbsoluteRegion]: Output absolute region is equal to [" + Sbi.toSource(absoluteRegion) + "]");

    	Sbi.trace("[WidgetContainer.convertToAbsoluteRegion]: OUT");

    	return absoluteRegion;
    }

    , convertToRelativeWidth: function(absoluteWidth) {
    	var relativeWidth =  absoluteWidth / this.getContainerWidth();
    	relativeWidth = relativeWidth.toFixed(2);
    	return relativeWidth;
    }

    , convertToRelativeX: function(absoluteX) {
    	return this.convertToRelativeWidth(absoluteX);
    }

    , convertToRelativeHeight: function(absoluteHeight) {
    	var relativeHeight =  absoluteHeight / this.getContainerHeight();
    	relativeHeight = relativeHeight.toFixed(2);
    	return relativeHeight;
    }

    , convertToRelativeY: function(absoluteY) {
    	return this.convertToRelativeHeight(absoluteY);
    }

    , convertToRelativeRegion: function(absoluteRegion) {
    	var relativeRegion = {};

    	relativeRegion.width = this.convertToRelativeWidth(absoluteRegion.width);
    	relativeRegion.height = this.convertToRelativeHeight(absoluteRegion.height);
    	relativeRegion.x = this.convertToRelativeX(absoluteRegion.x);
    	relativeRegion.y = this.convertToRelativeY(absoluteRegion.y);

    	return relativeRegion;
    }

    /**
     * @method
     *
     * @return  The default region of the container to  which all new widgets will
     * be added if not explicitly specified otherwise
     */
    , getDefaultRegion: function() {
    	var r = Ext.apply({}, this.defaultRegion || {});
    	Sbi.trace("[WidgetContainer.getDefaultRegion]: default region is equal to: [" + Sbi.toSource(r) + "]");
    	return r;
    }

    , addComponent: function(widget, layoutConf) {

    	Sbi.trace("[WidgetContainer.addComponent]: IN");

    	var componentConf = {};
    	if(widget) {
    		Sbi.trace("[WidgetContainer.addComponent]: add a component with an alredy embedded widget");
    		componentConf.widget = widget;
    	}

    	if( Sbi.isNotValorized(layoutConf) ) {
    		Sbi.trace("[WidgetContainer.addComponent]: input parameter [layoutConf] is not defined");
    		layoutConf = {};
    	}
    	if( Sbi.isNotValorized(layoutConf.region) ) {
    		Sbi.trace("[WidgetContainer.addComponent]: attribute [region] of input parameter [layoutConf] is not defined");
    		layoutConf.region = this.getDefaultRegion();
    	}

    	layoutConf.region = this.convertToAbsoluteRegion(layoutConf.region);
    	Sbi.trace("[WidgetContainer.addComponent]: the new component will be added to region: [" + Sbi.toSource(layoutConf.region) + "]");

    	Ext.apply(componentConf, layoutConf);
    	var component = new Sbi.cockpit.core.WidgetContainerComponent(componentConf);

    	component.on('move', this.onComponentMove, this);
    	component.on('resize', this.onComponentResize, this);
    	component.on("performaction", this.onComponentAction, this);
    	component.on("close", this.onComponentClose, this);

    	this.components.add(component.getId(), component);
    	component.setParentContainer(this);

    	if(widget) {
    		widget.setParentComponent(component);
    	}

    	component.show();

    	Sbi.trace("[WidgetContainer.addComponent]: OUT");

    	return component;
    }

    , addSelectionComponent: function(selectionWidget, layoutConf) {

    	Sbi.trace("[WidgetContainer.addSelectionComponent]: IN");

    	var componentConf = {};
    	if(selectionWidget) {
    		Sbi.trace("[WidgetContainer.addSelectionComponent]: add a component with an alredy embedded widget");
    		componentConf.widget = selectionWidget;
    	}

    	if( Sbi.isNotValorized(layoutConf) ) {
    		Sbi.trace("[WidgetContainer.addSelectionComponent]: input parameter [layoutConf] is not defined");
    		layoutConf = {};
    	}
    	if( Sbi.isNotValorized(layoutConf.region) ) {
    		Sbi.trace("[WidgetContainer.addComponent]: attribute [region] of input parameter [layoutConf] is not defined");
    		layoutConf.region = this.getDefaultRegion();
    	}

    	layoutConf.region = this.convertToAbsoluteRegion(layoutConf.region);
    	Sbi.trace("[WidgetContainer.addComponent]: the new component will be added to region: [" + Sbi.toSource(layoutConf.region) + "]");

    	Ext.apply(componentConf, layoutConf);
    	//var component = new Sbi.cockpit.core.WidgetContainerComponent(componentConf);
    	//var window  = new Sbi.cockpit.core.SelectionsWindow(componentConf);
    	var window = new Sbi.cockpit.core.WidgetContainerComponent(componentConf);

    	window.on('move', this.onComponentMove, this);
    	window.on('resize', this.onComponentResize, this);
    	window.on("performaction", this.onComponentAction, this);
    	window.on("close", this.onComponentClose, this);

    	this.components.add(window.getId(), window);
    	window.setParentContainer(this);

    	if(selectionWidget) {
    		selectionWidget.setParentComponent(window);
    	}

    	window.show();

    	Sbi.trace("[WidgetContainer.addComponent]: OUT");

    	return window;
    }

    /**
     * @method
     * Removes the component that contains the specified widget
     *
     * @param {Sbi.cockpit.core.WidgetRuntime/String} widget the widget or the id of the widget to remove
     */
    , removeComponent: function(widget) {
    	Sbi.trace("[WidgetContainer.removeComponent]: IN");
    	var widget = this.getWidgetManager().getWidget(widget);
    	if(Sbi.isValorized(widget)) {
    		var component = widget.getParentComponent();
    		if(Sbi.isValorized(component)) {
    			component.close();
    			Sbi.trace("[WidgetContainer.removeComponent]: component removed");
    		} else {
    			Sbi.warn("[WidgetContainer.removeComponent]: widget is not bound to any container");
    		}

    	} else {
    		Sbi.warn("[WidgetContainer.removeComponent]: widget not found");
    	}
    	Sbi.trace("[WidgetContainer.removeComponent]: OUT");
    }

    , getComponents: function() {
    	return this.components.getRange();
    }




    , onComponentMove: function(component){

    }

    , onComponentResize: function(component){

    }

    , onComponentClose: function(component) {
    	Sbi.trace("[WidgetContainer.onComponentClose]: IN");
    	if(component.isNotEmpty()) {
    		var widget = component.getWidget();
    		this.getWidgetManager().unregister(widget);
    	}
    	this.components.remove(component);
    	Sbi.trace("[WidgetContainer.onComponentClose]: OUT");
    }

    , onComponentAction: function(component, action) {
    	Sbi.trace("[WidgetContainer.onComponentAction]: IN");

    	if(!component) {
    		Sbi.warn("[WidgetContainer.onComponentAction]: component not defined");
    	}

    	if(action === 'showEditor') {
			this.onShowWidgetEditorWizard(component);
		} else if(action === 'showConfiguration') {
			this.onShowWidgetConfiguration(component);
		} else if (action === 'cloneWidget') {
			this.onWidgetClone(component);
		} else {
			Sbi.warn("[WidgetContainer.onComponentAction]: action [" + action + "] not recognized");
		}
		Sbi.trace("[WidgetContainer.onComponentAction]: OUT");
	}

    // -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

    , onRender : function(ct, position){
    	Sbi.trace("[WidgetContainer.onRender]: IN");
    	Sbi.cockpit.core.WidgetContainer.superclass.onRender.call(this, ct, position);
    	if( Sbi.isValorized(this.widgets)) {
    		Sbi.trace("[WidgetContainer.onRender]: There are [" + this.widgets.length + "] widget(s) to render");
    		this.setConfiguration({widgets: this.widgets});
    		delete this.widgets;
    	} else {
    		Sbi.trace("[WidgetContainer.onRender]: There are no widget to render");
    	}
    	Sbi.trace("[WidgetContainer.onRender]: OUT");
    }

    , onShowWidgetConfiguration: function(component) {
    	this.showWidgetConfiguration(component);
    }

    , onShowWidgetEditorWizard: function(component) {
    	this.showWidgetEditorWizard(component);
    }

    , onWidgetEditorWizardSubmit: function(wizard) {
		Sbi.trace("[WidgetContainer.onWidgetEditorWizardSubmit]: IN");
		var validated = this.applyWidgetEditorWizardState();

		if (validated){this.hideWidgetEditorWizard();}
		Sbi.trace("[WidgetContainer.onWidgetEditorWizardSubmit]: OUT");
	}

    , onWidgetEditorWizardApply: function(wizard) {
		Sbi.trace("[WidgetContainer.onWidgetEditorWizardApply]: IN");
		this.applyWidgetEditorWizardState();
		Sbi.trace("[WidgetContainer.onWidgetEditorWizardApply]: OUT");
	}

    , onWidgetEditorWizardCancel: function(wizard) {
		Sbi.trace("[WidgetContainer.onWidgetEditorWizardCancel]: IN");
		this.hideWidgetEditorWizard();
		Sbi.trace("[WidgetContainer.onWidgetEditorWizardCancel]: OUT");
	}

    , onWidgetClone: function (component){
    	var widget = component.getWidget();
    	var newConf = {};

    	var cloneConf = Ext.Object.merge(newConf, widget.getConfiguration());

		cloneConf.wlayout.region.x = (Ext.Number.from(cloneConf.wlayout.region.x,0) + 0.05).toFixed(2);
		cloneConf.wlayout.region.y = (Ext.Number.from(cloneConf.wlayout.region.y,0) + 0.05).toFixed(2);

		this.addWidget(cloneConf);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
    , init: function() {
    	this.components = new Ext.util.MixedCollection();
    }


});