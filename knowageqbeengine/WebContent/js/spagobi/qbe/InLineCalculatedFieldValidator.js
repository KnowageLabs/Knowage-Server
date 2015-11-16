/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
 /*
    Default template driver for JS/CC generated parsers running as
    browser-based JavaScript/ECMAScript applications.
    
    This class parses a InLineCalculated field. If an error occurs than the
    expression is not well formed.
      
	The LALR grammar:

/~ --- Token definitions --- ~/
			
			/~ Characters to be ignored ~/
			!	' |\r|\n|\t'
			
			/~ Non-associative tokens ~/
                            ','
			    '\('
			    '\)'
			    "AVG"
			    "SUM"
			    "COUNT"
			    "MIN"
			    "MAX"
			    "GG_between_dates"
			    "MM_between_dates"
			    "AA_between_dates"
			    "GG_up_today"
			    "MM_up_today"
			    "AA_up_today"
			    "\|\|"
			    '[0-9]+'                        				INT   [* %match = parseInt( %match ); *]
			    '[0-9]+\.[0-9]*|[0-9]*\.[0-9]+' 				FLOAT [* %match = parseFloat( %match ); *]
			    '\'[A-Za-z0-9_]+\''								String
			    '[A-Za-z0-9_ ]+'								Identifier
			    ;
			
			/~ Left-associative tokens, lowest precedence ~/
			<  '\+'
			   '\-'
			   ;
			        
			/~ Left-associative tokens, highest precedence ~/
			<  '\*'
			   '/'
			   ;
			
			##
			
			/~ --- Grammar specification --- ~/
			
			p:      numericexpression              
			        | stringexpression
					;
			
			numericexpression:	numericexpression '+' numericexpression        
			       				| numericexpression '-' numericexpression      
								| numericexpression '*' numericexpression      
								| numericexpression '/' numericexpression      
								| '-' numericexpression &'*'   
								| SUM '(' numericexpression ')'
								| COUNT '(' numericexpression ')'  
								| AVG '(' numericexpression ')'  
								| MIN '(' numericexpression ')'
								| MAX '(' numericexpression ')'  
								| '(' numericexpression ')'    
                                                                | GG_between_dates'('numericexpression ',' numericexpression')' 
                                                                | MM_between_dates'('numericexpression ',' numericexpression')'
                                                                | AA_between_dates'('numericexpression ',' numericexpression')'
                                                                | GG_up_today'('numericexpression')'
                                                                | MM_up_today'('numericexpression')'
                                                                | AA_up_today'('numericexpression')'
								| INT
								| FLOAT
								| Identifier
								;
				
			stringexpression:	stringexpression '||' stringexpression   
								| '(' stringexpression ')'
								| String
								| Identifier
								;
*/
SQLExpressionParser = {}; 

