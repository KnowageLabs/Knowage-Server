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
 * Authors - Monica Franceschini
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.ManageAddModelPanel = function(config) { 
	
	var conf = config;

	//DRAW west element
    this.modelsGrid = new Sbi.kpi.ManageModelsGrid(conf, this);
	//DRAW center element
	this.manageModelsTree = new Sbi.kpi.ManageModelsTree(conf, this.modelsGrid);

	
	var windowPanel = {
		layout: 'column'
		, height      : 400
		, autoScroll: false
		, scope: this
		, items: [
	         {
	           columnWidth: 0.4,
	           height:400,
	           collapseMode:'mini',
	           autoScroll: true,
	           split: true,
	           layout: 'fit',
	           items:[this.modelsGrid]
	          },
		    {
	           columnWidth: 0.6,
		       height:400,
		       split: true,
		       collapseMode:'mini',
		       autoScroll: true,
		       layout: 'fit',
		       items: [this.manageModelsTree]
		    }
		]	

	};
	
	
	var c = Ext.apply({}, config || {}, windowPanel);
	
	this.initPanels();

	Sbi.kpi.ManageAddModelPanel.superclass.constructor.call(this, c);	 		

};

Ext.extend(Sbi.kpi.ManageAddModelPanel, Ext.Panel, {
	modelsGrid: null,
	manageModelsTree: null
	
	,initPanels : function() {

		this.modelsGrid.addListener('rowclick', this.sendSelectedItem, this);

	}
	, displayTree: function(rec){
		this.manageModelsTree.rootNodeText = rec.get('code')+ " - "+rec.get('name');
		this.manageModelsTree.rootNodeId = rec.get('modelId');
		var newroot = this.manageModelsTree.createRootNodeByRec(rec);
		this.manageModelsTree.modelsTree.setRootNode(newroot);
		
		this.manageModelsTree.modelsTree.getSelectionModel().select(newroot);
		this.manageModelsTree.modelsTree.doLayout();
	}

	,sendSelectedItem: function(grid, rowIndex, e){
		var rec = grid.getSelectionModel().getSelected();
		this.displayTree(rec);
	}

});
