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

Sbi.kpi.ManageGoalsOUTree = function(config, ref) { 
	
	this.addEvents();
	var paramsOUChildList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "OU_CHILDS_LIST"};
	this.configurationObject = {};

	this.configurationObject.manageTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_OUS_ACTION'
			, baseParams: paramsOUChildList
	});	

	//reference to viewport container
	this.referencedCmp = ref;
	this.initConfigObject();
	
	config.configurationObject = this.configurationObject;
	
	var c = Ext.apply({}, config || {}, {});
	Sbi.kpi.ManageGoalsOUTree.superclass.constructor.call(this, c);	 	
	this.on('render', function(){this.getRootNode().expand(false, /*no anim*/false);}, this)
};

Ext.extend(Sbi.kpi.ManageGoalsOUTree, Sbi.widgets.SimpleTreePanel, {
	
	configurationObject: null
	, gridForm:null
	, mainElementsStore:null
	, root:null
	, referencedCmp : null

	,initConfigObject: function(){

		var thisPanel = this;
		this.treeLoader=new Ext.tree.TreeLoader({
			nodeParameter: 'nodeId',
			dataUrl: thisPanel.configurationObject.manageTreeService,
			createNode: function(attr) {
				
				if (attr.nodeId) {
					attr.id = attr.nodeId;
				}
				if (attr.ou!=null) {
					attr.text = attr.ou.name;
					attr.qtip = attr.ou.name;
				}
				var node = Ext.tree.TreeLoader.prototype.createNode.call(this, attr);
				return node;
			}
		}); 
    }


});
