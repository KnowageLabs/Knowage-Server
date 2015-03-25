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
 * Authors - Davide Zerbetto (davide.zerbetto@eng.it)
 */
Ext.ns("Sbi.tools.catalogue");

Sbi.tools.catalogue.MetaModelsCatalogue = function(config) {

	var defaultSettings = {
			configurationObject : {
				panelTitle : LN('sbi.tools.catalogue.metaModelsCatalogue')
				, listTitle : LN('sbi.tools.catalogue.metaModelsCatalogue')
			}
	};

	if (Sbi.settings && Sbi.settings.tools && Sbi.settings.tools.catalogue && Sbi.settings.tools.catalogue.metamodelscatalogue) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.tools.catalogue.metamodelscatalogue);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	var baseParams = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};

	// start services for main catalog list
	c.mainListServices = {
			'manageListService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'GET_META_MODELS_ACTION'
					, baseParams: baseParams
			})
			, 'saveItemService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'SAVE_META_MODEL_ACTION'
					, baseParams: baseParams
			})
			, 'deleteItemService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'DELETE_META_MODEL_ACTION'
					, baseParams: baseParams
			})
			, 'getCategoriesService' : Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName: 'domains/listValueDescriptionByType'
					, baseParams: {
						LIGHT_NAVIGATOR_DISABLED: 'TRUE',
						DOMAIN_TYPE:"BM_CATEGORY",
						EXT_VERSION: "3"
					}
			})	
	};
	// end services for main catalog list 

	// start services for item versions list
	c.singleItemServices = {
			'getVersionsService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'GET_META_MODEL_VERSIONS_ACTION'
					, baseParams: baseParams
			})
			, 'deleteVersionsService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'DELETE_META_MODEL_VERSIONS_ACTION'
					, baseParams: baseParams
			})
			, 'downloadVersionService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'DOWNLOAD_META_MODEL_VERSION_ACTION'
					, baseParams: baseParams
			})

	};
	// end services for item versions list

	// Set property for using a catalogue with categories
	c.isCategorizationEnabled = true;


	var dataSourceStore = new Ext.data.JsonStore(
			{
				url :  Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'datasourcespublic'}),
				autoLoad : true,
				fields : [ 'label'],
				restful : true
			});

	var dataSourcecombo = new Ext.form.ComboBox({
			fieldLabel : LN('sbi.tools.catalogue.metaModelsCatalogue.datasource'),
			store : dataSourceStore,
			width : 150,
			name: 'data_source_label',
			displayField : 'label',
			valueField : 'label',
			triggerAction : 'all'
		});

	c.additionalFormObjects={
			position: 4,
			items: [dataSourcecombo],
			itemFields:["data_source_label"],
			itemFieldsDefault:{
				data_source_label: ""
			}
	}

	Sbi.tools.catalogue.MetaModelsCatalogue.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.tools.catalogue.MetaModelsCatalogue, Sbi.widgets.Catalogue, {

});
