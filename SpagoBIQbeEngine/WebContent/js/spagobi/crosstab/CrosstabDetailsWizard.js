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

Ext.ns("Sbi.crosstab");

Sbi.crosstab.CrosstabDetailsWizard = function(config) {
	
	var defaultSettings = {
		title: LN('sbi.crosstab.crosstabdetailswizard.title')
		, width: 400
  	};
	if(Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.crosstabDetailsWizard) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.crosstabDetailsWizard);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);

	this.init(c);
	
	c = Ext.apply(c, {
		closeAction: 'hide'
      	, items: [this.crosstabDetailsForm]
	});
	
	// constructor
    Sbi.crosstab.CrosstabDetailsWizard.superclass.constructor.call(this, c);
	
    this.addEvents('apply');
    
};

Ext.extend(Sbi.crosstab.CrosstabDetailsWizard, Ext.Window, {
    
	crosstabDetailsForm: null
	
	, init: function(c) {
	
		var checkboxTotalRows = new Ext.form.Checkbox({boxLabel: LN('sbi.crosstab.crosstabdetailswizard.calculatetotalsonrows'), name: 'calculatetotalsonrows'});
		var checkboxTotalColumns = new Ext.form.Checkbox({boxLabel: LN('sbi.crosstab.crosstabdetailswizard.calculatetotalsoncolumns'), name: 'calculatetotalsoncolumns'});
		
		var crosstabCellLimit = Sbi.config.crosstabCellLimit;
		if(crosstabCellLimit==null || crosstabCellLimit==undefined){
			crosstabCellLimit=0;
		}

		var items = [
            {
	            xtype: 'radiogroup'
	            , fieldLabel: LN('sbi.crosstab.crosstabdetailswizard.measureson')
	            , items: [
	                {boxLabel: LN('sbi.crosstab.crosstabdetailswizard.rows'), name: 'measureson', inputValue: 'rows'}
	                , {boxLabel: LN('sbi.crosstab.crosstabdetailswizard.columns'), name: 'measureson', inputValue: 'columns', checked: true}
	            ]
            }
            , {
                xtype: 'checkboxgroup',
                fieldLabel: LN('sbi.crosstab.crosstabdetailswizard.onrows'),
                itemCls: 'x-check-group-alt',
                columns: 1,
                items: [
                    checkboxTotalRows
                    , {boxLabel: LN('sbi.crosstab.crosstabdetailswizard.calculatesubtotalsonrows'), name: 'calculatesubtotalsonrows'}
                ]
            }
            , {
                xtype: 'checkboxgroup',
                fieldLabel: LN('sbi.crosstab.crosstabdetailswizard.oncolumns'),
                columns: 1,
                items: [
                    checkboxTotalColumns
                    , {boxLabel: LN('sbi.crosstab.crosstabdetailswizard.calculatesubtotalsoncolumns'), name: 'calculatesubtotalsoncolumns'}
                ]
            }, 
            {
	            xtype: 'radiogroup'
	            , fieldLabel: LN('sbi.crosstab.crosstabdetailswizard.percenton')
	            , itemCls: 'x-check-group-alt'
	            , items: [
	                  //radioPercentageRows
	                  {boxLabel: LN('sbi.crosstab.crosstabdetailswizard.row'), name: 'percenton', inputValue: 'row', listeners:{'check':{fn: function(radio, checked){checkboxTotalRows.setValue(checked);}, scope: this}}}
	                , {boxLabel: LN('sbi.crosstab.crosstabdetailswizard.column'), name: 'percenton', inputValue: 'column', listeners:{'check':{fn:  function(radio, checked){checkboxTotalColumns.setValue(checked);}, scope: this}}}
	                , {boxLabel: LN('sbi.crosstab.crosstabdetailswizard.nopercent'), name: 'percenton', inputValue: 'no', checked: true}
	            ]
            }
		];
		if (this.isStatic == false) {
            items.push({
	            xtype: 'field'
	            , name: 'maxcellnumber' 
	            , fieldLabel: LN('sbi.crosstab.crosstabdetailswizard.maxcellnumber')
	            , value: crosstabCellLimit
            });
		}
		
		this.crosstabDetailsForm = new Ext.form.FormPanel({
			frame: true
			, labelWidth : 150
			, items : items
			, buttons: [{
    			text: LN('sbi.crosstab.crosstabdetailswizard.buttons.apply')
    		    , handler: function() {
    	    		this.fireEvent('apply', this.getFormState(), this);
                	this.hide();
            	}
            	, scope: this
    	    },{
    		    text: LN('sbi.crosstab.crosstabdetailswizard.buttons.cancel')
    		    , handler: function(){ this.hide(); }
            	, scope: this
    		}]
		});
		
	}

	, getFormState: function() {
		return this.crosstabDetailsForm.getForm().getValues();
	}
	
	, setFormState: function(values) {
		this.crosstabDetailsForm.getForm().reset(); // it is mandatory, since setValues method does not work properly for checkboxes
		this.crosstabDetailsForm.getForm().setValues(values);
	}
		
});