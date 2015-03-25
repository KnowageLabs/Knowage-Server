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

Sbi.kpi.ManageModels = function(config, ref) { 
	var paramsList = {MESSAGE_DET: "MODEL_NODES_LIST"};
	var paramsSave = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODEL_NODES_SAVE"};
	var paramsDel = {LIGHT_NAVIGATOR_DISABLED: 'TRUE',MESSAGE_DET: "MODEL_NODE_DELETE"};
	
	this.configurationObject = {};
	
	this.configurationObject.manageTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODELS_ACTION'
		, baseParams: paramsList
	});	
	this.configurationObject.saveTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODELS_ACTION'
		, baseParams: paramsSave
	});
	this.configurationObject.deleteTreeService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_MODELS_ACTION'
		, baseParams: paramsDel
	});
	//reference to viewport container
	this.referencedCmp = ref;
	this.initConfigObject();
	config.configurationObject = this.configurationObject;

	var c = Ext.apply({}, config || {}, {});

	Sbi.kpi.ManageModels.superclass.constructor.call(this, c);	 	
	  
	 this.mainTree.on('beforenodedrop', function(e){
		 /* * tree - The TreePanel
		    * target - The node being targeted for the drop
		    * data - The drag data from the drag source
		    * point - The point of the drop - append, above or below
		    * source - The drag source
		    * rawEvent - Raw mouse event
		    * dropNode - Drop node(s) provided by the source OR you can supply node(s) to be inserted by setting them on this object.
		    * cancel - Set this to true to cancel the drop.
		    * dropStatus - If the default drop action is cancelled but the drop is valid, setting this to true will prevent the animated 'repair' from appearing.
		*/

		   e.target.allowChildren = true;
		   if(e.target.attributes.leaf){
			   e.target.leaf = false;
			   e.target.allowChildren = true;
			   e.target.allowDrop = true;
			   e.point = 'append';
			   e.target.expand();
			   Ext.fly(e.target.getUI().getEl()).repaint();
			   
		   }

	 }, this);
	 
	 this.mainTree.on('nodedrop', function(e, newNode){
		 //e.tree.getSelectionModel().select();
		 //this.selectNode(null);
		 for(i =0 ; i< e.dropNode.length; i++){
			 //alert(e.dropNode[i].text);	
				var size = this.nodesToSave.length;
				this.nodesToSave[size] = e.dropNode[i];
		 }

	 }, this);

	 this.doLayout();
};

