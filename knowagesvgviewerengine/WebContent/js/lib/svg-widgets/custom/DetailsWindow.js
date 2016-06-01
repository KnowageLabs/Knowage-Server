/**
 * The constructor arguments are the following:
 * 
 * 1. id (String):
 *    unique id that represents a window. The outermost newly created group 
 *    of the Window geometry gets this id. Furthermore, this id is passed to 
 *    callBack functions when triggering Window events.
 *    
 * 2.  parentId (String or object):  
 *     an existing group id or node reference (<g/> or <svg/> element) where the 
 *     new Window object will be appended. This group does not need to be empty. 
 *     The new Window group is appended to this group as last child. If the 
 *     group id does not exist, the script creates a new empty parent group and 
 *     appends it to the root element. 
 *    
 * 3.  width (number):
 *     width of the Window in viewBox coordinates 
 *
 * 4.  height (number):
 *     height of the Window (incl. title and status bar) in viewBox coordinates 
 *
 * 5.  transX (number):
 *     the position of the left edge of the window in viewBox coordinates 
 *
 * 6.  transY (number):
 *     the position of the upper edge of the window in viewBox coordinates 
 *
 * 7.  moveable (boolean, true|false):
 *     indicates whether the Window may be moved or not 
 *
 * 8.  constrXmin (number):
 *     the left constraint, the constraints define the area where the Window can 
 *     be moved within 
 * 
 * 9.  constrYmin (number):
 *     the upper constraint 
 *     
 * 10. constrXmax (number):
 *     the right constraint 
 *     
 * 11. constrYmax (number):
 *     the lower constraint 
 * 
 * 12. showContent (boolean):
 *     value may hold true or false, indicates whether the Window content should 
 *     be visible or hidden during window movements 
 *     
 * 13. placeholderStyles (Array of literals with presentation attributes):
 *     an array literal containing the presentation attributes of the 
 *     placeholder rectangle; this is the style of the placeholder rectangle 
 *     that is displayed instead of the window content if showContent is set to 
 *     true; could include CSS classes; 
 *     example: 
 *     var winPlaceholderStyles = {"fill":"none","stroke":"dimgray",
 *     "stroke-width":1.5}; 
 *      
 * 14. windowStyles (Array of literals with presentation attributes):
 *     an array literal containing the presentation attributes of the Window 
 *     rectangle; could include CSS classes; 
 *     example: 
 *     var windowStyles = {"fill":"aliceblue","stroke":"dimgray",
 *     "stroke-width":1}; 
 *     
 * 15. margin (number):
 *     a number in viewBox coordinates describing a margin. 
 *     Used e.g. for placing text and buttons in statusBar or titleBar 
 *
 * 16. titleBarVisible (boolean, true|false):
 *     indicates whether the Window should have a title bar 
 *     
 * 17. statusBarVisible (boolean, true|false):
 *     indicates whether the Window should have a status bar  
 *     
 * 18. titleText (String or undefined):
 *     a string specifying the Window title text 
 *     
 * 19. statusText (String or undefined):
 *     a string or undefined value specifying the Window status text 
 *     
 * 20. closeButton (boolean, true|false):
 *     indicates whether the Window should have a closeButton. 
 *     The script loooks for an existing symbol with the id "closeButton" or 
 *     creates a new closeButton in the <defs/> section if a symbol with this id
 *     does not exist. Please note that a closed Window still exists and can be 
 *     opened again using the method .open() 
 *
 * 21. minimizeButton (boolean, true|false):
 *     indicates whether the Window should have a minimizeButton. 
 *     The script loooks for an existing symbol with the id "minimizeButton" or 
 *     creates a new minimizeButton in the <defs/> section if a symbol with this
 *     id does not exist 
 *     
 * 22. maximizeButton (boolean, true|false):
 *     indicates whether the Window should have a maximizeButton. 
 *     The script loooks for an existing symbol with the id "maximizeButton" or 
 *     creates a new maximizeButton in the <defs/> section if a symbol with this 
 *     id does not exist 
 *     
 * 23. titlebarStyles (Array of literals with presentation attributes):
 *     an array literal containing the presentation attributes of the titlebar 
 *     rectangle; could include CSS classes; 
 *     example: 
 *     var titlebarStyles = {"fill":"gainsboro","stroke":"dimgray",
 *     "stroke-width":1}; 
 *
 * 24. titlebarHeight (number):
 *     a number value specifiying the titleBar height in viewBox coordinates 
 *
 * 25. statusbarStyles (Array of literals with presentation attributes):
 *     an array literal containing the presentation attributes of the statusbar 
 *     rectangle; could include CSS classes; 
 *     example: var statusbarStyles = {"fill":"aliceblue","stroke":"dimgray",
 *      "stroke-width":1}; 
 *       
 * 26  statusbarHeight (number):
 *     a number value specifiying the statusBar height in viewBox coordinates 
 * 27. titletextStyles (Array of literals with presentation attributes):
 *     an array literal containing the presentation attributes of the titlebar 
 *     text; could include CSS classes; should at least include a "font-size" 
 *     attribute; 
 *     example: var titletextStyles = {"font-family":"Arial,Helvetica",
 *      "font-size":14,"fill":"dimgray"}; 
 *       
 * 28. tatustextStyles (Array of literals with presentation attributes):
 *     an array literal containing the presentation attributes of the statusbar 
 *     text; could include CSS classes; should at least include a "font-size" 
 *     attribute; 
 *     example: 
 *     var statustextStyles = {"font-family":"Arial,Helvetica",
 *     "font-size":10,"fill":"dimgray"}; 
 *     
 * 29. buttonStyles (Array of literals with presentation attributes):
 *     an array literal containing the presentation attributes of the buttons; 
 *     could include CSS classes; should at least include a "fill", "stroke" 
 *     and "stroke-width" attribute; 
 *     example: 
 *     var buttonStyles = {"fill":"gainsboro","stroke":"dimgray",
 *      "stroke-width":1}; 
 *      
 * 30. functionToCall (function, object or undefined):
 *     a callBack function that is called after a window event occurs. 
 *     The parameters for the callBack functions are as follows: 
 *      - id of the Window, eventType (string). 
 *     In case of an object, the method .windowStatusChanged(id,evtType) is 
 *     called. In case of a undefined value, no callBack function is executed. 
 *     "evtType" may hold the following values: 
 *      -  minimized, maximized, closed, opened, removed, moved, movedTo, resized, 
 *         moveStart, moveEnd and created
 *     For some event types, the callBack function is executed with a slight 
 *     delay (200ms) to allow the Window to change state before executing the 
 *     callback function. The events created and resized can be used to trigger 
 *     the creation or update of the window decoration.  
  
  
  
 */   


