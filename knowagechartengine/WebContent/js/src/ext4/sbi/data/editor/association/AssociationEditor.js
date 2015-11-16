/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.data.editor.association.AssociationEditor', {
	extend: 'Ext.Panel'
    , layout: 'border'

	, config:{
		  services: null
		, stores: null
		, associations: null
		, contextMenu: null
		, border: false
	}

	/**
	 * @property {Sbi.data.editor.association.AssociationEditorDatasetContainer} dsContainerPanel
	 * The container of datasets
	 */
	, dsContainerPanel: null
	/**
	 * @property {Sbi.data.editor.association.AssociationEditorList} assContainerPanel
	 * The container of associations
	 */
	, assContainerPanel: null
	/**
	 * @property {Ext.Array} associations
	 * The list with all associations
	 */
	, association: null

	, constructor : function(config) {
		Sbi.trace("[AssociationEditor.constructor]: IN");
		this.initConfig(config);
		this.initPanels(config);
		this.callParent(arguments);
		this.addEvents('addAssociation','addAssociationToList');
		Sbi.trace("[AssociationEditor.constructor]: OUT");
	}

	,  initComponent: function() {
	        Ext.apply(this, {
	            items:[{
						id: 'dsContainerPanel',
						region: 'center',
						layout: 'fit',
						autoScroll: true,
						split: true,
						items: [this.dsContainerPanel]
						},
						{
						id: 'assContainerPanel',
						region: 'south',
						autoScroll: true,
						split: true,
						items: [this.assContainerPanel]
						}]
	        });
	        this.callParent();
	    }

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	, initializeEngineInstance : function (config) {

	}

	, initPanels: function(config){
		this.initDatasetPanel(config);
		this.initAssociationPanel(config);
	}

	, initDatasetPanel: function(config) {
		this.dsContainerPanel = Ext.create('Sbi.data.editor.association.AssociationEditorDatasetContainer',{
			stores: this.stores
		});
	}

	, initAssociationPanel: function(config) {
		this.assContainerPanel = Ext.create('Sbi.data.editor.association.AssociationEditorList',{
			height:200,
			associations: this.associations
		});
		this.assContainerPanel.addListener('addAssociation', this.addAssociation, this);
		this.assContainerPanel.addListener('modifyAssociation', this.modifyAssociation, this);
		this.assContainerPanel.addListener('removeAssociation', this.removeAssociation, this);
		this.assContainerPanel.addListener('selectAssociation', this.selectAssociation, this);
		this.assContainerPanel.addListener('updateIdentifier', this.updateIdentifier, this);
	}

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method (fired)
	 * Adds a new Association with active selections to the associations and to the associations grid
	 *
	 * @param {String} n The identifier (set for update context)
	 */
	, addAssociation: function(n){
		var toReturn = true;

		var allDs = this.dsContainerPanel.getAllDatasets();
		var assToAdd = new Array();
		assToAdd.id = this.getAssociationId(n);
		for (var i=0; i< allDs.length; i++){
			var ds = allDs.get(i);
			var f = this.dsContainerPanel.getSelection(ds.dataset);
			if (f !== null){
				f.ds = ds.dataset;
				assToAdd.push(f);
			}
		}

		toReturn = this.addAssociationToList(assToAdd);

		return toReturn;
	}

	/**
	 * @method (fired)
	 * Remove the association from the associations
	 *
	 * @param {String} r The Association content to remove
	 */
	, removeAssociation: function(r){
		for (var i=0; i<this.associations.length; i++){
			var obj = this.associations[i];
			if (obj && obj.description == r){
				this.associations.splice(i,1);
				break;
			}
		}
		Sbi.trace("[AssociationEditor.removeAssociation]: Removed association ['"+ r +"']");
		Sbi.trace("[AssociationEditor.removeAssociation]: Associations List upgraded is  [ " +  Sbi.toSource(this.associations) + ']');
	}

	/**
	 * @method (fired)
	 * Update (with an add and remove of the element) the association from the associations and grid
	 *
	 */
	, modifyAssociation: function(){
		var assToModify = this.assContainerPanel.getCurrentAss();
		if (assToModify == null){
	   		  alert(LN('sbi.cockpit.association.editor.msg.modify'));
	   		  return;
		}
		var assToModifyRec = this.assContainerPanel.getAssociationById(assToModify.id);
	    if (this.addAssociation(assToModify.id)){
			this.assContainerPanel.removeAssociationFromGrid(assToModifyRec);
			this.removeAssociation(assToModify.description);
	    }

	}

	/**
	 * @method (fired)
	 * Select the cells linked to the list grid
	 *
	 * @param {String} r The Association content to use for the selection of elements
	 */
	, selectAssociation: function(r){
		this.dsContainerPanel.resetSelections();
		var lst = r.description.split('=');
		for (var i=0; i<lst.length; i++){
			var el = lst[i].split('.');
			this.dsContainerPanel.setSelection(el);
		}
	}

	/**
	 * @method (fired)
	 * Update the identifier modified manually from the user
	 *
	 * @param {Element} e The element (cell) modified.
	 */
	, updateIdentifier: function(e){
		var obj = this.getAssociationById(e.originalValue);
		if (Sbi.isValorized(obj)) obj.id = e.value;

	}

	/**
	 * @method
	 * Returns the Associations list
	 *
	 */
	, getAssociationsList: function(){
		return this.associations;
	}

	/**
	 * @method
	 * Set the Associations list
	 *
	 */
	, setAssociationsList: function(associations){
		this.associations = associations;
	}

	/**
	 * @method
	 * Reset the Associations list
	 *
	 */
	, removeAllAssociations: function(){
		this.associations = new Array();
	}

	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------
	/**
	 * @method
	 * Adds a new Association to the associations with the selected elements.
	 *
	 * @param {Array} r The array of elements
	 */
	, addAssociationToList: function(r){
		var toReturn = true;

		if (this.associations == null)
			this.associations = new Array();

		var obj = '';
		var objType = '';
		var equal = '';
		var wrongTypes = false;

		for (var i=0; i< r.length; i++){
			var el = r[i];

			if (i==0){
				objType = el.colType;
				equal = '=';
			}else{
				//check consistency between type fields
				if (objType !== el.colType){
					wrongTypes = true;
				}
			}
			//create association string (ex: tabA.colA=tabB.colB...)
			obj += el.ds + '.' + el.alias + ((i<r.length-1)?equal:'');
		}

		if (this.existsAssociation(obj)){
			alert(LN('sbi.cockpit.association.editor.msg.duplicate'));
			return false;
		}

		//adds only new associations
		if (wrongTypes){
			Ext.MessageBox.confirm(
					LN('sbi.generic.pleaseConfirm')
					, LN('sbi.cockpit.association.editor.msg.differentType')
		            , function(btn, text) {
		                if ( btn == 'yes' ) {
		                	this.associations.push({id: r.id, description:obj});
		                	this.assContainerPanel.addAssociationToList({id: r.id, description:obj});
		                	toReturn = true;
		                }else
		                	toReturn = false;
					}
					, this
				);
		}else{
			if (obj !== ''){
				this.associations.push({id: r.id, description:obj});
				this.assContainerPanel.addAssociationToList({id: r.id, description:obj});
				toReturn = true;
			}else{
				alert(LN('sbi.cockpit.association.editor.msg.selectFields'));
				toReturn = false;
			}
		}
		Sbi.trace("[AssociationEditor.addAssociation]: Associations List updated with  [ " +  Sbi.toSource(this.associations) + ']');
		return toReturn;
	}

	, existsAssociation: function(a){
		if (this.getAssociationByAss(a)  != null)
			return true;
		else
			return false;
	}

	/**
	 * @method
	 * Returns the identifier for the Association (insert or update action)
	 *
	 * @param {String} n The identifier. Setted only for update action
	 */
	, getAssociationId: function(n){
		var newId = '';
		//parameter n is valorized only in modify context
		if (n !== null && n !== undefined)
			newId += n;
		else{
			newId += '#';
			if (this.associations != null){
				//get max id already setted
				var maxId = 0;
				for (var i=0; i<this.associations.length; i++ ){
					var currId = this.associations[i].id.substring(1);
					if (maxId < parseInt(currId))
						maxId = parseInt(currId);
				}
				newId += (maxId+1).toString();
			}
			else
				newId += '0';
		}

		return newId;
	}

	/**
	 * @method
	 * Returns the Association object getted from the associations throught the id.
	 * Format: {id:xx, assyy}
	 *
	 * @param {String} id The identifier.
	 */
	, getAssociationById: function(id){
		if (!Sbi.isValorized(this.associations)) return null;
		for (var i=0; i<this.associations.length; i++){
			var obj = this.associations[i];
			if (obj && obj.id == id){
				return obj;
				break;
			}
		}
		return null;
	}

	/**
	 * @method
	 * Returns the Association object getted from the AssociationsList throught the Association content.
	 * Format: {id:xx, description:yy}
	 *
	 * @param {String} ass The Association content.
	 */
	, getAssociationByAss: function(r){
		if (this.associations == null) return null;
		for (var i=0; i<this.associations.length; i++){
			var obj = this.associations[i];
			if (obj && obj.description == r){
				return obj;
				break;
			}
		}
		return null;
	}
});
