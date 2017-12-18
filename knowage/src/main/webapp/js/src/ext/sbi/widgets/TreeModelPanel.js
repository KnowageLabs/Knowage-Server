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

Ext.ns("Sbi.widgets");

Sbi.widgets.TreeModelPanel = function(config) {

	var conf = config.configurationObject;
	this.services = new Array();
	this.services['listModelService'] = conf.manageTreeService;
	
	this.tabItems = conf.tabItems;
	this.notDraggable =config.notDraggable;
	//alert(this.notDraggable);
	this.treeTitle = conf.treeTitle;

	this.initWidget();	

	var c = Ext.apply( {}, config, this.modelPanel);

	Sbi.widgets.TreeModelPanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.widgets.TreeModelPanel, Ext.Panel, {

	gridForm : null,
	tabs : null,
	tabItems : null,
	treeLoader : null,
	rootNode : null,
	rootNodeId : null,
	preloadTree : true,
	rootNodeText : null,
	treeTitle : null,
	importCheck: null,
	treeLoader: null,
	

	initWidget : function() {

		this.modelsTree = new Ext.tree.TreePanel( {
			title : this.treeTitle,
			autoWidth : true,
			border: false,
			layout: 'fit',
			userArrows : true,
			animate : true,
			autoScroll : true,		
            style: {
                //"background-color": "#f1f1f1",
                "border":"none"
            },
			loader: this.treeLoader,

			preloadTree : this.preloadTree,
			enableDD : true,
            enableDrop: false,
            enableDrag: true,
            ddAppendOnly: false ,
            ddGroup  : 'tree2tree',
			scope : this,
			shadow : true,
			root : {
				nodeType : 'async',
				text : this.rootNodeText,
				modelId : this.rootNodeId,
				id:  this.rootNodeId
			}
		  // ,listeners:{  }
		});
		var label = LN('sbi.modelinstances.importCheck');
		if(this.notDraggable !== undefined && this.notDraggable !== null && this.notDraggable == true){
			label ='';
		}
		this.importCheck = new Ext.form.Checkbox({
             fieldLabel: label,
             allowBlank: false,
         	 inputValue  :'true',
         	 labelStyle: 'font-weight:bold; color: #3D8635;',
             name: 'importChildrenFlag'
         });
		this.checkboxPanel = new Ext.form.FormPanel( {
			labelWidth: 150,  
			labelAlign : 'left',
			height : 30,
			layoutConfig : {
				animate : true,
				activeOnTop : false
			},
			border: false,
			trackResetOnLoad : true,
			items: [this.importCheck]
		});
		
		this.modelPanel = new Ext.Panel( {
			autoScroll : true,
			labelAlign : 'left',
			items: [this.checkboxPanel,this.modelsTree ]
		});
		
		if(this.notDraggable !== undefined && this.notDraggable !== null && this.notDraggable == true){
			this.importCheck.hide();
			this.modelsTree.enableDD = false;
		}
		this.setListeners();

	}

});

