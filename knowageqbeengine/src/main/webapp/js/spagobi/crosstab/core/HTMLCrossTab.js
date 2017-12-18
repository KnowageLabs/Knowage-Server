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

//================================================================
//CrossTab
//================================================================
//
//The cross tab is a grid with headers and for the x and for the y. 
//it's look like this:
//       ----------------
//       |     k        |
//       ----------------
//       |  y  |  x     |
//       ----------------
//       |y1|y2|x1|x2|x3|
//-----------------------
//| | |x1|  |  |  |  |  |
//| | |------------------
//| |x|x2|  |  |  |  |  |
//| | |------------------
//|k| |x3|  |  |  |  |  |
//| |--------------------
//| | |y1|  |  |  |  |  |
//| |y|------------------
//| | |y2|  |  |  |  |  |
//-----------------------
//
//The grid is structured in 4 panels:
//         -----------------------------------------
//         |emptypanelTopLeft|    columnHeaderPanel|
// table=  -----------------------------------------
//         |rowHeaderPanel   |    datapanel        | 
//         -----------------------------------------

Ext.ns("Sbi.crosstab.core");

Sbi.crosstab.core.HTMLCrossTab = function(config) {

	var defaultSettings = {
		
  	};
	
	if(Sbi.settings && Sbi.settings.crosstab && Sbi.settings.crosstab.core && Sbi.settings.crosstab.core.htmlcrosstab) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.crosstab.core.htmlcrosstab);
	}

	var c = Ext.apply(defaultSettings, config || {});
	
	c = Ext.apply(c, {
  		html : c.htmlData
  		, border: false
  		, autoWidth: true
  		, autoScroll: true
	});
    
    Sbi.crosstab.core.HTMLCrossTab.superclass.constructor.call(this, c);
};
	
Ext.extend(Sbi.crosstab.core.HTMLCrossTab, Ext.Panel, {
    
});