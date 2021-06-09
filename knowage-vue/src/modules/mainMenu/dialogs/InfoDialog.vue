<template>
	<Dialog class="kn-dialog--toolbar--primary" v-bind:visible="visibility" footer="footer" :header="$t('InfoDialog.aboutKnowage')" :closable="false" modal>
		<div class="p-grid p-m-1">
			<div class="p-col">
				<div class="p-d-flex p-jc-center">
					<img :src="require('@/assets/images/commons/logo_knowage.svg')" height="100" />
				</div>
				<p>
					<strong>{{ $t('common.version') }}:</strong> {{ currentVersion }}
				</p>
				<p>
					<strong>{{ $t('common.loggedUser') }}:</strong> {{ user.fullName }}
				</p>
				<p>
					<strong>{{ $t('common.tenant') }}:</strong> {{ user.organization }}
				</p>
				<p>{{ $t('infoDialog.sourceCode') }} <a href="www.knowage-suite.com">Knowage Suite</a></p>
				<p>{{ $t('infoDialog.copyright', { year: currentYear }) }}</p>
			</div>
		</div>
		<template #footer>
			<Button class="kn-button kn-button--primary" v-t="'common.close'" @click="closeDialog" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import Dialog from 'primevue/dialog'
	import { mapState } from 'vuex'
	import moment from 'moment'
	import axios from 'axios'

	export default defineComponent({
		name: 'InfoDialog',
		components: {
			Dialog
		},
		props: {
			visibility: Boolean
		},
		emits: ['update:visibility'],
		data() {
			return {
				currentYear: moment().year(),
				currentVersion: process.env.VUE_APP_VERSION
			}
		},
		mounted() {
			this.licenseInformation()
		},
		methods: {
			closeDialog() {
				this.$emit('update:visibility', false)
			},
			licenseInformation() {
				axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/license').then(function(response) {
					for (var idx in response.data.hosts) {
						console.log('Hardware ID: ' + response.data.hosts[idx]['hardwareId'])
						console.log('Host Name: ' + response.data.hosts[idx]['hostName'])
					}
				})
			}
		},
		computed: {
			...mapState({
				user: 'user'
			})
		}
	})
</script>

<style scoped lang="scss">
	p {
		text-transform: capitalize;
	}
</style>
