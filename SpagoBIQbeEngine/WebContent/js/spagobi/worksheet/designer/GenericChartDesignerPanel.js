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
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.GenericChartDesignerPanel = function(config) { 

	var defaultSettings = {	};
	
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.genericChartDesignerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.genericChartDesignerPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Sbi.worksheet.designer.GenericChartDesignerPanel.superclass.constructor.call(this, c);
	
	this.on('afterLayout', this.addGenericToolTips, this);

};

Ext.extend(Sbi.worksheet.designer.GenericChartDesignerPanel, Ext.Panel, {
	
	/**
	 * Adds the font configurations
	 */
	addFontStyleCombos: function(controlsItems){
		this.innerFontSize= new Ext.form.ComboBox({
			columnWidth : .3,
			typeAhead: true,
			triggerAction: 'all',
			lazyRender:true,
			mode: 'local',
			store: new Ext.data.ArrayStore({
				fields: ['myId','displayText'],
				data: this.fontSizes
			}
			),    
			valueField: 'myId',
			displayField: 'displayText'
		});


		this.innerFontType = new Ext.form.ComboBox({
			columnWidth : .7,
			typeAhead: true,
			triggerAction: 'all',
			lazyRender:true,
			mode: 'local',
			store: new Ext.data.ArrayStore({
				fields: ['myId','displayText'],
				data:   this.fontTypes
			}),  
			valueField: 'myId',
			displayField: 'displayText'

		});


		//creates the font options
		this.outerFontSize= new Ext.form.ComboBox({
			columnWidth : .3,
			typeAhead: true,
			triggerAction: 'all',
			lazyRender:true,
			mode: 'local',
			store: new Ext.data.ArrayStore({
				fields: ['myId','displayText'],
				data: this.fontSizes
			}
			),    
			valueField: 'myId',
			displayField: 'displayText'
		});

		this.outerFontType = new Ext.form.ComboBox({
			columnWidth : .7,
			typeAhead: true,
			triggerAction: 'all',
			lazyRender:true,
			mode: 'local',
			store: new Ext.data.ArrayStore({
				fields: ['myId','displayText'],
				data:   this.fontTypes
			}),  
			valueField: 'myId',
			displayField: 'displayText'

		});

		this.fontTitleId = Ext.id();
		this.fontInnerId = Ext.id();

		//the width is necessary in the pie chart designer, otherwise the form takes all the wizard size..  
		var formWidth = this.getFontFormWidth();
		
		var innerItems = [];
		var outerItems = [];
		
		if(this.chartLib=="highcharts"){
			innerItems.push(this.innerFontType);
			outerItems.push(this.outerFontType);
		}//in ext it's not possible to define the font type and the value text size
		
		innerItems.push(this.innerFontSize);
		outerItems.push(this.outerFontSize);
		
		var conf = {
				layout: 'column',
				fieldLabel:   LN('sbi.worksheet.designer.form.font.titles'),
				border:false,
				id: this.fontTitleId,
				items: outerItems
			};
		
		if(formWidth){
			conf.width = formWidth;
		}
		
		controlsItems.push(conf);
		
		conf = {
				layout: 'column',
				fieldLabel:    LN('sbi.worksheet.designer.form.font.inner'),
				border:false,
				id: this.fontInnerId,
				items:innerItems 
			};
		
		if(formWidth){
			conf.width = formWidth;
		}
		
		controlsItems.push(conf);
			
			
	}
	
	
	, addGenericToolTips: function(){
		this.removeListener('afterLayout', this.addToolTips, this);

		var sharedConf = {
				anchor : 'top'
					, width : 200
					, trackMouse : true
		};

		if(this.chartLib=="highcharts"){
			new Ext.ToolTip(Ext.apply({
				target: 'x-form-el-' + this.fontTitleId,
				html: LN('sbi.worksheet.designer.form.font.titles.tt')
			}, sharedConf));
			new Ext.ToolTip(Ext.apply({
				target: 'x-form-el-' + this.fontInnerId,
				html: LN('sbi.worksheet.designer.form.font.inner.tt')
			}, sharedConf));
		}else{
			new Ext.ToolTip(Ext.apply({
				target: 'x-form-el-' + this.fontTitleId,
				html: LN('sbi.worksheet.designer.form.font.ext.tt')
			}, sharedConf));
		}
		
	}
	
	, getGenericFormState: function(state) {
		state.outerFontSize = this.outerFontSize.getValue();
		state.outerFontType =this.outerFontType.getValue();
		state.innerFontSize = this.innerFontSize.getValue();
		state.innerFontType =this.innerFontType.getValue();
		return state;
	}
	
	, setGenericFormState: function(state) {
		if (state.outerFontSize) this.outerFontSize.setValue(state.outerFontSize);
		if (state.outerFontType) this.outerFontType.setValue(state.outerFontType);
		if (state.innerFontSize) this.innerFontSize.setValue(state.innerFontSize);
		if (state.innerFontType) this.innerFontType.setValue(state.innerFontType);
	}
	
	, getFontFormWidth: function(){
		return null;
	}


});
