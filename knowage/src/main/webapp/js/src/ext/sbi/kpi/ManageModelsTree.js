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

Sbi.kpi.ManageModelsTree = function(config, ref) { 
	

	var paramsList = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODEL_NODES_LIST_WITH_KPI"};
	this.configurationObject = {};
	
	this.configurationObject.manageTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODELS_ACTION'
		, baseParams: paramsList
	});	

	//reference to viewport container
	this.referencedCmp = ref;
	this.initConfigObject();
	
	var isDraggable = config.notDraggable;
	config.configurationObject = this.configurationObject;
	config.notDraggable = isDraggable;
	
	var c = Ext.apply({}, config || {}, {});
	Sbi.kpi.ManageModelsTree.superclass.constructor.call(this, c);	 	
};

Ext.extend(Sbi.kpi.ManageModelsTree, Sbi.widgets.TreeModelPanel, {
	
	configurationObject: null
	, gridForm:null
	, mainElementsStore:null
	, root:null
	, referencedCmp : null

	,initConfigObject: function(){

		this.configurationObject.treeTitle = LN('sbi.models.treeTitle');
		
		this.treeLoader = new Ext.tree.TreeLoader({
			dataUrl: this.configurationObject.manageTreeService,
	        createNode: function(attr) {


	            if (attr.modelId) {
	                attr.id = attr.modelId;
	            }

	    		if (attr.kpi !== undefined && attr.kpi != null
	    				&& attr.kpi != '') {
	    			attr.iconCls = 'has-kpi';
	    		}
	    		if (attr.error !== undefined && attr.error != false) {
	    			attr.cls = 'has-error';
	    		}
	            return Ext.tree.TreeLoader.prototype.createNode.call(this, attr);
	        }
		});
    }
	,renderTree : function(tree) {
		tree.getLoader().nodeParameter = 'modelId';
		tree.getRootNode().expand(false, /*no anim*/false);
	}

	,setListeners : function() {
		this.modelsTree.addListener('render', this.renderTree, this);
	
	}
	,createRootNodeByRec: function(rec) {
		var iconClass = '';
		var cssClass = '';
		if (rec.get('kpi') !== undefined && rec.get('kpi') != null
				&& rec.get('kpi') != '') {
			iconClass = 'has-kpi';
		}
		if (rec.get('error') !== undefined && rec.get('error') != false) {
			cssClass = 'has-error';
		}
		var node = new Ext.tree.AsyncTreeNode({
	        text		: this.rootNodeText,
	        leaf		: false,
			modelId 	: this.rootNodeId,
			id			: this.rootNodeId,
			label		: rec.get('label'),
			type		: rec.get('type'),
			typeId		: rec.get('typeId'),
			description	: rec.get('description'),
			typeDescr	: rec.get('typeDescr'),
			kpi			: rec.get('kpi'),
			kpiId		: rec.get('kpiId'),
			code		: rec.get('code'),
			name		: rec.get('name'),
			iconCls		: iconClass,
			cls			: cssClass,
	        draggable	: false
	    });
		return node;
	}
});
