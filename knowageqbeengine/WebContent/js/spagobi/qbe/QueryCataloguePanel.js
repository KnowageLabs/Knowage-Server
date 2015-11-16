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
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Andrea Gioia (adrea.gioia@eng.it)
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.QueryCataloguePanel = function(config) {
	var c = Ext.apply({
		// set default values here
	}, config || {});
	
	this.services = new Array();
	var params = {};
	this.services['getCatalogue'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_CATALOGUE_ACTION'
		, baseParams: params
	});
	
	this.services['setCatalogue'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SET_CATALOGUE_ACTION'
		, baseParams: params
	});
	
	this.services['validateCatalogue'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'VALIDATE_CATALOGUE_ACTION'
		, baseParams: params
	});
	
	this.services['addQuery'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'ADD_QUERY_ACTION'
		, baseParams: params
	});
	
	this.services['deleteQueries'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DELETE_QUERIES_ACTION'
		, baseParams: params
	});
	
	
	this.addEvents('beforeselect');
	
	this.initTree(c);
	
	Ext.apply(c, {
		layout: 'fit'
		, border:false
		, autoScroll: true
		, containerScroll: true
		, items: [this.tree]
	});
	
	
	// constructor
	Sbi.qbe.QueryCataloguePanel.superclass.constructor.call(this, c);
    
    this.addEvents('load');
    
};

