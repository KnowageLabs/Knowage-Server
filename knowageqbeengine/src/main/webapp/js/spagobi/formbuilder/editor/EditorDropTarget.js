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
  * - Andrea Gioia (mail)
  */

Ext.ns("Sbi.formbuilder");

Sbi.formbuilder.EditorDropTarget = function(targetPanel, config) {
	
	var c = Ext.apply({
		ddGroup    : 'formbuilderDDGroup',
		copy       : false
	}, config || {});
	
	Ext.apply(this, c);
	
	this.targetPanel = targetPanel;
	
	// constructor
    Sbi.formbuilder.EditorDropTarget.superclass.constructor.call(this, this.targetPanel.getEl(), c);
};

Ext.extend(Sbi.formbuilder.EditorDropTarget, Ext.dd.DropTarget, {
    
	targetPanel: null

    , notifyOver : function(ddSource, e, data) {
		return this.dropAllowed;
	}
	
	, notifyDrop : function(ddSource, e, data) {
		var rows = ddSource.dragData.selections;
		if(rows.length > 1 ) {
			Ext.Msg.show({
				   title:'Wrong dragged source',
				   msg: 'Select just one field please',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
			return;
		}
		
		this.notifyFieldDrop(rows[0].data);
	}
	
	, notifyFieldDrop: function(field) {
		if(this.onFieldDrop) {
			this.onFieldDrop.call(this.targetPanel, field);
		}
	}
	
	
	
});