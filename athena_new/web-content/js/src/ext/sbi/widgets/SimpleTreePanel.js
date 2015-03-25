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

