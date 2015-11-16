/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.selection");

Sbi.cockpit.widgets.selection.SelectionWidget = function(config) {

	Sbi.trace("[SelectionWidget.constructor]: IN");

	var defaultSettings = {
			wconf : {}
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.selection.SelectionWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	this.init();

	Sbi.cockpit.widgets.selection.SelectionWidget.superclass.constructor.call(this, c);

	this.on('beforerender', this.onBeforeRender, this);

	Sbi.trace("[SelectionWidget.constructor]: OUT");
};

Ext.extend(Sbi.cockpit.widgets.selection.SelectionWidget, Sbi.cockpit.core.WidgetRuntime, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	selectionsPanel: null
	, widgetManager: null
	, gridHeader: null

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

	, onCancelSingle: function(grid, rowIndex, colIndex) {
		this.widgetManager.clearSingleSelection(grid, rowIndex, colIndex);
	}

	, onClearSelections: function(){
		this.widgetManager.clearSelections();
	}

	, onSelectionChange: function() {
	    this.selectionsPanel.refreshStore();
	}

	, onRender: function(ct, position) {
		Sbi.trace("[SelectionWidget.onRender]: IN");

		Sbi.cockpit.widgets.selection.SelectionWidget.superclass.onRender.call(this, ct, position);

		this.add(this.selectionsPanel);
		this.doLayout();

		Sbi.trace("[SelectionWidget.onRender]: OUT");
	}

	, onBeforeRender: function() {
		Sbi.trace("[SelectionWidget.onBeforeRender][" + this.getId() + "]: IN");

		var config = {};

		this.widgetManager = this.getWidgetManager();

		config.widgetManager = this.widgetManager;

		this.selectionsPanel = new Sbi.cockpit.core.SelectionsPanel({
			widgetManager: config.widgetManager,
			gridHeader: this.gridHeader
		});

		this.widgetManager.on('selectionChange',this.onSelectionChange,this);
		this.selectionsPanel.on('cancelSingle', this.onCancelSingle, this);

		Sbi.trace("[SelectionWidget.onBeforeRender][" + this.getId() + "]: OUT");
	}

	//-----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------
	, init: function() {
		Sbi.trace("[SelectionWidget.init]: IN");

		this.gridHeader =
		{
            xtype: 'header',
            titlePosition: 0,
            items: [
                {
                    xtype: 'button',
                    text: LN('sbi.selection.selectionpanel.btn.clearselections'),
                    tooltip: LN('sbi.selection.selectionpanel.btn.clearselections'),
                    handler: this.onClearSelections,
                    scope: this
                }
            ]
		};

		this.wtype = 'selection';

		Sbi.trace("[SelectionWidget.init]: OUT");
	}

});

Sbi.registerWidget('selection', {
	name: 'Selection'
		, icon: 'js/src/ext4/sbi/cockpit/widgets/selection/img/ico_panel_assoc.png'
	, runtimeClass: 'Sbi.cockpit.widgets.selection.SelectionWidget'
	//, designerClass: 'Sbi.cockpit.widgets.selection.SelectionWidgetDesigner'
});