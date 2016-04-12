<%@ page session="true" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
    <meta name="generator" content="HTML Tidy for Windows (vers 1 June 2005), see www.w3.org" />
    <title>KnowAge Login</title>
    <style>
      body {
	       padding: 0;
	       margin: 0;
      }
    </style>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

    <script language="javascript" type="text/javascript">
    //<![CDATA[
    function login(){
      var lgn = document.getElementById('lgn');
      var pwd = document.getElementById('pwd');
      document.getElementById('msgMandatory').style.display='none';
      document.getElementById('message').style.display='none';
      if(lgn.value=="" || pwd.value=="") {
         displayMsgMandatory();
      }else {
        document.getElementById('login-form').submit();
    //    setTimeout('displaybis()',1000);
      }
    } 
    function displayMsgMandatory(){
      document.getElementById('msgMandatory').style.display='inline';
    }
    
    function displaybis(){
      document.getElementById('message').style.display='inline';
    }
    
    function cleanMessage(){
      document.getElementById('message').style.display='none';
    }
    //]]>
    </script>
    
	<link rel="shortcut icon" href="/knowage/themes/sbi_default/img/favicon.ico" />
	<!-- Bootstrap -->
	<!-- Latest compiled and minified CSS -->
	<!-- <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"> -->
	<link rel="stylesheet" href="/knowage/js/lib/bootstrap/css/bootstrap.min.css">

	<!-- Optional theme -->
	<!--link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css"-->
	<LINK rel='StyleSheet' href='/knowage/themes/sbi_default/css/knowageHome/style.css' type='text/css' />

</head>

	<!--<body id="cas" onload="init()">-->
	<body id="cas" >
		<div class="container">
			<div class="card card-container">
				<img id="profile-img" class="logoHeader" src='/knowage/themes/sbi_default/img/wapp/logoKnowage.png' />
					<p id="profile-name" class="profile-name-card"/>