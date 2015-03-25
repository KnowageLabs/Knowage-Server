/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
function isIE() {
	navigatorname = navigator.appName;
    navigatorname = navigatorname.toLowerCase();
    return (navigatorname.indexOf('explorer') != -1);
}

function isMoz() {
	navigatorname = navigator.appName;
    navigatorname = navigatorname.toLowerCase();
    return (navigatorname.indexOf('explorer') == -1);
}

function isIE5() {
	navigatorname = navigator.appName;
    navigatorversion = navigator.appVersion;
    navigatorname = navigatorname.toLowerCase();
    return ( (navigatorname.indexOf('explorer') != -1) && (navigatorversion.indexOf('MSIE 5') != -1) );
}

function isIE6(){
	navigatorname = navigator.appName;
    navigatorversion = navigator.appVersion;
    navigatorname = navigatorname.toLowerCase();
    return ( (navigatorname.indexOf('explorer') != -1) && (navigatorversion.indexOf('MSIE 6') != -1) );
}

function isIE7() {
	navigatorname = navigator.appName;
    navigatorversion = navigator.appVersion;
    navigatorname = navigatorname.toLowerCase();
    return ( (navigatorname.indexOf('explorer') != -1) && (navigatorversion.indexOf('MSIE 7') != -1) );
}


function FCKeditor_OnComplete( editorInstance ) {
        completeName = editorInstance.Name;
        instanceName = completeName.substring(13, completeName.length);
        var functocall = 'initializeNotes' + instanceName +'()';
        eval(functocall);
}


function findPos(obj) {
       curleft = curtop = 0;
	   if (obj.offsetParent) {
		    curleft = obj.offsetLeft
		    curtop = obj.offsetTop
		    while (obj = obj.offsetParent) {
			     curleft += obj.offsetLeft
			     curtop += obj.offsetTop
		    }
	   }
	   return [curleft,curtop];
 }
 

function getCookie(NameOfCookie){
    if (document.cookie.length > 0) {              
   		 begin = document.cookie.indexOf(NameOfCookie+"=");       
    	if (begin != -1) {           
     		 begin += NameOfCookie.length+1;       
    		 end = document.cookie.indexOf(";", begin);
     		 if (end == -1) 
     		 		end = document.cookie.length;
       		return unescape(document.cookie.substring(begin, end));
   		 } 
 	 }
 	 return null;
}

function setCookie(NameOfCookie, value, expiredays) {
		var ExpireDate = new Date();
		ExpireDate.setTime(ExpireDate.getTime() + (expiredays * 24 * 3600 * 1000));
		 document.cookie = NameOfCookie + "=" + escape(value) +   ((expiredays == null) ? "" : "; expires=" + ExpireDate.toGMTString());
}

function delCookie (NameOfCookie) {
 	 if (getCookie(NameOfCookie)) {
   		 document.cookie = NameOfCookie + "=" +  "; expires=Thu, 01-Jan-70 00:00:01 GMT";
 	 }
}


// START PORLTET JAVASCRIPT INTIIALIZER
// When you wnat to exec a javascript when the page is loaded you simply set
// the onload attribute on body tag.
// Unfortunately with portlet you cannot because is the portal that write the body tag.
// for this reason you have to set into the portlet html code the js function window.onload=name function.
// Unfortunately if you have more than a portlet in a page the last one ovverrides 
// the window.onload set made by the previous one.
// The SpagoBI solution is to define a global object for the page. The global object
// is defined into this javascript which is linked by the html code of the portelt.
// In this way the page has only one object instance. The object is call "SbiJsInitializer".
// the base class for the object is "SbiJsInitializerClass".
// If you want that each portlet of the page executes a js funciont when the page is loaded 
// you need to add a function to the global object into the html page of the portlet. Example:
//
// SbiJsInitializer.myfunct = myfunctName;
// function myfunctName() {
//    do something
// }
// 
// Then for each portelt set the window.onload as below:
//
// window.onload = SbiJsInitializer.initialize;

 
function SbiJsInitializerClass() {

  this.initialize = Initialize;

  function Initialize() {
      for(prop in SbiJsInitializer)  {
        if(prop != "initialize") {
           eval("SbiJsInitializer."+prop+"()");
        }
      } 
  }
  
}


 try{
 	 SbiJsInitializer.prototype;
 } catch (err) {
 	 var SbiJsInitializer = new SbiJsInitializerClass();
 }


// END PORLTET JAVASCRIPT INTIIALIZER

