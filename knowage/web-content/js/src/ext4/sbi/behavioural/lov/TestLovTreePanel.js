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
 * 
 * Public Properties
 * 
 * 
 * Public Methods
 * 
 *  [list]
 * 
 * 
 * Public Events
 * 
 *  [list]
 * 
 * Authors
 * 
 * - Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.behavioural.lov.TestLovTreePanel', {
    extend: 'Ext.tree.Panel'

    ,config: {
      	stripeRows: true,
        useArrows: true,
        rootVisible: false,
        multiSelect: true,
        singleExpand: true,
        serializedTree: {}
    }

    , constructor: function(config) {
		this.title =  "Tree LOV definition";

     	
    	Ext.apply(this,config||{});
    	var thisPanel = this;
        this.viewConfig = {
            plugins: {
                ptype: 'treeviewdragdrop',
                containerScroll: true
            },                
			listeners: {
				drop: function(a, b, c,d){//move the node in the right position
					thisPanel.moveTreeNode(b.records[0].data.column, d);
                },
				beforedrop: function( node, data, overModel, dropPosition, dropFunction, eOpts ){
					thisPanel.beforeMoveTreeNode(data.records[0]);
                }
            }
        };
        
        this.columns = [{
            xtype: 'treecolumn', 
            text: 'Level',
            flex: 2,
            dataIndex: 'value',
            tdCls  : 'lov-tree-column-node' //we use class to understand the target position of the drop
        },{
            text: 'Value',
            flex: 1,
            dataIndex: 'value',
            tdCls  : 'lov-tree-column-value'
        
        },{
            text: 'Description',
            flex: 1,
            dataIndex: 'description',
            tdCls  : 'lov-tree-column-description'
        }];
        
    	
        Ext.define('TreeLov', {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'value',     type: 'string'},
                {name: 'description', type: 'string'}
            ]
        });

        //empty tree
        var store = Ext.create('Ext.data.TreeStore', {
            model: 'TreeLov',
            root:{"text":"root","children": []}
        });
        
        
        this.store = store;
        
        this.callParent(arguments);
        
        var treePanel = this;
        this.initContextMenu();
        this.on('itemcontextmenu', this.onContextMenu, this);
        
        this.on("render",function(){
            var formPanelDropTarget = Ext.create('Ext.dd.DropTarget', this.el, {
                ddGroup: 'GridLovDD',
                notifyEnter: function(ddSource, e, data ){
                	treePanel.body.stopAnimation();
                	treePanel.body.highlight();
                },
                notifyDrop  : function(ddSource, e, data, c){
                	var node=null;
                	//Get the targhet positin to understand if the record is dropped over the value or description
        			var target = e.getTarget();
        			var targetType = treePanel.getTargetPosition(target);
        			
        			node = treePanel.findTreeNode(treePanel.getStore().getRootNode(), 'value', data.records[0].data.name);//if the node already exist
        			
        			//If the node does not exist in the tree we add it
        			if(((target.id.indexOf('treeview')>=0) || targetType=='node' )){
        				if(node==null){
        					treePanel.addTreeNode(data.records[0].data);//add the node	
        				}
        			}else{//update the existing node
        				//find the existing node with the same value or description and update it
        				var targetNode = treePanel.findTreeNode(treePanel.getStore().getRootNode(), targetType, target.innerText);
        				treePanel.updateTreeNode(target, data.records[0].data.name, targetNode);
        			}

                    return true;
                }
            });
            
            this.setValues(config.lovConfig);
        
        
        },this);
    }
    
    //Add the node in the tree as leaf
    , addTreeNode: function(nodeConfig){
		var store = this.getStore();
		var root = store.getRootNode();
		var node = root;
		while (node.childNodes !=null && node.childNodes!=undefined && node.childNodes.length>0){
			node = node.childNodes[0];
		}
		node.set('leaf', false);
		node.set('expanded', true);	

		var description = nodeConfig.name;
		if(nodeConfig.description){
			description = 	nodeConfig.description;
		}
		
		node.appendChild({         
			value: nodeConfig.name,
			description: description
		});	

		this.normalizeTree();
		this.getView().refresh();   
		
	}
    
    //Update the node "node" setting as "name" the property position("target") (target position can be value or description)
    , updateTreeNode: function(target, name, node){
    	var position = this.getTargetPosition(target);
    	if(position!=null){
    		//if the node is not a leaf you can not set the description
    		if(position!='description' || (node.childNodes && node.childNodes.length==0) ){
    			var store = this.getStore();
    			var root = store.getRootNode();
    		 	if(position=='value'){
    		 		var existingNode = this.findTreeNode(root, "value", name);
    		 		if(existingNode){
    		 			return;
    		 		}
    		 		node.set('description', name);
    		 	}
    		 	node.set(position, name);
    		 	this.getView().refresh();   
    		}
    	}
	}
    
    //Understand if the target is a node, value or description column
    , getTargetPosition: function(target){
    	if(target!= null && target!= undefined && target.parentNode!= null && target.parentNode!= undefined && target.parentNode.className!= null && target.parentNode.className!= undefined){
    		if(target.parentNode.className.indexOf('lov-tree-column-description')>=0){
    			return 'description';
    		}else if(target.parentNode.className.indexOf('lov-tree-column-value')>=0){
    			return 'value';
    		}else if(target.parentNode.className.indexOf('lov-tree-column-node')>=0){
    			return 'node';
    		}
    	}
    	return null;
    }
    
    //When the node "node" is moved, we should take all its children and set them as children of
    // the father of "node". Example
    // root
    //   child1
    //      child2
    //         node
    //            child3
    // If we move node up, we should set child3 as child of child2:
    // root
    //   node
    //      child1
    //         child2
    //            child3
    , beforeMoveTreeNode: function(node){
		
		if(node.childNodes !=null && node.childNodes!=undefined && node.childNodes.length>0){
			var parentNode = node.parentNode;
			var child = node.childNodes[0];
			node.removeChild(child);
			parentNode.removeChild(node);
			parentNode.appendChild(child);
			parentNode.set('leaf', false);	
		}
		
		return true;
	}
    
    //adjust the tree after the node is moved.
    // after the node has been moved the parent node will have 2 children (the previous child and the moved one).
    // This method should keep the 1 parent 1 child structure
    ,  moveTreeNode: function(nodeSrcName, event){
		var store = this.getStore();
		var root = store.getRootNode();
		var node = root;
		while (node!=null){//find the parent node
			if(node.childNodes !=null && node.childNodes!=undefined && node.childNodes.length==2){
				var oldChild;
				var newChild;
				if(event=="append"){ // if the event is append (+ icon in the ddproxy)
					oldChild = node.childNodes[0];
					newChild = node.childNodes[1];
				}else{
					oldChild = node.childNodes[1];
					newChild = node.childNodes[0];				
				}
				newChild.set('leaf', false);	
				node.removeChild(oldChild);
				newChild.appendChild(oldChild);	
				this.normalizeTree();
				this.getView().refresh();   
				return true;
			}else if(node.childNodes !=null && node.childNodes!=undefined && node.childNodes.length>0){
				node = node.childNodes[0];
			}else{
				return false;
			}
		}
	}

    //Finds the node with "value" as value for the property "type" in the subtree of "node"
    ,  findTreeNode: function(node,type, value){
    	if(node.data[type] == value || (type == 'node' && node.data[value] == value )){//if the property is "node" we look in the property "value
			return node;
		}else{
			if(node.childNodes !=null && node.childNodes!=undefined && node.childNodes.length>0){
				return this.findTreeNode(node.childNodes[0], type, value);
			}else{
				return null;
			}
		}
	}

    , getValues: function(){
    	this.serializedTree ={};
    	this.serializedTree.treeLevelsColumns=[];
    	this.serializedTree.lovType = this.lovType;
    	var node = this.store.getRootNode();
    	if(node.childNodes!=null && node.childNodes!=undefined && node.childNodes.length>0){
    		this.serializeSubTree(node.childNodes[0]);
    	}else{
    		alert("Tree not defined");
    		return null;
    	}
    	this.serializedTree.visibleColumnNames = this.column;
    	this.serializedTree.column = this.column;
    	
    	return this.serializedTree;
    }
    
    ,serializeSubTree: function(node){
    	this.serializedTree.treeLevelsColumns.push(node.data.value);
    	if(node.childNodes !=null && node.childNodes!=undefined && node.childNodes.length>0){
    		this.serializeSubTree(node.childNodes[0]);
    	}else{
    		this.serializedTree.descriptionColumnName = node.data.description;
    		this.serializedTree.valueColumnName = node.data.value;
    	}
    }
    
    , setValues: function(config){
    	var treeColumnNames = config.treeColumnNames;
    	var valueColumnName = config.valueColumnName;
    	var descriptionColumnName = config.descriptionColumnName;
    	
    	if(treeColumnNames!=null && treeColumnNames.length>0){
    		for(var i=0; i<treeColumnNames.length-1; i++){
    			this.addTreeNode({name: treeColumnNames[i]});
    		}
    		this.addTreeNode({name: valueColumnName, description: descriptionColumnName });    	
    	}
    }
    
    //set the the description equal to the value
    , normalizeTree: function(){
		var store = this.getStore();
		var root = store.getRootNode();
		var node = root; 
		while (node.childNodes !=null && node.childNodes!=undefined && node.childNodes.length>0){
			node.set('description', node.data.value);
			node = node.childNodes[0];
		}
	}
    
    //updates the visible columns (all columns)
	,onParentStroreLoad: function(store){
		var fields = this.parentStore.proxy.reader.jsonData.metaData.fields;
		if(fields!=null && fields!=undefined && fields.length>0){
			var column = [];
			for(var i=0; i<fields.length; i++){
				column.push(fields[i].name);
			}
			
			this.column = column;
		}
	}
	
	
	, initContextMenu : function() {

		this.menu = new Ext.menu.Menu( {
			items : [
					{
						text : LN('sbi.behavioural.delete'),
						iconCls : 'delete-icon',
						handler : function() {
							this.removeNode(this.ctxNode);
						},
						scope : this
					} ]
		});

	}
	
	, removeNode : function(node) {
		if(node.childNodes){
			for(var i=0; i<node.childNodes.length; i++){
				node.parentNode.appendChild(node.childNodes[i]);
			}
		}
		node.parentNode.removeChild(node, true);
	}

	,onContextMenu : function(node, record, item, index, e, eOpts ) {
		if (this.menu == null) { // create context menu on first right click
			this.initContextMenu();
		}
		
		if (this.ctxNode) {
			this.ctxNode = null;
		}
		
		this.ctxNode = record;
		e.stopEvent();
		this.menu.showAt(e.getXY());
        return false;
		
	
	}
	
});
