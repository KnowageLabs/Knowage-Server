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
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.cockpit.widgets.crosstab");

Sbi.cockpit.widgets.crosstab.CrosstabDetailsWizard = function(config) {

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
	Sbi.cockpit.widgets.crosstab.CrosstabDetailsWizard.superclass.constructor.call(this, c);

    this.addEvents('apply');

};

Ext.extend(Sbi.cockpit.widgets.crosstab.CrosstabDetailsWizard, Ext.Window, {

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