// START IFRAMES NAVIGATOR INITIALIZER
/*
function IFramesNavigator() {

  
  	Structure of IFramesNavigator navigator:
  	it contains a variable 'frames' that is an array;
  	each element of 'frames' is a 'mainFrame';
  	a mainFrame is an array with length = 2:
  	- mainFrame[0] contains the String id of the spagobi execution flow id (drill-through share this id);
  	- mainFrame[1] contains an array: each element of this array is an 'iframeWithLabel', that is
  				- iframeWithLabel[0] is the nested iframe;
  				- iframeWithLabel[1] is the iframe label.
  

  this.frames = new Array();
  
  this.addFrame = AddFrame;
  
  function AddFrame(id, nestedFrame, label) {
    frameToAdd = new Array(nestedFrame, label);
    mainFrame = this.getMainFrame(id);
    if (mainFrame == null) {
      mainFrame = new Array();
      mainFrame[0] = id;
      temp = new Array();
      temp[0] = frameToAdd;
      mainFrame[1] = temp;
      this.frames[this.frames.length] = mainFrame;
    } else {
      temp = mainFrame[1];
      temp[temp.length] = frameToAdd;
    }
  }
  
  this.removeFrames = RemoveFrames;
  
  function RemoveFrames(id, index) {
    mainFrame = this.getMainFrame(id);
    if (mainFrame == null) return;
    initialLength = mainFrame[1].length;
    for (i = 1; i < initialLength - index; i++) {
      mainFrame[1].pop();
    }
    iframesWithLabel = mainFrame[1];
    iframeWithLabelToReload = iframesWithLabel[iframesWithLabel.length - 1];
    iframeToReload = iframeWithLabelToReload[0];
    iframeToReload.click();
    //try {
    //  iframeToReload.location.reload();
    //} catch (err) {
    //  alert(err.description);
    //}
    //window.history.go(-(initialLength - 1 - index));
  }
  
  this.getFrames = GetFrames;
  
  function GetFrames(id) {
    mainFrame = this.getMainFrame(id);
    return mainFrame[1];
  }
  
  this.getFrame = GetFrame;
  
  function GetFrame(id, index) {
    mainFrame = this.getMainFrame(id);
    if (mainFrame == null) return null;
    return mainFrame[index];
  }
  
  this.getMainFrame = GetMainFrame;
  
  function GetMainFrame(id) {
  	mainFrame = null;
    for (i = 0; i < this.frames.length; i++) {
      temp = this.frames[i];
      if (temp[0] == id) {
      	mainFrame = temp;
      	break;
      }
    }
    return mainFrame;
  }

}

try {
  iframesNavigator.prototype;
} catch (err) {
  var iframesNavigator = new IFramesNavigator();
}
// END IFRAMES NAVIGATOR INITIALIZER
*/

function toggle(elementId, togglerId, toggled) {
	Ext.onReady(function() {
		var element = Ext.get(elementId);
	    element.enableDisplayMode("block");
	    if (!toggled) {
	    		element.toggle(true);
	    }
	    Ext.get(togglerId).on('click', function() {
	        element.toggle(true);
	    });
	});
}

function setVisible(elementId, togglerId, visible) {
	Ext.onReady(function() {
		var element = Ext.get(elementId);
	    	
	    element.setVisible(visible);

	});
}

function toggleWithCookie(elementId, togglerId, toggled) {
	Ext.onReady(function() {
		var element = Ext.get(elementId);
	    element.enableDisplayMode("block");
	    var value = getCookie(elementId);
	    if (value == '') {
	    	if (toggled) {
	    		element.toggle(true);
	    		setCookie(elementId, 'open');
	    	}
	    } else {
	    	if (value == 'open') element.toggle(true);
	    }
	    
	    Ext.get(togglerId).on('click', function() {
	    	if (value == 'open') setCookie(elementId, 'closed');
	    	else setCookie(elementId, 'open');
	        element.toggle(true);
	    });
	});
}

// [Cookie] Gets a value from a cookie
function getCookie(cookieName) {
	var cookieValue = '';
	var posName = document.cookie.indexOf(escape(cookieName) + '=');
	if (posName != -1) {
		var posValue = posName + (escape(cookieName) + '=').length;
		var endPos = document.cookie.indexOf(';', posValue);
		if (endPos != -1) cookieValue = unescape(document.cookie.substring(posValue, endPos));
		else cookieValue = unescape(document.cookie.substring(posValue));
	}
	return (cookieValue);
};

// [Cookie] Sets value in a cookie
function setCookie(cookieName, cookieValue, expires, path, domain, secure) {
	document.cookie =
		escape(cookieName) + '=' + escape(cookieValue)
		+ (expires ? '; expires=' + expires.toGMTString() : '')
		+ (path ? '; path=' + path : '')
		+ (domain ? '; domain=' + domain : '')
		+ (secure ? '; secure' : '');
};

if (!Array.prototype.contains){
	Array.prototype.contains = function(obj){
		var len = this.length;
		for (var i = 0; i < len; i++){
			if(this[i]===obj){return true;}
		}
    	return false;
	};
}

if (!Array.prototype.removeFirst){
	Array.prototype.removeFirst = function(obj) {
		for (i = 0; i < this.length; i++) {
			if(this[i] == obj) {
				this.splice(i, 1);
				return;
			}
		}
	};
}

if(!String.prototype.trim) {
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g, '');
	}
}