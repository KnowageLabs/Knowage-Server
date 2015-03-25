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
Ext.ns("Sbi.kpi");

Sbi.kpi.KpiGUILayout =  function(config) {

		
		var defaultSettings = {
			layout:'border'
		};

		if (Sbi.settings && Sbi.settings.kpi && Sbi.settings.kpi.kpiGUILayout) {
			defaultSettings = Ext.apply(defaultSettings, Sbi.settings.kpi.kpiGUILayout);
		}

		var c = Ext.apply(defaultSettings);

		Ext.apply(this, c);
		this.addEvents();

		this.intPanels(config);
		this.title = config.accordion.titleDate;
		c = {
				items:[this.kpiMainPanel, this.kpiAccordionPanel]
			};


   
		Sbi.kpi.KpiGUILayout.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.kpi.KpiGUILayout , Ext.Panel, {
	kpiGridPanel: null,
	kpiAccordionPanel: null,
	kpiMainPanel: null,
	autoScroll:false,

	
	intPanels : function(config){
		var gridconf= config.grid;
		var accordionconf = config.accordion;
		
		
		this.kpiMainPanel = new Ext.Panel({

			region: 'center',
			border: false,
			name: 'eeee',
			layoutConfig: {
				padding: '2',
				align: 'left'
			} });

		//check whether there are more than one model instance tree--> # of json objects
		if(gridconf.json.length != 0){
			for(i =0; i<gridconf.json.length;i++){
				
				var resource = gridconf.json[i].resourceName;
				var treeI = new Array();
				treeI[0]=gridconf.json[i];

				gridconf.json[i].flex=1;
				var kpiGridPan = new Sbi.kpi.KpiGridPanel(gridconf,treeI);
				
				kpiGridPan.on('updateAccordion',function(field){
					this.kpiAccordionPanel.updateAccordion(field);
				},this);
				kpiGridPan.doLayout();
				
				var kpiPanel = new Ext.Panel({
					title: resource,
					collapsible: true,
					layout:'fit',
					border: false,
					items: [kpiGridPan],
					layoutConfig: {
						padding: '5',
						border: false,
						align: 'left'
					} });

				this.kpiMainPanel.add(kpiPanel);
			}
			
		}else{
			this.kpiGridPanel = new Sbi.kpi.KpiGridPanel(gridconf,gridconf.json);
			this.kpiGridPanel.on('updateAccordion',function(field){
				this.kpiAccordionPanel.updateAccordion(field);
			},this);
			this.kpiMainPanel.add(this.kpiGridPanel);
		}
		
		
		this.kpiAccordionPanel = new Sbi.kpi.KpiAccordionPanel(accordionconf);
		this.add(this.kpiMainPanel);
		this.kpiMainPanel.doLayout();
		this.doLayout();

	}

});
function execCrossNavigation(d,l,p,s,ti,t){
	sendMessage({'label': l, parameters: p, windowName: d, subobject: s, target: t, title: ti},'crossnavigation');
	
};