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
  * - Alberto Ghedin (alberto.ghedin@eng.it)
  */

/*
    Default template driver for JS/CC generated parsers running as
    browser-based JavaScript/ECMAScript applications.
    
    WARNING:     This parser template will not run as console and has lesser
                features for debugging than the console derivates for the
                various JavaScript platforms.
    
    Features:
    - Parser trace messages
    - Integrated panic-mode error recovery
    
    Written 2007, 2008 by Jan Max Meyer, J.M.K S.F. Software Technologies
    
    This is in the public domain.

	/~ --- Token definitions --- ~/
	
	/~ Characters to be ignored ~/
	!	' |\r|\n|\t'
	
	/~ Non-associative tokens ~/
	    '\('
	    '\)'
	    '[0-9]+'                        			INT   [* %match = parseInt( %match ); *]
	    '[0-9]+\.[0-9]*|[0-9]*\.[0-9]+' 			FLOAT [* %match = parseFloat( %match ); *]
	    '\'[A-Za-z0-9_]+\''					String
	    'field\[[^\]]*\]'		Identifier
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
	
	p:      numberexpression              
		;
	
	numberexpression:      	numberexpression '+' numberexpression        
	       			| numberexpression '-' numberexpression      
				| numberexpression '*' numberexpression      
				| numberexpression '/' numberexpression      
				| '(' numberexpression ')'    
				| INT
				| FLOAT
				| Identifier
				;
*/

Ext.ns("Sbi.crosstab.core");

Sbi.crosstab.core.ArithmeticExpressionParser = {}; 

