<template>
  <div class="layout-wrapper-content">

		<Knmenu :model="menu"></Knmenu>

		<div class="layout-main">
			<router-view />
		</div>

  </div>
</template>

<script>
  import Knmenu from './components/Knmenu'

  export default {
    components: {
    Knmenu
  },
  data() {
    return {
        menu : [
				{
					label: 'Favorites', icon: 'pi pi-fw pi-home',to: {name: 'home'} 
				},
				{
					label: 'UI Kit', icon: 'pi pi-fw pi-star', to: {name:'about'}
				},
				{
					label: "Utilities", icon:'pi pi-fw pi-compass', url: '/knowage/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER&OBJECT_ID=105&OBJECT_LABEL=KNOWAGE-5015&IS_SOURCE_DOCUMENT=true&LIGHT_NAVIGATOR_DISABLED=TRUE&SBI_EXECUTION_ID=null&OBJECT_NAME=As%20a%20final%20user%20I%20would%20like%20to%20be%20able%20to%20do%20the%20search%20on%20fontawesome%20icons%20on%20the%20map%20and%20table%20widget'
				},
				{
					label: "Utilities", icon:'pi pi-fw pi-compass', url: '/knowage/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/catalogue/menuConfiguration.jsp?LIGHT_NAVIGATOR_RESET_INSERT=TRUE'
				},
				{
					label: 'Logout', icon: 'pi pi-fw pi-sign-out', url: 'http://localhost:8080/knowage/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE'
				}

				
		],
    }
}
  }
</script>

<style lang="scss">
body {
  padding: 0;
    margin: 0;
}
.layout-wrapper-content {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  min-height: 100vh;
}
.layout-main {
	margin-left: 58px;
	flex:1;
}
</style>
