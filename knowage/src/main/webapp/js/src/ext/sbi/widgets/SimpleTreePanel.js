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

Sbi.widgets.SimpleTreePanel = function(config) {
	var c = this.initWidget(config);	
	Sbi.widgets.SimpleTreePanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.widgets.SimpleTreePanel, Ext.tree.TreePanel, {

	rootNode : null,
	rootNodeId : null,
	preloadTree : true,
	rootNodeText : null,
	treeTitle : null,
	treeLoader: null,

	initWidget : function(config) {

		if(config.treeTitle!=null && config.treeTitle!=undefined){
			this.treeTitle = config.treeTitle;
		}
		
		this.rootNodeText = config.rootNodeText;
		this.rootNodeId = config.rootNodeId;
		
		var conf = new Ext.tree.TreePanel( {
			title : this.treeTitle,
			autoWidth : true,
			height : 300,
			layout: 'fit',
			userArrows : true,
			animate : true,
			autoScroll : true,		
            style: {
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
				id:  this.rootNodeId
			}
		});
		return Ext.apply(conf,config);
	}

});

