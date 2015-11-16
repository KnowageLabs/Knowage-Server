/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.dummy");

Sbi.cockpit.widgets.dummy.DummyWidget = function(config) {

	Sbi.trace("[DummyWidget.constructor]: IN");
	// init properties...
	var defaultSettings = {
	};

	Sbi.trace("[DummyWidget.constructor]: config: " + Sbi.toSource(config));

	if(Sbi.settings && Sbi.settings.console && Sbi.settings.console.widget) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.console.widget);
	}
	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	// constructor
	Sbi.cockpit.widgets.dummy.DummyWidget.superclass.constructor.call(this, c);
	Sbi.trace("[DummyWidget.constructor]: OUT");
};

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.cockpit.widgets.dummy.DummyWidget, Sbi.cockpit.core.WidgetRuntime, {

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
		Sbi.trace("[DummyWidget.onRender]: IN");
		Sbi.cockpit.widgets.dummy.DummyWidget.superclass.onRender.call(this, ct, position);

		this.dummyContent = new Ext.Panel({
			border: false
			, bodyBorder: false
			, hideBorders: true
			, frame: false
			, html: this.msg || 'Sono un widget qualunque'
		});

		this.items.each( function(item) {
			this.items.remove(item);
	        item.destroy();
	    }, this);

		if(this.chart !== null) {
			this.add(this.dummyContent);
			this.doLayout();
		}
		Sbi.trace("[DummyWidget.onRender]: OUT");
	}

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================

	// ...
});
/*
Sbi.registerWidget('dummy', {
	name: 'Dummy'
	, icon: 'js/src/ext/sbi/cockpit/widgets/dummy/dummy_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.dummy.DummyWidget'
	//, designerClass: 'Sbi.cockpit.widgets.dummy.DummyWidgetDesigner'
	, designerClass: 'Ext.Panel'
});*/