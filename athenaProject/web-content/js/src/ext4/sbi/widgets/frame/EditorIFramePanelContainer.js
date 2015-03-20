/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * Container of an IFrame. It is a panel that contains an IFrame, so it gives some additional  features such as Toolbar.
 * It is an Abstract class all the additional functionalities should be implemented in the child class
 * 
 *     
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */
 
  
Ext.define('Sbi.widgets.EditorIFramePanelContainer', {
	extend: 'Ext.Panel',

	iframe: null

	, constructor : function(config) {
		Ext.apply(this, {}||config);
		this.init(config);
		this.callParent(arguments);
	}

	/**
	 * Init the component
	 * @param {Object} config configuration object
	 */
	, init: function(config){
		this.layout = 'fit';
		if(!this.iframe){
			this.iframe = Ext.create('Sbi.widgets.EditorIFramePanel',{}); 
		}
		this.items=[this.iframe];
	}
	
	/**
	 * loads the url into the frame
	 * @param {String} url The url to load
	 */
	, load: function(url){
		this.iframe.load(url);
	}
    
	
});