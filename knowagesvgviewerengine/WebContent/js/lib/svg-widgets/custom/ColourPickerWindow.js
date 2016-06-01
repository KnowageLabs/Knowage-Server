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


function ColourPickerWindow( conf ) {

  // Class members
  this.config = {};                    
  this.groupIndex = 0;
	this.groupColourBkp = 0;
  
  var defaults = {
    id: 'colourpicker'
    , parentNodeId: 'colourpickerWindow'
    , width: 320
    , height: 200
    , x: 236
    , y: 350
    , moovable: true
    , xMin: 8
    , yMin: 8
    , xMax: 1092
    , yMax: 690
    , showContent: true
    , margin: 3
    , titleBarVisible: true
    , statusBarVisible: false
    , title: 'Select a colour ...'
    , statusBarContent: ''
    , closeButtonVisible: false
    , minimizeButtonVisible: true
    , maximizeButtonVisible: true    
    
    , minimized: false    
    , closed: true
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
  
  if( c.closed === true) {
    this.close(true);
  } 

}


ColourPickerWindow.prototype = new Window(); // Set up the prototype chain.
ColourPickerWindow.prototype.constructor = ColourPickerWindow; // Set the constructor attribute to Author.


//append new content to the window main group
ColourPickerWindow.prototype.createContent = function() {
  
  var windowsEl;
  var windowContent;
  var boxes;
  var buttonsPanelEl;
  var applyButtonlEl;
  var okButtonEl;
  var cancelButtonEl;
  
  windowsEl = document.getElementById(this.config.parentNodeId);
 
  windowContent = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(windowContent, {
    'id' : 'colourpickerWindowBody'
  });   
  
  
  boxes = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(boxes, {
    'id' : 'colourPickerBox_1'
    , 'display' : 'inherit'
  });    
  windowContent.appendChild(boxes);    

  buttonsPanelEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(buttonsPanelEl, {
    'id' : 'colourPickerButtons'
    , 'display' : 'inherit'
  });    
  windowContent.appendChild(buttonsPanelEl); 
  
  applyButtonlEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(applyButtonlEl, {
    'id' : 'colourPickerApplay'
  });    
  buttonsPanelEl.appendChild(applyButtonlEl); 
  
  okButtonEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(okButtonEl, {
    'id' : 'colourPickerOk'
  });    
  buttonsPanelEl.appendChild(okButtonEl); 
  
  cancelButtonEl = document.createElementNS(Utils.svgNS,"g");
  Utils.applyAttributes(cancelButtonEl, {
    'id' : 'colourPickerCancel'
  });    
  buttonsPanelEl.appendChild(cancelButtonEl); 
  
						
  windowsEl.appendChild(windowContent);
  
  
  this.appendContent("colourPickerBox_1", true); 
  this.appendContent("colourPickerButtons", true); 
  
  
    
  
  // Create colour pickers
	myMapApp.colourPickers = new Array();
		
	// Colour picker styles
	var cpBgStyles = {"fill":"gainsboro"};
	var cpBgStyles = {"fill":"white","stroke":"dimgray","stroke-width":1};
	var cpTextStyles = {"font-family":"Arial,Helvetica","font-size":12,"fill":"dimgray"};
		
	myMapApp.colourPickers["colourPicker_1"]  = new colourPicker(
                 "colourPicker_1", //the id of the colour picker	
								 "colourPickerBox_1", //the id or node reference of the parent group where the button can be appended
								 10,27, //upper left corner of colour picker
								 300,120, //width & height of colour picker
								 cpBgStyles,
								 cpTextStyles,
								 "sliderSymbol", //id referencing the slider symbol to be used for the sliders
								 true, // satSliderVisible
								 true, // valSliderVisible
								 false, // alphaSliderVisible
								 true, // colValTextVisible
								 true, // fillVisible
								 false, // strokeVisible
								 0, 360, // start & end hue in degree (0 to 360)
								 7, //nr of stop vals in between, in addition to start and end
								 "255,0,0,1", // fillStartColor: string, rgba.format, f.e. 255,0,0,1
								 "0,0,0,0.7", // strokeStartColorstring, rgba.format, f.e. 255,0,0,1
								 undefined);  
								 
	//first a few styles
	var buttonTextStyles = {"font-family":"Arial,Helvetica","fill":"dimgray","font-size":10};
	var buttonStyles = {"fill":"white"};
	var shadeLightStyles = {"fill":"rgb(235,235,235)"};
	var shadeDarkStyles = {"fill":"dimgray"};
								 
	new button("colourPickerApplay", this.buttonClick,"rect","Applay", undefined, 95, 160, 40,25,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
	new button("colourPickerOk", this.buttonClick,"rect","Ok", undefined, 140, 160, 40,25,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
	new button("colourPickerCancel", this.buttonClick,"rect","Cancel", undefined, 185,160,40,25,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,1);
			
	
	windowsEl.setAttributeNS(null, 'transform', this.config.transform); 

};



ColourPickerWindow.prototype.buttonClick = function(groupId, evt, buttonText) {
      var scope = myMapApp.Windows['colourpicker'];
      
			if(buttonText == "Applay") {
				alert(scope.groupIndex);
				var values = myMapApp.colourPickers["colourPicker_1"].getValues();
				var colour = scope.getRGB16(values);
				
				myMapApp.colArray[scope.groupIndex-1] = colour;
				var rect = document.getElementById("rect" + scope.groupIndex);
				rect.setAttribute("fill", colour);
				colourMap();    
			} else if(buttonText == "Ok") {
				var values = myMapApp.colourPickers["colourPicker_1"].getValues();
				var colour = scope.getRGB16(values);
				myMapApp.colArray[scope.groupIndex-1] = colour;
				var rect = document.getElementById("rect" + scope.groupIndex);
				rect.setAttribute("fill", colour);
			  scope.close();
				colourMap();           		
			} else if(buttonText == "Cancel") {
				var values = myMapApp.colourPickers["colourPicker_1"].getValues();
				var colour = scope.getRGB16(values);
				myMapApp.colArray[this.groupIndex-1] = scope.groupColourBkp;
				var rect = document.getElementById("rect" + scope.groupIndex);
				rect.setAttribute("fill", scope.groupColourBkp);
				scope.close();
				colourMap(); 
			}
};
		
ColourPickerWindow.prototype.getRGB16 = function(values) {
			var red = values.fill.red.toString(16);
			if(red.length == 1) red = "0" + red;
			var green = values.fill.green.toString(16);
			if(green.length == 1) green = "0" + green;
			var blue = values.fill.blue.toString(16);
			if(blue.length == 1) blue = "0" + blue;
			var colour = "#" + red + green + blue;
			return colour;
};
		
ColourPickerWindow.prototype.openColourPiker = function(evt) {
		  
      var el = evt.target;
      var id = el.getAttribute("id");
      var fill = el.getAttribute("fill");
           
      this.groupIndex = parseInt(id.substring(4));
      this.groupColourBkp = myMapApp.colArray[this.groupIndex-1];
           
      var rgb16 = new Array();
      var rgb10 = new Array();
           
      rgb16["red"] = fill.substring(1,3);
      rgb16["green"] = fill.substring(3,5);
      rgb16["blue"] = fill.substring(5);
           
      rgb10["red"] = parseInt(rgb16["red"],16);
      rgb10["green"] = parseInt(rgb16["green"],16);
      rgb10["blue"] = parseInt(rgb16["blue"],16);
           
      this.open();
           
      myMapApp.colourPickers["colourPicker_1"].setRGBAColour("fill",rgb10["red"],rgb10["green"], rgb10["blue"],1, true);    
};



ColourPickerWindow.prototype.eventHandler = function(id, evtType) { 
  
};





