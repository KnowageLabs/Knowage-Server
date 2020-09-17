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
<html>
<head>
<style type="text/css">
.kn-info, .kn-warning, .kn-infoerror {
  margin: 8px !important;
  padding: 8px;
  text-align: center;
  position: relative;
  text-transform: uppercase;
  font-size: 0.6rem;
  font-family: "roboto";
}
.kn-info p, .kn-warning p, .kn-infoerror p {
  margin: 0;
}
.kn-info ul, .kn-warning ul, .kn-infoerror ul {
  padding: 0;
}
.kn-info.no-uppercase, .kn-warning.no-uppercase, .kn-infoerror.no-uppercase {
  text-transform: none;
}
.kn-info {
  border: 1px solid rgba(59, 103, 140, 0.1);
  background-color: #eaf0f6;
}
.kn-warning {
  border: 1px solid rgba(251, 192, 45, 0.5);
  background-color: #fef5dc;
}
.kn-infoerror {
  border: 1px solid rgba(244, 67, 54, 0.5);
  background-color: #fde1df;
}
</style>
</head>
<body>
<div style='width:100%;display:flex;justify-content:center;align-items:center'>
	<div class="kn-info flex-50" style="font-size:.8rem">
  
<%String name=(String)request.getAttribute("msgOK");
out.print(name);
request.removeAttribute("msgOK");	 %>

	</div>
</div>
</body>

</html>