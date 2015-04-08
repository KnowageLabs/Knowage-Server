/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.widgets");

Sbi.widgets.GenericDropTarget = function(targetPanel, config) {

	this.validateConfigObject(config);
	this.adjustConfigObject(config);

	// init properties...
	var defaultSettings = {
		//ddGroup must be provided by input config object!!
		copy       : false
	};

	var c = Ext.apply(defaultSettings, config || {});
	Ext.apply(this, c);

	this.targetPanel = targetPanel;
	this.targetPanel.on("beforeDestroy", function() {
		Sbi.trace("[GenericDropTarget.onBeforeDestroy]: IN");
		this.destroy();
		Sbi.trace("[GenericDropTarget.onBeforeDestroy]: OUT");
	}, this);

	// constructor
    Sbi.widgets.GenericDropTarget.superclass.constructor.call(this, this.targetPanel.getEl(), c);
};

/**
 * @class Sbi.widgets.GenericDropTarget
 * @extends Ext.dd.DropTarget
 *
 * bla bla bla bla bla ...
 */

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.widgets.GenericDropTarget, Ext.dd.DropTarget, {


	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {Ext.Panel} targetPanel
     * ...
     */
	targetPanel: null

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
		if(config.ddGroup === undefined || config.ddGroup === null) {
			throw "Impossible to build GenericDropTarget. Config property [ddGroup] is not defined";
		}
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
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	, notifyDrop : function(ddSource, e, data) {
		if (this.onFieldDrop) {
			this.onFieldDrop.call(this.targetPanel, ddSource);
		}
	}

});