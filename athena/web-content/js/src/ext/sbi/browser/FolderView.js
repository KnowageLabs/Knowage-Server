/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.ns("Sbi.browser");


Sbi.browser.FolderView = function(config) {
	
	this.tpl =  new Sbi.browser.FolderViewTemplate(config);
	this.viewState = {
		filterType: 'all'
		, sortGroup: 'Documents'
		, sortAttribute: 'name'
		, groupGroup: 'Documents'
		, groupAttribute: 'ungroup'
	};
    config.store.on('load', this.onLoad, this, {groupIndex: 'samples'});
       
    Sbi.browser.FolderView.superclass.constructor.call(this, config);
}; 
    
    
Ext.extend(Sbi.browser.FolderView, Ext.DataView, {
    frame:true
    , itemSelector: 'dd'
//    , overClass: 'over'
    , groups: null
    , lookup: null
    , viewState: null
    , ready: false
    , filteredProperties: null
    , tpl : null

    , onClick : function(e){
        // is of type Ext.EventObject		
        var group = e.getTarget('div[class=group-header]', 10, true);
        if(group){
            group.up('div[class*=group]').toggleClass('collapsed');
        }
        
        return Sbi.browser.FolderView.superclass.onClick.apply(this, arguments);
    }
    
	
    , onMouseOver : function(e) {    
//      var group = e.getTarget('div[class=group-header]', 10, true);
//      if(!group){
//            var d = e.getTarget('[class*=group-item]', 5, true);
//            if(d){
//                var t = d.first('div[class*=item-control-panel]', false);
//                if(t){   
//                  t.applyStyles('visibility:visible');
//                }
//            }
//        }
//        return Sbi.browser.FolderView.superclass.onMouseOver.apply(this, arguments);
    }
    
    , onMouseOut : function(e){
//        var group = e.getTarget('div[class=group-header]', 10, true);
//        if(!group){
//            var d = e.getTarget('[class*=group-item]', 5, true);
//            if(d){
//                var t = d.first('div[class*=item-control-panel]', false);
//                if(t){   
//                  t.applyStyles('visibility:hidden');
//                }
//            }
//        }
//        return Sbi.browser.FolderView.superclass.onMouseOut.apply(this, arguments);
    }
    
    , onLoad : function(s, r) {
    	this.groups = this.store.getRange();
    	this.ready = true;
    	this.applyState();
    }
    
    , createIndex : function() {
      var id = 0;   
      
      this.lookup = {};
      
      var groups = this.store.getRange(0, this.store.getCount());
      for(var i = 0; i < groups.length; i++) {
        var records = groups[i].data['samples'];
        for(var j = 0; j < records.length; j++) {
          this.lookup[id++] = records[j];
        }        
      }
    }
    
    , getRecord : function(n){
   //   var i = (typeof n == 'number')?n:n.viewIndex;
    	var i;
    	if (typeof n == 'number'){
    		i = n;
    	}else if (typeof n == 'string') {
    		var j=0;
    		for (el in this.lookup){
    			var doc = this.lookup[el];
    			if (doc.label === n){
    				i = j;
    				break;
    			}
    			j++;
    		}
    	}else{
    		i = viewIndex;
    	}
    	
     
        return this.lookup[i];
    }
    
    , reset : function() {
    	this.store.removeAll();
    	this.store.add( this.groups );
    }
    
    , getCollection : function(groupName) {  
    	var collection = null;
    	
    	var groupIndex = this.store.find('title', groupName);    	
    	if(groupIndex === -1) return null;
    	var group = this.store.getAt( groupIndex );
    	var records = group.data['samples'];
    	collection = new Ext.util.MixedCollection(false);
    	collection.addAll(records);
    	
    	return collection;
    }
    
    
    , applyState : function() {
    	if(!this.ready) return;
    	this.reset();
    	this.localize();
    	this.applyFilter(this.viewState.filterType);
    	this.applySort(this.viewState.sortGroup, this.viewState.sortAttribute);
    	this.applyGroup(this.viewState.groupGroup, this.viewState.groupAttribute);
    	this.createIndex();
    	this.refresh();
    }
    
    
    , localize: function() {
    	 var groups = this.store.getRange(0, this.store.getCount());
         for(var i = 0; i < groups.length; i++) {
        	 groups[i].data.titleLabel = LN(groups[i].data.title);
         }
    }
    
    , sort : function(groupName, attributeName) { 
    	this.viewState.sortGroup = groupName;
    	this.viewState.sortAttribute = attributeName;
    	this.applyState();
    	
    	/*
    	this.reset();
    	this.applySort(groupName, attributeName);
    	this.createIndex();
    	this.refresh();
    	*/
    }
    
    , applySort : function(groupName, attributeName) {
    	var collection = this.getCollection(groupName, attributeName);
    	if(collection == null) return;
    	collection.sort('ASC', function(r1, r2) {
            var v1 = r1[attributeName], v2 = r2[attributeName];
            return v1 > v2 ? 1 : (v1 < v2 ? -1 : 0);
        });
    	
    	var groupIndex = this.store.find('title', groupName);    	
    	var group = this.store.getAt( groupIndex );
    	group.data['samples'] = collection.getRange();   
    }
    
    , group : function(groupName, attributeName) { 
    	
    	this.viewState.groupGroup = groupName;
    	this.viewState.groupAttribute = attributeName;
    	this.applyState();
    	/*
    	this.reset();
    	this.applyGroup(groupName, attributeName);
    	this.createIndex();
    	this.refresh();
    	*/
    }
    
    , applyGroup : function(groupName, attributeName) { 
    	if(attributeName === 'ungroup') return;
    	var collection = this.getCollection(groupName);
    	if(collection == null) return;
    	var distinctValues = {};
    	collection.each(function(item) {
    		distinctValues[item[attributeName]] = true;
    	});
    	var groupIndex = this.store.find('title', groupName);    	
    	var group = this.store.getAt( groupIndex );
    	this.store.remove(group);    
    	
    	var GroupRecord = Ext.data.Record.create([
    	    {name: 'title', type: 'string'},
    	    {name: 'icon', type: 'string'}, 
    	    {name: 'samples', type: 'string'}
    	]);
    	
    	for(var p in distinctValues) {
    		var newGroup = collection.filter(attributeName, p);
    		this.store.add([
    		          	  new GroupRecord({
    		          		  title: p
    		          		  , icon: 'document.png'
    		          		  , samples: newGroup.getRange()
    		          	  })
    		          	]);
    	}
    	
    }
    
    , filter : function(type) {
    	this.viewState.filterType = type;
    	this.applyState();
    	/*
    	this.reset();
    	this.applyFilter(type);
    	this.createIndex();
    	this.refresh();
    	*/
    }
    
    , inMemoryFilter : function(value) {
    	var RE = new RegExp(value, "ig");
    	var foldersProperties = [ "code", "name","description"];
    	var documentsProperties = [ "code", "name","description","creationUser", "engine", "stateCode", "typeCode", "label"];
    	var newSamples = [];
    	
    	this.reset();
    	
    	var GroupRecord = Ext.data.Record.create([
			                                  	    {name: 'title', type: 'string'},
			                                  	    {name: 'icon', type: 'string'}, 
			                                  	    {name: 'samples', type: 'string'}
			                                  	]);    	
    	
    	//filter on folders and documents
    	var folders = this.getCollection('Folders');    	    	
    	for (var i=0; i< foldersProperties.length;i++){
    		var property = foldersProperties[i];    		
    		var newFoldersGroup = folders.filter(property, RE);
    		var tmpValues = newFoldersGroup.getRange();
    		if (tmpValues != null && tmpValues.length > 0){    			
    			newSamples = this.mergeArrays(newSamples,tmpValues);
    		}
    	}
    	var documents = this.getCollection('Documents');
    	for (var i=0; i< documentsProperties.length;i++){
    		var property = documentsProperties[i];
    		var newDocumentsGroup = documents.filter(property, RE);
    		var tmpValues = newDocumentsGroup.getRange();
    		if (tmpValues != null && tmpValues.length > 0){    			
    			newSamples = this.mergeArrays(newSamples,tmpValues);
    		}
    	}
		
    	this.store.removeAll(); 
    	//add folders if present
    	if (newSamples.length > 0){
			this.store.add([
				          	  new GroupRecord({
				          		  title: 'Documents'
				          		  , icon: 'document.png'
				          		  , samples: newSamples
				          	  })
				          	]);
    	}
    	this.inMemorySort('creationDate');
    }
    
    , inMemorySort : function(attributeName) {
    	var collection = this.getCollection('Documents');
    	if(collection == null) return;
    	var type = 'ASC';
    	if (attributeName == 'creationDate') type = 'DESC';  	
    	collection.sort(type, function(r1, r2) {
            var v1 =(r1[attributeName]==undefined)?0:r1[attributeName].toUpperCase();
            var v2 = (r2[attributeName]==undefined)?0:r2[attributeName].toUpperCase();
            var result;
            //if (type == 'DESC'){
            if (type == 'ASC'){
            	result = v1 < v2 ? 1 : (v1 > v2 ? -1 : 0);            	
            }else{
            	result = v1 > v2 ? 1 : (v1 < v2 ? -1 : 0);
            }
            return result;
        });
    	
    	var groupIndex = this.store.find('title', 'Documents');    	
    	var group = this.store.getAt( groupIndex );        	
    	group.data['samples'] = collection.getRange();
    	
    	this.refresh();
    }
    
    , applyFilter: function(type) {
    	if(type === 'folders') {
    		var groupIndex = this.store.find('title', 'Folders'); 
    		if(groupIndex !== -1) {
    			var group = this.store.getAt( groupIndex );
    			this.store.removeAll();  
    			this.store.add([group]);
    		}
    	} else if (type === 'documents') {
    		var groupIndex = this.store.find('title', 'Folders');  
    		if(groupIndex !== -1) {
	        	var group = this.store.getAt( groupIndex );
	        	this.store.remove(group);   
    		}
    	}    	
    }
    
    ,mergeArrays: function(array1, array2){
		if(array1){
			if(array2){
				for(var i=0; i<array2.length; i++){
					if(array1.indexOf(array2[i])<0){
						array1.push(array2[i]);
					}
				}
			}
			return  array1
		}else{
			return  array2;
		}
	}
});