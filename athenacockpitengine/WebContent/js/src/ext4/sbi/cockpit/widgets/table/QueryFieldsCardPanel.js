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

Ext.ns("Sbi.cockpit.widgets.table");

Sbi.cockpit.widgets.table.QueryFieldsCardPanel = function(config) {


	var defaultSettings = {
		emptyMsg: LN('sbi.cockpit.widgets.table.tabledesignerpanel.fields.emptymsg')
	};

	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.queryFieldsCardPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.queryFieldsCardPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	this.addEvents("attributeDblClick", "attributeRemoved");

	this.initEmptyMsgPanel();

	this.tableDesigner = new Sbi.cockpit.widgets.table.QueryFieldsContainerPanel( {
		ddGroup: this.ddGroup
	});
	// propagate events
	this.tableDesigner.on(
		'attributeDblClick' ,
		function (thePanel, attribute) {
			this.fireEvent("attributeDblClick", this, attribute);
		},
		this
	);
	this.tableDesigner.on(
		'attributeRemoved' ,
		function (thePanel, attribute) {
			this.fireEvent("attributeRemoved", this, attribute);
		},
		this
	);

	c = {
			items: [this.emptyMsgPanel, this.tableDesigner]
			//items: [this.emptyMsgPanel, new Ext.Panel({html: "tableDesigner"})]
		    , enableDragDrop: true
		    , border: false
		    , ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
			, layout: 'card'
			, activeItem: 0
			, height: 280
			, style: 'margin-top: 10px; margin-left: auto; margin-right: auto;'
			//, width: 250
			, width: 450
	};

	Sbi.cockpit.widgets.table.QueryFieldsCardPanel.superclass.constructor.call(this, c);

	this.on('render', this.initDropTarget, this);
	this.on('afterLayout', this.setActiveItem, this);
};

Ext.extend(Sbi.cockpit.widgets.table.QueryFieldsCardPanel, Ext.Panel, {
	tableDesigner: null,
	emptyMsgPanel : null,
	emptyMsg : null,


	initEmptyMsgPanel: function() {
		this.emptyMsgPanel = new Ext.Panel({
		    frame: true,
			title: LN('sbi.cockpit.widgets.table.tabledesignerpanel.fields'),
			html: this.emptyMsg
		});
	}

	, initDropTarget: function() {
		this.removeListener("render", this.initDropTarget, this);
		this.dropTarget = new Sbi.widgets.GenericDropTarget(this, {
			ddGroup: this.ddGroup
			, onFieldDrop: this.onFieldDrop
		});
	}

	, onFieldDrop: function(ddSource) {
		Sbi.trace("[QueryFieldsCardPanel.onFieldDrop]: IN");
		if (ddSource.id === "field-grid-body") {
			this.tableDesigner.notifyDropFromQueryFieldsPanel(ddSource);
		} else if (this.tableDesigner.view.getId() == ddSource.view.getId()) {
			this.tableDesigner.notifyDropFromItSelf(ddSource);
		} else {
			alert('Unknown drag sorurce [' + ddSource.id + ']');
		}
		Sbi.trace("[QueryFieldsCardPanel.onFieldDrop]: OUT");
	}

	, setActiveItem: function() {
		Sbi.trace("[QueryFieldsCardPanel.setActiveItem]: IN");
		this.un('afterLayout', this.setActiveItem, this);
    	if (this.tableDesigner.getContainedValues().length > 0) {
    		Sbi.trace("[QueryFieldsCardPanel.setActiveItem]: the designer contains [" + this.tableDesigner.getContainedValues().length + "] fields");
    		this.getLayout().setActiveItem( 1 );
    	} else {
    		Sbi.trace("[QueryFieldsCardPanel.setActiveItem]: The designer contains no field");
    		this.getLayout().setActiveItem( 0 );
    	}

    	//if the table has no data we show the empty message
    	this.tableDesigner.on('storeChanged', this.setActiveItem, this);
    	Sbi.trace("[QueryFieldsCardPanel.setActiveItem]: OUT");
	}

	, containsAttribute: function (attributeId) {
		return this.tableDesigner.containsAttribute(attributeId);
	}

	, validate: function (validFields) {
		return this.tableDesigner.validate(validFields);
	}



});
