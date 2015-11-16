/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.ns("Sbi.browser");

Sbi.browser.FilterPanel = function(config) { 
	
	this.sortRadioGroup = new Array();
	this.groupRadioGroup = new Array();
	
	var label;
	
	// set defaults
	label = LN('sbi.browser.misc.x') + ' ' + LN('sbi.browser.document.name');
	this.sortRadioGroup.push(
			new Ext.form.Radio({
				boxLabel: label, name: 'sort', hideLabel: true, inputValue: 'name', checked: true
			})
	);
	
	this.groupRadioGroup.push(
			new Ext.form.Radio({
				boxLabel: LN('sbi.browser.misc.ungroup'), name: 'group', hideLabel: true, inputValue: 'ungroup', checked: true
			})
	);
	
	// load other values
	if (config.browserConfig &&  config.browserConfig.metaDocument){
		for(var i = 0; i < config.browserConfig.metaDocument.length; i++) {
			var meta = config.browserConfig.metaDocument[i];
			
			if(meta.sortable && meta.id != 'name') {
				label = LN('sbi.browser.misc.x') + ' ' + LN('sbi.browser.document.' + meta.id);
				this.sortRadioGroup.push(
						new Ext.form.Radio({
							boxLabel: label, name: 'sort', hideLabel: true, inputValue: meta.id
							//boxLabel: 'by ' + meta.id, name: 'sort', hideLabel: true, inputValue: meta.id
						})
				);
			}
			
			if(meta.groupable) {
				label = LN('sbi.browser.misc.x') + ' ' + LN('sbi.browser.document.' + meta.id);
				this.groupRadioGroup.push(
						new Ext.form.Radio({
							boxLabel: label, name: 'group', hideLabel: true, inputValue: meta.id
						})
				);
			}
		}
	}	
	// init check boxes
	for(var i = 0; i < this.sortRadioGroup.length; i++) {
		this.sortRadioGroup[i].on('check', this.onSortGroupClick, this);
	}
	var sortGroup = new Ext.form.FieldSet({
	   title: LN('sbi.browser.filtrpanel.sortgroup.title'),
	   collapsible: true,
	   autoHeight:true,
	   defaultType: 'textfield',
	   items : this.sortRadioGroup
	});
	
	for(var i = 0; i < this.groupRadioGroup.length; i++) {
		this.groupRadioGroup[i].on('check', this.onGroupGroupClick, this);
	}
	var groupGroup = new Ext.form.FieldSet({
	   title: LN('sbi.browser.filtrpanel.groupgroup.title'),
	   collapsible: true,
	   autoHeight:true,
	   defaultType: 'textfield',
	   items : this.groupRadioGroup
	});
	
	// filter group
	this.filterRadioGroup = new Array();
	this.filterRadioGroup[0] =  new Ext.form.Radio({boxLabel: LN('sbi.browser.filtrpanel.filtergroup.opt.all'), name: 'filter', hideLabel: true, inputValue: 'all', checked: true});
	this.filterRadioGroup[1] =  new Ext.form.Radio({boxLabel: LN('sbi.browser.filtrpanel.filtergroup.opt.folders'), name: 'filter', hideLabel: true, inputValue: 'folders'});
	this.filterRadioGroup[2] = new Ext.form.Radio({boxLabel: LN('sbi.browser.filtrpanel.filtergroup.opt.documents'), name: 'filter', hideLabel: true, inputValue: 'documents'});  
	for(var i = 0; i < this.filterRadioGroup.length; i++) {
		this.filterRadioGroup[i].on('check', this.onFilterGroupClick, this);
	}
	var filterGroup = new Ext.form.FieldSet({
		   title: LN('sbi.browser.filtrpanel.filtergroup.title'),
		   collapsible: true,
		   autoHeight:true,
		   defaultType: 'textfield',
		   items : this.filterRadioGroup
	});
    
	var c = Ext.apply({}, config, {
		bodyStyle:'padding:6px 6px 6px 6px; background-color:#FFFFFF'
		, autoScroll: true
		, items: [ sortGroup ,  groupGroup, filterGroup	]
		        
	});   
	
	Sbi.browser.FilterPanel.superclass.constructor.call(this, c);   
	
	this.addEvents("onsort", "ongroup", "onfilter");
};

Ext.extend(Sbi.browser.FilterPanel, Ext.FormPanel, {
    
	sortRadioGroup : null
	, groupRadioGroup : null
	, filterRadioGroup : null
	
	, onSortGroupClick : function(cb, checked) {
		if(checked) {
			this.fireEvent('onsort', this, cb);
		}
	}

	, onGroupGroupClick : function(cb, checked) {
		if(checked) {
			this.fireEvent('ongroup', this, cb);
		}
	}
	
	, onFilterGroupClick : function(cb, checked) {
		if(checked) {
			this.fireEvent('onfilter', this, cb);
		}
	}
    
});