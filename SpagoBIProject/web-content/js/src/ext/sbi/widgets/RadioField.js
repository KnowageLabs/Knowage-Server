/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * Sbi.widgets.CheckboxFiel
 * 
 * Authors
 *  - Andrea Gioia (mail)
 */

Ext.ns("Sbi.widgets");

Sbi.widgets.RadioField = Ext.extend(Sbi.widgets.CheckboxField, {
    allowBlank : true,
    blankText : "You must select one item in this group",
    defaultType : 'radio',
    groupCls: 'x-form-radio-group'
	
    , createOptionItem: function(optionConfig) {
    	var radio = new Ext.form.Radio(optionConfig);
    	radio.on('check', function() {
			this.fireEvent('change', this);
		}, this);
    	return radio;
    }

	, getValue : function(){
        var out = Sbi.widgets.RadioField.superclass.getValue.call(this);
        if(out.length > 1) alert("Assertion failed. In a radio field only one value can be checked at the same time");
        return out[0];
    }
    
	, getValues : function() {
    	return [this.getValue()];
	}	
	
	, getRawValue : function() {
		return this.getValues().join();
	}
});