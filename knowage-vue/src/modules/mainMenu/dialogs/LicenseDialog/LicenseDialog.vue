<template>
	<Dialog class="kn-dialog--toolbar--primary" :style="licenseDialogDescriptor.card.style" v-bind:visible="visibility" footer="footer" :closable="false" modal>
		<template #header> {{ $t('licenseDialog.title') }} </template>
		<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
		<div v-if="!hosts || hosts.length === 0">
			<p>{{ $t('licenseDialog.noLicenses') }}</p>
		</div>
		<TabView class="kn-tab">
			<TabPanel v-for="host in hosts" :key="host.hardwareId">
				<template #header>
					<span>{{ host.hostName }}</span>
				</template>
				<LicenceTab :licenses="licenses[host.hostName]" :host="host" :cpunumber="cpuNumber" @reloadList="loadLicenses"></LicenceTab>
			</TabPanel>
		</TabView>
		<template #footer>
			<Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.ok') }}</Button>
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import { iHost, iLicense } from './License'
	import axios from 'axios'
	import Dialog from 'primevue/dialog'
	import licenseDialogDescriptor from './LicenseDialogDescriptor.json'
	import LicenceTab from './LicenseTab.vue'
	import TabView from 'primevue/tabview'
	import TabPanel from 'primevue/tabpanel'

	export default defineComponent({
		name: 'license-dialog',
		components: { Dialog, LicenceTab, TabView, TabPanel },
		data() {
			return {
				cpuNumber: Number,
				licenseDialogDescriptor,
				hosts: [] as iHost[],
				licenses: {} as { [key: string]: iLicense[] },
				loading: false
			}
		},
		async created() {
			await this.loadLicenses()
		},
		props: {
			visibility: Boolean
		},
		emits: ['update:visibility'],
		methods: {
			closeDialog() {
				this.$emit('update:visibility', false)
			},
			async loadLicenses() {
				this.loading = true
				await axios
					.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/license')
					.then((response) => {
						this.hosts = response.data.hosts
						this.licenses = response.data.licenses
						this.cpuNumber = response.data.cpuNumber
					})
					.finally(() => (this.loading = false))
			}
		}
	})
</script>
