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
  * Public Functions
  * 
  *  [list]
  * 
  * 
  * Authors
  * 
  * - Antonella Giachino (antonella.giachino@eng.it)
  */

Ext.define('Sbi.network.commons.Utils', {
	statics: {
		unimplementedFunction: function(fnName) {
			var msg = fnName? 
				'Sorry, the functionality [' + fnName + '] has not been implemented yet':
				'Sorry, this functionality has not been implemented yet';
				
			Ext.Msg.show({
				   title:'Unimplemented functionality',
				   msg: msg,
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
			});
		}
	
	    , deprectadeFunction: function(fnClass, fnName) {
			var msg = fnName + ' in class ' + fnClass + 'is deprecated';
				
			Ext.Msg.show({
				   title:'Deprecated functionality',
				   msg: msg,
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
		}
	
		
		, log: function(severity, message) {
			this.unimplementedFunction('Sbi.network.commons.log');
		}
		
		, assertTrue: function(condition, msg) {
			this.unimplementedFunction('Sbi.network.commons.assertTrue');
		}
		
		, toStr : function(o) {
			var str = "";
			
			if(o === 'undefined') {return 'undefined';}
			
			str += "Type: [" + typeof(o) + "]\n------------------------\n";
			
	        for(p in o) {
	        	str += p + ": " +  o[p] + "\n";
	        }
	        return str;
		},
		
		dump : function(o) {
			alert(this.toStr(o));
		}
	}
});

/*
Ext.ns("Sbi.qbe.commons");

Sbi.qbe.commons.Utils = function(){
 
    // private variables
	
    // public space
	return {
		
		unimplementedFunction: function(fnName) {
			var msg = fnName? 
				'Sorry, the functionality [' + fnName + '] has not been implemented yet':
				'Sorry, this functionality has not been implemented yet';
				
			Ext.Msg.show({
				   title:'Unimplemented functionality',
				   msg: msg,
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
			});
		}
	
	    , deprectadeFunction: function(fnClass, fnName) {
			var msg = fnName + ' in class ' + fnClass + 'is deprecated';
				
			Ext.Msg.show({
				   title:'Deprecated functionality',
				   msg: msg,
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
		}
	
		
		, log: function(severity, message) {
			this.unimplementedFunction('Sbi.qbe.commons.log');
		}
		
		, assertTrue: function(condition, msg) {
			this.unimplementedFunction('Sbi.qbe.commons.assertTrue');
		}
        
	};
}();








// ----- deprecated ------------------------------------------------------------------------------
Ext.namespace('it.eng.spagobi.engines.qbe.commons');


it.eng.spagobi.engines.qbe.commons = function(){
	// do NOT access DOM from here; elements don't exist yet
 
    // private variables
   
 
    // public space
	return {
	
		init : function() {
			//alert("init");
		},
		
		toStr : function(o) {
			var str = "";
			
			if(o === 'undefined') return 'undefined';
			
			str += "Type: [" + typeof(o) + "]\n------------------------\n";
			
	        for(p in o) {
	        	str += p + ": " +  o[p] + "\n";
	        }
	        return str;
		},
		
		dump : function(o) {
			alert(this.toStr(o));
		}
        
	};
}();

//----- deprecated ------------------------------------------------------------------------------

*/
						