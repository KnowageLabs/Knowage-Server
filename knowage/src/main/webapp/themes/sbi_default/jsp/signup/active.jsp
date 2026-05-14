<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page language="java" pageEncoding="UTF-8" session="true"%>

<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>

<%
    IUrlBuilder urlBuilder = UrlBuilderFactory.getUrlBuilder("WEB");
    IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
    String baseUrl = request.getRequestURL().toString().substring(0, request.getRequestURL().toString().indexOf("/knowage")) + "/knowage/restful-services/signup/active";
    String getURL = request.getRequestURL().toString().substring(0, request.getRequestURL().toString().indexOf("/knowage")) + "/knowage/";
    String currTheme = ThemesManager.getDefaultTheme();
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-title" content="Knowage">
    <title>Knowage - Activation</title>
    <link rel="shortcut icon" href="<%=urlBuilder.getResourceLinkByTheme(request, "img/favicon.ico", currTheme)%>" />
    <link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "/node_modules/bootstrap/dist/css/bootstrap.min.css")%>">
    <link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "/node_modules/toastify-js/src/toastify.css")%>">
    <link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
</head>
<body class="kn-login" ng-app="userActivation" ng-controller="userActivationController" ng-cloak>
    <div class="container-fluid" style="height:100%;">
        <div class="col-12 col-lg-5 offset-lg-7" style="height:100%;background-color:white;display:flex;flex-direction:column;padding:20px;justify-content:center;align-items:center">
            <h3><%=msgBuilder.getMessage("signup.active.subject", "messages")%></h3>
            <div class="col-8" style="text-align:center;">
                <img id="profile-img" src='<%=urlBuilder.getResourceLinkByTheme(request, "../commons/img/defaultTheme/logoCover.svg", currTheme)%>' />

                <div class="loading" ng-if="loading" style="margin-top:20px;">
                    <p><%=msgBuilder.getMessage("loading", "messages")%></p>
                </div>

                <div class="activation" ng-if="activation" style="margin-top:20px;">
                    <div class="alert alert-success" role="alert">{{activation}}</div>
                    <button class="btn btn-lg btn-primary btn-block btn-signin" onclick="location.href='<%=getURL%>'"><%=msgBuilder.getMessage("login", "messages")%></button>
                </div>

                <div class="error" ng-if="error" style="margin-top:20px;">
                    <div class="alert alert-danger" role="alert">{{error}}</div>
                    <button class="btn btn-lg btn-primary btn-block btn-signin" onclick="location.href='<%=getURL%>'"><%=msgBuilder.getMessage("login", "messages")%></button>
                </div>
            </div>
        </div>
    </div>

    <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/angular_1.4/angular.js")%>"></script>
    <script type="text/javascript" nonce="<%= request.getAttribute("cspNonce") %>">
        var baseUrl = "<%= baseUrl %>";

        angular.module('userActivation', [])
            .config(['$locationProvider', function($locationProvider) {
                $locationProvider.html5Mode({ enabled: true, requireBase: false });
            }])
            .controller('userActivationController', function($scope, $location, $http) {
                $scope.loading = true;
                $scope.version = ($location.search().version || '').match(/^[0-9]{1,2}\.[0-9]{1,2}$/) ? $location.search().version : "master";

                var url = new URL(baseUrl);
                url.searchParams.append('token', $location.search().token);
                url.searchParams.append('locale', $location.search().locale);
                url.searchParams.append('version', $scope.version);

                var uniqueToken = localStorage.getItem('X-CSRF-TOKEN') || (Math.random() + 1).toString(36);
                document.cookie = "X-CSRF-TOKEN=" + uniqueToken + "; path=/";

                $http.get(url.toString(), {
                    headers: { 'x-csrf-token': uniqueToken }
                }).then(function(response) {
                    $scope.loading = false;
                    if (response.data.errors) {
                        $scope.error = response.data.errors;
                    } else {
                        $scope.activation = response.data.message;
                    }
                }, function() {
                    $scope.loading = false;
                    $scope.error = 'Connection error';
                });
            });
    </script>
</body>
</html>
