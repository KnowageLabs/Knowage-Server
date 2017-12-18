/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
 
  
 
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