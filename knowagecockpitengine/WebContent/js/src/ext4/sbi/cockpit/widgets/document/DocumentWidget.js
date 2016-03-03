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
    	var url = Sbi.config.contextName+'/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ACTION&NEW_SESSION=TRUE&TOOLBAR_VISIBLE=FALSE&OBJECT_LABEL='+this.wconf.documentLabel+parametersString+"&SELECTED_ROLE="+Sbi.config.currentRole;
    	
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