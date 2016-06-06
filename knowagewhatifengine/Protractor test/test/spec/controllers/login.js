'use strict';

describe('Controller: LoginCtrl', function () {

  // load the controller's module
  beforeEach(module('myNgAppApp'));

  var LoginCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    LoginCtrl = $controller('LoginCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(LoginCtrl.awesomeThings.length).toBe(3);
  });
});
