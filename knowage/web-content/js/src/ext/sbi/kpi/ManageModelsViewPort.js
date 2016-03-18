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

Sbi.kpi.ManageModelsViewPort = function(config) { 
	
	var conf = config;
	var d = conf;
	d.udpEmptyList = conf.udpModelEmptyListJSON;
	d.udpList = conf.udpModelListCdt ;

	//DRAW center element
	this.manageModels = new Sbi.kpi.ManageModels(d, this);

	//DRAW west element
    this.modelsGrid = new Sbi.kpi.ManageModelsGrid(d, this.manageModels);
   //DRAW east element
    conf.singleSelection = false;
    conf.tabPanelWidth = 260;
	conf.gridWidth = 255;
	conf.textAreaWidth = 120;
	conf.fieldsDefaultWidth= 120;
	conf.gridColumnNumber = 2;

	var c = conf;
	c.udpEmptyList = conf.udpKpiEmptyListJSON ;
	c.udpList = conf.udpKpiListCdt;
	c.filterWidth = 250;
	
    this.manageKpis = new Sbi.kpi.ManageKpis(c);
	
	var viewport = {
		layout: 'border'
		, height:560
		, autoScroll: true
		, items: [
	         {
	           id: 'modelsgrid00',	 
	           region: 'west',
	           width: 275,
	           height:560,
	           collapseMode:'mini',
	           autoScroll: true,
	           split: true,
	           layout: 'fit',
	           items:[this.modelsGrid]
	          },
		    {
		       region: 'center',

		       height:560,
		       split: true,
		       collapseMode:'mini',
		       autoScroll: true,
		       layout: 'fit',
		       items: [this.manageModels]
		    }, {
		        region: 'east',
		        split: true,
		        width: 550,
		        height:560,
		        collapsed:true,
		        collapseMode:'mini',
		        autoScroll: true,
		        layout: 'fit',
		        items:[this.manageKpis],
	            listeners : {
                    beforeCollapse: function(cmp){
                        //expand model instances list
                        var toCollapse = Ext.getCmp('modelsgrid00');
                        toCollapse.expand();
                    },
                    beforeExpand: function(cmp){
                        //collapse model instances list
                        var toCollapse = Ext.getCmp('modelsgrid00');
                        toCollapse.collapse();
                    }
                }
		    }
		]
		
	};
	
	
	var c = Ext.apply({}, config || {}, viewport);
	
	this.initPanels();

	Sbi.kpi.ManageModelsViewPort.superclass.constructor.call(this, c);	 		

};

Ext.extend(Sbi.kpi.ManageModelsViewPort, Ext.Viewport, {
	modelsGrid: null,
	manageModels: null,
	manageKpis: null,
	lastRecSelected: null
	
	,initPanels : function() {

		this.manageKpis.addListener('render', this.configureDD, this);
		this.modelsGrid.addListener('rowclick', this.sendSelectedItem, this);

	}
	, displayTree: function(rec){
		this.manageModels.rootNodeText = rec.get('code')+ " - "+rec.get('name');
		this.manageModels.rootNodeId = rec.get('modelId');
		var newroot = this.manageModels.createRootNodeByRec(rec);
		this.manageModels.mainTree.setRootNode(newroot);
		
		this.manageModels.mainTree.getSelectionModel().select(newroot);
		this.manageModels.mainTree.doLayout();
	}

	,sendSelectedItem: function(grid, rowIndex, e){
		var rec = grid.getSelectionModel().getSelected();


		//if unsaved changes
		if(this.manageModels.nodesToSave.length > 0){
			//if there are modification on current selection
			Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm'),
					LN('sbi.generic.confirmChangeNode'),            
		            function(btn, text) {

		                if (btn=='yes') {

		                	this.manageModels.cleanAllUnsavedNodes();	        			
		        			this.displayTree(rec);
		        			//this.manageModels.renderAttributeGrid(rec);
			        		if(rec != this.lastRecSelected){
			        			this.lastRecSelected = rec;
			        		}

		                }else{
		                	grid.getSelectionModel().selectRecords([this.lastRecSelected]);
		                	
		                }

		            },
		            this
				);
		}else{
			this.displayTree(rec);
			//this.manageModels.renderAttributeGrid(rec);
			if(rec != this.lastRecSelected){
				this.lastRecSelected = rec;
			}
		}

	}

	, configureDD: function() {
	  	  /****
		  * Setup Drop Targets
		  ***/

		  var nodeTreePanelDropTarget = new Ext.tree.TreeDropZone(this.manageModels.mainTree, {
		    ddGroup  : 'grid2treeAndDetail',
		    dropAllowed : true,
		    overClass: 'over',
		    scope: this,
		    initialConfig: this.manageModels
		  });

		  // This will make sure we only drop to the view container
		  var fieldDropTargetEl =  this.manageModels.detailFieldKpi.getEl().dom; 
		  var formPanelDropTarget = new Ext.dd.DropTarget(fieldDropTargetEl, {
			    ddGroup  : 'grid2treeAndDetail',
			    overClass: 'over',
			    scope: this,
			    initialConfig: this.manageModels,
			    notifyEnter : function(ddSource, e, data) {
			      //Add some flare to invite drop.
			      Ext.fly(Ext.getCmp('model-detailFieldKpi').getEl()).frame("00AE00");

			    },
			    notifyDrop  : function(ddSource, e, data){
	  
			      // Reference the record (single selection) for readability
			      var selectedRecord = ddSource.dragData.selections[0];

			      // Load the record into the form field
			      Ext.getCmp('model-detailFieldKpi').setValue(selectedRecord.get('name')); 

			      var node = this.initialConfig.mainTree.getSelectionModel().getSelectedNode() ;

			      if(node !== undefined && node != null){
			    	  var nodesList = this.initialConfig.nodesToSave;
			    	  
			    	  //if the node is already present in the list
			    	  var exists = nodesList.indexOf(node);
			    	  if(exists == -1){
						  var size = nodesList.length;
						  this.initialConfig.nodesToSave[size] = node;
						  node.attributes.toSave = true;
			    	  }
			    	  
				      node.attributes.kpi = selectedRecord.get('name');
				      node.attributes.kpiId = selectedRecord.get('id');
				      Ext.fly(node.getUI().getIconEl() ).replaceClass('', 'has-kpi');
			      }
			      Ext.fly(this.getEl()).frame("ff0000");
			      return(true);
			    }
			  }, this);
	}
});
