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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Davide Zerbetto (davide.zerbetto@eng.it)
 */

Ext.ns("Sbi.widgets");

Sbi.widgets.EditablePanel = function(config) {
    
    this.fieldName = config.fieldName || '';
    
    this.contentPanel = new Ext.Panel({
        html : '<div class="x-editable-panel">' + config.html + '</div>'
        , layout: 'fit'
        , autoScroll: false
    });
    delete config.html;
    
    // initial state is 'view'
    this.state = 'view';
    
    this.htmlEditor = null;
    
    var c = Ext.apply({}, config, {
        items : [this.contentPanel]
        , autoScroll: true
    });
    
    Sbi.widgets.EditablePanel.superclass.constructor.call(this, c);
    
    this.addEvents('change');
    
    if (config.editable === undefined || config.editable !== false) {
        
        this.on('render', function(panel) {
            panel.getEl().on('click', function() {
            	if (this.state === 'view') {
            		Ext.QuickTips.init();
	                this.htmlEditor = new Ext.form.HtmlEditor({
	                    value: this.contentPanel.body.dom.childNodes[0].innerHTML
	                    , name: this.fieldName
	                    , enableLists: false
	                    
	                });
	                this.htmlEditor.setSize(this.getSize());
	                this.htmlEditor.on('sync', function () {
	                    this.fireEvent('change', this, this.htmlEditor.getValue());
	                }, this);
	                this.remove(this.contentPanel);
	                //this.doLayout();
	                this.add(this.htmlEditor);
	                this.doLayout();

	                
	                // workaround: in IE the double-click does not make the editing cursor appear (this happens very often)
	                this.htmlEditor.focus(); 
	                
	                this.htmlEditor.syncSize();
	                // state changes to 'edit'
	                this.state = 'edit';
            	}
                
            }, this);
        }, this);
        
    }

};

Ext.extend(Sbi.widgets.EditablePanel, Ext.Panel, {

	state: null
	
    , commitChanges: function() {
    	if (this.state === 'edit') {
	        var newHtml = this.htmlEditor.getValue();
	        if(this.contentPanel !== undefined && this.contentPanel !== null){
		        this.contentPanel = new Ext.Panel({
		            html : '<div class="x-editable-panel">' + newHtml + '</div>'
		            , layout: 'fit'
		        });   
		        this.add(this.contentPanel);
		        this.doLayout();
	        }
	        this.remove(this.htmlEditor);
	        this.doLayout();
	        
            // state returns to 'view'
            this.state = 'view';
        }       
    }
    
});