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

Ext.define('Sbi.network.NetworkInfoPanel', {
    extend: 'Ext.panel.Panel',
    info: null


	
	, constructor: function(config) {

		this.info = config.info;
		
		
		var title= 'Infos';
		if(config.networkInfo.title!=null && config.networkInfo.title!=undefined){
			title = config.networkInfo.title;
		}
		
		
    	var defaultSettings = {
    		title: title,
   			html: config.networkInfo.content,
   			layout: 'fit',
   			region: 'west',
   			collapsible: true,
   			width: 200
    	};
		
    	Ext.apply(this,defaultSettings);

        
    	this.callParent(arguments);
        
    }

	
});