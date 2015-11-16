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

Ext.ns("Sbi.formbuilder");

Sbi.formbuilder.FormPreviewPage = function(config) {
	
	var defaultSettings = {
		//title: LN('sbi.formbuilder.formpreviewpage.title')
		defaultSrc: 'about:blank'
		, autoLoad: true
        , loadMask: {msg: 'Loading...'}
        , fitToParent: true  // not valid in a layout
        , disableMessaging: true
	};
	
	if(Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.formPreviewPage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.formPreviewPage);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
	
	// constructor
	Sbi.formbuilder.FormPreviewPage.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.formbuilder.FormPreviewPage, Ext.ux.ManagedIFramePanel, {
    
});