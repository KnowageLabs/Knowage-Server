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

Ext.ns("Sbi.widgets");

Sbi.widgets.GenericDropTarget = function(targetPanel, config) {
	
	var c = Ext.apply({
		//ddGroup must be provided by input config object!!
		copy       : false
	}, config || {});
	
	Ext.apply(this, c);
	
	this.targetPanel = targetPanel;
	
	// constructor
    Sbi.widgets.GenericDropTarget.superclass.constructor.call(this, this.targetPanel.getEl(), c);
};

Ext.extend(Sbi.widgets.GenericDropTarget, Ext.dd.DropTarget, {
    
	targetPanel: null
	
	/*
    , notifyOver : function(ddSource, e, data) {
		return this.dropAllowed;
	}
	*/
	
	, notifyDrop : function(ddSource, e, data) {
		if (this.onFieldDrop) {
			this.onFieldDrop.call(this.targetPanel, ddSource);
		}
	}
	
});