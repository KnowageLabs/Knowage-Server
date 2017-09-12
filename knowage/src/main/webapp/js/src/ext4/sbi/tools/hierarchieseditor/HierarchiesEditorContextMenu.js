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
 * 
 *  @author
 *  Marco Cortella (marco.cortella@eng.it)
 */

Ext.define('Sbi.tools.hierarchieseditor.HierarchiesEditorContextMenu', {
	extend:'Ext.menu.Menu',
	itemId : 'idTreeContextMenu',
	items : [new Ext.Action({
	    text: LN('sbi.hierarchies.node.delete'),
	    handler: function(){
	    	var tree = Ext.getCmp('customTreePanel');
	    	var selectedNodes = tree.selModel.getSelection();
	    	for (var i=0; i < selectedNodes.length; i++){
		    	var selectedNode = tree.selModel.getSelection()[i];
		    	selectedNode.remove();
	    	}
	    	Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.hierarchies.node.deleted'));
	    },
	    iconCls: 'button-remove',
	    itemId: 'myAction'
	}),
	new Ext.Action({
	    text: LN('sbi.hierarchies.node.add'),
	    handler: function(){
	    	var tree = Ext.getCmp('customTreePanel');
	    	var selectedNode = tree.selModel.getSelection()[0];
	    	
	    	if (!selectedNode.isLeaf()){
				this.newNodeCode = new Ext.form.Text({
					name: 'code',
			        fieldLabel: LN('sbi.generic.code'),
			        labelWidth: 130,
					width : 300,
			        allowBlank: false,
			        enforceMaxLength: true,
			        maxLength: 45
				});
				
				this.newNodeName = new Ext.form.Text({
					name: 'name',
			        fieldLabel: LN('sbi.generic.name'),
			        labelWidth: 130,
					width : 300,
			        allowBlank: false,
			        enforceMaxLength: true,
			        maxLength: 45
				});
		    	
				var win = new Ext.Window(
					    {
					        layout: 'fit',
					        width: 400,
					        height: 200,
					        modal: true,
					        closeAction: 'destroy',
					        title:LN('sbi.hierarchies.node.add'),
					        items: new Ext.Panel(
					        {
								
								bodyStyle:'padding:20px',
					        	items: [this.newNodeCode,this.newNodeName]
					        }),
					        buttons:[
					                 {
					                	 text:'OK',
					                	 handler:function() {
					                		 var newNodeCode = this.newNodeCode.getValue(); //must be unique in the tree!
					                		 var newNodeName = this.newNodeName.getValue();
					                		 
					                		 selectedNode.appendChild({
						           	    	      text: newNodeName,
						           	    	      id: newNodeCode,
						           	    	      leaf: false,
						           	    	      children:[]
					                		 });  
					                		 
					                		 win.close();
					                	 }
					                 	 ,scope:this 
					                 },
					                 {
					                	 text:LN('sbi.general.cancel'),
					                	 handler:function() {
					                		 win.close();
					                	 }
					                 }
					      ]
					    });
				win.show();	
	    	} else {
				Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.hierarchies.node.add.leaf.error'), LN('sbi.generic.error'));

	    	}
	    },
	    iconCls: 'icon-add',
	    itemId: 'myActionAdd'
	  }),
	  new Ext.Action({
		    text: LN('sbi.hierarchies.node.edit'),
		    handler: function(){
		    	var tree = Ext.getCmp('customTreePanel');
		    	var selectedNode = tree.selModel.getSelection()[0];		    
		    	this.newNodeCode = new Ext.form.Text({
		    		name: 'code',
		    		fieldLabel: LN('sbi.generic.code'),
		    		labelWidth: 130,
		    		width : 350,
		    		allowBlank: false,
		    		enforceMaxLength: true,
		    		maxLength: 45,
		    		value:  selectedNode.get( "id" ), // selectedNode.raw.id || '' 
		    		hidden: selectedNode.isLeaf()
		    	});

		    	this.newNodeName = new Ext.form.Text({
		    		name: 'name',
		    		fieldLabel: LN('sbi.generic.name'),
		    		labelWidth: 130,
		    		width : 350,
		    		allowBlank: false,
		    		enforceMaxLength: true,
		    		maxLength: 45,
		    		value:  selectedNode.get( "text" )  // selectedNode.raw.text || ''
		    	});

		    	var win = new Ext.Window(
		    			{
		    				layout: 'fit',
		    				width: 400,
		    				height: 200,
		    				modal: true,
		    				closeAction: 'destroy',
		    				title:LN('sbi.hierarchies.node.edit'),
		    				items: new Ext.Panel(
		    						{

		    							bodyStyle:'padding:20px',
		    							items: [this.newNodeCode,this.newNodeName]
		    						}),
		    						buttons:[
		    						         { 
		    						        	 text:'OK',
		    						        	 handler:function() {
		    						        		 var newNodeCode = this.newNodeCode.getValue(); //must be unique in the tree!
		    						        		 var newNodeName = this.newNodeName.getValue();
		    						        		 selectedNode.set( "text", newNodeName );  	

		    						        		 if (!selectedNode.isLeaf()){
		    						        			 selectedNode.set( "id", newNodeCode ); 
		    						        			 //propagate new Id and Name of this node to the childrens leaves (as a Parent Node)
		    						        			 selectedNode.eachChild(function(n) {
		    						        				 if (n.isLeaf()){
		    						        					 n.set("parentId",selectedNode.get("id") );
		    						        					 n.set("leafParentCode",selectedNode.get("id") );
		    						        					 n.set("leafParentName",selectedNode.get("text") );
		    						        				 }

		    						        			 });						                			 
		    						        		 }


		    						        		 win.close();
		    						        	 }
		    						         ,scope:this 
		    						         },
		    						         {
		    						        	 text:LN('sbi.general.cancel'),
		    						        	 handler:function() {
		    						        		 win.close();
		    						        	 }
		    						         }
		    						         ]
		    			});
		    	win.show();	

		    },
		    iconCls: 'icon-edit',
		    itemId: 'myActionEdit'
		})
	]

});