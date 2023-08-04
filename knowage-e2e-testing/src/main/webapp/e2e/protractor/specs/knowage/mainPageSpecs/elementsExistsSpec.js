
var knowageLoginPage = require('pageObjects/knowageLoginPage');
var adminSideMenu = require('pageObjects/sideMenu/adminSideMenu');
var knowageQbePage;
describe('testing qbe from model', function() {
	

	beforeAll(function(){
		knowageLoginPage
			.get()
			.setUserName('biadmin')
		  	.setPassword('biadmin')
		  	.login();
		
		knowageQbePage =  	adminSideMenu
			.openMenu()
			.getIconBar()
			.getWorkSpace()
			.toggleDataList()
			.getDataListItem('Models')
			.openModeInQbe(0);
		
		
	})
	
	 it('should have save button', function(done) {

	 		expect(knowageQbePage.getMainToolbar().getSaveButton().isPresent()).toEqual(true);
		  	done();

	 });
	
	it('should have entity manager', function(done) {
		
 		expect(knowageQbePage.getEditor().getEntityManager().getRoot().isPresent()).toEqual(true);
	  	done();

 });
	
	
});