Sbi.crosstab.core.ArithmeticExpressionParser.module = function(){ 

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
	            return 14;

	        do
	        {

	switch( state )
	{
	    case 0:
	        if( ( info.src.charCodeAt( pos ) >= 9 && info.src.charCodeAt( pos ) <= 10 ) || info.src.charCodeAt( pos ) == 13 || info.src.charCodeAt( pos ) == 32 ) state = 1;
	        else if( info.src.charCodeAt( pos ) == 40 ) state = 2;
	        else if( info.src.charCodeAt( pos ) == 41 ) state = 3;
	        else if( info.src.charCodeAt( pos ) == 42 ) state = 4;
	        else if( info.src.charCodeAt( pos ) == 43 ) state = 5;
	        else if( info.src.charCodeAt( pos ) == 45 ) state = 6;
	        else if( info.src.charCodeAt( pos ) == 47 ) state = 7;
	        else if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) ) state = 8;
	        else if( info.src.charCodeAt( pos ) == 39 ) state = 12;
	        else if( info.src.charCodeAt( pos ) == 46 ) state = 13;
	        else if( info.src.charCodeAt( pos ) == 102 ) state = 14;
	        else state = -1;
	        break;

	    case 1:
	        state = -1;
	        match = 1;
	        match_pos = pos;
	        break;

	    case 2:
	        state = -1;
	        match = 2;
	        match_pos = pos;
	        break;

	    case 3:
	        state = -1;
	        match = 3;
	        match_pos = pos;
	        break;

	    case 4:
	        state = -1;
	        match = 10;
	        match_pos = pos;
	        break;

	    case 5:
	        state = -1;
	        match = 8;
	        match_pos = pos;
	        break;

	    case 6:
	        state = -1;
	        match = 9;
	        match_pos = pos;
	        break;

	    case 7:
	        state = -1;
	        match = 11;
	        match_pos = pos;
	        break;

	    case 8:
	        if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) ) state = 8;
	        else if( info.src.charCodeAt( pos ) == 46 ) state = 9;
	        else state = -1;
	        match = 4;
	        match_pos = pos;
	        break;

	    case 9:
	        if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) ) state = 9;
	        else state = -1;
	        match = 5;
	        match_pos = pos;
	        break;

	    case 10:
	        state = -1;
	        match = 6;
	        match_pos = pos;
	        break;

	    case 11:
	        state = -1;
	        match = 7;
	        match_pos = pos;
	        break;

	    case 12:
	        if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 15;
	        else state = -1;
	        break;

	    case 13:
	        if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) ) state = 9;
	        else state = -1;
	        break;

	    case 14:
	        if( info.src.charCodeAt( pos ) == 105 ) state = 16;
	        else state = -1;
	        break;

	    case 15:
	        if( info.src.charCodeAt( pos ) == 39 ) state = 10;
	        else if( ( info.src.charCodeAt( pos ) >= 48 && info.src.charCodeAt( pos ) <= 57 ) || ( info.src.charCodeAt( pos ) >= 65 && info.src.charCodeAt( pos ) <= 90 ) || info.src.charCodeAt( pos ) == 95 || ( info.src.charCodeAt( pos ) >= 97 && info.src.charCodeAt( pos ) <= 122 ) ) state = 15;
	        else state = -1;
	        break;

	    case 16:
	        if( info.src.charCodeAt( pos ) == 101 ) state = 17;
	        else state = -1;
	        break;

	    case 17:
	        if( info.src.charCodeAt( pos ) == 108 ) state = 18;
	        else state = -1;
	        break;

	    case 18:
	        if( info.src.charCodeAt( pos ) == 100 ) state = 19;
	        else state = -1;
	        break;

	    case 19:
	        if( info.src.charCodeAt( pos ) == 91 ) state = 20;
	        else state = -1;
	        break;

	    case 20:
	        if( info.src.charCodeAt( pos ) == 93 ) state = 11;
	        else if( ( info.src.charCodeAt( pos ) >= 0 && info.src.charCodeAt( pos ) <= 92 ) || ( info.src.charCodeAt( pos ) >= 94 && info.src.charCodeAt( pos ) <= 254 ) ) state = 20;
	        else state = -1;
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
	    case 4:
	        {
	         info.att = parseInt( info.att );
	        }
	        break;

	    case 5:
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
	    new Array( 13/* p */, 1 ),
	    new Array( 12/* numberexpression */, 3 ),
	    new Array( 12/* numberexpression */, 3 ),
	    new Array( 12/* numberexpression */, 3 ),
	    new Array( 12/* numberexpression */, 3 ),
	    new Array( 12/* numberexpression */, 3 ),
	    new Array( 12/* numberexpression */, 1 ),
	    new Array( 12/* numberexpression */, 1 ),
	    new Array( 12/* numberexpression */, 1 )
	);

	/* Action-Table */
	var act_tab = new Array(
	    /* State 0 */ new Array( 2/* "(" */,3 , 4/* "INT" */,4 , 5/* "FLOAT" */,5 , 7/* "Identifier" */,6 ),
	    /* State 1 */ new Array( 14/* "$" */,0 ),
	    /* State 2 */ new Array( 11/* "/" */,7 , 10/* "*" */,8 , 9/* "-" */,9 , 8/* "+" */,10 , 14/* "$" */,-1 ),
	    /* State 3 */ new Array( 2/* "(" */,3 , 4/* "INT" */,4 , 5/* "FLOAT" */,5 , 7/* "Identifier" */,6 ),
	    /* State 4 */ new Array( 14/* "$" */,-7 , 8/* "+" */,-7 , 9/* "-" */,-7 , 10/* "*" */,-7 , 11/* "/" */,-7 , 3/* ")" */,-7 ),
	    /* State 5 */ new Array( 14/* "$" */,-8 , 8/* "+" */,-8 , 9/* "-" */,-8 , 10/* "*" */,-8 , 11/* "/" */,-8 , 3/* ")" */,-8 ),
	    /* State 6 */ new Array( 14/* "$" */,-9 , 8/* "+" */,-9 , 9/* "-" */,-9 , 10/* "*" */,-9 , 11/* "/" */,-9 , 3/* ")" */,-9 ),
	    /* State 7 */ new Array( 2/* "(" */,3 , 4/* "INT" */,4 , 5/* "FLOAT" */,5 , 7/* "Identifier" */,6 ),
	    /* State 8 */ new Array( 2/* "(" */,3 , 4/* "INT" */,4 , 5/* "FLOAT" */,5 , 7/* "Identifier" */,6 ),
	    /* State 9 */ new Array( 2/* "(" */,3 , 4/* "INT" */,4 , 5/* "FLOAT" */,5 , 7/* "Identifier" */,6 ),
	    /* State 10 */ new Array( 2/* "(" */,3 , 4/* "INT" */,4 , 5/* "FLOAT" */,5 , 7/* "Identifier" */,6 ),
	    /* State 11 */ new Array( 11/* "/" */,7 , 10/* "*" */,8 , 9/* "-" */,9 , 8/* "+" */,10 , 3/* ")" */,16 ),
	    /* State 12 */ new Array( 11/* "/" */,-5 , 10/* "*" */,-5 , 9/* "-" */,-5 , 8/* "+" */,-5 , 14/* "$" */,-5 , 3/* ")" */,-5 ),
	    /* State 13 */ new Array( 11/* "/" */,-4 , 10/* "*" */,-4 , 9/* "-" */,-4 , 8/* "+" */,-4 , 14/* "$" */,-4 , 3/* ")" */,-4 ),
	    /* State 14 */ new Array( 11/* "/" */,7 , 10/* "*" */,8 , 9/* "-" */,-3 , 8/* "+" */,-3 , 14/* "$" */,-3 , 3/* ")" */,-3 ),
	    /* State 15 */ new Array( 11/* "/" */,7 , 10/* "*" */,8 , 9/* "-" */,-2 , 8/* "+" */,-2 , 14/* "$" */,-2 , 3/* ")" */,-2 ),
	    /* State 16 */ new Array( 14/* "$" */,-6 , 8/* "+" */,-6 , 9/* "-" */,-6 , 10/* "*" */,-6 , 11/* "/" */,-6 , 3/* ")" */,-6 )
	);

	/* Goto-Table */
	var goto_tab = new Array(
	    /* State 0 */ new Array( 13/* p */,1 , 12/* numberexpression */,2 ),
	    /* State 1 */ new Array( ),
	    /* State 2 */ new Array( ),
	    /* State 3 */ new Array( 12/* numberexpression */,11 ),
	    /* State 4 */ new Array( ),
	    /* State 5 */ new Array( ),
	    /* State 6 */ new Array( ),
	    /* State 7 */ new Array( 12/* numberexpression */,12 ),
	    /* State 8 */ new Array( 12/* numberexpression */,13 ),
	    /* State 9 */ new Array( 12/* numberexpression */,14 ),
	    /* State 10 */ new Array( 12/* numberexpression */,15 ),
	    /* State 11 */ new Array( ),
	    /* State 12 */ new Array( ),
	    /* State 13 */ new Array( ),
	    /* State 14 */ new Array( ),
	    /* State 15 */ new Array( ),
	    /* State 16 */ new Array( )
	);



	/* Symbol labels */
	var labels = new Array(
	    "p'" /* Non-terminal symbol */,
	    "WHITESPACE" /* Terminal symbol */,
	    "(" /* Terminal symbol */,
	    ")" /* Terminal symbol */,
	    "INT" /* Terminal symbol */,
	    "FLOAT" /* Terminal symbol */,
	    "String" /* Terminal symbol */,
	    "Identifier" /* Terminal symbol */,
	    "+" /* Terminal symbol */,
	    "-" /* Terminal symbol */,
	    "*" /* Terminal symbol */,
	    "/" /* Terminal symbol */,
	    "numberexpression" /* Non-terminal symbol */,
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
	        act = 18;
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
	        if( act == 18 )
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
	            
	            while( act == 18 && la != 14 )
	            {
	                if( _dbg_withtrace )
	                    __dbg_print( "\tError recovery\n" +
	                                    "Current lookahead: " + labels[la] + " (" + info.att + ")\n" +
	                                    "Action: " + act + "\n\n" );
	                if( la == -1 )
	                    info.offset++;
	                    
	                while( act == 18 && sstack.length > 0 )
	                {
	                    sstack.pop();
	                    vstack.pop();
	                    
	                    if( sstack.length == 0 )
	                        break;
	                        
	                    act = 18;
	                    for( var i = 0; i < act_tab[sstack[sstack.length-1]].length; i+=2 )
	                    {
	                        if( act_tab[sstack[sstack.length-1]][i] == la )
	                        {
	                            act = act_tab[sstack[sstack.length-1]][i+1];
	                            break;
	                        }
	                    }
	                }
	                
	                if( act != 18 )
	                    break;
	                
	                for( var i = 0; i < rsstack.length; i++ )
	                {
	                    sstack.push( rsstack[i] );
	                    vstack.push( rvstack[i] );
	                }
	                
	                la = __lex( info );
	            }
	            
	            if( act == 18 )
	            {
	                if( _dbg_withtrace )
	                    __dbg_print( "\tError recovery failed, terminating parse process..." );
	                break;
	            }


	            if( _dbg_withtrace )
	                __dbg_print( "\tError recovery succeeded, continuing" );
	        }
	        
	        /*
	        if( act == 18 )
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
	        rval = vstack[ vstack.length - 3 ];
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
	        rval = vstack[ vstack.length - 1 ];
	    }
	    break;
	    case 8:
	    {
	        rval = vstack[ vstack.length - 1 ];
	    }
	    break;
	    case 9:
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
	
	validateCrossTabCalculatedField: function (str){

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