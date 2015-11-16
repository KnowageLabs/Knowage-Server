/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  

/**
 * Object name
 * 
 * Contains the content and the filters
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
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it), Davide Zerbetto (davide.zerbetto@eng.it)
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.SheetFilterContentPanel = function(config, filterStore) { 

	var defaultSettings = {
		emptyMsg: LN('sbi.worksheet.designer.sheetcontentpanel.emptymsg')
	};
		
	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.sheetFilterContentPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.sheetFilterContentPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.addEvents("addDesigner", "attributeDblClick", "attributeRemoved", "designerRemoved");

	this.contentPanel = new Sbi.worksheet.designer.SheetContentPanel({style:'padding: 5px 15px 0px 15px;'});
	this.contentPanel.on('addDesigner', function(sheet, state){this.fireEvent('addDesigner',sheet, state);}, this);
	// propagate events
	this.contentPanel.on(
		'attributeDblClick' , 
		function (thePanel, attribute) { 
			this.fireEvent("attributeDblClick", this, attribute); 
		}, 
		this
	);
	this.contentPanel.on(
		'attributeRemoved' , 
		function (thePanel, attribute) { 
			this.fireEvent("attributeRemoved", this, attribute); 
		}, 
		this
	);
	this.contentPanel.on(
		'designerRemoved' , 
		function (thePanel, attribute) { 
			this.fireEvent("designerRemoved", this, attribute); 
		}, 
		this
	);
	
	
	this.filtersPanel = new Sbi.worksheet.designer.DesignSheetFiltersPanel({
		style:'padding:5px 10px 0px 15px; float: left; overflow: auto'
		, hidden: true
		, store: filterStore
		, ddGroup: 'worksheetDesignerDDGroup'
		, height: 400 
		, width: 150
		, tools:[{
			id: 'up',
        	qtip: LN('sbi.worksheet.designer.sheetpanel.tool.up.filter'),
        	handler:this.showTopFilters,
        	scope: this
        }]
	});
	// propagate events
	this.filtersPanel.on(
		'attributeDblClick' , 
		function (thePanel, attribute) { 
			this.fireEvent("attributeDblClick", this, attribute); 
		}, 
		this
	);
	this.filtersPanel.on(
		'attributeRemoved' , 
		function (thePanel, attribute) { 
			this.fireEvent("attributeRemoved", this, attribute); 
		}, 
		this
	);
		
	c = {
		height: 400,
		border: false,
		items: [this.filtersPanel, this.contentPanel]
	};
	
	if(Ext.isIE){
		//workaround.. without this patch the content panel becomes invisible if the filters stay on the left
		this.filtersPanel.on('show',function(){
			try{
				this.contentPanel.setWidth(this.getWidth()-this.filtersPanel.getWidth()-5);
			}catch (e){}
		}, this);
		this.filtersPanel.on('hide',function(){
			try{
				this.contentPanel.setWidth(this.getWidth());
			}catch (e){}
		}, this);
		this.on('resize',function(a,newWidth,c,d,e){
			try{
				if(this.filtersPanel.hidden){
					this.contentPanel.setWidth(newWidth);
				}else{
					this.contentPanel.setWidth(newWidth-this.filtersPanel.getWidth()-5);
				}
			}catch (e){}
		}, this);
	}
	
	Sbi.worksheet.designer.SheetFilterContentPanel.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.worksheet.designer.SheetFilterContentPanel, Ext.Panel, {
	
	showTopFilters: function(){
		this.filtersPanel.hide();
		this.fireEvent('topFilters');
	},

	showLeftFilter: function(){
		this.filtersPanel.show();
		this.filtersPanel.updateFilters();
	},
	
	getDesignerState: function(){
		return this.contentPanel.getDesignerState();
	},
	
	setDesignerState: function(state){
		this.contentPanel.setDesignerState(state);
	},
	
	addDesigner: function(state){
		this.contentPanel.addDesigner(state);
	},
	
	validate: function(validFields){
		
		//validate filters Panel invalid Fields
		var toReturn;
		var errMesg = this.contentPanel.validate(validFields);
		if(errMesg!= undefined && errMesg!= null){
			toReturn = errMesg;
		}		
		var validMesg = this.filtersPanel.validate(validFields);
		if(validMesg!= undefined && validMesg!= null && validMesg!= ''){
			if(toReturn == undefined) {
					validMesg = validMesg.substring(0, validMesg.length - 1)
					toReturn = LN("sbi.worksheet.designer.validation.invalidFields")+ validMesg;
			}
			else {
				toReturn = toReturn + ' / '+ LN("sbi.worksheet.designer.validation.invalidFieldsFilters") +validMesg.substring(0, validMesg.length - 1);
			}
		}

		return toReturn;
	},	
	containsAttribute: function (attributeId) {
		var toReturn = this.contentPanel.containsAttribute(attributeId);
		if (!toReturn) {
			return this.filtersPanel.containsAttribute(attributeId);
		} else {
			return true;
		}
	}

	
});
