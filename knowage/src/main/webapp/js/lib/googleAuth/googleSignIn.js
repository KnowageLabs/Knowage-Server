function onSignIn(googleUser) {
  var profile = googleUser.getBasicProfile();
  var id_token = googleUser.getAuthResponse().id_token;
  $.post("/knowage/servlet/AdapterHTTP", {
    ACTION_NAME: "LOGIN_ACTION_BY_TOKEN",
    NEW_SESSION: true,
    token: id_token,
  })
    .done(function (data) {
      // reload current page, in order to keep input GET parameters (such as required document and so on)
      location.reload();
    })
    .fail(function (error) {
      $("#kn-infoerror-message").show();
      $(".kn-infoerror").html("Authentication failed. Please check if you are to allowed to enter this application.");
    });
}
