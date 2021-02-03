<template>
  <div class="layout-wrapper-content">

		<Knmenu :model="menu"></Knmenu>
		

		<div class="layout-main">
			<router-view />
		</div>
		<Button @click="setUser()">asd{{getUser()}}</Button>
  </div>
</template>

<script>
	import Knmenu from './components/Knmenu'
	import { defineComponent } from 'vue'
	import {  mapGetters } from 'vuex'

	export default defineComponent({
		components: {
			Knmenu
		},
		methods: {
			setUser(){
				this.$store.commit("setUser",{name:'davide'});
			},
			getUser(){
				return this.user.name
			}
		},
		computed: {
			...mapGetters({
				user: 'getUser'
				})
		}

	})
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
	margin-left: 175px;
	flex:1;
}
</style>
