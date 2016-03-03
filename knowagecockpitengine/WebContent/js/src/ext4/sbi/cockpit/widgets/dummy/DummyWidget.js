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