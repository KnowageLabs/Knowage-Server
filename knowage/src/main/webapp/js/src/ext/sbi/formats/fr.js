Ext.ns("Sbi.locale");
Sbi.locale.ln = Sbi.locale.ln || new Array();

Sbi.locale.formats = {
		/*
		number: {
			decimalSeparator: '.',
			decimalPrecision: 2,
			groupingSeparator: ',',
			groupingSize: 3,
			//currencySymbol: '$',
			nullValue: ''
		},
		*/
		
		float: {
			decimalSeparator: ',',
			decimalPrecision: 2,
			groupingSeparator: ' ',
			groupingSize: 3,
			//currencySymbol: '$',
			nullValue: ''
		},
		int: {
			decimalSeparator: ',',
			decimalPrecision: 0,
			groupingSeparator: ' ',
			groupingSize: 3,
			//currencySymbol: '$',
			nullValue: ''
		},
		
		string: {
			trim: true,
    		maxLength: null,
    		ellipsis: true,
    		changeCase: null, // null | 'capitalize' | 'uppercase' | 'lowercase'
    		//prefix: '',
    		//suffix: '',
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
			trueSymbol: 'true',
    		falseSymbol: 'false',
    		nullValue: ''
		}
};

//===================================================================
//MESSAGE BOX BUTTONS
//===================================================================
Ext.Msg.buttonText.yes='Oui';
Ext.Msg.buttonText.no='Non';

if(Ext.DatePicker){
	Ext.override(Ext.DatePicker, {
	todayText : "Aujourd'hui",
	minText : "Cette date est ant\u00e9rieure \u00e0 la date minimum",
	maxText : "Cette date est post\u00e9rieure \u00e0 la date maximum",
	disabledDaysText : "",
	disabledDatesText : "",
	monthNames : ["Janvier","F\u00e9vrier","Mars","Avril","Mai","Juin","Juillet","Aout","Septembre","Octobre","Novembre","Decembre"],
	dayNames : ["Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"],
	nextText: 'Mois suivant (CTRL+Fl\u00e8che droite)',
	prevText: "Mois pr\u00e9c\u00e9dent (CTRL+Fl\u00e8che gauche)",
	monthYearText: "Choisissez un mois (CTRL+Fl\u00e8che haut ou bas pour changer d'ann\u00e9e.)",
	todayTip : "{0} (Barre d'espace)",
	okText : "&#160;OK&#160;",
	cancelText : "Annuler",
	format : "dd/mm/yyyyy",
	startDay : 1
	});
}
