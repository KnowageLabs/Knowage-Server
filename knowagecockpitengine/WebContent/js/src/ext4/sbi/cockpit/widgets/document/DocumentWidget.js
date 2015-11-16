/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.document");
/**
 * @cfg {Object} config
 * ...
 */
Sbi.cockpit.widgets.document.DocumentWidget = function(config) {
	Sbi.trace("[DocumentWidget.constructor]: IN");
	this.initConfig(config);
	var defaultSettings= {};
	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.document.DocumentWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);
	
	// constructor
	Sbi.cockpit.widgets.document.DocumentWidgetDesigner.superclass.constructor.call(this, c);
	
	this.createContent();
	
	this.on("afterrender", function(){
		if(Sbi.isNotValorized(this.documentId)){
    		this.documentId = Sbi.commons.Constants.DOCUMENT_WIDGET_STORE_PREFIX + this.getWidgetManager().widgets.getCount() + '_' + this.wconf.documentLabel;
    		this.wconf.documentId = this.documentId;
    	}
		var docParametersStore = Ext.create('Ext.data.Store', {
			storeId: this.documentId,
		    fields: [{name: 'alias', mapping: 'parameterUrlName'},
		             {name: 'colType', mapping: 'fieldType'},
		             {name: 'values', mapping: 'value'}],
		    data : this.parameters,
		    proxy: {
		        type: 'memory',
		        reader: {
		            type: 'json'
		        }
		    }
		 });
		Sbi.storeManager.addStore(docParametersStore);
		this.store = docParametersStore;
		this.getParentComponent().refreshTitle();
	}, this);
	
	Sbi.trace("[DocumentWidget.constructor]: OUT");
};
Ext.extend(Sbi.cockpit.widgets.document.DocumentWidget, Sbi.cockpit.core.WidgetRuntime, {
   
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	widgetContent: null,
	externalParameterMap: new Ext.util.HashMap(),
	documentId: null,
	store: null,
    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
	refresh:  function() {
    	Sbi.trace("[DocumentWidget.refresh]: IN");
    	Sbi.cockpit.widgets.document.DocumentWidget.superclass.refresh.call(this);
		this.createContent();
		this.doLayout();
		Sbi.trace("[DocumentWidget.refresh]: OUT");
	},
	getStore: function(){
		return this.store;
	},
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------
	
	createContent: function() {
    	Sbi.trace("[DocumentWidget.createContent]: IN");
    	var parametersString = "";
    	this.documentId = this.wconf.documentId;
    	this.parameters = this.wconf.parameters?this.wconf.parameters:[];
    	if(this.parameters){
    		parametersString = "";
    		Ext.Array.forEach(this.parameters, function(param){
    			// external parameters will override configured parameters
    			var value = this.externalParameterMap.containsKey(param.parameterUrlName) ? this.externalParameterMap.get(param.parameterUrlName) : param.value;
    			parametersString += param.parameterUrlName + "=" + value + "&";
    		},this);
    		parametersString = "&PARAMETERS="+encodeURIComponent(parametersString);
    	}
    	var url = Sbi.config.contextName+'/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ACTION&NEW_SESSION=TRUE&TOOLBAR_VISIBLE=FALSE&OBJECT_LABEL='+this.wconf.documentLabel+parametersString;
    	
    	this.widgetContent = new Ext.ux.IFrame({xtype:'uxiframe',src: url,style: {height: '100%', width: '100%'}});

		if(this.items){
			this.items.each( function(item) {
				this.items.remove(item);
				item.destroy();
			}, this);
		}
		
		this.add(this.widgetContent);
		
		
		Sbi.trace("[DocumentWidget.createContent]: OUT");
	},
	/**
	 * @method sets new values into externalParameterMap
	 * 
	 * @param {Array} array of selections
	 * @param {Object} associations
	 * @param {boolean} if set to true, selected parameter will be removed from externalParameterMap
	 */
	setExternalParameters: function(selections, associationGroup, clear){
		if(Sbi.isValorized(selections) && associationGroup && associationGroup.associations){
			var selectedValue = '';
			var associationId = '';
			for(var fieldHeader in selections){
				if(fieldHeader.indexOf('.') == -1){
					associationId = fieldHeader;
				}else{
					selectedValue = selections[fieldHeader][0];
				}
			}
			var associations = associationGroup.associations;
			for(var i = 0; i < associations.length; i++){
				var association = associations[i];
				if(association.id == associationId){
					for(var j = 0; j < association.fields.length; j++){
						var field = association.fields[j];
						if(field.store == this.documentId){
							if(clear){
								this.externalParameterMap.removeAtKey(field.column);
							}else{
								this.externalParameterMap.add(field.column, selectedValue);
							}
						}
					}
				}
			}
		}
	}

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================

	// ...
});

Sbi.registerWidget('document', {
	name: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.name')
	, icon: 'js/src/ext4/sbi/cockpit/widgets/document/dummy_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.document.DocumentWidget'
	, designerClass: 'Sbi.cockpit.widgets.document.DocumentWidgetDesigner'
});