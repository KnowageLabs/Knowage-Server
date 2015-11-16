Ext.ns("Sbi.locale");

Sbi.locale.dummyFormatter = function(v){return v;};
Sbi.locale.formatters = {
	//number: Sbi.locale.dummyFormatter,
	int: Sbi.locale.dummyFormatter,
	float: Sbi.locale.dummyFormatter,
	string: Sbi.locale.dummyFormatter,		
	date: Sbi.locale.dummyFormatter,		
	boolean: Sbi.locale.dummyFormatter,
	html: Sbi.locale.dummyFormatter
};


if(Sbi.qbe.commons.Format){
	if(Sbi.locale.formats) {
		Sbi.locale.formatters.int  = Sbi.qbe.commons.Format.numberRenderer(Sbi.locale.formats['int']);		
		Sbi.locale.formatters.float  = Sbi.qbe.commons.Format.numberRenderer(Sbi.locale.formats['float']);		
		Sbi.locale.formatters.string  = Sbi.qbe.commons.Format.stringRenderer(Sbi.locale.formats['string']);		
		Sbi.locale.formatters.date    = Sbi.qbe.commons.Format.dateRenderer(Sbi.locale.formats['date']);		
		Sbi.locale.formatters.boolean = Sbi.qbe.commons.Format.booleanRenderer(Sbi.locale.formats['boolean']);
		Sbi.locale.formatters.html    = Sbi.qbe.commons.Format.htmlRenderer();
	} else {
		Sbi.locale.formatters.int  = Sbi.qbe.commons.Format.numberRenderer( );	
		Sbi.locale.formatters.float  = Sbi.qbe.commons.Format.numberRenderer( );	
		Sbi.locale.formatters.string  = Sbi.qbe.commons.Format.stringRenderer( );		
		Sbi.locale.formatters.date    = Sbi.qbe.commons.Format.dateRenderer( );		
		Sbi.locale.formatters.boolean = Sbi.qbe.commons.Format.booleanRenderer( );
		Sbi.locale.formatters.html    = Sbi.qbe.commons.Format.htmlRenderer();
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




