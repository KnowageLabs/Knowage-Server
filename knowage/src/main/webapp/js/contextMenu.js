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
 

function hideMenu(event, divId) {
	//divM = document.getElementById('divmenuFunct');
	divM = document.getElementById(divId);
  var theTop;
	if (document.documentElement && document.documentElement.scrollTop) {
		theTop = document.documentElement.scrollTop;
	}
	else {
		if (document.body){
			theTop = document.body.scrollTop;
		}
	}
	
	parentContainerPosition = findParentContainerPosition(divM);
	
  yup = parseInt(divM.style.top) + parentContainerPosition[1] - parseInt(theTop);
  ydown = parseInt(divM.style.top) + parentContainerPosition[1] + parseInt(divM.offsetHeight) - parseInt(theTop);
  xleft = parseInt(divM.style.left) + parentContainerPosition[0];
  xright = parseInt(divM.style.left) + parentContainerPosition[0] + parseInt(divM.offsetWidth);
  if( (event.clientY<=(yup+2)) || (event.clientY>=ydown) || (event.clientX<=(xleft+2)) || (event.clientX>=xright) ) {
		divM.style.display = 'none' ;
	}
}	

function showMenu(event, divM) {
	var theTop;
	if (document.documentElement && document.documentElement.scrollTop) {
		theTop = document.documentElement.scrollTop;
	}
	else {
		if (document.body){
			theTop = document.body.scrollTop;
		}
	}
	var theLeft;
	if (document.documentElement && document.documentElement.scrollLeft) {
		theLeft = document.documentElement.scrollLeft;
	}
	else {
		if (document.body){
			theLeft = document.body.scrollLeft;
		}
	}
	
	parentContainerPosition = findParentContainerPosition(divM);
	
  divM.style.left = '' + (event.clientX + theLeft - parentContainerPosition[0] - 5) + 'px';
	divM.style.top = '' + (event.clientY + theTop - parentContainerPosition[1] - 5) + 'px';
	divM.style.display = 'inline' ;

}

function findParentContainerPosition(obj) {
	parentNode = divM.parentNode;
	while (true) {
  	 if (parentNode == document) {break;}
  	 if (parentNode.style && parentNode.style.position) {
  	   break;
     } else {
       parentNode = parentNode.parentNode;
     }
  }
  if (parentNode != null) {
  	return findPos(parentNode);
	} else {
    return [0,0];
  }
}