/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


	/**
	 * patch a bug in the datepicker (see SPAGOBI-324). 
	 * The error is in the way Ext.util.ClickRepeater manage repeated click event. The problem seem to be related to the event 
	 * mouseup that is not handled properly by ClickRepeater. This cause the click event sequence to continue even after the
	 * mouse button is released. This patch is not resolutive. It is just a work-around that eliminate ClickRepeater behaviour 
	 */

/* =============================================================================
* Added by Monica Franceschini (October 2013)
* IE does not support Array.indexOf function
* See https://www.spagoworld.org/jira/browse/SPAGOBI-1293
* See http://coderesponsible.com/array-indexof-in-javascript-not-working-in-ie/
============================================================================= */

/* =============================================================================
* Added by Monica Franceschini (November 2013)
* to avoid XSS Injection / HTML Injection vulnerabilities
============================================================================= */

/* http://www.sencha.com/forum/showthread.php?136911
 * http://www.sencha.com/forum/showthread.php?232805
 * http://www.sencha.com/forum/archive/index.php/t-136911.html?s=1b63f75ac473d93b04d1917ed5567746
 * [EXTJSIV-3414] - validateedit does not have new values*/
Ext.override(Ext.grid.Panel, {

	listeners: { 
		'validateedit': function(ed, e) {

			var editor = ed.getEditor(e.record, e.column),
			v = editor.getValue();
			//only deletes tags not minus major symbols
			if(v.indexOf('<') != -1 && v.indexOf('>') != -1){
				alert("Characters < and > not allowed at the same time");
				var safeVal = Ext.util.Format.stripTags(v);
				if(safeVal==""){
					safeVal=" ";
				}
				if(safeVal != v){
					editor.setValue(safeVal);
					editor.setRawValue(safeVal);
				}
				
			}

		} 
	}
});

