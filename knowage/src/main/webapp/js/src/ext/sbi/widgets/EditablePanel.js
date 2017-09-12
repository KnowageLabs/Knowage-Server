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