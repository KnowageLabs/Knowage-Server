/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.chart");

Sbi.cockpit.widgets.chart.ChartCategoryPanel = function(config) {

	var defaultSettings = {
		  title: LN('sbi.cockpit.widgets.chartcategorypanel.title')
		, frame: true
		, emptyMsg: LN('sbi.cockpit.widgets.chartcategorypanel.emptymsg')
	};

	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.chart && Sbi.settings.cockpit.widgets.chart.chartCategoryPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.chart.chartCategoryPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	this.addEvents("attributeDblClick", "attributeRemoved", "beforeAddAttribute");

	this.init();

	c = Ext.apply(c, {
		items: [this.emptyMsgPanel]
	});

	// constructor
	Sbi.cockpit.widgets.chart.ChartCategoryPanel.superclass.constructor.call(this, c);

	this.on('render', this.initDropTarget, this);

};

Ext.extend(Sbi.cockpit.widgets.chart.ChartCategoryPanel, Ext.Panel, {

	emptyMsg : null
	, emptyMsgPanel : null
    , category : null
	, content : null

	, init: function() {
		this.initEmptyMsgPanel();
		this.content = this.emptyMsgPanel;
	}

	, initDropTarget: function() {
		this.removeListener('render', this.initDropTarget, this);
		this.dropTarget = new Sbi.widgets.GenericDropTarget(this, {
			ddGroup: this.ddGroup
			, onFieldDrop: this.onFieldDrop
		});
	}

	, onFieldDrop: function(ddSource) {
		Sbi.trace("[ChartCategoryPanel.onFieldDrop]: IN");
		/*
		if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'queryFieldsPanel') {
			this.notifyDropFromQueryFieldsPanel(ddSource);
		} else {
			Ext.Msg.show({
			   title: LN('sbi.worksheet.designer.chartcategorypanel.cannotdrophere.title'),
			   msg: LN('sbi.worksheet.designer.chartcategorypanel.cannotdrophere.unknownsource'),
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.WARNING
			});
		}
		*/

		if (ddSource.id === "field-grid-body") {
			this.notifyDropFromQueryFieldsPanel(ddSource);
		} else {
			Ext.Msg.show({
				   title: LN('sbi.cockpit.widgets.chartcategorypanel.cannotdrophere.title'),
				   msg: LN('sbi.cockpit.widgets.chartcategorypanel.cannotdrophere.unknownsource'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
				});
		}

		Sbi.trace("[ChartCategoryPanel.onFieldDrop]: OUT");

	}

	, notifyDropFromQueryFieldsPanel: function(ddSource) {
		var rows = ddSource.dragData.records;
		if (rows.length > 1) {
			Ext.Msg.show({
				   title:'Drop not allowed',
				   msg: 'You can move only one field at a time',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		} else {
			var aRow = rows[0];
			// if the field is a measure show a warning
			if (aRow.data.nature === 'measure' || aRow.data.nature === 'mandatory_measure') {
				Ext.Msg.show({
					   title: LN('sbi.cockpit.widgets.chartcategorypanel.cannotdrophere.title'),
					   msg: LN('sbi.cockpit.widgets.chartcategorypanel.cannotdrophere.measures'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
				return;
			}
			// if the field is a postLineCalculated show an error
			if (aRow.data.nature === 'postLineCalculated') {
				Ext.Msg.show({
					   title: LN('sbi.cockpit.widgets.chartcategorypanel.cannotdrophere.title'),
					   msg: LN('sbi.cockpit.widgets.chartcategorypanel.cannotdrophere.postlinecalculated'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
				});
				return;
			}

			if (this.fireEvent('beforeAddAttribute', this, aRow.data) == false) {
				return;
			}

			this.setCategory(aRow.data, true);
		}

	}

	, setCategory: function (category, isValid) {
		var previousCategory = this.category;
		this.category = Ext.apply({}, category); // making a clone of the input object
		this.content.destroy();
		var panel = this.createCategoryPanel(isValid);
		this.add(panel);
		this.content = panel;
		if (previousCategory != null) {
			this.fireEvent('attributeRemoved', this, previousCategory);
		}
		this.doLayout();
	}

	, getCategory: function () {
		return this.category;
	}

	, removeCategory: function() {
		var previousCategory = this.category;
		this.category = null;
		this.content.destroy();
		this.initEmptyMsgPanel();
		this.add(this.emptyMsgPanel);
		this.content = this.emptyMsgPanel;
		if (previousCategory != null) {
			this.fireEvent('attributeRemoved', this, previousCategory);
		}
		this.doLayout();
	}

	, initEmptyMsgPanel: function() {
		this.emptyMsgPanel = new Ext.Panel({
			html: this.emptyMsg
			, height: 40
		});
	}

	, createCategoryPanel: function(isValid) {

		var validation= '';

		if(isValid != undefined && isValid==false){
			validation = 'color:#ff0000; text-decoration:line-through;';

		}


//		thePanel.on('render', function(panel) {
//			panel.getEl().on('dblclick', function() {
//		     	this.fireEvent("attributeDblClick", this, this.category);
//			}, this);
//		}, this);


		var item = new Ext.Panel({
            layout: {
                type:'column'
            }
			, style:'padding:5px 5px 5px 5px'
       		, items: [
       		  new Ext.Button({
       		    template: new Ext.Template(
       		         '<div class="smallBtn">',
       		             '<div class="delete-icon"></div>',
       		             '<div class="btnText"></div>',
       		         '</div>')
       		     , buttonSelector: '.delete-icon'
       		  	 , iconCls: 'delete-icon-tab'
       		     , text: this.category.alias
       		     , handler: this.removeCategory
       		     , scope: this
       		})]
		});

		item.on('dblclick', function() {
		     	this.fireEvent("attributeDblClick", this, this.category);
			}, this);

		return item;
	}

	, validate: function (validFields) {
		var invalidFields ='';
		if (this.category != null){
			var isValid = this.validateRecord(this.category,validFields);
			if(isValid == false){
				invalidFields +=''+this.category.alias+',';
			}
			this.setCategory(this.category, isValid);
		}
		return invalidFields;
	}

	, validateRecord: function (category, validFields) {
		var isValid = false;
		var i = 0;
		for(; i<validFields.length && isValid == false; i++){
			if(validFields[i].id == category.id){
			isValid = true;
			}
		}
		return isValid;
	}

});