Ext.extend(Sbi.qbe.QueryCataloguePanel, Ext.Panel, {
    
	services: null
	, treeSelectionModel: null
	, treeLoader: null
	, rootNode: null
	, tree: null
	, type: 'querycataloguetree'
	
	// public methods
	
	, load: function() { // ma quando viene chiamato?
		this.treeLoader.load(this.rootNode, function(){});
	}

	, commit: function(callback, scope) {
		
		var currentQuery = this.getSelectedQuery();
		var ambiguousFields = [];
		var ambiguousRoles = [];
		if (currentQuery) {
			ambiguousFields = this.getStoredAmbiguousFields();
			ambiguousRoles = this.getStoredRoles();
		}
		
		var params = {
				catalogue: Ext.util.JSON.encode(this.getQueries())
				, currentQueryId : (currentQuery) ? currentQuery.id : ''
				, ambiguousFieldsPaths : Ext.util.JSON.encode(ambiguousFields)
				, ambiguousRoles : Ext.util.JSON.encode(ambiguousRoles)
		};

		Ext.Ajax.request({
		    url: this.services['setCatalogue'],
		    success: this.onCommitSuccessHandler.createDelegate(this, [callback, scope], true), // before invoking callback, we have to resolve ambiguous fields, if any
		    failure: Sbi.exception.ExceptionHandler.handleFailure,	
		    scope: this,
		    params: params
		});
		
	}
	
	, manageAmbiguousFields: function(callback, scope) {
		
		var currentQuery = this.getSelectedQuery();
		if (currentQuery) {
			ambiguousRoles = this.getStoredRoles();
		}
		var params = {
				catalogue: Ext.util.JSON.encode(this.getQueries())
				, ambiguousRoles : Ext.util.JSON.encode(ambiguousRoles)
				, currentQueryId : (currentQuery) ? currentQuery.id : ''
		};

		Ext.Ajax.request({
		    url: this.services['setCatalogue'],
		    success: this.onCommitSuccessHandler.createDelegate(this, [callback, scope, true], true), // before invoking callback, we have to resolve ambiguous fields, if any
		    failure: Sbi.exception.ExceptionHandler.handleFailure,	
		    scope: this,
		    params: params
		});
		
	}
	
	,
	onCommitSuccessHandler : function (response, options, callback, scope, forceOpenAmbiguous) {
		var decodedResponce = "";
		var catalogueErrors = "";
		var ambiguousFields = "";
		var ambiguousWarinig = "";
		var userRolesSolved = "";
		var queryString = "";
		
		if(response.responseText  && response.responseText !=""){
			decodedResponce = Ext.util.JSON.decode( response.responseText );
		}

		if(decodedResponce.ambiguousFieldsPaths  && decodedResponce.ambiguousFieldsPaths !=""){
			ambiguousFields  = Ext.util.JSON.decode(decodedResponce.ambiguousFieldsPaths);
		}
		
		if(decodedResponce.ambiguousRoles  && decodedResponce.ambiguousRoles !=""){
			userRolesSolved = Ext.util.JSON.decode(decodedResponce.ambiguousRoles);
		}
		
		
		if(decodedResponce.catalogueErrors  && decodedResponce.catalogueErrors !=""){
			catalogueErrors = Ext.util.JSON.decode(decodedResponce.catalogueErrors);
		}
		
		ambiguousWarinig =(decodedResponce.ambiguousWarinig);
		queryString = decodedResponce.queryString;

		if(catalogueErrors && catalogueErrors.length>0){
			var error = "";
			for(var i=0; i<catalogueErrors.length; i++){
				error = error+ LN("sbi.qbe.queryeditor.error."+catalogueErrors[i]);
			}
			Sbi.exception.ExceptionHandler.showErrorMessage(error);
		}else{
		
			//open the ambiguous fields wizard but there is no ambiguous fields
			if (forceOpenAmbiguous && (ambiguousFields.length == 0 )) {
				Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.qbe.queryeditor.noambiguousfields.msg'),LN('sbi.qbe.queryeditor.noambiguousfields.title'));
			}
	
			if ((!forceOpenAmbiguous && decodedResponce.executeDirectly) || (forceOpenAmbiguous && (ambiguousFields.length == 0 ) )) {
				if (callback) {
					var callBackParams = {
							catalogueErrors: catalogueErrors,
							ambiguousFields: ambiguousFields,
							ambiguousWarinig: ambiguousWarinig,
							userRolesSolved: userRolesSolved,
							queryString: queryString
					}
					callback.call(scope, callBackParams);  // proced execution with the specified callback function
				}
			} else {
				
				ambiguousFields = this.mergeAmbiguousFields(ambiguousFields);
				var relationshipsWindow = new Sbi.qbe.RelationshipsWizardWindow({
					ambiguousFields : ambiguousFields
					, ambiguousRoles : userRolesSolved 
					, closeAction : 'close'
					, modal : true
				});
				relationshipsWindow.show();
				relationshipsWindow.on('apply', this.onAmbiguousFieldsSolved.createDelegate(this, [callback, scope], true), this);
				if(ambiguousWarinig!=null && ambiguousWarinig!="null" && ambiguousWarinig!=""){
					Sbi.exception.ExceptionHandler.showInfoMessage(LN(ambiguousWarinig));
				}
			}
		}

	}
	
	,
	mergeAmbiguousFields : function (ambiguousFields) {
		var previousAmbiguousFields = this.getStoredAmbiguousFields();
		var ambiguousFieldsObj = new Sbi.qbe.AmbiguousFields({ ambiguousFields : ambiguousFields });
		var cachedObj = new Sbi.qbe.AmbiguousFields({ ambiguousFields : previousAmbiguousFields });
		ambiguousFieldsObj.merge(cachedObj);
		return ambiguousFieldsObj.getAmbiguousFieldsAsJSONArray();
	}
	
	,
	onAmbiguousFieldsSolved : function (theWindow, ambiguousFieldsSolved, userRolesSolved, callback, scope) {
		theWindow.close();
		this.storeAmbiguousFields(ambiguousFieldsSolved, userRolesSolved);
		this.commit(callback, scope);
	}

	,
	storeAmbiguousFields : function (ambiguousFields, userRolesSolved) {
		var query = this.getSelectedQuery();
		Sbi.cache.memory.put(query.id, ambiguousFields);
		Sbi.cache.memory.put(query.id+"_roles",  userRolesSolved);
		//query.ambiguousFields = ambiguousFields;
	}
	
	,
	getStoredAmbiguousFields : function () {
		var query = this.getSelectedQuery();
		var cached = Sbi.cache.memory.get(query.id);
		return cached || [];
		//return query.ambiguousFields || [];
	}
	
	,
	getStoredRoles : function () {
		var query = this.getSelectedQuery();
		var cached = Sbi.cache.memory.get(query.id+"_roles");
		return cached || [];
		//return query.ambiguousFields || [];
	}
	
	,getQueryRoles : function (queryId) {
		var cached = Sbi.cache.memory.get(queryId+"_roles");
		return cached || [];
		//return query.ambiguousFields || [];
	}
	
	, validate: function(callback, scope) {
		var params = {};
		Ext.Ajax.request({
		    url: this.services['validateCatalogue'],
		    success: callback,
		    failure: Sbi.exception.ExceptionHandler.handleFailure,	
		    scope: scope,
		    params: params
		});   
	}
	
	, addQuery: function(query) {
		var queryItem;
		if(query) queryItem = {query: query};
		this.addQueryItem(queryItem);
	}
	
	, insertQuery: function(parentQuery) {
		var parentQueryItem;
		if(parentQuery) parentQueryItem = {query: parentQuery};
		this.insertQueryItem(parentQueryItem);
	}

	, setQuery: function(queryItemId, query) {
		var oldQuery;
		var item = this.getQueryItemById(queryItemId);
		if(item) {
			oldQuery = item.query;
			item.query = query;
		}
		
		return oldQuery;
	}
	
	, getQueries: function() {
		var queries = [];
    	if( this.rootNode.childNodes && this.rootNode.childNodes.length > 0 ) {
			for(var i = 0; i < this.rootNode.childNodes.length; i++) {
				queries.push( this.getQueryById(this.rootNode.childNodes[i].id) );
			}
		}
    	
    	return queries;
	}
	
	, getQueryById: function(queryId) {
		var query;
		var queryNode = this.tree.getNodeById(queryId);
		
		if(queryNode) {
			query = queryNode.props.query;
			query.name = queryNode.text;
			var cachedGraph = this.getqueryGraph(queryId);
			var cachedRoles = this.getQueryRoles(queryId);
			var cachedAmbiguousFields = this.getAmbiguousFields(queryId);
			
			if(cachedGraph){
				query.graph =cachedGraph;
			}

			if(cachedRoles){
				query.relationsRoles = cachedRoles;
			}
			
			if(cachedAmbiguousFields){
				query.ambiguousFields = cachedAmbiguousFields;
			}
			
			query.subqueries = [];
			if( queryNode.childNodes && queryNode.childNodes.length > 0 ) {
				for(var i = 0; i < queryNode.childNodes.length; i++) {
					var subquery = this.getQueryById( queryNode.childNodes[i].id );
					query.subqueries.push( subquery );

					
				}
			}
		}
		
		return query;
	}
	
	, getParentQuery: function(queryId) {
		var query = null;
		var queryNode = this.tree.getNodeById(queryId);
		if(queryNode) {
			var parentQueryNode = queryNode.parentNode;
			if(parentQueryNode && parentQueryNode.id !== this.rootNode.id) {
				query = this.getQueryById(parentQueryNode.id);
			}
		}
		return query;
	}
	
	, getSelectedQuery: function() {
		var queryItem = this.getSelectedQueryItem();
		return queryItem? queryItem.query: undefined;
	}
	
	
	, deleteQueries: function(queries) {
		this.deleteQueryItems(queries);
	}
	
	
	// PRIVATE:  item level
	
	, getQueryItems: function() {
		var queryItems = [];
    	if( this.rootNode.childNodes && this.rootNode.childNodes.length > 0 ) {
			for(var i = 0; i < this.rootNode.childNodes.length; i++) {
				queryItems.push( this.getQueryItemById(this.rootNode.childNodes[i].id) );
			}
		}
    	
    	return queryItems;
	}

	, getQueryItemById: function(queryId) {
		var queryItem;
		var queryNode = this.tree.getNodeById(queryId);
		
		if(queryNode) {
			
			queryItem = queryNode.props;
			queryItem.subqueries = [];
			if( queryNode.childNodes && queryNode.childNodes.length > 0 ) {
				for(var i = 0; i < queryNode.childNodes.length; i++) {
					var subquery = this.getQueryItemById( queryNode.childNodes[i].id );
					queryItem.subqueries.push( subquery );
				}
			}
		}
		
		return queryItem;
	}
	
	, getSelectedQueryItem: function() {
		var queryNode = this.tree.getSelectionModel().getSelectedNode();
		return queryNode? queryNode.props: undefined;
	}
	
	, addQueryItem: function(queryItem) {
		this.insertQueryItem(this.rootNode.id, queryItem);
	}
	
	

	, insertQueryItem: function(parentQueryItem, queryItem) {
		var nodeId = (typeof parentQueryItem === 'string')? parentQueryItem: parentQueryItem.query.id;
		var parentQueryNode = this.tree.getNodeById(nodeId);
		 
		if(!queryItem) {
			this.createQueryNode(this.insertQueryNode.createDelegate(this, [parentQueryNode], 0), this);
		} else {
			 var queryNode = {
			    id: queryItem.query.id
			   	, text: queryItem.query.id
			   	, leaf: true
			   	, props: {
			 		query: queryItem.query
			 		, iconCls: 'icon-query'
			    }
			 };
			 this.insertQueryNode(parentQueryNode, queryNode);			 
		}
	}
	
	, deleteQueryItems: function(queries) {
		this.deleteQueryNodes(queries);
	}
	
	
	
	// PRIVATE:  node level
	
	, addQueryNode: function(queryNode) {
		this.insertQueryNode(this.rootNode, queryNode);
	}
	
	, insertQueryNode: function(parentQueryNode, queryNode) {
		if(!queryNode) {
			this.createQueryNode(this.insertQueryNode.createDelegate(this, [parentQueryNode], 0), this);
		} else {			
			parentQueryNode.leaf = false;					
			parentQueryNode.appendChild( queryNode );
			parentQueryNode.expand();
			queryNode.select();		
			
			var te = this.treeEditor;
			var edit = function(){
                te.editNode = queryNode;
                te.startEdit(queryNode.ui.textNode);
            };
			setTimeout(edit, 10);
		}
	}
	
	, createQueryNode: function(callback, scope) {
		Ext.Ajax.request({
		   	url: this.services['addQuery'],
		   	success: function(response, options) {
    			if(response !== undefined && response.responseText !== undefined) {
					var content = Ext.util.JSON.decode( response.responseText );
					var queryNode = new Ext.tree.TreeNode(content);
					queryNode.props = content.attributes;
					callback.call(scope, queryNode);
				} else {
			      	Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
			    }     					
   			},
   			failure: Sbi.exception.ExceptionHandler.handleFailure,
   			scope: this
		});	   
	}
	
	, deleteQueryNodes: function(queries, callback, scope) {
		var p;
    	if(queries) {
    		if( !(queries instanceof Array) ) {
    			queries = [queries];
    		}
    		
    		for(var i = 0, p = []; i < queries.length; i++) {
    			var query = queries[i];
    			if(typeof query === 'string') {
    				p.push( query );
    			} else if(typeof query === 'object') {
    				p.push( query.id || query.query.id );
    			} else {
    				alert('Invalid type [' + (typeof query) + '] for object query in function [deleteQueries]');
    			}
    		}
    		// don't let to erase the root query
    		if(p == 'q1'){
				Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.qbe.queryeditor.eastregion.tools.wanringEraseRoot'), 'Warning');
    			return;	
    		}
    		
			Ext.Ajax.request({
			   	url: this.services['deleteQueries'],
			   	params: {queries: Ext.util.JSON.encode(p)},
			
			   	success: function(response, options) {
			   		var q = Ext.util.JSON.decode( options.params.queries );
			   		for(var i = 0; i < q.length; i++) {
			   			var node = this.tree.getNodeById(q[i]);
			   			node.remove();
			   		}
			   		
			   		if(callback) callback.call(scope, q);
	   			},
	   			failure: Sbi.exception.ExceptionHandler.handleFailure,
	   			scope: this
			});	
    	}
	}
		

	// private methods
	
	, initTree: function(config) {
		
		this.treeLoader = new Ext.tree.TreeLoader({
	        dataUrl: this.services['getCatalogue']
	    });
		// redefine createnode function in order to disable node expansion on dblclick
		this.treeLoader.createNode = function(attr){
	        // apply baseAttrs, nice idea Corey!
	        if(this.baseAttrs){
	            Ext.applyIf(attr, this.baseAttrs);
	        }
	        if(this.applyLoader !== false){
	            attr.loader = this;
	        }
	        if(typeof attr.uiProvider == 'string'){
	           attr.uiProvider = this.uiProviders[attr.uiProvider] || eval(attr.uiProvider);
	        }
	        
	     
	        var resultNode;
	        if(attr.leaf) {
	        	resultNode = new Ext.tree.TreeNode(attr);
	        	resultNode.props = attr.attributes;
	        } else {
	        	resultNode = new Ext.tree.AsyncTreeNode(attr);
	        	resultNode.props = attr.attributes;
	        	//resultNode.attributes = attr.attributes;
	        }
	        
	        resultNode.getUI().onDblClick = function(e){
	            e.preventDefault();
	            if(this.disabled){
	                return;
	            }
	            if(this.checkbox){
	                this.toggleCheck();
	            }
	            
	            this.fireEvent("dblclick", this.node, e);
	        };
	        
	        return resultNode;
	    };
	    
	   
		
		this.treeSelectionModel = new Ext.tree.DefaultSelectionModel({
			init : function(tree){
		        this.tree = tree;
		        tree.on("dblclick", this.onNodeDbClick, this);
	    	},
	    
	    	onNodeDbClick : function(node, e){
	    		this.select(node);
	    	}
		});
				
		this.rootNode = new Ext.tree.AsyncTreeNode({
			id			: 'root',
	        text		: 'Queries',
	        iconCls		: 'database',
	        expanded	: true,
	        draggable	: false
	    });
		
		this.tree = new Ext.tree.TreePanel({
	        collapsible: false,
	        
	        enableDD: true,	        
	        ddGroup: 'gridDDGroup',
	        dropConfig: {
				ddGroup: 'gridDDGroup',
				// avoid in tree drop
				isValidDropPoint : function(n, pt, dd, e, data){
					return false;
				}      
	      	},
	      	
	      	dragConfig: {
	      		// if dragConfig in set the ddGroup is taken from there and not from the tree
	      		// so if not defined there the defaut one will be used : 'treeDD'
	      		ddGroup: 'gridDDGroup', 
	      		onInitDrag : function(e){
		            var data = this.dragData;
		            // when start a new drag we do not want to select the dragged node
		            //this.tree.getSelectionModel().select(data.node);
		            this.tree.eventModel.disable();
		            this.proxy.update("");
		            data.node.ui.appendDDGhost(this.proxy.ghost.dom);
		            this.tree.fireEvent("startdrag", this.tree, data.node, e);
	      		}
	      		
	      		, beforeInvalidDrop : function(e, id){
	      	        // when a drop fails we do not want to select the dragged node
	      	        //var sm = this.tree.getSelectionModel();
	      	        //sm.clearSelections();
	      	        //sm.select(this.dragData.node);
	      	    }
	      	}, 
	      	
	      	
	        animCollapse     : true,
	        collapseFirst	 : false,
	        border           : false,
	        autoScroll       : true,
	        containerScroll  : true,
	        animate          : false,
	        trackMouseOver 	 : true,
	        useArrows 		 : true,
	        selModel		 : this.treeSelectionModel,
	        loader           : this.treeLoader,
	        root 			 : this.rootNode
	    });	
		
		// defines the tree sorting
		new Ext.tree.TreeSorter(this.tree, {
		    folderSort: true
		    , dir: 'asc'
		    , property: 'id'
		});
		
		this.tree.type = this.type;
		
		/*
		this.tree.on('startdrag', function(tree, node, e) {
			alert(tree.dragZone.ddGroup);
		}, this);
		*/
		
		// add an inline editor for the nodes
	    this.treeEditor = new Ext.tree.TreeEditor(this.tree, {/* fieldconfig here */ }, {
	        allowBlank:false,
	        blankText:'A name is required',
	        selectOnFocus:true
	    });
	    // we do not want editing to start after node clicking
	    //this.treeEditor.beforeNodeClick = Ext.emptyFn;

		
		this.tree.getSelectionModel().on('beforeselect', this.onSelect, this);
		this.treeLoader.on('load', this.onLoad, this);
	}
	
	, onLoad: function(loader, node, response) {
		node.expandChildNodes();
		     
		if( node.childNodes && node.childNodes.length > 0 ) {
			this.tree.getSelectionModel().suspendEvents(false);  // workaround (work-around): when GUI is initialized, the first node is selected twice (why?)
															     // therefore we suspend (and resume just after) events to avoid this 
			this.tree.getSelectionModel().select( node.childNodes[0] );
			this.tree.getSelectionModel().resumeEvents();
		}
		
		this.fireEvent('load', this);
	}
	
	, onSelect: function(sm, newnode, oldnode) {
		if (newnode.id == this.rootNode.id) {
			return false;
		}
		if (oldnode && oldnode != null && newnode.id == oldnode.id) {
			return false; // in case the user selects the old node, we don't allow this just to avoid unuseful calls to the server
		}
		
		var allowSelection = true;
		var oldquery = oldnode ? oldnode.props.query: undefined;
		var b = this.fireEvent('beforeselect', this, newnode.props.query, oldquery);
		if(b === false) allowSelection = b;
		return allowSelection;
	}
	
	,
	setQueriesCatalogue : function (queriesCatalogue) {
		this.clear();
		var queries = queriesCatalogue.catalogue.queries;
		for (var i = 0; i < queries.length; i++) {
			var query = queries[i];
			var root = this.tree.getRootNode();
			this.setQueryOnCatalogue(root, query);
		}
		this.tree.getSelectionModel().select( root.childNodes[0] );
	}
	
	,
	setQueryOnCatalogue: function (parentNode, query) {
		var queryNode = {
		    id: query.id
		   	, text: query.name
		   	, leaf: true
		   	, attributes: {
		 		query: query
		 		, iconCls: 'icon-query'
		    }
		};
		Sbi.cache.memory.put(query.id+"_roles",  query.relationsRoles);
		Sbi.cache.memory.put(query.id,  query.ambiguousFields);
		
		
		parentNode.appendChild( queryNode );
		parentNode.expand();
		
		var subqueries = query.subqueries;
		if (subqueries !== undefined && subqueries !== null && subqueries.length > 0) {
			for (var i = 0; i < subqueries.length; i++) {
				setQueryOnCatalogue( queryNode , subqueries[i] );
			}
		}
	}
	
	,
	clear : function () {
		var root = this.tree.getRootNode();
		root.removeAll(true);
	}
	
	, getqueryGraph: function(queryId){
		var relationships = this.parseGraph(Sbi.cache.memory.get(queryId));
		return relationships;
	}
	
	, getAmbiguousFields: function(queryId){
		var relationships = Sbi.cache.memory.get(queryId);
		return relationships;
	}
	
	, parseGraph: function(graph){
		var relationships = new Array();
		if(graph){
			for(var i=0; i<graph.length; i++){
				var choices = graph[i].choices;
				if(choices){
					for(var j=0; j<choices.length; j++){
						var choice = choices[j];
						if(choice.active && choice.active && choice.nodes){
							var nodes =choice.nodes;
							for(var k=0; k<nodes.length; k++){
								var node = nodes[k];
								if(node){
									relationships.push({relationshipId: node.relationshipId});
								}
							}
						}
					}
				}

			}
		}
		return relationships;
	}
	
});