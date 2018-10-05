var adminIconBar = require('./iconBar/adminIconBar');
var helper = require('pageObjects/helper');
var AdminSideMenu = function(){
	
	var menuToogleButton = element(by.css('a[data-ng-click="toggleMenu()"]'));
	var iconBar = adminIconBar;
	
	this.openMenu = function(){
		browser.switchTo().defaultContent();
		helper.waitForElementToBeClickable(menuToogleButton);
		menuToogleButton.click();
		browser.ignoreSynchronization = false;
		return this;
	}
	
	this.getIconBar = function(){
		return iconBar;
	}
	
	
}

module.exports = new AdminSideMenu();