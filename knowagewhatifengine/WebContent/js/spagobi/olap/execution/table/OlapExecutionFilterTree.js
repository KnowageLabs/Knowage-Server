/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Panel with tree filter
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.execution.table.OlapExecutionFilterTree', {
	extend: 'Ext.window.Window',

	layout: 'fit',

	config:{
    	/**
    	 * @cfg {Sbi.olap.DimensionModel} dimension
    	 * The dimension linked to the filter
    	 */
		dimension: null,

    	/**
    	 * @cfg {Sbi.olap.MemberModel} selectedMember
    	 * The value of the filter
    	 */
		selectedMember: null,

    	/**
    	 * @cfg {boolean} multiSelection
    	 * allows the multiselection. default to false
    	 */
		multiSelection: false,

		title: '',
		height: 500,
		width: 400
	},

	tree: null,


	constructor : function(config) {
		this.initConfig(config);



		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.table && Sbi.settings.olap.execution.table.OlapExecutionFilterTree) {
			Ext.apply(this, Sbi.settings.olap.execution.OlapExecutionFilterTree);
		}

		var service = Ext.create("Sbi.service.RestService",{
			url: "hierarchy",
			pathParams: [this.dimension.get("selectedHierarchyUniqueName"), "filtertree", this.dimension.get("axis")]
		});

		var treeStore =  new Ext.data.TreeStore({
			proxy: {
				type: 'ajax',
				idProperty : 'uniqueName',
				url: service.getRestUrlWithParameters(),
				extraParams: service.getRequestParams()
			},
			root: {
				name: 'member',
				id: 'root',
				expanded: true
			}
		});

		//Initialize the filter
		this.tree = Ext.create("Ext.tree.Panel",{
			title: this.dimension.raw.caption,
			rootVisible: false,
			store:treeStore
		});

		if(this.multiSelection){
			treeStore.on("beforeappend",function( store, node, eOpts ){
				node.raw.checked = (node.raw.visible!=null && node.raw.visible!=undefined && (node.raw.visible =="true" || node.raw.visible ==true) );
				node.data.checked = node.raw.checked;
			},this);
		}


		this.callParent(arguments);

		//select the selected node
		this.tree.on("render",this.initializeMemberSelection,this);
	},


	initComponent: function() {
		var thisPanel = this;
		Ext.apply(this,{
			items:[this.tree],
			tools: [
			       {
			    	   type: 'expand',
			    	   tooltip: LN('sbi.olap.execution.table.filter.expand'),
			    	   handler: function(){
			    		   thisPanel.tree.expandAll();
			    	   }
			       }, {
			    	   type: 'collapse',
			    	   tooltip: LN('sbi.olap.execution.table.filter.collapse'),
			    	   handler: function(){
			    		   thisPanel.tree.collapseAll();
			    	   }

			       }],
			       bbar:[
			             '->',    {
			            	 text: LN('sbi.common.cancel'),
			            	 handler: function(){
			            		 thisPanel.destroy();
			            	 }
			             },    {
			            	 text: LN('sbi.common.select'),
			            	 handler: function(){
			            		 var selected = null;
			            		 if(thisPanel.multiSelection){
			            			 selected = thisPanel.tree.getChecked( );
			            		 }else{
			            			 selected = thisPanel.tree.getSelectionModel( ).getSelection();
			            		 }


			            		 thisPanel.fireEvent("select",thisPanel.getMembersData(selected));
			            		 thisPanel.destroy();
			            	 }
			             }]
		});
		this.callParent();
	},

    /**
     * @private
     * It looks for the this.selectedMember in the tree.
     * It expands the nodes from the root to the this.selectedMember node and selects it
     */
	initializeMemberSelection: function(){
		if(this.selectedMember){
			var root = this.tree.getRootNode();
			root.on("expand",this.expandNode, this);
			root.expand();

		}
	},

    /**
     * @private
     * It looks for the this.selectedMember in the subtree rooted in the passed node.
     * It expands the nodes from the root to the this.selectedMember node and selects it
     * @param node{Ext.data.NodeInterface} the root node of the subtree
     */
	expandNode: function(node){
		node.un("expand",this.expandNode, this);
		var children = node.childNodes;

		var memberId = this.selectedMember.raw.uniqueName;
		var nodeId = node.internalId;

		if(memberId  == nodeId){
			this.tree.getSelectionModel().select(node);
			return;
		}

		if(nodeId =="root"){
			children[0].on("expand",this.expandNode, this);
			children[0].expand();
		}else if(children){
			for(var i=0; i<children.length; i++){
				var childId = children[i].internalId;
				if(memberId  == childId){
					this.tree.getSelectionModel().select(children[i]);
					return;
				}
				if(memberId.indexOf(childId) ==0){
					children[i].on("expand",this.expandNode, this);
					children[i].expand();
					break;
				}
			}
		}
	},


	getMembersData: function(members){
		var toReturn = new Array();
		if(members){
			for(var i=0; i<members.length; i++){
				toReturn.push(members[i].raw);
			}
		}
		return toReturn;
	}

});







