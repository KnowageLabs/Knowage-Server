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
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.SheetPanel = function(config) { 

	
	var defaultSettings = {};

	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.sheetsPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.sheetsPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);
	

	
	this.initPanels();
	
	var emptyPanel = new Ext.Panel({
		htlm:'&nbsp;',
		height: 0,
		layout: 'fit',
		hidden: true
	});

	this.addEvents("attributeDblClick");
	
	c = {
			border: false,
			scrollable: true,
            layout: 'fit',
            items:[emptyPanel, this.headerPanel, this.filtersPanel, this.contentPanel, this.footerPanel]
	};
	this.filtersPositionPanel = 'top';
	Ext.apply(this,c);
	
	
	Sbi.worksheet.designer.SheetPanel.superclass.constructor.call(this, c);

	this.on('resize',this.resizePanels,this);
	this.addEvents("attributeDblClick");
	

	this.headerPanel.imgPosition.on('afterrender', function() {
	this.updateHeaderLayout(this.sheetLayout);
	},this);
	this.footerPanel.imgPosition.on('afterrender', function() {
	this.updateFooterLayout(this.sheetLayout);
	},this);

};

Ext.extend(Sbi.worksheet.designer.SheetPanel, Ext.Panel, {
	headerPanel: null,
	filtersPanel: null,
	contentPanel: null,
	footerPanel: null,
	sheetLayout: null,
	filtersPositionPanel: null,
	filtersOnDomainValues: null, 
	
	initPanels: function(){
		//'layout_headerfooter';
		this.sheetLayout = 'layout-content';
		this.headerPanel = new Sbi.worksheet.designer.SheetTitlePanel({});
		
		var filtersConf ={
			style:'padding:5px 15px 0px 15px',
			ddGroup: 'worksheetDesignerDDGroup'	
		};
		
		filtersConf.tools=[{
	       	qtip: LN('sbi.worksheet.designer.sheetpanel.tool.left.filter'),
	       	id: 'left',
	       	handler:this.showLeftFilters,
	       	scope: this
	       }];

		
		this.filtersPanel = new Sbi.worksheet.designer.DesignSheetFiltersPanel(filtersConf);
		// propagate event
		this.filtersPanel.on(
			'attributeDblClick' , 
			function (thePanel, attribute) { 
				this.fireEvent("attributeDblClick", this, attribute); 
			}, 
			this
		);
		this.filtersPanel.on('attributeRemoved' , this.attributeRemovedHandler, this);
		
		this.contentPanel = new Sbi.worksheet.designer.SheetFilterContentPanel({},this.filtersPanel.store);
		// propagate event
		this.contentPanel.on(
			'attributeDblClick' , 
			function (thePanel, attribute) { 
				this.fireEvent("attributeDblClick", this, attribute); 
			}, 
			this
		);
		this.contentPanel.on('attributeRemoved' , this.attributeRemovedHandler, this);
		this.contentPanel.on('designerRemoved' , this.designerRemovedHandler, this);
		
		this.contentPanel.on('topFilters', function() {
			this.filtersPanel.show();
			this.filtersPanel.updateFilters();
			this.filtersPositionPanel = 'top';
			var w = this.getWidth()-22;//10 + 10 of left and right paddings
			this.filtersPanel.setWidth(w);
		},this)
		
		this.footerPanel  = new Sbi.worksheet.designer.SheetTitlePanel({});
	

		
	}

	/* 
	 * check if there is a filter on domain values on that attribute:
	 * it is exists and it is still used, does nothing, elsewhere remove the filter on domain values 
	 */
	, attributeRemovedHandler : function (thePanel, attribute) {
		if (this.filtersOnDomainValues == null || this.filtersOnDomainValues[attribute.id] == undefined) {
			return;
		}
		if (thePanel instanceof Sbi.worksheet.designer.SheetFilterContentPanel) {
			var onContent = this.contentPanel.containsAttribute(attribute.id);
			if ( !onContent ) {
				delete this.filtersOnDomainValues[attribute.id];
			}
		} else {
			var onFilters = this.filtersPanel.containsAttribute(attribute.id);
			if ( !onFilters ) {
				delete this.filtersOnDomainValues[attribute.id];
			}
		}
	}
	
	/* 
	 * a designer (ex: a pivot table) was removed. We have to check if some filters on domain values are still useful 
	 * (i.e. the relevant attribute is still in use in the filters panel)
	 */
	, designerRemovedHandler : function () {
		for (var attributeId in this.filtersOnDomainValues) {
			if ( !this.filtersPanel.containsAttribute(attributeId) ) {
				delete this.filtersOnDomainValues[attributeId];
			}
		}
	}

	, updateLayout: function (sheetLayout) {
		if(sheetLayout!==null){
			 this.sheetLayout=sheetLayout;
			 if(sheetLayout==='layout-header' || sheetLayout==='layout-content'){
				 this.footerPanel.hide();
			 }
			 if(sheetLayout==='layout-footer' || sheetLayout==='layout-content'){
				 this.headerPanel.hide();
			 }
			 if(sheetLayout==='layout-footer' || sheetLayout==='layout-headerfooter'){
				 this.footerPanel.show();
			 }
			 if(sheetLayout==='layout-header' || sheetLayout==='layout-headerfooter'){
				 this.headerPanel.show();
			 }
		}
	}
	
	, updateHeaderLayout: function (sheetLayout) {
		if(sheetLayout!==null){
			 if(sheetLayout==='layout-footer' || sheetLayout==='layout-content'){
				 this.headerPanel.hide();
			 }
			 if(sheetLayout==='layout-header' || sheetLayout==='layout-headerfooter'){
				 this.headerPanel.show();
			 }
		}
	}
	, updateFooterLayout: function (sheetLayout) {
		if(sheetLayout!==null){
			 this.sheetLayout=sheetLayout;
			 if(sheetLayout==='layout-header' || sheetLayout==='layout-content'){
				 this.footerPanel.hide();
			 }
			 if(sheetLayout==='layout-footer' || sheetLayout==='layout-headerfooter'){
				 this.footerPanel.show();
			 }
		}
	}
	, getSheetState: function(){
		var state = {};
		state.name = this.title;
		state.sheetLayout = this.sheetLayout;
		if(!this.headerPanel.hidden){
			state.header = this.headerPanel.getTitleState();
		}
		state.filters ={};
		var filters = this.filtersPanel.getFilters();
		if(filters!==null){
			state.filters.filters = filters;
			if(this.filtersPositionPanel == null){
				if(this.filtersPanel.hidden){
					state.filters.position='left';
				}else{
					state.filters.position='top';
				}				
			}else{
				state.filters.position=this.filtersPositionPanel;
			}
		}

		state.content = this.contentPanel.getDesignerState();
		
		if(!this.footerPanel.hidden){
			state.footer = this.footerPanel.getTitleState();
		}
		
		state.filtersOnDomainValues = this.getFiltersOnDomainValues();

		return state;
	}
	
	, setSheetState: function(sheetState){

		this.title = sheetState.name;
		this.sheetLayout = sheetState.sheetLayout;
		this.updateLayout(this.sheetLayout);
		this.setTitle(this.title);
		if (sheetState.header!==null) {
			this.headerPanel.setTitleState(sheetState.header);
		}
		if (sheetState.filters !== undefined && sheetState.filters !== null && sheetState.filters.filters !== null  && sheetState.filters.filters.length>0) {
			var filters = sheetState.filters.filters;
			this.filtersPanel.setFilters(filters);
			this.filtersPositionPanel = sheetState.filters.position;
			if(sheetState.filters.position=='left'){
				if(this.filtersPanel.rendered){
					this.showLeftFilters();
				}else{
					this.filtersPanel.on('afterrender',this.showLeftFilters, this);	
				}
			}
		}
		if (sheetState.content !== null) {
			this.contentPanel.addDesigner(sheetState.content);
		}
		if (sheetState.footer !== null) {
			this.footerPanel.setTitleState(sheetState.footer);
		}
		
		this.filtersOnDomainValues = {};
		if (sheetState.filtersOnDomainValues !== undefined && sheetState.filtersOnDomainValues !== null 
				&& sheetState.filtersOnDomainValues.length > 0) {
			for (var i = 0; i < sheetState.filtersOnDomainValues.length; i++) {
				var key = sheetState.filtersOnDomainValues[i].id;
				this.filtersOnDomainValues[key] = sheetState.filtersOnDomainValues[i];
			}
		}
		
	}
	
	, validate: function(validFields){
		var valid= '';
		//if(this.headerPanel!==null){
		//	valid = valid && this.headerPanel.isValid();
		//}
		if(this.content!==null){
			valid = this.contentPanel.validate(validFields);
		}
		//if(this.footerPanel!==null){
		//	valid = valid && this.footerPanel.isValid();
		//}
		return valid;
	}
	
	
	, showLeftFilters: function(){
		this.filtersPanel.hide();
		this.contentPanel.showLeftFilter();
		this.filtersPositionPanel = 'left';
	}
	
	/**
	 * Resizes the panels in the sheet panel (header, footer, content and filters)
	 */
	, resizePanels: function(a,newWidth,c,d,e){
		var w = newWidth-22;//10 + 10 of left and right paddings
		if(this.headerPanel != undefined && this.headerPanel != null){
			this.headerPanel.setWidth(w);
		}
		if(this.filtersPanel != undefined && this.filtersPanel != null && !this.filtersPanel.hidden){
			this.filtersPanel.setWidth(w);
		}
		if(this.contentPanel != undefined && this.contentPanel != null){
			this.contentPanel.setWidth(w);
		}
		if(this.footerPanel != undefined && this.footerPanel != null){
			this.footerPanel.setWidth(w);
		}
	}
	
	, getName : function () {
		return this.title;
	}
	
	, getFilterOnDomainValues : function(attribute) {
		if (this.filtersOnDomainValues == null) {
			this.filtersOnDomainValues = {};
		}
		if (this.filtersOnDomainValues[attribute.id] !== undefined) {
			return this.filtersOnDomainValues[attribute.id];
		} else {
			var clone = Ext.apply({}, attribute);
			this.filtersOnDomainValues[clone.id] = clone;
			return clone;
		}
	}
	
	, getFiltersOnDomainValues : function() {
		var toReturn = [];
		for (var x in this.filtersOnDomainValues) {
			if (this.filtersOnDomainValues[x].values !== '[]') {
				toReturn.push(Ext.apply({}, this.filtersOnDomainValues[x])); // we clone the field in order to solve https://spagobi.eng.it/jira/browse/SPAGOBI-1530
			}
		}
		return toReturn;
	}

	
});