function DetailsWindow( conf ) {  

 this.config = {};
 
 var defaults = {
    id: 'detail'
    , parentNodeId: 'detailWindow'
    , width: 210
    , height: 200
    , x: 649
    , y: 391
    , moovable: true
    , xMin: 8
    , yMin: 8
    , xMax: 1092
    , yMax: 690
    , showContent: true
    , margin: 3
    , titleBarVisible: true
    , statusBarVisible: false
    , title: 'Detail'
    , statusBarContent: ''
    , closeButtonVisible: false
    , minimizeButtonVisible: true
    , maximizeButtonVisible: true    
    
    , minimized: false
    , transform: 'scale(1.0)'     
  };
  
  var defualtStyles = {
    winPlaceholderStyles: {"fill":"none","stroke":"dimgray","stroke-width":1.5}
    , windowStyles: {"fill":"#fffce6","stroke":"dimgray","stroke-width":1}
    , titlebarStyles: {"fill":"steelblue","stroke":"dimgray","stroke-width":1}
    , titlebarHeight: 17
    , statusbarStyles: {"fill":"aliceblue","stroke":"dimgray","stroke-width":1}
    , statusbarHeight: 13
    , titletextStyles: {"font-family":"Arial,Helvetica","font-size":14,"fill":"white"}
    , statustextStyles: {"font-family":"Arial,Helvetica","font-size":10,"fill":"dimgray"}
    , buttonStyles: {"fill":"steelblue","stroke":"white","stroke-width":2}
  };
  
  var c = {};
  Utils.apply(c, conf || {}, defaults);
  var s = {};
  Utils.apply(s, c.styles, defualtStyles);
  c.styles = s;
  this.config = c;
  
  
  // Call the superclass's constructor in the scope of this.
  Window.call(this, c.id
                    , c.parentNodeId
                    , c.width, c.height, c.x, c.y
                    , c.moovable
                    , c.xMin, c.yMin, c.xMax, c.yMax
                    , c.showContent    // showContent while mooving
                    , c.styles.winPlaceholderStyles
                    , c.styles.windowStyles
                    , c.margin               
                    , c.titleBarVisible             
                    , c.statusBarVisible           
                    , c.title          
                    , c.statusBarContent               
                    , c.closeButtonVisible
                    , c.minimizeButtonVisible
                    , c.maximizeButtonVisible 
                    , c.styles.titlebarStyles, c.styles.titlebarHeight 
                    , c.styles.statusbarStyles, c.styles.statusbarHeight
                    , c.styles.titletextStyles, c.styles.statustextStyles
                    , c.styles.buttonStyles
                    , this.eventHandler); 
  
  this.createContent();  
  
  if( c.minimized === true) {
    this.minimize(true);
  }
}


