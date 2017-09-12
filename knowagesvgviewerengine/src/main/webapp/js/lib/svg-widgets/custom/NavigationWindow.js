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


function NavigationWindow( conf ) {

  // Class members
  this.config = {};
  this.map = undefined;
  this.toolbar = undefined;
  
  var defaults = {
    id: 'navigation'
    , parentNodeId: 'navigationWindow'
    , width: 210
    , height: 200
    , x: 10
    , y: 391
    , moovable: true
    , xMin: 0
    , yMin: 0
    , xMax: 1100 
    , yMax: 768
    , showContent: true
    , margin: 3
    , titleBarVisible: true
    , statusBarVisible: false
    , title: 'Navigation'
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


NavigationWindow.prototype = new Window(); // Set up the prototype chain.
NavigationWindow.prototype.constructor = NavigationWindow; // Set the constructor attribute to Author.


NavigationWindow.prototype.createContent = function() {

  var windowsEl;
  var windowContent;
  var svgEl;
  var controlEl;
  var useEl;
  
  windowsEl = document.getElementById(this.config.parentNodeId);
  
  windowContent = document.getElementById("navigationWindowBody");
  if(!windowContent) {
    alert('ERROR');
    windowContent = document.createElementNS(Utils.svgNS,"g");
    Utils.applyAttributes(windowContent, {
      'id' : 'navigationWindowBody'
      , 'transform' : 'translate(10,537)'
    });   
  }
 
 
  svgEl = document.getElementById("referenceMap");
  if(!svgEl) {
    alert('ERROR');
    svgEl = document.createElementNS(Utils.svgNS,"svg");
    Utils.applyAttributes(svgEl, {
      'id' : 'referenceMap'
      , 'viewBox' : '-2471570.4 -1631093.7 4844781 3030131.8'
      , 'width' : '190'
      , 'height' : '150'
      , 'x' : '10'
      , 'y' : '8'
      , 'preserveAspectRatio' : 'xMidYMid meet'
      , 'zoomAndPan' : 'magnify'
      , 'display' : 'inherit'
      , 'contentScriptType' : 'text/ecmascript'
      , 'contentStyleType' : 'text/css'
      , 'xmlns' : 'http://www.w3.org/2000/svg'
      , 'xmlns:xlink' : 'http://www.w3.org/1999/xlink'
      , 'version' : '1.0'
      , 'cursor' : 'crosshair'     
    }); 
    windowContent.appendChild(svgEl);  
    
    useEl = document.createElementNS(Utils.svgNS,"g");
    Utils.applyAttributes(useEl, {
      'fill' : 'white'
      //, 'xlink:href' : '#mainMapGroup'
      , 'stroke-width' : '2419.9'
      , 'stroke-linecap' : 'round'
      , 'stroke-linejoin' : 'round'
      , 'pointer-events' : 'none'
    });
    useEl.setAttributeNS("xlink", "href", "#mainMapGroup");
    svgEl.appendChild(useEl); 
    
    useEl = document.createElementNS(Utils.svgNS,"g");
    Utils.applyAttributes(useEl, {
      'visibility' : 'hidden'
      //, 'xlink:href' : '#myDragCrossSymbol'
      , 'x' : '-2471570.4'
      , 'y' : '-1631093.7'
    });
    useEl.setAttributeNS("xlink", "href", "#myDragCrossSymbol");
    svgEl.appendChild(useEl); 
  }  
  
  
  
  
  controlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(controlEl, {
    'id' : 'zoomIn'
    , 'cursor' : 'pointer'
    , 'display' : 'inherit'
  });
  windowContent.appendChild(controlEl);   
  
  controlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(controlEl, {
    'id' : 'zoomOut'
    , 'cursor' : 'pointer'
    , 'display' : 'inherit'
  });
  windowContent.appendChild(controlEl); 
  
  controlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(controlEl, {
    'id' : 'zoomFull'
    , 'cursor' : 'pointer'
    , 'display' : 'inherit'
  });
  windowContent.appendChild(controlEl); 
  
  controlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(controlEl, {
    'id' : 'zoomManual'
    , 'cursor' : 'pointer'
    , 'display' : 'inherit'
  });
  windowContent.appendChild(controlEl); 
  
  controlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(controlEl, {
    'id' : 'panManual'
    , 'cursor' : 'pointer'
    , 'display' : 'inherit'
  });
  windowContent.appendChild(controlEl); 
  
  controlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(controlEl, {
    'id' : 'infoButton'
    , 'cursor' : 'pointer'
    , 'display' : 'inherit'
  });
  windowContent.appendChild(controlEl); 
  
  controlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(controlEl, {
    'id' : 'backwardExtent'
    , 'cursor' : 'pointer'
    , 'display' : 'inherit'
  });
  windowContent.appendChild(controlEl); 
  
  controlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(controlEl, {
    'id' : 'forwardExtent'
    , 'cursor' : 'pointer'
    , 'display' : 'inherit'
  });
  windowContent.appendChild(controlEl); 

  controlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(controlEl, {
    'id' : 'mapZoomSlider'
    , 'cursor' : 'pointer'
    , 'display' : 'inherit'
  });
  windowContent.appendChild(controlEl);   
  
  controlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(controlEl, {
    'id' : 'recenterMap'
    , 'display' : 'none'
  });
  windowContent.appendChild(controlEl);   
  
  // create buttons
	myMapApp.buttons = new Array();
	
	 //first a few styles
	var buttonTextStyles = {"font-family":"Arial,Helvetica","fill":"dimgray","font-size":10};
	var buttonStyles = {"fill":"white"};
	var shadeLightStyles = {"fill":"rgb(235,235,235)"};
	var shadeDarkStyles = {"fill":"dimgray"};
	var sliderStyles = {"stroke":"dimgray","stroke-width":"2","fill":"dimgray"};
 
  myMapApp.buttons["zoomIn"] = new button("zoomIn",zoomImageButtons,"rect",undefined,"magnifyerZoomIn",7,165,20,20,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
  myMapApp.buttons["zoomOut"] = new button("zoomOut",zoomImageButtons,"rect",undefined,"magnifyerZoomOut",32,165,20,20,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
	myMapApp.buttons["zoomFull"] = new button("zoomFull",zoomImageButtons,"rect",undefined,"magnifyerFull",57,165,20,20,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
	myMapApp.buttons["zoomManual"] = new switchbutton("zoomManual",zoomImageSwitchButtons,"rect",undefined,"magnifyerManual",82,165,20,20,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
  myMapApp.buttons["panManual"] = new switchbutton("panManual",zoomImageSwitchButtons,"rect",undefined,"symbPan",107,165,20,20,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
  
  myMapApp.buttons["infoButton"] = new switchbutton("infoButton",zoomImageSwitchButtons,"rect",undefined,"infoBut",132,165,20,20,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
	
  myMapApp.buttons["infoButton"].setSwitchValue(true,false);
	//statusChange("Mode: Infomode");  
  myMapApp.buttons["backwardExtent"] = new button("backwardExtent",zoomImageButtons,"rect",undefined,"symbArrowLeft",157,165,20,20,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
  myMapApp.buttons["forwardExtent"] = new button("forwardExtent",zoomImageButtons,"rect",undefined,"symbArrowRight",182,165,20,20,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
	  
	 
	  
	//myMapApp.zoomSlider = 
  myMapApp.zoomSlider = new slider("zoomSliderId", 
						 "mapZoomSlider", 
											 180,153,
											 myMainMap.minWidth,
											 20,153,
											 myMainMap.maxWidth,
											 myMainMap.maxWidth,
											 sliderStyles, 
											 15,
											 "sliderSymbol",
											 myMapApp.refMapDragger,			
										     true);  
	  
  myMapApp.buttons["recenterMap"] = new switchbutton("recenterMap",zoomImageSwitchButtons,"rect",undefined,"symbRecenter",132,165,20,20,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
  
  myMainMap.checkButtons();
 

  windowsEl.appendChild(windowContent); 
 
  
  this.appendContent("referenceMap", true); 
  
  this.appendContent("zoomIn", true);   
  this.appendContent("zoomOut", true); 
  this.appendContent("zoomFull", true); 
  this.appendContent("zoomManual", true); 
  this.appendContent("panManual", true); 
  this.appendContent("infoButton", true); 
  this.appendContent("backwardExtent", true); 
  this.appendContent("forwardExtent", true); 
  
  this.appendContent("mapZoomSlider", true);   
  
    
  windowsEl.setAttributeNS(null, 'transform', this.config.transform);   
  
  
}

NavigationWindow.prototype.eventHandler = function(id, evtType) { 
  
};