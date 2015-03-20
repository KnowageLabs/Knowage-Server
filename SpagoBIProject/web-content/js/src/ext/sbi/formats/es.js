/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
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
