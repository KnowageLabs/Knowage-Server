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
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.execution");

Sbi.execution.ShortcutsPanel = function(config, doc) {
	
	config = Ext.applyIf(config, {border: false});
	
	this.subobjectsPanel =  new Sbi.execution.SubobjectsPanel(config, doc);
	this.snapshotsPanel =  new Sbi.execution.SnapshotsPanel(config, doc);
		
	var c = Ext.apply({}, config, {
		//layout:'accordion',
		activeTab: 0
	    , layoutConfig:{
	          animate:true
	    },
	    border: false,
	    items: this.getSortedPanels()
	});
	
	// constructor
    Sbi.execution.ShortcutsPanel.superclass.constructor.call(this, c);
    
    this.addEvents('subobjectexecutionrequest', 'snapshotexcutionrequest','subobjectshowmetadatarequest');
        
    this.subobjectsPanel.on('executionrequest', function(subObjectId) {
    	this.fireEvent('subobjectexecutionrequest', subObjectId);
    }, this);
    
    this.snapshotsPanel.on('executionrequest', function(snapshotId) {
    	this.fireEvent('snapshotexcutionrequest', snapshotId);
    }, this);
    
     this.subobjectsPanel.on('showmetadatarequest', function(subObjectId) {
    	this.fireEvent('subobjectshowmetadatarequest', subObjectId);
    }, this);
    
};

Ext.extend(Sbi.execution.ShortcutsPanel, Ext.TabPanel, {
	
    subobjectsPanel: null
    , snapshotsPanel: null		
	
	, synchronize: function( executionInstance ) {
	
		this.synchronizeSubobjects(executionInstance);
		this.synchronizeSnapshots(executionInstance);
	}
	
	, synchronizeSubobjects: function( executionInstance ) {
		this.subobjectsPanel.synchronize( executionInstance );
	}
	
	//method use with checkbox , synchronizeSubobjectsAndOpenMetadata: function( id, meta, executionInstance ) {
	, synchronizeSubobjectsAndOpenMetadata: function( id, executionInstance ) {
		// synchronize subobjects
		this.subobjectsPanel.synchronize( executionInstance );
		// open the metadata windows if so chosen in saveWindow
		//if(meta == true) {
		this.subobjectsPanel.openMetadataWindowAfterSaving( id, executionInstance );

		//}
	}
	, synchronizeSnapshots: function( executionInstance ) {
		this.snapshotsPanel.synchronize( executionInstance );
	}
	
	, getSortedPanels: function() {
		var toReturn = new Array();
		if(Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.shortcutsPanel && Sbi.settings.execution.shortcutsPanel.panelsOrder) {
			var subobjectsPosition = Sbi.settings.execution.shortcutsPanel.panelsOrder.subobjects - 1;
			var snapshotsPosition = Sbi.settings.execution.shortcutsPanel.panelsOrder.snapshots - 1;
			toReturn[subobjectsPosition] = this.subobjectsPanel;
			toReturn[snapshotsPosition] = this.snapshotsPanel;
		} else {
			toReturn[0] = this.subobjectsPanel;
			toReturn[1] = this.snapshotsPanel;
		}
		return toReturn;
	}
	
});