<template>

   <transition class="layout-menu-container">
         <div class="menu-scroll-content">
            <AppSubmenu class="layout-menu layout-main-menu clearfix" :items="model" />
         </div>
   </transition>

</template>

<script>
import AppSubmenu from './AppSubmenu';
import axios from 'axios'

export default {
   name: 'Knmenu',
	props: {
		model: Array
	},
	components: {
		'AppSubmenu': AppSubmenu
   },
   data() {
      return {
         test: Array
      }
   },
   created() {
		axios.get('/knowage/restful-services/1.0/menu/enduser?curr_country=US&curr_language=en')
			.then((response) => {
            this.test = response
				console.log(response)
			},(error) => console.error(error))
		
  },
}
</script>

<style lang="scss" scoped>
   .layout-menu-container {
      width: 58px;
      z-index: 100;
      top: 0;
      background-color: #363a41;
      height: 100%;
      position: fixed;
   }
</style>