DetailsWindow.prototype = new Window(); // Set up the prototype chain.
DetailsWindow.prototype.constructor = DetailsWindow; // Set the constructor attribute to Author.

//append new content to the window main group
DetailsWindow.prototype.createContent = function() {
  
  var windowsEl;
  var windowContent;
  var tabGroup;
  var tab1;
  var tab2;
  var dText;
  
  
  windowsEl = document.getElementById(this.config.parentNodeId);
    
  windowContent = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(windowContent, {
    'id' : 'detailWindowBody'
  });
  
  tabGroup = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(tabGroup, {
    'id' : 'detailTabGroup'
    , 'display' : 'none'
  });    
  windowContent.appendChild(tabGroup);    
    
    
   
  tab1 = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(tab1, {
    'id' : 'detailTabGroup__0_content'
  });    
  tabGroup.appendChild(tab1); 
  
  
  
  dText = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(dText, {
    'id' : 'dynamText'
    , 'class' : 'allText normalText'
  });    
  tab1.appendChild(dText);    
  
  tab2 = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(tab2, {
    'id' : 'detailTabGroup__1_content'
  });    
  tabGroup.appendChild(tab2);              


  tabGroup.appendChild(tab1);   
  tabGroup.appendChild(tab2);  
  
  
  windowsEl.appendChild(windowContent);
  
    
  this.appendContent("detailTabGroup", true); 
  
  windowsEl.setAttributeNS(null, 'transform', this.config.transform); 
};

DetailsWindow.prototype.initTabGroups = function() {
		
			//create tabGroups
			myMapApp.TabGroups = new Array();
			
			//first a few styles
			var tabStyles = {"fill":"rgb(180,180,180)","stroke":"dimgray","stroke-width":1,"cursor":"pointer"};
		  var tabwindowStyles = {"fill":"white","stroke":"dimgray","stroke-width":1};
		  var tabtextStyles = {"font-family":"Arial,Helvetica","font-size":15,"fill":"dimgray","font-weight":"normal"};
		  var tabtextStylesCenter = {"font-family":"Arial,Helvetica","font-size":15,"fill":"dimgray","font-weight":"normal","text-anchor":"middle"};
		                
		  //setting tabTitles
		  var tabTitles = new Array("Values","Chart");
		     
		  // tabGroup instances creation
		  myMapApp.TabGroups["detailTabGroup"] = new tabgroup("detailTabGroup","detailTabGroup",5,21,200,175,20,"round","round",5,0,tabStyles,"lightgray",tabwindowStyles,tabtextStyles,tabTitles,0,false,undefined);
		      		              
		  //add new content
		  myMapApp.TabGroups["detailTabGroup"].addContent("detailTabGroup__0_content",0,true);
		  myMapApp.TabGroups["detailTabGroup"].addContent("detailTabGroup__1_content",1,true);

};

DetailsWindow.prototype.initDetailInfopanels =  function () {
			// create detail infopanels
			myMapApp.infoPanel = new Array();
			
      var dynamText = document.getElementById("dynamText");
			for(i = 0; i < sbi.geo.conf.measures.length; i++) {
				var offset = 50 + (i*25);
				var panel = document.createElementNS(Utils.svgNS,"text");
				panel.setAttribute("id","infopanel" + (i+1));
				panel.setAttribute("x", "20");
				panel.setAttribute("y", "" + offset);
				panel.setAttribute("fill", "dimgray");
				panel.setAttribute("font-family", "Arial,Helvetica");
				panel.setAttribute("font-size", "14px");
				panel.setAttribute("startOffset", "0");
				
				var text_node = document.createTextNode(sbi.geo.conf.measures[i].description);
				panel.appendChild(text_node);
				
				dynamText.appendChild(panel);
				
				myMapApp.infoPanel[i] = panel;
			}
};


DetailsWindow.prototype.eventHandler = function(id, evtType) { 
  
};

