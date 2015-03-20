/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  

/**
 * Public Methods
 * 
 * getLayoutValue(): returns a string with the selected layout. 
 * 			Available values:
 * 				'layout-content' (default)
 * 				'layout-header'
 * 				'layout-footer'
 * 				'layout-headerfooter' 
 * 
 * 
 * setLayoutValue(value): sets the layout value..
 * 			The available values are the same of getLayoutValue
 * 
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.DesignToolsLayoutPanel = function(config) { 
	
	var defaultSettings = {
		border: false,
		title:  LN('sbi.worksheet.designer.designtoolslayoutpanel.title'),
		border: false,
		bodyStyle: 'padding-top: 15px; padding-left: 15px'
	};
			
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.designToolsLayoutPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.designToolsLayoutPanel);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
		
	Ext.apply(this, c);
			
	this.layoutRadioGroup = new Ext.form.RadioGroup({
		hideLabel: true,
		columns: 2,
		items: [
		        {name: 'layout', height: 40, id:'layout-content', ctCls:'layout-content', inputValue: 'layout-content' , checked: true},
		        {name: 'layout', height: 40, id:'layout-header', ctCls:'layout-header',inputValue: 'layout-header'},
		        {name: 'layout', height: 40, id:'layout-footer', ctCls:'layout-footer', inputValue: 'layout-footer'},
		        {name: 'layout', height: 40, id:'layout-headerfooter', ctCls:'layout-headerfooter',inputValue: 'layout-headerfooter'}
		        ]
	});
	
	c = {
		//autoScroll : true,
		items: [this.layoutRadioGroup]
	};

	this.layoutRadioGroup.on('change', this.updateSheetLayout, this);
	
	Sbi.worksheet.designer.DesignToolsLayoutPanel.superclass.constructor.call(this, c);	

	this.on('afterLayout',this.addToolTips,this);


};

Ext.extend(Sbi.worksheet.designer.DesignToolsLayoutPanel, Ext.FormPanel, {
	layoutRadioGroup: null,

	addToolTips: function(){

		var sharedConf ={anchor: 'top',width:200,trackMouse:true};

		new Ext.ToolTip(Ext.apply({
			target: 'x-form-el-layout-content',
			html: LN('sbi.worksheet.designer.designtoolslayoutpanel.tooltip.content')
		},sharedConf));
		new Ext.ToolTip(Ext.apply({
			target: 'x-form-el-layout-header',
			html: LN('sbi.worksheet.designer.designtoolslayoutpanel.tooltip.header')
		},sharedConf));
		new Ext.ToolTip(Ext.apply({
			target: 'x-form-el-layout-footer',
			html: LN('sbi.worksheet.designer.designtoolslayoutpanel.tooltip.footer')
		},sharedConf));
		new Ext.ToolTip(Ext.apply({
			target: 'x-form-el-layout-headerfooter',
			html: LN('sbi.worksheet.designer.designtoolslayoutpanel.tooltip.headerfooter')
		},sharedConf));
		
	},

	//returns a string with the selected layout (for the available values look the..
	//.. class comment)
	getLayoutValue: function(){
		if(this.layoutRadioGroup!==null && this.layoutRadioGroup.getValue()!==null && this.layoutRadioGroup.getValue().getGroupValue()!==null){
			return this.layoutRadioGroup.getValue().getGroupValue();
		}else{
			// Default no header or footer
			this.layoutRadioGroup.setValue('layout-content');
			return 'layout-content';
			//			this.layoutRadioGroup.setValue('layout-headerfooter');
			//			return 'layout-headerfooter';
		}
	},

	//set the layout (for the available values look the..
	//.. class comment)
	setLayoutValue: function(value){
		this.layoutRadioGroup.setValue(value);
	},
	
	updateSheetLayout: function(){
		this.fireEvent('layoutchange',this.getLayoutValue());
	}


});