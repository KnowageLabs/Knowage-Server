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
  * - Chiara Chiarelli (chiara.chiarelli@eng.it)
  */

Ext.ns("Sbi.home");

Sbi.home.Banner = function(config) {
		
		this.initButtons(config);
		this.initToolbar();
		
		var itemsForBanner = [];
		
		if(showBanner){
			itemsForBanner.push({
			        contentEl: 'Banner'
		            });
	    }

		//add first menu toolbar		
		itemsForBanner.push(this.tbx);
		
       	if(this.useToolbar2){	
       		//add second menu toolbar
       		itemsForBanner.push(this.tbx2);
       	}
        if(this.useToolbar3){	
        	//add third menu toolbar
       		itemsForBanner.push(this.tbx3);
       	}
		
		var c = Ext.apply({}, config, {
			region: 'north',
	        margins:'0 0 0 0',
	        items: itemsForBanner,
	        border: false,
	        scrolling  : 'no',	
			collapseMode: 'mini',
	        autoHeight: true,
		forceLayout: true
		});   	
      
		Sbi.home.Banner.superclass.constructor.call(this, c);	
};

Ext.extend(Sbi.home.Banner, Ext.Panel, {
	
	// ---------------------------------------------------------------------------
    // object's members
	// ---------------------------------------------------------------------------
	
	//main toolbar
	 tbx:null ,  
	//eventual second toolbar
	 tbx2:null ,  
	//boolean for weather the second toolbar is used or not
	 useToolbar2: false,
	//eventual third toolbar
	 tbx3:null ,  
	//boolean for weather the third toolbar is used or not 
	 useToolbar3: false,
	//languages menu
	 languages: null,
	//themes menu 
	 themes: null,
	//themes menu button
	 tbThemesButton: null,
	//info menu button   
	 tbInfoButton: null,
	 	//role menu button   
	 tbRoleButton: null,	
	//languages menu button	        
	 tbLanguagesButton: null,	
	//toolbar welcome text
	 tbWelcomeText: null,	
	//array containing all user menus
	 menuArray: null,
	//array containing all possible themes (>1) 
	 menuThemesArray: null,
	 
	// ---------------------------------------------------------------------------
    // public methods
	// ---------------------------------------------------------------------------
	
	//Verifies if the menu has subitems
	hasItems: function(menu){
		var toReturn = false;
		if(menu.items){toReturn = true;}
		else{toReturn = false;}
		
		return toReturn;
	},
	
	//returns a menu with its items (recursive method)
	getItems: function(menu){
	 		var toReturn = [];
	 		var hasIt= this.hasItems(menu);
	 		if(hasIt){	
	 				for(var i = 0; i < menu.items.length; i++) {
	 				var hasIt2= this.hasItems(menu.items[i]);
		 				if(hasIt2){
		 				//in case menu has items
		 				 var tempIt = this.getItems(menu.items[i]);
		 				 toReturn.push(    
                          new Ext.menu.Item({
                              id: menu.items[i].id,
							  menu:{
									listeners: {'mouseexit': function(item) {item.hide();}},
				        			items: tempIt
				        		   },
				        	  text: menu.items[i].text,
						 	  icon: menu.items[i].icon,
							  href: menu.items[i].href                       
                         	 }) 
						  )
		 				}else{
		 				//in case menu doesn't have items
		 				 toReturn.push(
						   new Ext.menu.Item({
								id: menu.items[i].id,
				        		text: menu.items[i].text,
						 		icon: menu.items[i].icon,
								href: menu.items[i].href
							})
						  )
		 				}
					}
			}	
	 		return toReturn;	
	 },
	 
	 //returns a menuButton with its Menu and MenuItems
	 getMenu: function(menu){
	 	var toReturn ;
	 	var hasIt= this.hasItems(menu);
	 		if (hasIt){
	 		    //in case menu Button has items
	 			toReturn = new Ext.Toolbar.MenuButton({
							id: menu.id,
					        text: menu.text,
							path: menu.path,	
	            			menu: new Ext.menu.Menu({
	            				id: menu.id,
								listeners: {'mouseexit': function(item) {item.hide();}},
								items: this.getItems(menu)
								}),
							handler:  function(item) {
	 							// if toolbar button has href property, the handler will call that href, otherwise it will show the button's menu
	 							if (menu.href !== undefined && menu.href !== null && menu.href !== '') {
	 								eval(menu.href);
	 							} else {
	 								item.showMenu();
	 							}
	 						},
	            			icon:  menu.icon,
					        cls: 'x-btn-menubutton x-btn-text-icon bmenu '
					        })
	 		}else{
	 			//in case menu Button doesn't have items
	 			toReturn = new Ext.Toolbar.Button({
							id: menu.id,
					        text: menu.text,
							path: menu.path,	
							handler:  function() {eval(menu.href);} ,
	            			icon:  menu.icon,
	            			scope: this,
					        cls: 'x-btn-menubutton x-btn-text-icon bmenu '
					        })
	 		}	
	 	return toReturn;
	 },
	 
	 //returns the language url to be called in the language menu
	 getLanguageUrl: function(config){
	   var languageUrl = "javascript:execUrl('"+Sbi.config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=CHANGE_LANGUAGE&LANGUAGE_ID="+config.language+"&COUNTRY_ID="+config.country+"')";
	   return languageUrl;
	 },
	
	//initializes all menu Buttons
	 initButtons: function(config){
	 		
	 		var menus = config.bannerMenu;
	 		//user menus initialization
	 		if(menus){
	 			this.menuArray = [];
	 			if(menus.items){
	 			for(var i = 0; i < menus.items.length; i++) {
					this.menuArray.push(this.getMenu(menus.items[i]));
	 				}
	 			  }
	 			}
	 		
	 		//themes menus initialization
	 		var menuThemesList = config.themesMenu;
	 		if(drawSelectTheme && menuThemesList){
	 			this.menuThemesArray = [];
	 			for(var i = 0; i < menuThemesList.items.length; i++) {
					this.menuThemesArray.push(
					new Ext.menu.Item({
					 	id: menuThemesList.items[i].id,
 						text: menuThemesList.items[i].text,
						href: menuThemesList.items[i].href				
 						})
					);
	 			}
	 			
	 			this.themes = new Ext.menu.Menu({ 
 								id: 'themes', 
			 					items: this.menuThemesArray
			 					});
			 	this.themes.addListener('mouseexit', function(item) {item.hide();});
			 	
			 	this.tbThemesButton = new Ext.Toolbar.Button({
			 		text: themesViewName,
			 		icon: themesIcon,
			 		cls: 'x-btn-text-icon bmenu',
			 		menu: this.themes,
			 		scope: this
		 		});	
	 		}
	 		
	 		var languagesMenuItems = [];
	 		for (var i = 0; i < Sbi.config.supportedLocales.length ; i++) {
	 			var aLocale = Sbi.config.supportedLocales[i];
 				var aLanguagesMenuItem = new Ext.menu.Item({
					id: '',
					text: aLocale.language,
					iconCls:'icon-' + aLocale.language,
					href: this.getLanguageUrl(aLocale)
				})
 				languagesMenuItems.push(aLanguagesMenuItem);
	 		}
	 		
	 		//languages menus initialization
	 		this.languages = new Ext.menu.Menu({ 
	 			id: 'languages', 
	 			items: languagesMenuItems
	 		});
	 	this.languages.addListener('mouseexit', function(item) {item.hide();});	
 	    
 	    
 	    
 	    //exit button initialization
 		this.tbExitButton = new Ext.Toolbar.Button({
	            id: '5',
	            text:  LN('sbi.home.Exit'),
	            iconCls: 'icon-exit',
	            cls: 'x-btn-logout x-btn-text-icon bmenu',
	            handler: this.logout,	
	            scope: this
	        })  ;
	    
	    //info button initialization
	    this.tbInfoButton = new Ext.Toolbar.Button({
		            id: '',
		            iconCls: 'icon-info',
		            cls: 'x-btn-logout x-btn-text-icon bmenu',
		            handler: this.info,
		            scope: this
		        })	;	
		 
		//role button initialization
		// draw only if user has more than one role
		//alert(Sbi.user.roles.length);
		if(Sbi.user.roles && Sbi.user.roles.length > 1){
			this.tbRoleButton = new Ext.Toolbar.Button({
		            id: '',
		            iconCls: 'icon-role',
		            cls: 'x-btn-logout x-btn-text-icon bmenu',
		            handler: this.role,
		            scope: this
			});	
		 }
		 
		//languages button initialization       
		this.tbLanguagesButton = new Ext.Toolbar.Button({
			 		text: '',
			 		iconCls:'icon-'+Sbi.config.curr_language,
			 		cls: 'x-btn-text-icon bmenu',
			 		menu: this.languages,
			 		scope: this
		 		});			 		
		//Welcome Text initialization
		 this.tbWelcomeText = new Ext.Toolbar.TextItem({
					text: LN('sbi.home.Welcome')+'<b>'+ Sbi.user.userName+'<b>&nbsp;&nbsp;&nbsp;'
				});
	 },
	 	
	 //initialization of all toolbars
	 initToolbar: function(){
		this.tbx = new Ext.Toolbar({
			items: ['']
		});
		
		this.tbx2 = new Ext.Toolbar({
			items: ['']
		});
		
		this.tbx3 = new Ext.Toolbar({
			items: ['']
		});

		var lenghtUserName = Sbi.user.userName.length+10;
	    var lenghtUserNameInPixel = lenghtUserName*10;
	    var menulenght = lenghtUserNameInPixel + 170;
	    if(drawSelectTheme){
	   		 menulenght = menulenght + 150;
	    }
	    var menuArrayIterator2 = this.menuArray.length;
	    var menuArrayIterator3 = this.menuArray.length;
       	
       	//if at the end menuArrayIterator2 doesn't change, all menus can fit in only one toolbar
       	for(var i = 0; i < this.menuArray.length; i++) {
				    var tempMenuLength = this.menuArray[i].text.length*9;
				    if(!tempMenuLength){
				    	tempMenuLength = 30;
				    }
					if(menulenght+tempMenuLength<browserWidth){
						menulenght = menulenght+tempMenuLength;
					}else{
					//will use a second toolbar
						menulenght = 0;
						this.useToolbar2 = true;
						menuArrayIterator2 = i;
						break;
					}
		}
		
		//if at the end menuArrayIterator3 doesn't change, all menus can fit in first and second toolbar
		if(this.useToolbar2){
			for(var i = menuArrayIterator2; i < this.menuArray.length; i++) {
						    var tempMenuLength = this.menuArray[i].text.length*9;
						    if(!tempMenuLength){
						    	tempMenuLength = 30;
						    }
							if(menulenght+tempMenuLength<browserWidth){
								menulenght = menulenght+tempMenuLength;
							}else{
							//will use a third toolbar
								menulenght = 0;
								this.useToolbar3 = true;
								menuArrayIterator3 = i;
								break;
							}
			}
		}
	
		this.tbx.on('render', function() {
			//adding all menus of the first toolbar
			if(this.menuArray){		
				for(var i = 0; i < menuArrayIterator2; i++) {
						this.tbx.add(this.menuArray[i]);
						this.tbx.addSeparator();
				}
			}				
		    this.tbx.addFill();   
		    
		    //all the menus concerning languages, themes, exit,welcome are always added in the first toolbar
		 	this.tbx.add(this.tbWelcomeText);
			this.tbx.addSeparator();
			if(drawSelectTheme){
				this.tbx.add(this.tbThemesButton);
				this.tbx.addSeparator();
			}
			this.tbx.add(this.tbLanguagesButton);
	 	    this.tbx.addButton(this.tbInfoButton);
			if(this.tbRoleButton && this.tbRoleButton != null) {
	 	    	this.tbx.addButton(this.tbRoleButton);
	 	    }
			this.tbx.add(this.tbExitButton);

		}, this);

		if(this.useToolbar2){
		
			this.tbx2.on('render', function() {
				if(this.menuArray){		
					//adding all menus of the second toolbar		
					for(var i = menuArrayIterator2; i < menuArrayIterator3; i++) {
							this.tbx2.add(this.menuArray[i]);
							this.tbx2.addSeparator();
					}
				}	
				this.tbx2.addFill();    			
			}, this);
		}
		
		if(this.useToolbar3){      		 
			this.tbx3.on('render', function() {	
				if(this.menuArray){		
					//adding all menus of the third toolbar			
					for(var i = menuArrayIterator3; i < this.menuArray.length; i++) {
							this.tbx3.add(this.menuArray[i]);
							this.tbx3.addSeparator();
					}
				}	
				this.tbx3.addFill();    				
			}, this);
		}		
		
	  },	
	 
	  info: function(){
		var win_info_1;
		if(!win_info_1){
			win_info_1= new Ext.Window({
			frame: false,
			style:"background-color: white",
			id:'win_info_1',
			autoLoad: {url: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/infos.jsp'},             				
			layout:'fit',
			width:210,
			height:180,
			//closeAction:'hide',
			closeAction:'close',
			buttonAlign : 'left',
			plain: true,
			title: LN('sbi.home.Info')
			});
		};
		win_info_1.show();
	  },
	
		role: function(){
				if(Sbi.user.roles && Sbi.user.roles.length > 1){
				this.win_roles = new Sbi.home.DefaultRoleWindow({'SBI_EXECUTION_ID': ''});
				this.win_roles.show();
				}

	  },
	
	  logout: function() {
		  // reset parameters stored in session
		  Sbi.execution.SessionParametersManager.resetSessionObjects();
		  //logouturl taken by the homepage
		  window.location =logoutUrl;
	  }
	  
});