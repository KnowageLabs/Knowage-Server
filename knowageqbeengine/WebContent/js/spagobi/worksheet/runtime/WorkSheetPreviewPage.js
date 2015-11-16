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
  * - Alberto Ghedin (alberto.ghedin@eng.it)
  */

Ext.ns("Sbi.worksheet.runtime");

Sbi.worksheet.runtime.WorkSheetPreviewPage = function(config) {
	
	var defaultSettings = {
		//title: LN('sbi.worksheet.runtime.worksheetpreviewpage.title')
		defaultSrc: 'about:blank'
		, border : false
		, autoLoad: true
        , loadMask: {msg: 'Loading...'}
        , fitToParent: true  // not valid in a layout
        , disableMessaging: true
	};
	
	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.runtime && Sbi.settings.worksheet.runtime.workSheetPreviewPage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.runtime.workSheetPreviewPage);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
	
	// constructor
	Sbi.worksheet.runtime.WorkSheetPreviewPage.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.worksheet.runtime.WorkSheetPreviewPage, Ext.ux.ManagedIFramePanel, {
    
});