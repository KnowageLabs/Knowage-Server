<template>
	<Toast></Toast>
	<ConfirmDialog></ConfirmDialog>
	<div class="layout-wrapper-content">
		<MainMenu></MainMenu>

		<div class="layout-main">
			<router-view />
		</div>
	</div>
</template>

<script>
	import ConfirmDialog from 'primevue/confirmdialog'
	import MainMenu from '@/modules/mainMenu/MainMenu'
	import Toast from 'primevue/toast'
	import { defineComponent } from 'vue'
	import store from '@/App.store'
	import { mapState } from 'vuex'
	import axios from 'axios'
	import WS from '@/services/webSocket.js'

	export default defineComponent({
		components: {
			ConfirmDialog,
			MainMenu,
			Toast
		},
		data() {
			return {
				connection: null
			}
		},
		beforeMount() {
			axios
				.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/currentuser')
				.then((response) => {
					store.commit('setUser', response.data)

					let storedLocale = response.data.locale
					if (localStorage.getItem('locale')) {
						storedLocale = localStorage.getItem('locale')
					}
					localStorage.setItem('locale', storedLocale)
					localStorage.setItem('token', response.data.userUniqueIdentifier)
					store.commit('setLocale', storedLocale)
					this.$i18n.locale = storedLocale
				})
				.catch(function(error) {
					if (error.response) {
						console.log(error.response.data)
						console.log(error.response.status)
						console.log(error.response.headers)
					}
				})
		},
		created() {
			this.newsDownloadHandler()
		},
		mounted() {
			this.newsDownloadHandler()
		},
		methods: {
			newsDownloadHandler() {
				console.log('Starting connection to WebSocket Server')

				this.connection = WS

				this.connection.update = function(event) {
					if (event.data) {
						let json = JSON.parse(event.data)
						if (json.news) {
							store.commit('setNews', json.news)
						}
						if (json.downloads) {
							store.commit('setDownloads', json.downloads)
						}
					}
				}
				this.connection.onopen = function(event) {
					this.update(event)
				}
				this.connection.onmessage = function(event) {
					this.update(event)
				}
			}
		},
		computed: {
			...mapState({
				error: 'error',
				info: 'info',
				user: 'user'
			})
		},
		watch: {
			error(newError) {
				this.$toast.add({
					severity: 'error',
					summary: newError.title,
					detail: newError.msg,
					life: 5000
				})
			},
			info(newInfo) {
				this.$toast.add({
					severity: 'info',
					summary: newInfo.title,
					detail: newInfo.msg,
					life: 5000
				})
			},
			user() {
				/* if (!oldUser.userId && oldUser != newUser)  */
			}
		}
	})
</script>

<style lang="scss">
	body {
		padding: 0;
		margin: 0;
		font-family: 'Roboto';
	}
	.layout-wrapper-content {
		display: flex;
		flex-direction: row;
		justify-content: space-between;
		min-height: 100vh;
	}
	.layout-main {
		margin-left: 58px;
		flex: 1;
	}
</style>
