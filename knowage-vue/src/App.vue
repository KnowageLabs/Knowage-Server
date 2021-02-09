<template>
	<Toast></Toast>
  <div class="layout-wrapper-content">
		<Knmenu :model="menu"></Knmenu>
		
		<div class="layout-main">
			<router-view />
		</div>
  </div>
</template>

<script>
	import Knmenu from '@/components/knmenu/KnMenu'
	import Toast from 'primevue/toast';
	import { defineComponent } from 'vue'
	import {  mapState } from 'vuex'

	export default defineComponent({
		components: {
			Knmenu,
			Toast
		},
		methods: {
			getUser(){
				return this.user.name
			}
		},
		data() {
			return {
				test:'ciao'
			}
		},
		computed: {
			...mapState({
				error: 'error',
				user: 'user'
			})
		},
		watch: {
			error: function(oldError, newError) {
				if(newError.visible) {
					this.$toast.add({severity:'success', summary: 'Success Message', detail:'Order submitted', life: 3000})
				}
			}
		}
	})

</script>

<style lang="scss">
body {
  padding: 0;
    margin: 0;
	font-family: "Roboto";
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
