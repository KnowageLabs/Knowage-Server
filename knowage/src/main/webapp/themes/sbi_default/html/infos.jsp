<md-dialog id="kn-info" layout="column">
	<form>
		<md-dialog-content layout="column" layout-padding layout-margin>
			<div layout="row" style="width:100%" layout-align="center center">
				<img src="/knowage/themes/commons/img/defaultTheme/logo.svg" />
			</div>
			 	 
		 	 <div layout-padding>
				<p>
					<span>{{translate.load('kn.info.version')}}</span> {{config.knowageVersion}}</p>
				<p>
					<span>{{translate.load('kn.info.user')}}</span> {{user.userName}}</p>
				<p>
					<span>{{translate.load('kn.info.tenant')}}</span> {{user.tenant}}</p>
				<p>
					{{translate.load('kn.info.source')}} 
					<a href="http://www.knowage-suite.com" target="_blank">www.knowage-suite.com</a>
				</p>
				<p layout-align ="center center" >
					<i>&copy; {{translate.load('kn.generic.copyright')}}</i>
				</p>
			</div>
		</md-dialog-content>
		<div class="md-actions">
			<md-button class="md-primary md-raised" ng-click="closeDialog()" >
				{{okMessage}}
	        </md-button>
	     </div>
     </form>
</md-dialog>