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