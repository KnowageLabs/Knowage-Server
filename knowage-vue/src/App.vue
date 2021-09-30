<template>
	<Toast></Toast>
	<ConfirmDialog></ConfirmDialog>
	<KnOverlaySpinnerPanel :visibility="loading" />
	<div class="layout-wrapper-content">
		<MainMenu></MainMenu>

		<div class="layout-main">
			<router-view />
		</div>
	</div>
</template>

<script>
	import ConfirmDialog from 'primevue/confirmdialog'
	import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
	import MainMenu from '@/modules/mainMenu/MainMenu'
	import Toast from 'primevue/toast'
	import { defineComponent } from 'vue'
	import store from '@/App.store'
	import { mapState } from 'vuex'
	import axios from 'axios'
	import WEB_SOCKET from '@/services/webSocket.js'

	export default defineComponent({
		components: { ConfirmDialog, KnOverlaySpinnerPanel, MainMenu, Toast },

		beforeCreate() {
			axios
				.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/currentuser')
				.then((response) => {
					let currentUser = response.data
					if (localStorage.getItem('sessionRole')) {
						currentUser.sessionRole = localStorage.getItem('sessionRole')
					} else if (currentUser.defaultRole) currentUser.sessionRole = currentUser.defaultRole

					store.dispatch('initializeUser', currentUser)

					let responseLocale = response.data.locale
					let storedLocale = responseLocale
					if (localStorage.getItem('locale')) {
						storedLocale = localStorage.getItem('locale')
					}
					localStorage.setItem('locale', storedLocale)
					localStorage.setItem('token', response.data.userUniqueIdentifier)
					store.commit('setLocale', storedLocale)
					this.$i18n.locale = storedLocale

					let language = this.$i18n
					let splittedLanguage = language.locale.split('_')

					let url = '/knowage/servlet/AdapterHTTP?'
					url += 'ACTION_NAME=CHANGE_LANGUAGE'
					url += '&LANGUAGE_ID=' + splittedLanguage[0]
					url += '&COUNTRY_ID=' + splittedLanguage[1].toUpperCase()
					url += '&SCRIPT_ID=' + (splittedLanguage.length > 2 ? splittedLanguage[2].replaceAll('#', '') : '')
					url += '&THEME_NAME=sbi_default'

					this.$emit('update:loading', true)
					axios.get(url).then(
						() => {
							store.commit('setLocale', language.locale)
							localStorage.setItem('locale', language.locale)
							this.$i18n.locale = language.locale
						},
						(error) => console.error(error)
					)
					this.$emit('update:loading', false)
				})
				.catch(function(error) {
					if (error.response) {
						console.log(error.response.data)
						console.log(error.response.status)
						console.log(error.response.headers)
					}
				})
				.finally(() => {
					if (this.isEnterprise) {
						axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/license').then((response) => {
							store.commit('setLicenses', response.data)
						})
					}
				})
		},
		mounted() {
			this.onLoad()
		},
		methods: {
			closeDialog() {
				this.$emit('update:visibility', false)
			},
			async onLoad() {
				await axios
					.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/export/dataset')
					.then((response) => {
						let totalDownloads = response.data.length
						let alreadyDownloaded = response.data.filter((x) => x.alreadyDownloaded).length

						let json = { downloads: { count: { total: 0, alreadyDownloaded: 0 } } }
						json.downloads.count.total = totalDownloads
						json.downloads.count.alreadyDownloaded = alreadyDownloaded

						store.commit('setDownloads', json.downloads)

						this.newsDownloadHandler()
						this.loadInternationalization()
					})
					.catch(function(error) {
						if (error.response) {
							console.log(error.response.data)
							console.log(error.response.status)
							console.log(error.response.headers)
						}
					})
			},
			async loadInternationalization() {
				let currentLocale = localStorage.getItem('locale') ? localStorage.getItem('locale') : store.state.locale
				console.log(currentLocale)
				if (currentLocale) currentLocale = currentLocale.replaceAll('_', '-')
				else currentLocale = 'en-US'
				await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/internationalization?currLanguage=' + currentLocale).then((response) => store.commit('setInternationalization', response.data))
			},
			newsDownloadHandler() {
				console.log('Starting connection to WebSocket Server')

				WEB_SOCKET.update = function(event) {
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
				WEB_SOCKET.onopen = function(event) {
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
				WEB_SOCKET.onmessage = function(event) {
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
			}
		},
		computed: {
			...mapState({
				error: 'error',
				info: 'info',
				user: 'user',
				loading: 'loading',
				isEnterprise: 'isEnterprise'
			})
		},
		watch: {
			error(newError) {
    	        this.$toast.add({
        	        severity: 'error',
            	    summary: newError.title ? this.$t(newError.title) : '',
					detail: newError.msg ? this.$t(newError.msg) : '',
					baseZIndex: typeof newError.baseZIndex == 'undefined' ? 0 : newError.baseZIndex,
    	            life: typeof newError.duration == 'undefined' ? process.env.VUE_APP_TOAST_DURATION : newError.duration
            	})
        	},
			info(newInfo) {
				this.$toast.add({
                	severity: 'info',
                	summary: newInfo.title ? this.$t(newInfo.title) : '',
                	detail: newInfo.msg ? this.$t(newInfo.msg) : '',
                	baseZIndex: typeof newInfo.baseZIndex == 'undefined' ? 0 : newInfo.baseZIndex,
                	life: typeof newInfo.duration == 'undefined' ? process.env.VUE_APP_TOAST_DURATION : newInfo.duration
            	})
        	},
			loading(newLoading) {
				this.loading = newLoading
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
	.layout-wrapper-content::after {
		content: '';
		display: table; /* NOTE: Display "block" does not seem to work with height: 0px. */
		height: 0px;
	}
	.layout-main {
		margin-left: 58px;
		flex: 1;
	}
</style>
