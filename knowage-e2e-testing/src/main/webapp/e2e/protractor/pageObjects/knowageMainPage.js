

var KnowageMainPage = function(){
	
	this.menuToogleButton = element(by.css('a[data-ng-click="toggleMenu()"]'));
	this.knowageAdminIcon = element(by.css('img[src="../img/adminLogo.png"]'));
	
	this.openMenu = function(){
		this.menuToogleButton.click();
	}
	
	
}

module.exports = new KnowageMainPage();