SQLExpressionParser.module = function(){ 

	var _dbg_withtrace        = false;
	var _dbg_string            = new String();

	function __dbg_print( text )
	{
	    _dbg_string += text + "\n";
	}

	function __lex( info )
	{
	    var state        = 0;
	    var match        = -1;
	    var match_pos    = 0;
	    var start        = 0;
	    var pos            = info.offset + 1;

	    do
	    {
	        pos--;
	        state = 0;
	        match = -2;
	        start = pos;

	        if( info.src.length <= start )
	            return 28;

	        do
	        {

	switch( state )
	{
	    case 0:
	        if( ( info.src.charCodeAt( pos ) >= 9 && info.src.charCodeAt( pos ) <= 10 ) || info.src.charCodeAt( pos ) == 13 ) state = 1;
	        else if( info.src.charCodeAt( pos ) == 40 ) state = 2;
	        else if( info.src.charCodeAt( pos ) == 41 ) state = 3;
	        else if( info.src.charCodeAt( pos ) == 42 ) state = 4;
	        else if( info.src.charCodeAt( pos ) == 43 ) state = 5;
	        else if( info.src.charCodeAt( pos ) == 44 ) state = 6;
	        else if( info.src.charCodeAt( pos ) == 45 ) state = 7;
	        else if( info.src.charCodeAt( pos ) == 47 ) state = 8;
	        else if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) ) state = 9;
	        else if( info.src.charCodeAt( pos ) == 65 || info.src.charCodeAt( pos ) == 97 ) state = 10;
	        else if( info.src.charCodeAt( pos ) == 39 ) state = 25;
	        else if( info.src.charCodeAt( pos ) == 32 ) state = 26;
	        else if( info.src.charCodeAt( pos ) == 46 ) state = 28;
	        else if( info.src.charCodeAt( pos ) == 124 ) state = 30;
	        else if( info.src.charCodeAt( pos ) == 66 || ( info.src.charCodeAt( pos ) >= 68 && info.src.charCodeAt( pos ) <= 70 ) || ( info.src.charCodeAt( pos ) >= 72 && info.src.charCodeAt( pos ) <= 76 ) || ( info.src.charCodeAt( pos ) >= 78 && info.src.charCodeAt( pos ) <= 82 ) || ( info.src.charCodeAt( pos ) >= 84 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || info.src.charCodeAt( pos ) == 98 || ( info.src.charCodeAt( pos ) >= 100 && info.src.charCodeAt( pos ) <= 102 ) || ( info.src.charCodeAt( pos ) >= 104 && info.src.charCodeAt( pos ) <= 108 ) || ( info.src.charCodeAt( pos ) >= 110 && info.src.charCodeAt( pos ) <= 114 ) || ( info.src.charCodeAt( pos ) >= 116 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 67 || info.src.charCodeAt( pos ) == 99 ) state = 63;
	        else if( info.src.charCodeAt( pos ) == 71 || info.src.charCodeAt( pos ) == 103 ) state = 66;
	        else if( info.src.charCodeAt( pos ) == 77 || info.src.charCodeAt( pos ) == 109 ) state = 68;
	        else if( info.src.charCodeAt( pos ) == 83 || info.src.charCodeAt( pos ) == 115 ) state = 70;
	        else state = -1;
	        break;

	    case 1:
	        state = -1;
	        match = 1;
	        match_pos = pos;
	        break;

	    case 2:
	        state = -1;
	        match = 3;
	        match_pos = pos;
	        break;

	    case 3:
	        state = -1;
	        match = 4;
	        match_pos = pos;
	        break;

	    case 4:
	        state = -1;
	        match = 23;
	        match_pos = pos;
	        break;

	    case 5:
	        state = -1;
	        match = 21;
	        match_pos = pos;
	        break;

	    case 6:
	        state = -1;
	        match = 2;
	        match_pos = pos;
	        break;

	    case 7:
	        state = -1;
	        match = 22;
	        match_pos = pos;
	        break;

	    case 8:
	        state = -1;
	        match = 24;
	        match_pos = pos;
	        break;

	    case 9:
	        if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) ) state = 9;
	        else if( info.src.charCodeAt( pos ) == 46 ) state = 11;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 17;
	        match_pos = pos;
	        break;

	    case 10:
	        if( info.src.charCodeAt( pos ) == 86 || info.src.charCodeAt( pos ) == 118 ) state = 27;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 66 && info.src.charCodeAt( pos ) <= 85 ) || ( info.src.charCodeAt( pos ) >= 87 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 98 && info.src.charCodeAt( pos ) <= 117 ) || ( info.src.charCodeAt( pos ) >= 119 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 65 || info.src.charCodeAt( pos ) == 97 ) state = 71;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 11:
	        if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) ) state = 11;
	        else state = -1;
	        match = 18;
	        match_pos = pos;
	        break;

	    case 12:
	        state = -1;
	        match = 16;
	        match_pos = pos;
	        break;

	    case 13:
	        state = -1;
	        match = 19;
	        match_pos = pos;
	        break;

	    case 14:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 5;
	        match_pos = pos;
	        break;

	    case 15:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 9;
	        match_pos = pos;
	        break;

	    case 16:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 8;
	        match_pos = pos;
	        break;

	    case 17:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 6;
	        match_pos = pos;
	        break;

	    case 18:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 7;
	        match_pos = pos;
	        break;

	    case 19:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 15;
	        match_pos = pos;
	        break;

	    case 20:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 13;
	        match_pos = pos;
	        break;

	    case 21:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 14;
	        match_pos = pos;
	        break;

	    case 22:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 12;
	        match_pos = pos;
	        break;

	    case 23:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 10;
	        match_pos = pos;
	        break;

	    case 24:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 11;
	        match_pos = pos;
	        break;

	    case 25:
	        if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 32;
	        else state = -1;
	        break;

	    case 26:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 1;
	        match_pos = pos;
	        break;

	    case 27:
	        if( info.src.charCodeAt( pos ) == 71 || info.src.charCodeAt( pos ) == 103 ) state = 14;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 70 ) || ( info.src.charCodeAt( pos ) >= 72 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 102 ) || ( info.src.charCodeAt( pos ) >= 104 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 28:
	        if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) ) state = 11;
	        else state = -1;
	        break;

	    case 29:
	        if( info.src.charCodeAt( pos ) == 85 || info.src.charCodeAt( pos ) == 117 ) state = 37;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 84 ) || ( info.src.charCodeAt( pos ) >= 86 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 116 ) || ( info.src.charCodeAt( pos ) >= 118 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 30:
	        if( info.src.charCodeAt( pos ) == 124 ) state = 12;
	        else state = -1;
	        break;

	    case 31:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 95 ) state = 94;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 32:
	        if( info.src.charCodeAt( pos ) == 39 ) state = 13;
	        else if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 32;
	        else state = -1;
	        break;

	    case 33:
	        if( info.src.charCodeAt( pos ) == 88 || info.src.charCodeAt( pos ) == 120 ) state = 15;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 87 ) || ( info.src.charCodeAt( pos ) >= 89 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 119 ) || ( info.src.charCodeAt( pos ) >= 121 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 34:
	        if( info.src.charCodeAt( pos ) == 78 || info.src.charCodeAt( pos ) == 110 ) state = 16;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 77 ) || ( info.src.charCodeAt( pos ) >= 79 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 109 ) || ( info.src.charCodeAt( pos ) >= 111 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 35:
	        if( info.src.charCodeAt( pos ) == 77 || info.src.charCodeAt( pos ) == 109 ) state = 17;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 76 ) || ( info.src.charCodeAt( pos ) >= 78 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 108 ) || ( info.src.charCodeAt( pos ) >= 110 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 36:
	        if( info.src.charCodeAt( pos ) == 66 || info.src.charCodeAt( pos ) == 98 ) state = 38;
	        else if( info.src.charCodeAt( pos ) == 85 || info.src.charCodeAt( pos ) == 117 ) state = 39;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || info.src.charCodeAt( pos ) == 65 || ( info.src.charCodeAt( pos ) >= 67 && info.src.charCodeAt( pos ) <= 84 ) || ( info.src.charCodeAt( pos ) >= 86 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || info.src.charCodeAt( pos ) == 97 || ( info.src.charCodeAt( pos ) >= 99 && info.src.charCodeAt( pos ) <= 116 ) || ( info.src.charCodeAt( pos ) >= 118 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 37:
	        if( info.src.charCodeAt( pos ) == 78 || info.src.charCodeAt( pos ) == 110 ) state = 40;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 77 ) || ( info.src.charCodeAt( pos ) >= 79 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 109 ) || ( info.src.charCodeAt( pos ) >= 111 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 38:
	        if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 41;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 39:
	        if( info.src.charCodeAt( pos ) == 80 || info.src.charCodeAt( pos ) == 112 ) state = 42;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 79 ) || ( info.src.charCodeAt( pos ) >= 81 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 111 ) || ( info.src.charCodeAt( pos ) >= 113 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 40:
	        if( info.src.charCodeAt( pos ) == 84 || info.src.charCodeAt( pos ) == 116 ) state = 18;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 83 ) || ( info.src.charCodeAt( pos ) >= 85 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 115 ) || ( info.src.charCodeAt( pos ) >= 117 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 41:
	        if( info.src.charCodeAt( pos ) == 84 || info.src.charCodeAt( pos ) == 116 ) state = 43;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 83 ) || ( info.src.charCodeAt( pos ) >= 85 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 115 ) || ( info.src.charCodeAt( pos ) >= 117 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 42:
	        if( info.src.charCodeAt( pos ) == 95 ) state = 44;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 43:
	        if( info.src.charCodeAt( pos ) == 87 || info.src.charCodeAt( pos ) == 119 ) state = 45;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 86 ) || ( info.src.charCodeAt( pos ) >= 88 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 118 ) || ( info.src.charCodeAt( pos ) >= 120 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 44:
	        if( info.src.charCodeAt( pos ) == 84 || info.src.charCodeAt( pos ) == 116 ) state = 46;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 83 ) || ( info.src.charCodeAt( pos ) >= 85 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 115 ) || ( info.src.charCodeAt( pos ) >= 117 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 45:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 61;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 46:
	        if( info.src.charCodeAt( pos ) == 79 || info.src.charCodeAt( pos ) == 111 ) state = 47;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 78 ) || ( info.src.charCodeAt( pos ) >= 80 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 110 ) || ( info.src.charCodeAt( pos ) >= 112 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 47:
	        if( info.src.charCodeAt( pos ) == 68 || info.src.charCodeAt( pos ) == 100 ) state = 49;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 67 ) || ( info.src.charCodeAt( pos ) >= 69 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 99 ) || ( info.src.charCodeAt( pos ) >= 101 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 48:
	        if( info.src.charCodeAt( pos ) == 78 || info.src.charCodeAt( pos ) == 110 ) state = 50;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 77 ) || ( info.src.charCodeAt( pos ) >= 79 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 109 ) || ( info.src.charCodeAt( pos ) >= 111 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 49:
	        if( info.src.charCodeAt( pos ) == 65 || info.src.charCodeAt( pos ) == 97 ) state = 51;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 66 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 98 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 50:
	        if( info.src.charCodeAt( pos ) == 95 ) state = 54;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 51:
	        if( info.src.charCodeAt( pos ) == 89 || info.src.charCodeAt( pos ) == 121 ) state = 19;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 88 ) || info.src.charCodeAt( pos ) == 90 || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 120 ) || info.src.charCodeAt( pos ) == 122 ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 52:
	        if( info.src.charCodeAt( pos ) == 89 || info.src.charCodeAt( pos ) == 121 ) state = 20;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 88 ) || info.src.charCodeAt( pos ) == 90 || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 120 ) || info.src.charCodeAt( pos ) == 122 ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 53:
	        if( info.src.charCodeAt( pos ) == 89 || info.src.charCodeAt( pos ) == 121 ) state = 21;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 88 ) || info.src.charCodeAt( pos ) == 90 || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 120 ) || info.src.charCodeAt( pos ) == 122 ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 54:
	        if( info.src.charCodeAt( pos ) == 68 || info.src.charCodeAt( pos ) == 100 ) state = 55;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 67 ) || ( info.src.charCodeAt( pos ) >= 69 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 99 ) || ( info.src.charCodeAt( pos ) >= 101 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 55:
	        if( info.src.charCodeAt( pos ) == 65 || info.src.charCodeAt( pos ) == 97 ) state = 56;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 66 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 98 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 56:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 83 ) || ( info.src.charCodeAt( pos ) >= 85 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 115 ) || ( info.src.charCodeAt( pos ) >= 117 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 84 || info.src.charCodeAt( pos ) == 116 ) state = 64;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 57:
	        if( info.src.charCodeAt( pos ) == 83 || info.src.charCodeAt( pos ) == 115 ) state = 22;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 82 ) || ( info.src.charCodeAt( pos ) >= 84 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 114 ) || ( info.src.charCodeAt( pos ) >= 116 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 58:
	        if( info.src.charCodeAt( pos ) == 83 || info.src.charCodeAt( pos ) == 115 ) state = 23;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 82 ) || ( info.src.charCodeAt( pos ) >= 84 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 114 ) || ( info.src.charCodeAt( pos ) >= 116 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 59:
	        if( info.src.charCodeAt( pos ) == 83 || info.src.charCodeAt( pos ) == 115 ) state = 24;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 82 ) || ( info.src.charCodeAt( pos ) >= 84 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 114 ) || ( info.src.charCodeAt( pos ) >= 116 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 60:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 61:
	        if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 48;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 62:
	        if( info.src.charCodeAt( pos ) == 65 || info.src.charCodeAt( pos ) == 97 ) state = 52;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 66 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 98 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 63:
	        if( info.src.charCodeAt( pos ) == 79 || info.src.charCodeAt( pos ) == 111 ) state = 29;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 78 ) || ( info.src.charCodeAt( pos ) >= 80 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 110 ) || ( info.src.charCodeAt( pos ) >= 112 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 64:
	        if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 57;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 65:
	        if( info.src.charCodeAt( pos ) == 65 || info.src.charCodeAt( pos ) == 97 ) state = 53;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 66 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 98 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 66:
	        if( info.src.charCodeAt( pos ) == 71 || info.src.charCodeAt( pos ) == 103 ) state = 31;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 70 ) || ( info.src.charCodeAt( pos ) >= 72 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 102 ) || ( info.src.charCodeAt( pos ) >= 104 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 67:
	        if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 58;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 68:
	        if( info.src.charCodeAt( pos ) == 65 || info.src.charCodeAt( pos ) == 97 ) state = 33;
	        else if( info.src.charCodeAt( pos ) == 73 || info.src.charCodeAt( pos ) == 105 ) state = 34;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 66 && info.src.charCodeAt( pos ) <= 72 ) || ( info.src.charCodeAt( pos ) >= 74 && info.src.charCodeAt( pos ) <= 76 ) || ( info.src.charCodeAt( pos ) >= 78 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 98 && info.src.charCodeAt( pos ) <= 104 ) || ( info.src.charCodeAt( pos ) >= 106 && info.src.charCodeAt( pos ) <= 108 ) || ( info.src.charCodeAt( pos ) >= 110 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 77 || info.src.charCodeAt( pos ) == 109 ) state = 98;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 69:
	        if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 59;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 70:
	        if( info.src.charCodeAt( pos ) == 85 || info.src.charCodeAt( pos ) == 117 ) state = 35;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 84 ) || ( info.src.charCodeAt( pos ) >= 86 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 116 ) || ( info.src.charCodeAt( pos ) >= 118 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 71:
	        if( info.src.charCodeAt( pos ) == 95 ) state = 36;
	        else if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 72:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 67 ) || ( info.src.charCodeAt( pos ) >= 69 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 99 ) || ( info.src.charCodeAt( pos ) >= 101 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 68 || info.src.charCodeAt( pos ) == 100 ) state = 62;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 73:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 83 ) || ( info.src.charCodeAt( pos ) >= 85 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 115 ) || ( info.src.charCodeAt( pos ) >= 117 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 84 || info.src.charCodeAt( pos ) == 116 ) state = 67;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 74:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 67 ) || ( info.src.charCodeAt( pos ) >= 69 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 99 ) || ( info.src.charCodeAt( pos ) >= 101 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 68 || info.src.charCodeAt( pos ) == 100 ) state = 65;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 75:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 83 ) || ( info.src.charCodeAt( pos ) >= 85 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 115 ) || ( info.src.charCodeAt( pos ) >= 117 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 84 || info.src.charCodeAt( pos ) == 116 ) state = 69;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 76:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 78 ) || ( info.src.charCodeAt( pos ) >= 80 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 110 ) || ( info.src.charCodeAt( pos ) >= 112 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 79 || info.src.charCodeAt( pos ) == 111 ) state = 72;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 77:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 66 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 98 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 65 || info.src.charCodeAt( pos ) == 97 ) state = 73;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 78:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 78 ) || ( info.src.charCodeAt( pos ) >= 80 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 110 ) || ( info.src.charCodeAt( pos ) >= 112 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 79 || info.src.charCodeAt( pos ) == 111 ) state = 74;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 79:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 66 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 98 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 65 || info.src.charCodeAt( pos ) == 97 ) state = 75;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 80:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 83 ) || ( info.src.charCodeAt( pos ) >= 85 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 115 ) || ( info.src.charCodeAt( pos ) >= 117 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 84 || info.src.charCodeAt( pos ) == 116 ) state = 76;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 81:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 67 ) || ( info.src.charCodeAt( pos ) >= 69 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 99 ) || ( info.src.charCodeAt( pos ) >= 101 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 68 || info.src.charCodeAt( pos ) == 100 ) state = 77;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 82:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 83 ) || ( info.src.charCodeAt( pos ) >= 85 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 115 ) || ( info.src.charCodeAt( pos ) >= 117 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 84 || info.src.charCodeAt( pos ) == 116 ) state = 78;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 83:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 67 ) || ( info.src.charCodeAt( pos ) >= 69 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 99 ) || ( info.src.charCodeAt( pos ) >= 101 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 68 || info.src.charCodeAt( pos ) == 100 ) state = 79;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 84:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 95 ) state = 80;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 85:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 95 ) state = 81;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 86:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 95 ) state = 82;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 87:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 95 ) state = 83;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 88:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 79 ) || ( info.src.charCodeAt( pos ) >= 81 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 111 ) || ( info.src.charCodeAt( pos ) >= 113 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 80 || info.src.charCodeAt( pos ) == 112 ) state = 84;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 89:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 77 ) || ( info.src.charCodeAt( pos ) >= 79 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 109 ) || ( info.src.charCodeAt( pos ) >= 111 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 78 || info.src.charCodeAt( pos ) == 110 ) state = 85;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 90:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 89;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 91:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 79 ) || ( info.src.charCodeAt( pos ) >= 81 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 111 ) || ( info.src.charCodeAt( pos ) >= 113 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 80 || info.src.charCodeAt( pos ) == 112 ) state = 86;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 92:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 77 ) || ( info.src.charCodeAt( pos ) >= 79 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 109 ) || ( info.src.charCodeAt( pos ) >= 111 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 78 || info.src.charCodeAt( pos ) == 110 ) state = 87;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 93:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 92;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 94:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || info.src.charCodeAt( pos ) == 65 || ( info.src.charCodeAt( pos ) >= 67 && info.src.charCodeAt( pos ) <= 84 ) || ( info.src.charCodeAt( pos ) >= 86 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || info.src.charCodeAt( pos ) == 97 || ( info.src.charCodeAt( pos ) >= 99 && info.src.charCodeAt( pos ) <= 116 ) || ( info.src.charCodeAt( pos ) >= 118 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 85 || info.src.charCodeAt( pos ) == 117 ) state = 88;
	        else if( info.src.charCodeAt( pos ) == 66 || info.src.charCodeAt( pos ) == 98 ) state = 103;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 95:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 90;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 96:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || info.src.charCodeAt( pos ) == 65 || ( info.src.charCodeAt( pos ) >= 67 && info.src.charCodeAt( pos ) <= 84 ) || ( info.src.charCodeAt( pos ) >= 86 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || info.src.charCodeAt( pos ) == 97 || ( info.src.charCodeAt( pos ) >= 99 && info.src.charCodeAt( pos ) <= 116 ) || ( info.src.charCodeAt( pos ) >= 118 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 85 || info.src.charCodeAt( pos ) == 117 ) state = 91;
	        else if( info.src.charCodeAt( pos ) == 66 || info.src.charCodeAt( pos ) == 98 ) state = 104;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 97:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 93;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 98:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 95 ) state = 96;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 99:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 86 ) || ( info.src.charCodeAt( pos ) >= 88 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 118 ) || ( info.src.charCodeAt( pos ) >= 120 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 87 || info.src.charCodeAt( pos ) == 119 ) state = 95;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 100:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 86 ) || ( info.src.charCodeAt( pos ) >= 88 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 118 ) || ( info.src.charCodeAt( pos ) >= 120 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 87 || info.src.charCodeAt( pos ) == 119 ) state = 97;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 101:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 83 ) || ( info.src.charCodeAt( pos ) >= 85 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 115 ) || ( info.src.charCodeAt( pos ) >= 117 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 84 || info.src.charCodeAt( pos ) == 116 ) state = 99;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 102:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 83 ) || ( info.src.charCodeAt( pos ) >= 85 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 115 ) || ( info.src.charCodeAt( pos ) >= 117 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 84 || info.src.charCodeAt( pos ) == 116 ) state = 100;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 103:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 101;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	    case 104:
	        if( info.src.charCodeAt( pos ) == 32 || ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 68 ) || ( info.src.charCodeAt( pos ) >= 70 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 100 ) || ( info.src.charCodeAt( pos ) >= 102 && info.src.charCodeAt( pos ) <= 122 ) ) state = 60;
	        else if( info.src.charCodeAt( pos ) == 69 || info.src.charCodeAt( pos ) == 101 ) state = 102;
	        else state = -1;
	        match = 20;
	        match_pos = pos;
	        break;

	}


	            pos++;

	        }
	        while( state > -1 );

	    }
	    while( 1 > -1 && match == 1 );

	    if( match > -1 )
	    {
	        info.att = info.src.substr( start, match_pos - start );
	        info.offset = match_pos;
	        
	switch( match )
	{
	    case 17:
	        {
	         info.att = parseInt( info.att );
	        }
	        break;

	    case 18:
	        {
	         info.att = parseFloat( info.att );
	        }
	        break;

	}


	    }
	    else
	    {
	        info.att = new String();
	        match = -1;
	    }

	    return match;
	}


	function __parse( src, err_off, err_la )
	{
	    var        sstack            = new Array();
	    var        vstack            = new Array();
	    var     err_cnt            = 0;
	    var        act;
	    var        go;
	    var        la;
	    var        rval;
	    var     parseinfo        = new Function( "", "var offset; var src; var att;" );
	    var        info            = new parseinfo();
	    
	/* Pop-Table */
	var pop_tab = new Array(
	    new Array( 0/* p' */, 1 ),
	    new Array( 27/* p */, 1 ),
	    new Array( 27/* p */, 1 ),
	    new Array( 25/* numericexpression */, 3 ),
	    new Array( 25/* numericexpression */, 3 ),
	    new Array( 25/* numericexpression */, 3 ),
	    new Array( 25/* numericexpression */, 3 ),
	    new Array( 25/* numericexpression */, 2 ),
	    new Array( 25/* numericexpression */, 4 ),
	    new Array( 25/* numericexpression */, 4 ),
	    new Array( 25/* numericexpression */, 4 ),
	    new Array( 25/* numericexpression */, 4 ),
	    new Array( 25/* numericexpression */, 4 ),
	    new Array( 25/* numericexpression */, 3 ),
	    new Array( 25/* numericexpression */, 6 ),
	    new Array( 25/* numericexpression */, 6 ),
	    new Array( 25/* numericexpression */, 6 ),
	    new Array( 25/* numericexpression */, 4 ),
	    new Array( 25/* numericexpression */, 4 ),
	    new Array( 25/* numericexpression */, 4 ),
	    new Array( 25/* numericexpression */, 1 ),
	    new Array( 25/* numericexpression */, 1 ),
	    new Array( 25/* numericexpression */, 1 ),
	    new Array( 26/* stringexpression */, 3 ),
	    new Array( 26/* stringexpression */, 3 ),
	    new Array( 26/* stringexpression */, 1 ),
	    new Array( 26/* stringexpression */, 1 )
	);

	/* Action-Table */
	var act_tab = new Array(
	    /* State 0 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,10 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,19 , 19/* "String" */,20 ),
	    /* State 1 */ new Array( 28/* "$" */,0 ),
	    /* State 2 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 28/* "$" */,-1 ),
	    /* State 3 */ new Array( 16/* "||" */,25 , 28/* "$" */,-2 ),
	    /* State 4 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 5 */ new Array( 3/* "(" */,29 ),
	    /* State 6 */ new Array( 3/* "(" */,30 ),
	    /* State 7 */ new Array( 3/* "(" */,31 ),
	    /* State 8 */ new Array( 3/* "(" */,32 ),
	    /* State 9 */ new Array( 3/* "(" */,33 ),
	    /* State 10 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,10 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,19 , 19/* "String" */,20 ),
	    /* State 11 */ new Array( 3/* "(" */,36 ),
	    /* State 12 */ new Array( 3/* "(" */,37 ),
	    /* State 13 */ new Array( 3/* "(" */,38 ),
	    /* State 14 */ new Array( 3/* "(" */,39 ),
	    /* State 15 */ new Array( 3/* "(" */,40 ),
	    /* State 16 */ new Array( 3/* "(" */,41 ),
	    /* State 17 */ new Array( 28/* "$" */,-20 , 21/* "+" */,-20 , 22/* "-" */,-20 , 23/* "*" */,-20 , 24/* "/" */,-20 , 4/* ")" */,-20 , 2/* "," */,-20 ),
	    /* State 18 */ new Array( 28/* "$" */,-21 , 21/* "+" */,-21 , 22/* "-" */,-21 , 23/* "*" */,-21 , 24/* "/" */,-21 , 4/* ")" */,-21 , 2/* "," */,-21 ),
	    /* State 19 */ new Array( 28/* "$" */,-22 , 21/* "+" */,-22 , 22/* "-" */,-22 , 23/* "*" */,-22 , 24/* "/" */,-22 , 4/* ")" */,-22 , 16/* "||" */,-26 ),
	    /* State 20 */ new Array( 28/* "$" */,-25 , 16/* "||" */,-25 , 4/* ")" */,-25 ),
	    /* State 21 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 22 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 23 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 24 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 25 */ new Array( 3/* "(" */,47 , 19/* "String" */,20 , 20/* "Identifier" */,48 ),
	    /* State 26 */ new Array( 24/* "/" */,-7 , 23/* "*" */,-7 , 22/* "-" */,-7 , 21/* "+" */,-7 , 28/* "$" */,-7 , 4/* ")" */,-7 , 2/* "," */,-7 ),
	    /* State 27 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 28 */ new Array( 28/* "$" */,-22 , 21/* "+" */,-22 , 22/* "-" */,-22 , 23/* "*" */,-22 , 24/* "/" */,-22 , 4/* ")" */,-22 , 2/* "," */,-22 ),
	    /* State 29 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 30 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 31 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 32 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 33 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 34 */ new Array( 16/* "||" */,25 , 4/* ")" */,54 ),
	    /* State 35 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,55 ),
	    /* State 36 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 37 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 38 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 39 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 40 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 41 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 42 */ new Array( 24/* "/" */,-6 , 23/* "*" */,-6 , 22/* "-" */,-6 , 21/* "+" */,-6 , 28/* "$" */,-6 , 4/* ")" */,-6 , 2/* "," */,-6 ),
	    /* State 43 */ new Array( 24/* "/" */,-5 , 23/* "*" */,-5 , 22/* "-" */,-5 , 21/* "+" */,-5 , 28/* "$" */,-5 , 4/* ")" */,-5 , 2/* "," */,-5 ),
	    /* State 44 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,-4 , 21/* "+" */,-4 , 28/* "$" */,-4 , 4/* ")" */,-4 , 2/* "," */,-4 ),
	    /* State 45 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,-3 , 21/* "+" */,-3 , 28/* "$" */,-3 , 4/* ")" */,-3 , 2/* "," */,-3 ),
	    /* State 46 */ new Array( 16/* "||" */,25 , 28/* "$" */,-23 , 4/* ")" */,-23 ),
	    /* State 47 */ new Array( 3/* "(" */,47 , 19/* "String" */,20 , 20/* "Identifier" */,48 ),
	    /* State 48 */ new Array( 28/* "$" */,-26 , 16/* "||" */,-26 , 4/* ")" */,-26 ),
	    /* State 49 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,62 ),
	    /* State 50 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,63 ),
	    /* State 51 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,64 ),
	    /* State 52 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,65 ),
	    /* State 53 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,66 ),
	    /* State 54 */ new Array( 28/* "$" */,-24 , 16/* "||" */,-24 , 4/* ")" */,-24 ),
	    /* State 55 */ new Array( 28/* "$" */,-13 , 21/* "+" */,-13 , 22/* "-" */,-13 , 23/* "*" */,-13 , 24/* "/" */,-13 , 4/* ")" */,-13 , 2/* "," */,-13 ),
	    /* State 56 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 2/* "," */,67 ),
	    /* State 57 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 2/* "," */,68 ),
	    /* State 58 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 2/* "," */,69 ),
	    /* State 59 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,70 ),
	    /* State 60 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,71 ),
	    /* State 61 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,72 ),
	    /* State 62 */ new Array( 28/* "$" */,-8 , 21/* "+" */,-8 , 22/* "-" */,-8 , 23/* "*" */,-8 , 24/* "/" */,-8 , 4/* ")" */,-8 , 2/* "," */,-8 ),
	    /* State 63 */ new Array( 28/* "$" */,-9 , 21/* "+" */,-9 , 22/* "-" */,-9 , 23/* "*" */,-9 , 24/* "/" */,-9 , 4/* ")" */,-9 , 2/* "," */,-9 ),
	    /* State 64 */ new Array( 28/* "$" */,-10 , 21/* "+" */,-10 , 22/* "-" */,-10 , 23/* "*" */,-10 , 24/* "/" */,-10 , 4/* ")" */,-10 , 2/* "," */,-10 ),
	    /* State 65 */ new Array( 28/* "$" */,-11 , 21/* "+" */,-11 , 22/* "-" */,-11 , 23/* "*" */,-11 , 24/* "/" */,-11 , 4/* ")" */,-11 , 2/* "," */,-11 ),
	    /* State 66 */ new Array( 28/* "$" */,-12 , 21/* "+" */,-12 , 22/* "-" */,-12 , 23/* "*" */,-12 , 24/* "/" */,-12 , 4/* ")" */,-12 , 2/* "," */,-12 ),
	    /* State 67 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 68 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 69 */ new Array( 22/* "-" */,4 , 6/* "SUM" */,5 , 7/* "COUNT" */,6 , 5/* "AVG" */,7 , 8/* "MIN" */,8 , 9/* "MAX" */,9 , 3/* "(" */,27 , 10/* "GG_between_dates" */,11 , 11/* "MM_between_dates" */,12 , 12/* "AA_between_dates" */,13 , 13/* "GG_up_today" */,14 , 14/* "MM_up_today" */,15 , 15/* "AA_up_today" */,16 , 17/* "INT" */,17 , 18/* "FLOAT" */,18 , 20/* "Identifier" */,28 ),
	    /* State 70 */ new Array( 28/* "$" */,-17 , 21/* "+" */,-17 , 22/* "-" */,-17 , 23/* "*" */,-17 , 24/* "/" */,-17 , 4/* ")" */,-17 , 2/* "," */,-17 ),
	    /* State 71 */ new Array( 28/* "$" */,-18 , 21/* "+" */,-18 , 22/* "-" */,-18 , 23/* "*" */,-18 , 24/* "/" */,-18 , 4/* ")" */,-18 , 2/* "," */,-18 ),
	    /* State 72 */ new Array( 28/* "$" */,-19 , 21/* "+" */,-19 , 22/* "-" */,-19 , 23/* "*" */,-19 , 24/* "/" */,-19 , 4/* ")" */,-19 , 2/* "," */,-19 ),
	    /* State 73 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,76 ),
	    /* State 74 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,77 ),
	    /* State 75 */ new Array( 24/* "/" */,21 , 23/* "*" */,22 , 22/* "-" */,23 , 21/* "+" */,24 , 4/* ")" */,78 ),
	    /* State 76 */ new Array( 28/* "$" */,-14 , 21/* "+" */,-14 , 22/* "-" */,-14 , 23/* "*" */,-14 , 24/* "/" */,-14 , 4/* ")" */,-14 , 2/* "," */,-14 ),
	    /* State 77 */ new Array( 28/* "$" */,-15 , 21/* "+" */,-15 , 22/* "-" */,-15 , 23/* "*" */,-15 , 24/* "/" */,-15 , 4/* ")" */,-15 , 2/* "," */,-15 ),
	    /* State 78 */ new Array( 28/* "$" */,-16 , 21/* "+" */,-16 , 22/* "-" */,-16 , 23/* "*" */,-16 , 24/* "/" */,-16 , 4/* ")" */,-16 , 2/* "," */,-16 )
	);

	/* Goto-Table */
	var goto_tab = new Array(
	    /* State 0 */ new Array( 27/* p */,1 , 25/* numericexpression */,2 , 26/* stringexpression */,3 ),
	    /* State 1 */ new Array( ),
	    /* State 2 */ new Array( ),
	    /* State 3 */ new Array( ),
	    /* State 4 */ new Array( 25/* numericexpression */,26 ),
	    /* State 5 */ new Array( ),
	    /* State 6 */ new Array( ),
	    /* State 7 */ new Array( ),
	    /* State 8 */ new Array( ),
	    /* State 9 */ new Array( ),
	    /* State 10 */ new Array( 26/* stringexpression */,34 , 25/* numericexpression */,35 ),
	    /* State 11 */ new Array( ),
	    /* State 12 */ new Array( ),
	    /* State 13 */ new Array( ),
	    /* State 14 */ new Array( ),
	    /* State 15 */ new Array( ),
	    /* State 16 */ new Array( ),
	    /* State 17 */ new Array( ),
	    /* State 18 */ new Array( ),
	    /* State 19 */ new Array( ),
	    /* State 20 */ new Array( ),
	    /* State 21 */ new Array( 25/* numericexpression */,42 ),
	    /* State 22 */ new Array( 25/* numericexpression */,43 ),
	    /* State 23 */ new Array( 25/* numericexpression */,44 ),
	    /* State 24 */ new Array( 25/* numericexpression */,45 ),
	    /* State 25 */ new Array( 26/* stringexpression */,46 ),
	    /* State 26 */ new Array( ),
	    /* State 27 */ new Array( 25/* numericexpression */,35 ),
	    /* State 28 */ new Array( ),
	    /* State 29 */ new Array( 25/* numericexpression */,49 ),
	    /* State 30 */ new Array( 25/* numericexpression */,50 ),
	    /* State 31 */ new Array( 25/* numericexpression */,51 ),
	    /* State 32 */ new Array( 25/* numericexpression */,52 ),
	    /* State 33 */ new Array( 25/* numericexpression */,53 ),
	    /* State 34 */ new Array( ),
	    /* State 35 */ new Array( ),
	    /* State 36 */ new Array( 25/* numericexpression */,56 ),
	    /* State 37 */ new Array( 25/* numericexpression */,57 ),
	    /* State 38 */ new Array( 25/* numericexpression */,58 ),
	    /* State 39 */ new Array( 25/* numericexpression */,59 ),
	    /* State 40 */ new Array( 25/* numericexpression */,60 ),
	    /* State 41 */ new Array( 25/* numericexpression */,61 ),
	    /* State 42 */ new Array( ),
	    /* State 43 */ new Array( ),
	    /* State 44 */ new Array( ),
	    /* State 45 */ new Array( ),
	    /* State 46 */ new Array( ),
	    /* State 47 */ new Array( 26/* stringexpression */,34 ),
	    /* State 48 */ new Array( ),
	    /* State 49 */ new Array( ),
	    /* State 50 */ new Array( ),
	    /* State 51 */ new Array( ),
	    /* State 52 */ new Array( ),
	    /* State 53 */ new Array( ),
	    /* State 54 */ new Array( ),
	    /* State 55 */ new Array( ),
	    /* State 56 */ new Array( ),
	    /* State 57 */ new Array( ),
	    /* State 58 */ new Array( ),
	    /* State 59 */ new Array( ),
	    /* State 60 */ new Array( ),
	    /* State 61 */ new Array( ),
	    /* State 62 */ new Array( ),
	    /* State 63 */ new Array( ),
	    /* State 64 */ new Array( ),
	    /* State 65 */ new Array( ),
	    /* State 66 */ new Array( ),
	    /* State 67 */ new Array( 25/* numericexpression */,73 ),
	    /* State 68 */ new Array( 25/* numericexpression */,74 ),
	    /* State 69 */ new Array( 25/* numericexpression */,75 ),
	    /* State 70 */ new Array( ),
	    /* State 71 */ new Array( ),
	    /* State 72 */ new Array( ),
	    /* State 73 */ new Array( ),
	    /* State 74 */ new Array( ),
	    /* State 75 */ new Array( ),
	    /* State 76 */ new Array( ),
	    /* State 77 */ new Array( ),
	    /* State 78 */ new Array( )
	);



	/* Symbol labels */
	var labels = new Array(
	    "p'" /* Non-terminal symbol */,
	    "WHITESPACE" /* Terminal symbol */,
	    "," /* Terminal symbol */,
	    "(" /* Terminal symbol */,
	    ")" /* Terminal symbol */,
	    "AVG" /* Terminal symbol */,
	    "SUM" /* Terminal symbol */,
	    "COUNT" /* Terminal symbol */,
	    "MIN" /* Terminal symbol */,
	    "MAX" /* Terminal symbol */,
	    "GG_between_dates" /* Terminal symbol */,
	    "MM_between_dates" /* Terminal symbol */,
	    "AA_between_dates" /* Terminal symbol */,
	    "GG_up_today" /* Terminal symbol */,
	    "MM_up_today" /* Terminal symbol */,
	    "AA_up_today" /* Terminal symbol */,
	    "||" /* Terminal symbol */,
	    "INT" /* Terminal symbol */,
	    "FLOAT" /* Terminal symbol */,
	    "String" /* Terminal symbol */,
	    "Identifier" /* Terminal symbol */,
	    "+" /* Terminal symbol */,
	    "-" /* Terminal symbol */,
	    "*" /* Terminal symbol */,
	    "/" /* Terminal symbol */,
	    "numericexpression" /* Non-terminal symbol */,
	    "stringexpression" /* Non-terminal symbol */,
	    "p" /* Non-terminal symbol */,
	    "$" /* Terminal symbol */
	);


	    
	    info.offset = 0;
	    info.src = src;
	    info.att = new String();
	    
	    if( !err_off )
	        err_off    = new Array();
	    if( !err_la )
	    err_la = new Array();
	    
	    sstack.push( 0 );
	    vstack.push( 0 );
	    
	    la = __lex( info );

	    while( true )
	    {
	        act = 80;
	        for( var i = 0; i < act_tab[sstack[sstack.length-1]].length; i+=2 )
	        {
	            if( act_tab[sstack[sstack.length-1]][i] == la )
	            {
	                act = act_tab[sstack[sstack.length-1]][i+1];
	                break;
	            }
	        }

	        if( _dbg_withtrace && sstack.length > 0 )
	        {
	            __dbg_print( "\nState " + sstack[sstack.length-1] + "\n" +
	                            "\tLookahead: " + labels[la] + " (\"" + info.att + "\")\n" +
	                            "\tAction: " + act + "\n" +
	                            "\tSource: \"" + info.src.substr( info.offset, 30 ) + ( ( info.offset + 30 < info.src.length ) ?
	                                    "..." : "" ) + "\"\n" +
	                            "\tStack: " + sstack.join() + "\n" +
	                            "\tValue stack: " + vstack.join() + "\n" );
	        }
	        
	            
	        //Panic-mode: Try recovery when parse-error occurs!
	        if( act == 80 )
	        {
	            if( _dbg_withtrace )
	                __dbg_print( "Error detected: There is no reduce or shift on the symbol " + labels[la] );
	            
	            err_cnt++;
	            err_off.push( info.offset - info.att.length );            
	            err_la.push( new Array() );
	            for( var i = 0; i < act_tab[sstack[sstack.length-1]].length; i+=2 )
	                err_la[err_la.length-1].push( labels[act_tab[sstack[sstack.length-1]][i]] );
	            
	            //Remember the original stack!
	            var rsstack = new Array();
	            var rvstack = new Array();
	            for( var i = 0; i < sstack.length; i++ )
	            {
	                rsstack[i] = sstack[i];
	                rvstack[i] = vstack[i];
	            }
	            
	            while( act == 80 && la != 28 )
	            {
	                if( _dbg_withtrace )
	                    __dbg_print( "\tError recovery\n" +
	                                    "Current lookahead: " + labels[la] + " (" + info.att + ")\n" +
	                                    "Action: " + act + "\n\n" );
	                if( la == -1 )
	                    info.offset++;
	                    
	                while( act == 80 && sstack.length > 0 )
	                {
	                    sstack.pop();
	                    vstack.pop();
	                    
	                    if( sstack.length == 0 )
	                        break;
	                        
	                    act = 80;
	                    for( var i = 0; i < act_tab[sstack[sstack.length-1]].length; i+=2 )
	                    {
	                        if( act_tab[sstack[sstack.length-1]][i] == la )
	                        {
	                            act = act_tab[sstack[sstack.length-1]][i+1];
	                            break;
	                        }
	                    }
	                }
	                
	                if( act != 80 )
	                    break;
	                
	                for( var i = 0; i < rsstack.length; i++ )
	                {
	                    sstack.push( rsstack[i] );
	                    vstack.push( rvstack[i] );
	                }
	                
	                la = __lex( info );
	            }
	            
	            if( act == 80 )
	            {
	                if( _dbg_withtrace )
	                    __dbg_print( "\tError recovery failed, terminating parse process..." );
	                break;
	            }


	            if( _dbg_withtrace )
	                __dbg_print( "\tError recovery succeeded, continuing" );
	        }
	        
	        /*
	        if( act == 80 )
	            break;
	        */
	        
	        
	        //Shift
	        if( act > 0 )
	        {            
	            if( _dbg_withtrace )
	                __dbg_print( "Shifting symbol: " + labels[la] + " (" + info.att + ")" );
	        
	            sstack.push( act );
	            vstack.push( info.att );
	            
	            la = __lex( info );
	            
	            if( _dbg_withtrace )
	                __dbg_print( "\tNew lookahead symbol: " + labels[la] + " (" + info.att + ")" );
	        }
	        //Reduce
	        else
	        {        
	            act *= -1;
	            
	            if( _dbg_withtrace )
	                __dbg_print( "Reducing by producution: " + act );
	            
	            rval = void(0);
	            
	            if( _dbg_withtrace )
	                __dbg_print( "\tPerforming semantic action..." );
	            
	switch( act )
	{
	    case 0:
	    {
	        rval = vstack[ vstack.length - 1 ];
	    }
	    break;
	    case 1:
	    {
	        rval = vstack[ vstack.length - 1 ];
	    }
	    break;
	    case 2:
	    {
	        rval = vstack[ vstack.length - 1 ];
	    }
	    break;
	    case 3:
	    {
	        rval = vstack[ vstack.length - 3 ];
	    }
	    break;
	    case 4:
	    {
	        rval = vstack[ vstack.length - 3 ];
	    }
	    break;
	    case 5:
	    {
	        rval = vstack[ vstack.length - 3 ];
	    }
	    break;
	    case 6:
	    {
	        rval = vstack[ vstack.length - 3 ];
	    }
	    break;
	    case 7:
	    {
	        rval = vstack[ vstack.length - 2 ];
	    }
	    break;
	    case 8:
	    {
	        rval = vstack[ vstack.length - 4 ];
	    }
	    break;
	    case 9:
	    {
	        rval = vstack[ vstack.length - 4 ];
	    }
	    break;
	    case 10:
	    {
	        rval = vstack[ vstack.length - 4 ];
	    }
	    break;
	    case 11:
	    {
	        rval = vstack[ vstack.length - 4 ];
	    }
	    break;
	    case 12:
	    {
	        rval = vstack[ vstack.length - 4 ];
	    }
	    break;
	    case 13:
	    {
	        rval = vstack[ vstack.length - 3 ];
	    }
	    break;
	    case 14:
	    {
	        rval = vstack[ vstack.length - 6 ];
	    }
	    break;
	    case 15:
	    {
	        rval = vstack[ vstack.length - 6 ];
	    }
	    break;
	    case 16:
	    {
	        rval = vstack[ vstack.length - 6 ];
	    }
	    break;
	    case 17:
	    {
	        rval = vstack[ vstack.length - 4 ];
	    }
	    break;
	    case 18:
	    {
	        rval = vstack[ vstack.length - 4 ];
	    }
	    break;
	    case 19:
	    {
	        rval = vstack[ vstack.length - 4 ];
	    }
	    break;
	    case 20:
	    {
	        rval = vstack[ vstack.length - 1 ];
	    }
	    break;
	    case 21:
	    {
	        rval = vstack[ vstack.length - 1 ];
	    }
	    break;
	    case 22:
	    {
	        rval = vstack[ vstack.length - 1 ];
	    }
	    break;
	    case 23:
	    {
	        rval = vstack[ vstack.length - 3 ];
	    }
	    break;
	    case 24:
	    {
	        rval = vstack[ vstack.length - 3 ];
	    }
	    break;
	    case 25:
	    {
	        rval = vstack[ vstack.length - 1 ];
	    }
	    break;
	    case 26:
	    {
	        rval = vstack[ vstack.length - 1 ];
	    }
	    break;
	}



	            if( _dbg_withtrace )
	                __dbg_print( "\tPopping " + pop_tab[act][1] + " off the stack..." );
	                
	            for( var i = 0; i < pop_tab[act][1]; i++ )
	            {
	                sstack.pop();
	                vstack.pop();
	            }
	                                    
	            go = -1;
	            for( var i = 0; i < goto_tab[sstack[sstack.length-1]].length; i+=2 )
	            {
	                if( goto_tab[sstack[sstack.length-1]][i] == pop_tab[act][0] )
	                {
	                    go = goto_tab[sstack[sstack.length-1]][i+1];
	                    break;
	                }
	            }
	            
	            if( act == 0 )
	                break;
	                
	            if( _dbg_withtrace )
	                __dbg_print( "\tPushing non-terminal " + labels[ pop_tab[act][0] ] );
	                
	            sstack.push( go );
	            vstack.push( rval );            
	        }
	        
	        if( _dbg_withtrace )
	        {        
	            alert( _dbg_string );
	            _dbg_string = new String();
	        }
	    }

	    if( _dbg_withtrace )
	    {
	        __dbg_print( "\nParse complete." );
	        alert( _dbg_string );
	    }
	    
	    return err_cnt;
	}

	
return{
	
	validateInLineCalculatedField: function (str){
		return "";
		
		var error_offsets = new Array(); 
		var error_lookaheads = new Array(); 
		var error_count = 0; 
		if( ( error_count = __parse( str, error_offsets, error_lookaheads ) ) > 0 ) { 
			var errstr = new String(); 
			for( var i = 0; i < error_count; i++ ) 
				errstr += "Parse error in line " + ( str.substr( 0, error_offsets[i] ).match( /\n/g ) ? str.substr( 0, error_offsets[i] ).match( /\n/g ).length : 1 ) + " near \"" + str.substr( error_offsets[i] ) + "\", expecting \"" + error_lookaheads[i].join() + "\"\n" ; 
				return errstr;
		}else{
			return "";
		}
	}

};
}();