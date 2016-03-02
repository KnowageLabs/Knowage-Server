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
			, width: 250
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
