
var knowageLoginPage = require('pageObjects/knowageLoginPage');
var knowageMainPage = require('pageObjects/knowageMainPage');
describe('testing user logins', function() {
	

	beforeEach(function(){
		knowageLoginPage.get();
	})
	
 
  
  it('should login as biuser', function(done) {
	  
		 knowageLoginPage
		  	.setUserName('biuser')
		  	.setPassword('biuser')
		  	.login();
		  	 expect(knowageMainPage.menuToogleButton.isPresent()).toEqual(true);
		 
		  	done();
	  
	 
	  
  });
	
	 it('should login as biadmin', function(done) {
		  

			 knowageLoginPage
			  	.setUserName('biadmin')
			  	.setPassword('biadmin')
			  	.login();
			 expect(knowageMainPage.menuToogleButton.isPresent()).toEqual(true);
			 done();

			 
		 
	  });
  
  
  
});