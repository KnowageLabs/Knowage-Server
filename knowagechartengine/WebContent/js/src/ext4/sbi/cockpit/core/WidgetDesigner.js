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
