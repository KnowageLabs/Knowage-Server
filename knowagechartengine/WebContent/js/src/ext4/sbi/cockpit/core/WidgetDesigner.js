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
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.ns("Sbi.cockpit.core");

/**
 * @class Sbi.cockpit.core.WidgetDesigner
 * @extends Ext.Panel
 *
 * It's an abstract class that simplify the implementation of the design facet of a new widget extension.
 * See {@link Sbi.cockpit.core.WidgetExtensionPointManager WidgetExtensionPointManager} to find out more
 * information on widget's extension point.
 */

/**
 * @cfg {Object} config the widget configuration object
 * @cfg {String} config.storeId The label of the dataset used to feed the widget
 * @cfg {String} config.wtype The type of the widget
 * @cfg {Object} config.wconf The custom configuration of the widget. Its content depends on the widget's #wtype
 * @cfg {Object} config.wstyle The style configuration of the widget. Its content depends on the widget's #wtype
 * @cfg {Object} config.wlayout The layout configuration of the widget. Its content depends on the widget's parent {@link #parentContainer container container}
 */
Sbi.cockpit.core.WidgetDesigner = function(config) {

	Sbi.trace("[WidgetDesigner.constructor]: OUT");

	var defaultSettings = {
		layout: 'fit'
		, border: false
	};
	var settings = Sbi.getObjectSettings('Sbi.cockpit.core.WidgetDesigner', defaultSettings);

	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	if(!c.items) {
		c.html = "I'm a widget designer";
	}

	Sbi.cockpit.core.WidgetDesigner.superclass.constructor.call(this, c);

	Sbi.trace("[WidgetDesigner.constructor]: OUT");
};

Ext.extend(Sbi.cockpit.core.WidgetDesigner, Ext.Panel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {String} wtype
     * The wtype of the widget extension as registered in {@link Sbi.cockpit.core.WidgetExtensionPointManager}
     * to which this runtime class is associated.
     */
	wtype: null

	, getDesignerType: function() {
		return this.wtype;
	}

	, getDesignerState: function() {
		return {};
	}

	, setDesignerState: function(state) {
		this.wstate = state;
	}

	, validate: function(validFields){
		return;
	}

	, containsAttribute: function (attributeId) {
		return false;
	}



});
