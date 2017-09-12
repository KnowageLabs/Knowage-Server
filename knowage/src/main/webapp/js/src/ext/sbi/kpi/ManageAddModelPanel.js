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
