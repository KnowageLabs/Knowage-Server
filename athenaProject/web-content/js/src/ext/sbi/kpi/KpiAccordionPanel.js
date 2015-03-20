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

Sbi.kpi.KpiAccordionPanel =  function(config) {
		
		var defaultSettings = {};

		if (Sbi.settings && Sbi.settings.kpi && Sbi.settings.kpi.kpiAccordionPanel) {
			defaultSettings = Ext.apply(defaultSettings, Sbi.settings.kpi.kpiAccordionPanel);
		}

		var c = Ext.apply(defaultSettings, config || {});

		Ext.apply(this, c);
	
	    c = {
	        region:'east',
	        fill: true,
	        split:true,
	        width: 700,
	        minSize: 400,
			//autoWidth: true,
	        collapsible: false,
	        layout:'accordion',
	        items: []
	    };
	    this.initDetail(config);
	    this.initDescription();
	    this.initDocCollegato();
	    this.initComments(config);
	    this.initHistorical(config);
		this.initAccordion(c);
		
		
		Sbi.kpi.KpiAccordionPanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.kpi.KpiAccordionPanel , Ext.Panel, {
	detail: null
	, description: null
	, docs: null
	, comments: null
	, historical: null
	, itemDetail: null
	, itemDocColl: null
	, itemNoKpi: null
	
	, initAccordion: function(c){

		this.itemNoKpi = new Ext.form.DisplayField({value: LN('sbi.kpi.nokpi'), 
			style: 'font-weight: bold; align:center; margin: 20px;'});
		
	    this.itemDetail = new Ext.Panel({
	        title: LN('sbi.kpi.accordionmenu.detail'),
	        items: [this.detail, this.itemNoKpi ],
	        autoScroll: true,
            listeners : {
                expand: function(p){
                    p.doLayout();
                }
            },
	        cls:'empty'
	    });
	    
	    var item2 = new Ext.Panel({
	        title: LN('sbi.kpi.accordionmenu.desciption'),
	        items: [this.description],
	        cls:'empty'
	    });

	    this.itemDocColl = new Ext.Panel({
	        title: LN('sbi.kpi.accordionmenu.linkeddoc'),
	        items: [this.docs],
	        scope: this,
            listeners : {
                expand: function(p){
                    p.doLayout();
                }
            },
            autoScroll: true
	    });

	    var item4 = new Ext.Panel({
	        title: LN('sbi.kpi.accordionmenu.comments'),
	        scope: this,
	        items: [this.comments],
	        
	        listeners : {
	            expand: function(p){
	                p.doLayout();
	            }
	        },
	        autoScroll: true
	    });

	    var item5 = new Ext.Panel({
	        title: LN('sbi.kpi.accordionmenu.history'),
	        items: [this.historical],
	        autoScroll: true,
	        cls:'empty'
	    });
	    c.items = [this.itemDetail, item2, this.itemDocColl, item4, item5];
	}
	, initDetail: function(c){
		this.detail = new Sbi.kpi.KpiGUIDetail(c);

	}
	, initDescription: function(){
		this.description = new Sbi.kpi.KpiGUIDescription();
	}
	, initHistorical: function(c){
		this.historical = new Sbi.kpi.KpiGUIHistorical(c);
	}
	, initDocCollegato: function(){
		this.docs = new Sbi.kpi.KpiGUIDocCollegato();

	}
	, initComments: function(c){
		this.comments = new Sbi.kpi.KpiGUIComments(c);	

	}
	, updateAccordion: function(field){
		//detail
		if(field.attributes == undefined || field.attributes.kpiName === undefined || field.attributes.kpiName === null){
			this.detail.updateEmpy();
			this.itemNoKpi.show();
		}else{
			this.detail.update(field);
			this.itemNoKpi.hide();
		}
		
		//description
		this.description.update(field);
		
		//linked docs
		if(field.attributes != undefined && field.attributes.documentLabel != undefined){
			this.itemDocColl.setTitle(LN('sbi.kpi.accordionmenu.linkeddoc')+' '+field.attributes.documentLabel);
			this.itemDocColl.show();
		}else{
			this.itemDocColl.hide();
		}
		this.docs.update(field);
		//comments
		this.comments.update(field);
		//historical
		this.historical.update(field);
		this.render();
	}
});