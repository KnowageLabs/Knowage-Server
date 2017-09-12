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

Sbi.qbe.RelationshipsWizardRole = function(config) {

	var defaultSettings = {
			entities: [
//						{
//							   name: "entity1",
//							   aliases: [
//							             {name :"alias1", alias:"", role:"role", fields: [{name:"field1"},{name:"field3"}]},
//							             {name :"alias2", alias:"", role:"role", fields: [{name:"field2"}]}],
//							   fields:[
//							           {name: "field1"},
//							           {name: "field2"},
//							           {name: "field4"}
//							           ]
//						}
						]
			
	};

	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.relationshipswizardrole) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.relationshipswizardrole);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	this.addEvents();
	this.init();
	this.services = this.services || new Array(); 

	//this.init();
	c = Ext.apply(c, {
		activeTab: 0,
		items        : [
		                this.relationshipsWizardsRoleForEntity
		                ]
	});

	// constructor
	Sbi.qbe.RelationshipsWizardRole.superclass.constructor.call(this, c);


};

Ext.extend(Sbi.qbe.RelationshipsWizardRole, Ext.TabPanel, {

	relationshipsWizardsRoleForEntity: null,
	signature: null//from caller
	
	, init: function(){
		if(!this.entities){
			alert("There must be at least an entity to built the field role association wizard");
		}
		this.relationshipsWizardsRoleForEntity = new Array();
		for(var i=this.entities.length-1; i>=0;i--){
			var aRelationshipsWizardsRoleForEntityConfig = {roleEntityConfig: this.entities[i]};
			var aRelationshipsWizardsRoleForEntity = new Sbi.qbe.RelationshipsWizardRoleForEntity(Ext.apply(this.relationshipsWizardRoleForEntityConfig||{},aRelationshipsWizardsRoleForEntityConfig));
			
			//to solve problem in the layout of the entityfieldgrid
			aRelationshipsWizardsRoleForEntity.on("activate",function(panel){
				var height = panel.getHeight();
				if(height){
					if(height%2==0){
						panel.setHeight(height-1);
					}else{
						panel.setHeight(height+1);
					}
				}
			},this);
			
			this.relationshipsWizardsRoleForEntity.push( aRelationshipsWizardsRoleForEntity);
		}
	}

	, setFormState: function(state){
		for(var i=0; i<this.relationshipsWizardsRoleForEntity.length; i++){
			state.push(this.relationshipsWizardsRoleForEntity[i].setFormState(state[i]));
		}
	}

	, getFormState: function(){
		var stateArray = new Array();
		for(var i=0; i<this.relationshipsWizardsRoleForEntity.length; i++){
			stateArray.push(this.relationshipsWizardsRoleForEntity[i].getFormState());
		}
		var state ={
				entities : stateArray,
				signature: this.signature
		}
		return state;
	}
	
	, validate: function(){
		var errors= new Array();
		for(var i=0; i<this.relationshipsWizardsRoleForEntity.length; i++){
			var entityErrors = this.relationshipsWizardsRoleForEntity[i].validate();
			if(entityErrors){
				for(var j=0; j<entityErrors.length; j++){
					errors.push(entityErrors[j]);
				}
			}
		}
		return errors;
	}

});