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
Sbi.locale.ln = Sbi.locale.ln || new Array();

Sbi.locale.formats = {
		
		float: {
			decimalSeparator: ',',
			decimalPrecision: 2,
			groupingSeparator: '.',
			groupingSize: 3,
			nullValue: ''
		},
		int: {
			decimalSeparator: ',',
			decimalPrecision: 0,
			groupingSeparator: '.',
			groupingSize: 3,
			nullValue: ''
		},
	
		string: {
			trim: true,
    		maxLength: null,
    		ellipsis: true,
    		changeCase: null, 
    		nullValue: ''
		},
		
		date: {
			dateFormat: 'd/m/Y',
    		nullValue: ''
		},
		
		timestamp: {
			dateFormat: 'd/m/Y H:i:s',
    		nullValue: ''
		},
		
		boolean: {
			trueSymbol: 'vero',
    		falseSymbol: 'falso',
    		nullValue: ''
		}
		
};

//===================================================================
//MESSAGE BOX BUTTONS
//===================================================================
Ext.Msg.buttonText.yes = 'S\u00ed'; 
Ext.Msg.buttonText.no = 'No';

if(Ext.DatePicker){
	Ext.override(Ext.DatePicker, {
	todayText : "Hoy",
	minText : "La Fecha es anterior al minimo permitido",
	maxText : "La Fecha es posterior al maximo permitido",
	disabledDaysText : "",
	disabledDatesText : "",
	monthNames : ["Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"],
	dayNames : ["Domingo","Lunes","Martes","Miercoles","Jueves","Viernes","Sabado"],
	nextText: 'Mes Siguiente (Control+Right)',
	prevText: 'Mes Anterior (Control+Left)',
	monthYearText: 'Elija un Mes (Control+Up/Down moverse entre a\u00c3\u00b1os)',
	todayTip : "{0} (Spacebar)",
	okText : " OK ",
	cancelText : "Cancelar",
	format : "dd/mm/yyyyy",
	startDay : 1
	});
	}
