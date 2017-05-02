/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
  
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
 
var highlightcolor="#fc3";
var ns6=document.getElementById&&!document.all;
var previous='';
var eventobj;

// SET FOCUS TO FIRST ELEMENT AND HIDE/SHOW ELEMENTS IF JAVASCRIPT ENABLED


// REGULAR EXPRESSION TO HIGHLIGHT ONLY FORM ELEMENTS
	var intended=/INPUT|TEXTAREA|SELECT|OPTION/

// FUNCTION TO CHECK WHETHER ELEMENT CLICKED IS FORM ELEMENT
	function checkel(which){
		if (which.style && intended.test(which.tagName)){return true}
		else return false
	}

// FUNCTION TO HIGHLIGHT FORM ELEMENT
	function highlight(e){
		if(!ns6){
			eventobj=event.srcElement
			if (previous!=''){
				if (checkel(previous))
				previous.style.backgroundColor=''
				previous=eventobj
				if (checkel(eventobj)) eventobj.style.backgroundColor=highlightcolor
			}
			else {
				if (checkel(eventobj)) eventobj.style.backgroundColor=highlightcolor
				previous=eventobj
			}
		}
	}

