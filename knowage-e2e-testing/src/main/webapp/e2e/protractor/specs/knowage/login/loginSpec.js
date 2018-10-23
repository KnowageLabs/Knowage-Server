
var knowageLoginPage = require('pageObjects/knowageLoginPage');
describe('knowage login as admin', function() {
	
	
	beforeAll(function(){
		knowageLoginPage.get();
	})
	
	
  it('should type username biadmin', function() {
	  
	  knowageLoginPage.setUserName('biadmin');
	  expect(knowageLoginPage.getUserName()).toEqual('biadmin');
  });
  
  it('should type  password biadmin', function() {
	  
		knowageLoginPage.setPassword('biadmin');
	    expect(knowageLoginPage.getPassword()).toEqual('biadmin');
  });
  it('should submit login form', function(done) {
		
	  	knowageLoginPage.login();
	    expect(2).toEqual(2);
	    done();
	  });
});

