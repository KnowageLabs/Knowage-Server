/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  

/**
 * Object name 
 * 
 * 	Properties networkEscaped, networkLink, networkType, networkOptions must be defined in the custructors object config
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
 * - Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.network.NetworkContainerPanel', {
    extend: 'Ext.panel.Panel',
	networkObject: null, // the network object
	networInfo: null


	
	, constructor: function(config) {
		

    	var defaultSettings = {
    		 border: false,
   			 layout: 'border',
   			 region: 'center',
   			 style: 'height:100%; width:100%',
   			 items: []
    	};
    	
    	//build the contained panels
		this.networkObject = Ext.create('Sbi.network.NetworkObject',config); 
		defaultSettings.items.push(this.networkObject);
		if(config.networkInfo!=null && config.networkInfo!=undefined){
			this.networkInfo = Ext.create('Sbi.network.NetworkInfoPanel',config);
			defaultSettings.items.push(this.networkInfo);
		}
    	
    	Ext.apply(this,defaultSettings);
        
    	this.callParent(arguments);
        
    }

	, exportNetwork : function(mimeType) {
		this.networkObject.exportNetwork(mimeType);
	}

	
});