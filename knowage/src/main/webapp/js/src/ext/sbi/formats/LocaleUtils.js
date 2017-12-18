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
 
  
 
Ext.ns("Sbi.locale");
Ext.ns("Sbi.commons");

Sbi.locale.dummyFormatter = function(v){return v;};
Sbi.locale.formatters = {
	//number: Sbi.locale.dummyFormatter,
	'int': Sbi.locale.dummyFormatter,
	'float': Sbi.locale.dummyFormatter,
	'string': Sbi.locale.dummyFormatter,		
	'date': Sbi.locale.dummyFormatter,
	'timestamp': Sbi.locale.dummyFormatter,
	'boolean': Sbi.locale.dummyFormatter,
	'html': Sbi.locale.dummyFormatter
};


if(Sbi.commons.Format){
	if(Sbi.locale.formats) {
		Sbi.locale.formatters['int']  = Sbi.commons.Format.numberRenderer(Sbi.locale.formats['int']);		
		Sbi.locale.formatters['float']  = Sbi.commons.Format.numberRenderer(Sbi.locale.formats['float']);		
		Sbi.locale.formatters['string']  = Sbi.commons.Format.stringRenderer(Sbi.locale.formats['string']);		
		Sbi.locale.formatters['date']   = Sbi.commons.Format.dateRenderer(Sbi.locale.formats['date']);
		Sbi.locale.formatters['timestamp']   = Sbi.commons.Format.dateRenderer(Sbi.locale.formats['timestamp']);
		Sbi.locale.formatters['boolean'] = Sbi.commons.Format.booleanRenderer(Sbi.locale.formats['boolean']);
		Sbi.locale.formatters['html']    = Sbi.commons.Format.htmlRenderer();
	} else {
		Sbi.locale.formatters['int']  = Sbi.commons.Format.numberRenderer( );	
		Sbi.locale.formatters['float']  = Sbi.commons.Format.numberRenderer( );	
		Sbi.locale.formatters['string']  = Sbi.commons.Format.stringRenderer( );		
		Sbi.locale.formatters['date']    = Sbi.commons.Format.dateRenderer( );
		Sbi.locale.formatters['timestamp']    = Sbi.commons.Format.dateRenderer( );	
		Sbi.locale.formatters['boolean'] = Sbi.commons.Format.booleanRenderer( );
		Sbi.locale.formatters['html']    = Sbi.commons.Format.htmlRenderer();
	}
};

Sbi.locale.localize = function(key) {
	var value = messageResource.get(key, 'messages');
	
	//If the message is not defined in the current language the english message is used
	if (value == key){
		value = messageResource.get(key, 'messages', 'en_US');
	}
	return value || key;
};

// alias
LN = Sbi.locale.localize;
FORMATTERS = Sbi.locale.formatters;