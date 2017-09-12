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
 * - Davide Zerbetto (davide.zerbetto@eng.it)
 */

Ext.ns("Sbi.qbe");

Sbi.qbe.RelationshipsWizardContainer = function(config) {
	 
	var defaultSettings = {
		title : LN('sbi.qbe.relationshipswizard.title')
		, width : 700
		, height : 400
	};
	  
	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.relationshipswizardcontainer) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.relationshipswizardcontainer);
	}
	  
	var c = Ext.apply(defaultSettings, config || {});
	  
	Ext.apply(this, c);
	  
	this.services = this.services || new Array(); 
	
	this.init();
	c = Ext.apply(c, {
		layout: "card"
			, activeItem: 0
		, items : [this.relationshipsWizard, this.rolesWizard]
	});

	// constructor
	Sbi.qbe.RelationshipsWizardContainer.superclass.constructor.call(this, c);

	this.addEvents("next","back");
	this.on("next",function(){this.cardNav(1)},this);
	this.on("back",function(){this.cardNav(-1)},this);

};

Ext.extend(Sbi.qbe.RelationshipsWizardContainer, Ext.Panel, {
    
    services : null
    , relationshipsWizard : null
    , ambiguousFields : null // must be set in the object passed to the constructor
    , rolesWizard: null
    , ambiguousRoles: null //the previous selection
   
    // private methods
    ,
    init : function () {
    	this.initRelationshipsWizard();
    	this.initRolesWizard();
    }

    ,
    initRelationshipsWizard : function () {
    	this.relationshipsWizard = new Sbi.qbe.RelationshipsWizard({
    		ambiguousFields : this.ambiguousFields
    		, active: true
    		, title : ''
    	});
    }
    ,
    initRolesWizard: function(){
    	this.rolesWizard = new Sbi.qbe.RelationshipsWizardRole({});
    }
    ,
    cardNav: function(direction){
    	if(direction=='-1'){
    		this.getLayout().setActiveItem( this.relationshipsWizard );
    	}else{
    		this.getLayout().setActiveItem( this.rolesWizard );
    	}
    }
    
    ,getUserChoices: function(){
    	return this.relationshipsWizard.getUserChoices();
    }
    
    ,getUserSelectedRoles: function(){
    	return this.rolesWizard.getFormState();
    }
    
    , updateRoleWizard: function(roles){
    	this.rolesWizard.destroy();
    	
    	var roleSignature = "";
    	if(roles!=null){
    		for(var i=0; i<roles.length; i++){
    			var fields = roles[i].fields;
    			for(var j=0; j<fields.length; j++){
    				roleSignature = roleSignature+fields[j].queryFieldAlias;
    			}
    			
    		}
    	}
    	
    	this.rolesWizard = new Sbi.qbe.RelationshipsWizardRole({entities: roles, signature: roleSignature});

    	if(this.ambiguousRoles && this.ambiguousRoles.signature && this.checkSignatures(roleSignature, this.ambiguousRoles.signature)){
        	this.rolesWizard.setFormState(this.ambiguousRoles.entities);
    	}

    	this.add(this.rolesWizard);
    }
    

	, checkSignatures: function(signature1, signature2){
		if(signature1 == null || signature2 == null || signature1.length!=signature2.length){
			return false;
		}
		if(signature1.length>10){
			var substr = signature1.substring(0,9);
			return signature2.indexOf(substr)>=0;
		}
	}
    
    , getUserGraph: function(){
    	var graph = {};
    	var userChoices = this.getUserChoices();
    	if(userChoices){
    		for(var i=0; i<userChoices.length; i++){
    			var choices = userChoices[i].choices;
    			if(choices){
    				for(var j=0; j<choices.length; j++){
    					var choice = choices[j];
    					if(choice.active){
    						for(var k=0; k<choice.nodes.length; k++){
    							
    							var node = choice.nodes[k];
    							var rel = node.relationshipName;
    							var src = node.sourceName;
    							var trg = node.targetName;
    							var sourceFields = node.sourceFields;
    							var targetFields = node.targetFields;
    							
    							if(!graph[src]){
    								graph[src] = {};
    							}

    							if(!graph[src][trg]){
    								graph[src][trg] = new Array();
    							}
    							
    							var relationsBetweenNode = graph[src][trg];
    							
    							var alreadyExists = false;
    							for(var y=0; y<relationsBetweenNode.length; y++){
    								var relationObject = relationsBetweenNode[y];
    								if(relationObject.rel == rel){
    									alreadyExists = true;
    									break;
    								}
    							}
    							
    							if(!alreadyExists){							
    								var relationObject ={
    										rel : rel,
    										targetFields : targetFields,
    										sourceFields: sourceFields
    								};
    								relationsBetweenNode.push(relationObject);
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    	return graph;
    }
    
    , getRoles: function(){
    	var graph = this.getUserGraph();
    	var fieldsEntityMap = this.buildFieldsMap();
    	var entitiesWithRoles = new Array();
    	for(var source in graph){
        	for(var target in graph[source]){
        		if(graph[source][target].length>1){
        			var roleSelections = [];
        			var roleDef = this.buildRoleSelection(graph,source,target,fieldsEntityMap, roleSelections, 2);
        			for(var i=0; i<roleSelections.length; i++){
        				entitiesWithRoles.push(roleSelections[i]);
        			}
        		}
        	}
    	}
    	return entitiesWithRoles;
    }
    
    , buildRoleSelection: function(graph,src, trg,fieldsEntityMap,roleSelections,deepLevel, rels){
    	
    	if(deepLevel<1){
    		return;
    	}
    	var fields = [];
    	
    	if(rels==null){
    		 rels = graph[src][trg];
    	}
    	
    	for(p in fieldsEntityMap){
    		if((p+"")==trg){
    			fields = fieldsEntityMap[p];
    			break;
    		}
    	}
    	
    	if(fields.length==0){
    		alert("You must select some field from the source entity with multiple roles");
    	}
    	
    	var aRoleSelection = {
    			name: trg,
    			aliases: new Array(),
    			fields: fields
    	};
    	
    	for(var i=0; i<rels.length; i++){
    		aRoleSelection.aliases.push({
    				name:  trg,
    				alias: this.buildAlias(src, trg, rels[i], i),
    				aliasTooltip: this.buildTooltip(src, trg, rels[i], i),
    				role: rels[i],
    				fields: []
    			});
    	}
    	
    	//clone also the next entities
    	//for the next entities we should pass the roles
    	if(graph[trg]){
        	for(node in graph[trg]){
        		this.buildRoleSelection(graph,trg, node,fieldsEntityMap,roleSelections,deepLevel-1, rels)
        	}
    	}

    	roleSelections.push(aRoleSelection);
    	
    }
    
    ,buildAlias: function(src, trg, rel, pos){
		var alias = trg+" (rel: "+rel.rel+")";
    	if(alias && alias.length>40){
    		alias = alias.substring(0,38)+"...";
    	}
    	return alias;
    }    
    
    ,buildTooltip: function(src, trg, rel, pos){
    	var tooltip = src+"("+rel.sourceFields+")<br/>";
    	tooltip+= '&nbsp;&nbsp;&nbsp;';
    	tooltip+= rel.rel;
    	tooltip+= '<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
    	tooltip+="("+rel.targetFields+")"+ trg;
    	return tooltip;
    }
    
    ,buildFieldsMap: function(){
    	var entityFieldsMap= {};
    	if(this.ambiguousFields){
    		for(var i=0; i<this.ambiguousFields.length; i++){
    			var aAmbiguousField = this.ambiguousFields[i];
    			if(!entityFieldsMap[aAmbiguousField.entity]){
    				entityFieldsMap[aAmbiguousField.entity]=new Array();
    			} 
    			var field = Ext.apply({},aAmbiguousField);
    			if(field.queryFieldType!="filter"){
    				entityFieldsMap[aAmbiguousField.entity].push(field);
    			}
    			
    		}
    	}
    	return entityFieldsMap;
    }
    
    ,validate: function(){
    	return this.rolesWizard.validate();
    }
    
});