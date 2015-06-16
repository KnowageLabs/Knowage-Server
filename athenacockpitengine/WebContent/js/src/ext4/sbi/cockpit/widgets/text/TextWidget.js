/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.text");

Sbi.cockpit.widgets.text.TextWidget = function(config) {

	Sbi.trace("[TextWidget.constructor]: IN");
	// init properties...
	var defaultSettings = {
		fieldValue: ''
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.table.TableWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);
	/*Sbi.trace("[TextWidget.constructor]: config: " + Sbi.toSource(config));

	if(Sbi.settings && Sbi.settings.console && Sbi.settings.console.widget) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.console.widget);
	}
	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);*/

	// constructor
	Sbi.cockpit.widgets.text.TextWidget.superclass.constructor.call(this, c);
	Sbi.trace("[TextWidget.constructor]: OUT");
};

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.cockpit.widgets.text.TextWidget, Sbi.cockpit.core.WidgetRuntime, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	// ...

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------


	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	onRender: function(ct, position) {
		Sbi.trace("[TextWidget.onRender]: IN");
		Sbi.cockpit.widgets.text.TextWidget.superclass.onRender.call(this, ct, position);

		
		this.widgetContent = new Ext.Panel({
			border: false
			, bodyBorder: false
			, hideBorders: true
			, frame: false
			, height: '100%'
			, html: this.wconf.textValue
		});

		this.items.each( function(item) {
			this.items.remove(item);
	        item.destroy();
	    }, this);

		if(this.chart !== null) {
			this.add(this.widgetContent);
			this.doLayout();
		}
		Sbi.trace("[TextWidget.onRender]: OUT");
	}

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================

	// ...
});

Sbi.registerWidget('text', {
	name: 'Text'
	, icon: 'js/src/ext4/sbi/cockpit/widgets/text/dummy_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.text.TextWidget'
	, designerClass: 'Sbi.cockpit.widgets.text.TextWidgetDesigner'
});