Ext.extend(Sbi.kpi.ManageModels, Sbi.widgets.TreeDetailForm, {
	
	configurationObject: null
	, gridForm:null
	, mainElementsStore:null
	, root:null
	, referencedCmp : null
	, treeLoader : null
	, nodesLabels : new Array()
	, initContextMenu : function() {

		this.menu = new Ext.menu.Menu( {
			items : [
			// ACID operations on nodes
					'-', {
						text : LN('sbi.models.addNode'),
						iconCls : 'icon-add',
						handler : function() {
							this.addNewItem(this.ctxNode);
						},
						scope : this
					}, {
						text : LN('sbi.models.remodeNode'),
						iconCls : 'icon-remove',
						handler : function() {
							this.deleteItem(this.ctxNode);
						},
						scope : this
					} ]
		});

	}

	,initConfigObject: function(){

		this.configurationObject.treeTitle = LN('sbi.models.treeTitle');;
	
		this.configurationObject.panelTitle = LN('sbi.models.panelTitle');
		this.configurationObject.listTitle = LN('sbi.models.listTitle');
		this.configurationObject.dragndropGroup ='treeAutoDD';
		
		this.initTabItems();
    }

	,initTabItems: function(){
		
		this.kpitreeLoader =new Ext.tree.TreeLoader({
			dataUrl: this.configurationObject.manageTreeService,
			scope: this,
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
	    		
	            var node =Ext.tree.TreeLoader.prototype.createNode.call(this, attr);
	            return node;
	        }

		});
		
		//Store of the combobox
 	    this.typesStore = new Ext.data.SimpleStore({
 	        fields: ['typeId', 'typeCd', 'typeDs', 'domainCd'],
 	        data: config.nodeTypesCd,
 	        autoLoad: false
 	    });
		/*DETAIL FIELDS*/

	 	   this.detailFieldName = new Ext.form.TextField({
	             fieldLabel: LN('sbi.generic.name'),
	             enableKeyEvents: true,
	             name: 'name'
	         });
	 			  
	 	   this.detailFieldCode = new Ext.form.TextField({
	             fieldLabel:LN('sbi.generic.code'),
	             enableKeyEvents: true,
	             name: 'code'
	         });  
	 		   
	 	   this.detailFieldDescr = new Ext.form.TextArea({
	          	 maxLength:400,
	       	     width : 250,
	             height : 80,
	             enableKeyEvents: true,
	             fieldLabel: LN('sbi.generic.descr'),
	             name: 'description'
	         });
	 	 		   
	 	   this.detailFieldLabel = new Ext.form.TextField({
	             fieldLabel:LN('sbi.generic.label'),
	             enableKeyEvents: true,
	             name: 'label'
	         });	  
	 	 	 			  
	 	   this.detailFieldKpi = new Ext.form.TextField({
	 		   	 itemId: 'model-detailFieldKpi',
	 		   	 columnWidth: .75,
	 		   	 id: 'model-detailFieldKpi',
	             fieldLabel: LN('sbi.generic.kpi'),
	             readOnly: true,
	             enableKeyEvents: true,
	             width: 30,
	             style: '{ color: #74B75C; border: 1px solid #74B75C; font-style: italic;}',
	             name: 'kpi'
	         });	 
	 	   this.detailFieldKpi.addListener('focus', this.kpiFiledNotify, this);  
	 	   
	 	    this.kpiClearBtn = new Ext.Button({
 	    		iconCls: 'icon-clear'
				, tooltip: LN('sbi.generic.deleteKpi')					
 	    		, style: '{border:none; width: 30px; border:none; margin-left: 5px;}'
				, scope: this
				, handler: this.clearKpi
				, columnWidth: .25
			});
	 	    
			this.kpiPanel = new Ext.Panel({
				fieldLabel:LN('sbi.generic.kpi'),
	            layout : 'column',
				labelWidth: 90,
	            defaults: {width: 140, border:false},   
				items: [this.detailFieldKpi,
				        this.kpiClearBtn],
				width: 30
			});
	 	   
	 	   this.detailFieldNodeType =  new Ext.form.ComboBox({
	        	  name: 'typeCd',
	              store: this.typesStore,
	              fieldLabel: LN('sbi.generic.nodetype'),
	              displayField: 'typeCd',   // what the user sees in the popup
	              valueField: 'typeId',        // what is passed to the 'change' event
	              typeAhead: true,
	              forceSelection: true,
	              mode: 'local',
	              triggerAction: 'all',
	              selectOnFocus: true,
	              editable: false,
	              allowBlank: false
	              //,validationEvent:true
	          });
	 	  this.detailFieldTypeDescr = new Ext.form.DisplayField({
	          	 maxLength:400,
	       	     width : 250,
	             height : 80,
	             readOnly: true,
	             name: 'typeDescr'
	         });
	 	   /*END*/
	 	  
	 	 this.udpValueGrid = new Sbi.kpi.ManageUdpValues(config);
	 	 this.udpValueGrid.setSource(config.udpEmptyList); 
	 	 this.udpValueGrid.identifica='identifica';
	 	  
	 	  this.configurationObject.tabItems = [{
		        title: LN('sbi.generic.details')
		        , itemId: 'detail'
		        , width: 430
		        , items: [{
			   		 id: 'items-detail-models',   	
		 		   	 itemId: 'items-detail1',   	              
		             xtype: 'fieldset',
		             labelWidth: 90,
		             defaults: {width: 140, border:false},    
		             bodyStyle: Ext.isIE ? 'padding:15 0 5px 10px;' : 'padding:10px 15px;',
		             defaultType: 'textfield',
		             autoHeight: true,
		             autoScroll  : true,
		             border: false,
		             items: [this.detailFieldLabel, this.detailFieldCode, this.detailFieldName,  this.detailFieldDescr,
		                     this.kpiPanel, this.detailFieldNodeType, this.detailFieldTypeDescr]
		    	}]
		    },
		    {  	title: LN('sbi.generic.udpValues')
		        , itemId: 'upd-values'
		        , width: 350
		        , deferredRender : false
		        , items: this.udpValueGrid  
		        , layout: 'fit'
		        , autoScroll  : true   	
		    } ];


	}
	, clearKpi: function() {
		this.detailFieldKpi.setValue('');
		var node = this.mainTree.getSelectionModel().getSelectedNode() ;
		if(node !== undefined && node != null){
			node.attributes.kpi = '';
			node.attributes.kpiId = '';
			node.attributes.iconCls = '';
			Ext.fly(node.getUI().getIconEl() ).replaceClass('has-kpi', '');

		}

	}
	, kpiFiledNotify : function() {
		this.detailFieldKpi.getEl().highlight('#E27119');
		var node = this.mainTree.getSelectionModel().getSelectedNode() ;
		var tooltip = new Ext.ToolTip({
	        target: 'model-detailFieldKpi',
	        anchor: 'right',
	        trackMouse: true,
	        html: LN('sbi.models.DDKpiMsg')
	    });

	}
	
	, editNodeUdpValues: function(source, recordId, value, oldValue) {

		//alert('source ='+source.toSource()+' recordId ='+recordId+' value='+value+ ' oldValue='+oldValue);
		if( this.selectedNodeToEdit === undefined ||  this.selectedNodeToEdit === null){
			this.selectedNodeToEdit = this.mainTree.getSelectionModel().getSelectedNode();
		}
		var node = this.selectedNodeToEdit;
		if (node !== undefined && node !== null) {
			node.attributes.toSave = true;
			//get the array of all attributes (would be better to change only current one but recordId is not very useful
			var arrayUdps = this.udpValueGrid.saveUdpValues('MODEL');
			node.attributes.udpValues = arrayUdps;
		}
	}
	, filterAttributes: function(node){
	    var attributeFilter = ["id", "modelId", "parentId", "code", "description", "label", "name", "type", "typeId", "typeDescr", "kpiId", "udpValues", "toSave"];
	    var c = false;
	    var result = "{";
	    for(var key in node.attributes) {
	        if (attributeFilter.indexOf(key) != -1) {
		        if (c) { 
		        	result += ','; 
		        }
		        //alert(node.attributes[key]+" of type "+typeof node.attributes[key]);
		        if(key == 'udpValues'){
		        	result += '"' + key + '":' + Ext.encode(node.attributes[key]);
		        }else{
		        	result += '"' + key + '":"' + node.attributes[key] + '"';
		        }
		        c = true;
		    }

	    }
	    var children = node.childNodes;
	    if(children !== undefined && children!= null){
		    var clen = children.length;
		    if(clen != 0){
		        if (c) {result += ',';}
		        result += '"children":[';
		        for(var i = 0; i < clen; i++){
		            if (i > 0) {result += ',';}
		            result += this.filterAttributes(children[i]);
		        }
		        result += ']';
		    }
	    }
	    return result + "}";
	}
    //OVERRIDING save method
	,save : function() {
    	var jsonStr = '[';

		Ext.each(this.nodesToSave, function(node, index) {
			if(node instanceof Ext.tree.TreeNode){
				jsonStr += this.filterAttributes(node);
				if(this.nodesToSave != undefined &&  index !== this.nodesToSave.length-1){
					jsonStr +=',';
				}

			}
		}, this);

		
		jsonStr += ']';
		
		var params = {
			nodes : jsonStr
		};

		Ext.Ajax.request( {
			url : this.services['saveTreeService'],
			success : function(response, options) {
				if(response.responseText !== undefined ) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if(content !== undefined && content !== null){
	      				var hasErrors = false;
	      				//alert(content.toSource());
	      				for (var key in content) {
		      				var value = content[key];
		      				var nodeSel = this.mainTree.getNodeById(key);
		      				//alert(key+' '+value+' w/ label :'+content['label']);
		      				//response returns key = guiid, value = 'KO' if operation fails, or modelId if operation succeded
		      				if(nodeSel !== undefined && nodeSel != null){
		      				  if(value  == 'KO'){
		      					  hasErrors= true;
		 		      			  ///contains error gui ids      						  
	      						  nodeSel.attributes.error = true;
	      						  Ext.fly(nodeSel.getUI().getEl()).applyStyles('{ border: 1px solid red; font-weight: bold; font-style: italic; color: #cd2020; text-decoration: underline; }');
		      				  }else{
		      					  nodeSel.attributes.error = false; 
		      					  nodeSel.attributes.modelId = value; 
		      					  nodeSel.attributes.label = content[value];
		      					  this.detailFieldLabel.setValue(content[value]);
		      					  this.detailFieldLabel.show();
		      					  Ext.fly(nodeSel.getUI().getEl()).applyStyles('{ border: 0; font-weight: normal; font-style: normal; text-decoration: none; }');
		      					  this.fireEvent('parentsave-complete', nodeSel);
		      				  }
		      				}
		      		    }
	      				
	      				if(hasErrors){
	      					alert(LN('sbi.generic.savingItemError'));
	      					
	      				}else{
	      					///success no errors!
	      					this.cleanAllUnsavedNodes();
	      					alert(LN('sbi.generic.resultMsg'));
		      				this.referencedCmp.modelsGrid.mainElementsStore.load();
	      				}
	      			}else{
	      				alert(LN('sbi.generic.savingItemError'));
	      			}
				}else{
      				this.cleanAllUnsavedNodes();
      				alert(LN('sbi.generic.resultMsg'));
      				this.referencedCmp.modelsGrid.mainElementsStore.load();
				}
      			this.mainTree.doLayout();
      			this.referencedCmp.modelsGrid.getView().refresh();
				this.referencedCmp.modelsGrid.doLayout();
				
				
				
      			return;
			},
			scope : this,
			failure : function(response) {
				if(response.responseText !== undefined) {
					alert(LN('sbi.generic.savingItemError'));
				}
			},
			params : params
		});
		
    }
	,saveParentNode : function(parent, child) {
		var jsonStr = '[';
    	jsonStr +=  Ext.util.JSON.encode(parent.attributes)
    	jsonStr += ']';

		var params = {
			nodes : jsonStr
		};

		Ext.Ajax.request( {
			url : this.services['saveTreeService'],
			success : function(response, options) {
				if(response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );
	      			if(content !== undefined && content !== null){
	      				var hasErrors = false;
	      				for (var key in content) {
		      				  var value = content[key];
		      				  var nodeSel = this.mainTree.getNodeById(key);
		      				  //response returns key = guiid, value = 'KO' if operation fails, or modelId if operation succeded
		      				if(nodeSel !== undefined && nodeSel != null){
		      				  if(value  == 'KO'){
		      					  hasErrors= true;
		 		      			  ///contains error gui ids      						  
	      						  nodeSel.attributes.error = true;
	      						  Ext.fly(nodeSel.getUI().getEl()).applyStyles('{ border: 1px solid red; font-weight: bold; font-style: italic; color: #cd2020; text-decoration: underline; }');
		      				  }else{
		      					  nodeSel.attributes.error = false; 
		      					  nodeSel.attributes.modelId = value; 
		      					  
		      					  nodeSel.attributes.label = content['label'];
			      					this.detailFieldLabel.setValue(content['label']);
			      					this.detailFieldLabel.show();
			      					
		      					  Ext.fly(nodeSel.getUI().getEl()).applyStyles('{ border: 0; font-weight: normal; font-style: normal; text-decoration: none; }');
		      					  
		      					  //completes child node instanciation
			  	        		  this.selectedNodeToEdit = child;
				        		  //this.mainTree.getSelectionModel().select(child);

			  	        		  child.attributes.parentId = parent.attributes.modelId;
			  	        		  this.selectNode(null);

	      				      }
		      				}
		      		    }
	      				
	      				if(hasErrors){
	      					alert(LN('sbi.generic.savingItemError'));
	      					
	      				}else{
	      					///success no errors!
	      					this.cleanAllUnsavedNodes();
	      					alert(LN('sbi.generic.resultMsg'));
		      				this.referencedCmp.modelsGrid.mainElementsStore.load();
	      				}
	      			}else{
	      				alert(LN('sbi.generic.savingItemError'));
	      			}
				}else{
      				this.cleanAllUnsavedNodes();
      				alert(LN('sbi.generic.resultMsg'));
      				this.referencedCmp.modelsGrid.mainElementsStore.load();
				}
      			this.mainTree.doLayout();
      			this.referencedCmp.modelsGrid.getView().refresh();
				this.referencedCmp.modelsGrid.doLayout();
				
				
				
      			return;
			},
			scope : this,
			failure : function(response) {
				if(response.responseText !== undefined) {
					alert(LN('sbi.generic.savingItemError'));
				}
			},
			params : params
		});
		
    }
	,editNode : function(field, event) {

		var node = this.selectedNodeToEdit;

		if (node !== undefined) {


			var val = node.text;
			var aPosition = val.indexOf(" - ");
			var name = "";
			var code = "";
			if (aPosition !== undefined && aPosition != -1) {
				name = val.substr(aPosition + 3);
				code = val.substr(0, aPosition);
				if (field.getName() == 'name') {
					name = field.getValue();
				} else if (field.getName() == 'code') {
					code = field.getValue();
				}
			}
			var text = code + " - " + name;
			node.setText(text);
			node.attributes.toSave = true;

			node.attributes.name = name;
			node.attributes.code = code;

			if(this.referencedCmp.modelsGrid.emptyRecord != null){
				this.referencedCmp.modelsGrid.emptyRecord.set('name', name);
				this.referencedCmp.modelsGrid.emptyRecord.set('code', code);
				
				this.referencedCmp.modelsGrid.getView().refresh();
				this.referencedCmp.modelsGrid.doLayout();
			}
		}
	}
	, editNodeAttribute: function(field, event) {
		var node = this.selectedNodeToEdit;
		if (node !== undefined && node !== null) {
			node.attributes.toSave = true;
			var fName = field.name;
			if(fName == 'description'){
				node.attributes.description = field.getValue();
			}else if(fName == 'label'){
				node.attributes.label = field.getValue();
			}
		}
	}
	, setDomainType: function(field, rec, index) {
		var node = this.selectedNodeToEdit;		
		if (node !== undefined && node !== null) {
			node.attributes.toSave = true;
			node.attributes.typeId = rec.data.typeId;
			node.attributes.type = rec.data.typeCd;//unused server side		

			this.detailFieldTypeDescr.setValue(LN(rec.data.typeDs));			
		}
	}
	,fillDetail : function(sel, node) {
		if(node !== undefined && node != null){
			var val = node.text;
			if (val != null && val !== undefined) {
				var aPosition = val.indexOf(" - ");
	
				var name = node.attributes.name;
				var code = node.attributes.code;
				if (aPosition !== undefined && aPosition != -1) {
					name = val.substr(aPosition + 3);
					code = val.substr(0, aPosition)
				}
	
				this.detailFieldDescr.setValue(node.attributes.description);			
				this.detailFieldLabel.setValue(node.attributes.label);
				this.detailFieldKpi.setValue(node.attributes.kpi);
				this.detailFieldNodeType.setValue(node.attributes.type);
	
				this.detailFieldName.setValue(name);
				this.detailFieldCode.setValue(code);
				
				this.detailFieldTypeDescr.setValue(LN(node.attributes.typeDescr));
			}
		}
	}
	,fillUdpValues : function(sel, node) {	
		
		if(node !== undefined && node != null){			
			// get Udpvalues array
			var udpValues = node.attributes.udpValues;
			
			this.udpValueGrid.fillUdpValues(udpValues);		
			this.udpValueGrid.doLayout();
		}
	}	
	,renderTree : function(tree) {
		tree.getLoader().nodeParameter = 'modelId';
		tree.getRootNode().expand(false, /*no anim*/false);
	}
	,selectNode : function(field) {
		
		/*utility to store node that has been edited*/
		this.selectedNodeToEdit = this.mainTree.getSelectionModel().getSelectedNode();
		
		if(this.selectedNodeToEdit !== null){
			if(this.selectedNodeToEdit.attributes.toSave === undefined || this.selectedNodeToEdit.attributes.toSave == false){
				//checks for the label
				if(this.selectedNodeToEdit.attributes.label !== undefined){
					if(this.nodesLabels.indexOf(this.selectedNodeToEdit.attributes.label) == -1){						
						var labSize = this.nodesLabels.length;
						this.nodesLabels[labSize] = this.selectedNodeToEdit.attributes.label;
						var size = this.nodesToSave.length;
						this.nodesToSave[size] = this.selectedNodeToEdit;
					}
				}else{
					//dropped nodes
					var size = this.nodesToSave.length;
					this.nodesToSave[size] = this.selectedNodeToEdit;
				}

			}//else skip because already taken
		}
	}
	,setListeners : function() {
			this.mainTree.getSelectionModel().addListener('selectionchange',
					this.fillDetail, this);
			this.mainTree.getSelectionModel().addListener('selectionchange',
					this.fillUdpValues, this);
			this.mainTree.addListener('render', this.renderTree, this);

			/* form fields editing */
			this.detailFieldName.addListener('focus', this.selectNode, this);
			this.detailFieldName.addListener('keyup', this.editNode, this);

			this.detailFieldCode.addListener('focus', this.selectNode, this);
			this.detailFieldCode.addListener('keyup', this.editNode, this);

			this.detailFieldDescr.addListener('focus', this.selectNode, this);
			this.detailFieldDescr.addListener('keyup', this.editNodeAttribute, this);

			this.detailFieldLabel.addListener('focus', this.selectNode, this);
			this.detailFieldLabel.addListener('keyup', this.editNodeAttribute, this);
			
			this.detailFieldNodeType.addListener('focus', this.selectNode, this);
			this.detailFieldNodeType.addListener('select', this.setDomainType, this);			
			
			this.detailFieldKpi.addListener('focus', this.selectNode, this);
			this.detailFieldKpi.addListener('keyup', this.editNodeAttribute, this);			
			
			// udp mylisteners
			this.udpValueGrid.addListener('click', this.selectNode, this);
			this.udpValueGrid.addListener('propertychange', this.editNodeUdpValues, this);
			// end my listeners
			
			this.kpiClearBtn.addListener('click', this.selectNode, this);

	},	
	createRootNodeByRec: function(rec) {
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
		        expanded	: true,
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
		        draggable	: false,
		        udpValues   : rec.get('udpValues')
		    });
			return node;
	}
	, cleanAllUnsavedNodes: function() {
		
		Ext.each(this.nodesToSave, function(node, index) {
			node.attributes.toSave = false;  
					
		});
		this.nodesToSave = new Array();
		this.nodesLabels = new Array();
	}
	,dropNodeBehavoiur: function(e) {


		   // e.data.selections is the array of selected records
		   if(Ext.isArray(e.data.selections)) {					    
			   // reset cancel flag
			   e.cancel = false;						    
			   // setup dropNode (it can be array of nodes)
			   e.dropNode = [];
			   var r;
			   for(var i = 0; i < e.data.selections.length; i++) {
			    
				   // get record from selectons
				   r = e.data.selections[i];
				  /*    * tree - The TreePanel
					    * target - The node being targeted for the drop
					    * data - The drag data from the drag source
					    * point - The point of the drop - append, above or below
					    * source - The drag source
					    * rawEvent - Raw mouse event
					    * dropNode - Drop node(s) provided by the source OR you can supply node(s) to be inserted by setting them on this object.
					    * cancel - Set this to true to cancel the drop.
					    * dropStatus - If the default drop action is cancelled but the drop is valid, setting this to true will prevent the animated 'repair' from appearing.
					*/  

				   var parent = e.target;
				   if(e.target.attributes.modelId == null || e.target.attributes.modelId === undefined){
					   //drop forbidden!
					   alert(LN('sbi.models.DDNoParentMsg'));
					   return false;
				   }
				   var idxNodeType = this.typesStore.find('domainCd', 'MODEL_NODE');			
				   var recDomain = this.typesStore.getAt(idxNodeType);

				   var newNode = this.mainTree.getLoader().createNode({
					   kpi: r.get('name')
					   , kpiId: r.get('id')
					   , text: r.get('code')+' - '+r.get('name')
					   , parentId: e.target.attributes.modelId
					   , type: recDomain.get('typeCd')
					   , typeId: recDomain.get('typeId')
					   , typeDescr: recDomain.get('typeDs')
					   , leaf: false
					   , code: r.get('code')
					   , description: r.get('description')
					   , name: r.get('name')
				   });
				   //this.detailFieldLabel.disable();
				   // create node from record data
				   e.dropNode.push(newNode);
				   newNode.select();
				   this.fireEvent('nodedrop', newNode);
			   }
		    
			   // we want Ext to complete the drop, thus return true
			   return true;
		   }else{
			   //inner DD to reorder tree forbidden
			   	e.dropNode.parentId = e.target.attributes.modelId;
			   	e.cancel = true;	
		   }
 
	   // if we get here the drop is automatically cancelled by Ext
	   }
	, addNewItem : function(parent) {
			var idxNodeType = this.typesStore.find('domainCd', 'MODEL_NODE');			
			var recDomain = this.typesStore.getAt(idxNodeType);	
			
			if (parent === undefined || parent == null) {
				alert(LN('sbi.models.DDNoParentMsg'));
				return;
			} else {
				parent.leaf = false;
			}
			var parentId = parent.attributes.modelId;

			//if parent is newly created --> confirm
			if(parentId == null || parentId == undefined){
				Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm'),
					LN('sbi.models.confirmSaveParent'),            
		            function(btn, text) {
		                if (btn=='yes') {
		                	//save parent
		                	
		                	this.mainTree.getLoader().load(parent);
		                	var newparentId = parent.attributes.modelId;

		        	    	//then create child node
		        			var node = new Ext.tree.TreeNode( {
		        				text : '... - ...',
		        				leaf : false,
		        				parentId: newparentId,
		        				type: recDomain.get('typeCd'),
		        				typeId: recDomain.get('typeId'),
		        				typeDescr: recDomain.get('typeDs'),
		        				toSave :false,
		        				allowDrag : false
		        			});
		        			//save parent
		                	this.saveParentNode(parent, node);
		                	
		        			this.mainTree.render();
		        			if (!parent.isExpanded()) {
		        				parent.expand(false, /*no anim*/false);
		        			}
		        			this.mainTree.render();
		        			parent.appendChild(node);
		        	
		        			this.mainTree.getSelectionModel().select(node);
		        			
		                }else{
		                	//exit
		                	return;
		                }
		            },
		            this
				);
				return null;
			}else{

		    	//then create child node
				var node = new Ext.tree.TreeNode( {
					text : '... - ...',
					leaf : false,
					parentId: parentId,
					toSave :false,
					type: recDomain.get('typeCd'),
					typeId: recDomain.get('typeId'),
					typeDescr: recDomain.get('typeDs'),
					allowDrag : false
				});
				this.mainTree.render();
				if (!parent.isExpanded()) {
					parent.expand(false, /*no anim*/false);
				}
				this.mainTree.render();
				parent.appendChild(node);
		
				this.selectedNodeToEdit = node;
				this.mainTree.getSelectionModel().select(node);
				return node;
			}

		},
		deleteItem : function(node) {
			
			if (node === undefined || node == null) {
				alert(LN('sbi.models.selectNode'));
				return;
			}
			
			if(node.attributes.modelId === undefined || node.attributes.modelId == null || node.attributes.modelId ==''){
				this.mainTree.getSelectionModel().clearSelections(false);
				this.nodesToSave.remove(node);
				node.remove();
				
				return;
			}
			
			Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm'),
					LN('sbi.generic.confirmDelete'),            
		            function(btn, text) {
		                if (btn=='yes') {
		                	if (node != null) {	
								Ext.Ajax.request({
						            url: this.services['deleteTreeService'],
						            params: {'modelId': node.attributes.modelId},
						            //method: 'GET',
						            success: function(response, options) {
										if (response !== undefined) {
											this.mainTree.getSelectionModel().clearSelections(false);
											node.remove();
											//this.mainTree.doLayout();
										} else {
											Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.generic.deletingItemError'), LN('sbi.generic.serviceError'));
										}
						            },
						            failure: function() {
						                Ext.MessageBox.show({
						                    title: LN('sbi.generic.error'),
						                    msg: LN('sbi.generic.deletingItemError'),
						                    width: 150,
						                    buttons: Ext.MessageBox.OK
						               });
						            }
						            ,scope: this
					
								});
							} else {
								Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.generic.error.msg'),LN('sbi.generic.warning'));
							}
		                }
		            },
		            this
				);

			
		}
		
});
