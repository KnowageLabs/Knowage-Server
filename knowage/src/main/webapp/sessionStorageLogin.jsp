<html>
<head>
</head>
<body>
<form name="myForm" action="/knowage/servlet/AdapterHTTP" method="post">
  <input type="hidden" name="PAGE" value="LoginPage">
  <input type="hidden" name="NEW_SESSION" value="TRUE">
  <input type="hidden" name="<%=System.getProperty("JWT_LABEL", System.getenv("JWT_LABEL")) %>" value="">
</form>
</body>
<script>
document.forms["myForm"]['<%=System.getProperty("JWT_LABEL", System.getenv("JWT_LABEL")) %>'].value = sessionStorage.getItem('<%=System.getProperty("JWT_SESSION_STORAGE", System.getenv("JWT_SESSION_STORAGE")) %>');
document.forms["myForm"].submit();
</script>
</html>