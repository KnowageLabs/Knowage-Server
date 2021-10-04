<template>
	<Dialog class="kn-dialog--toolbar--primary RoleDialog" v-bind:visible="visibility" footer="footer" :header="$t('downloadsDialog.title')" :closable="false" modal>
		<DataTable :value="downloadsList" style="width:800px" :resizableColumns="true" columnResizeMode="fit | expand">
			<Column v-for="(column, index) in columnDefs" v-bind:key="index" :field="column.field" :header="$t(column.headerName)" :bodyStyle="column.bodyStyle">
				<template v-if="column.template" #body="slotProps">
					<Button icon="pi pi-download" class="p-button-text p-button-rounded p-button-plain" @click="downloadContent(slotProps.data)" />
				</template>
				<template v-else #body="slotProps">
					<template v-if="column.type && column.type == 'date'">{{ getDate(slotProps.data[column.field]) }}</template
					><template v-else>{{ slotProps.data[column.field] }} </template>
				</template>
			</Column>
			<template #empty>
				{{ $t('common.info.noDataFound') }}
			</template>
		</DataTable>
		<template #footer>
			<Button class="kn-button p-button-danger" :disabled="downloadsList.length == 0" @click="deleteAllDownloads">{{ $t('common.deleteAll') }}</Button>
			<Button class="kn-button--primary" @click="closeDialog">{{ $t('common.close') }}</Button>
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import { formatDate } from '@/helpers/commons/localeHelper'
	import axios from 'axios'
	import Column from 'primevue/column'
	import DataTable from 'primevue/datatable'
	import Dialog from 'primevue/dialog'
	import descriptor from './DownloadsDialogDescriptor.json'
	import { downloadDirectFromResponse } from '@/helpers/commons/fileHelper'
	import WEB_SOCKET from '@/services/webSocket.js'

	interface Download {
		filename: string
		startDate: Date
		alreadyDownloaded: boolean
	}
	export default defineComponent({
		name: 'role-dialog',
		components: {
			Column,
			DataTable,
			Dialog
		},
		props: {
			visibility: Boolean
		},
		data() {
			return {
				columnDefs: {},
				downloadsList: new Array<Download>(),
				gridOptions: {}
			}
		},
		beforeMount() {
			this.gridOptions = { headerHeight: 30 }
			this.columnDefs = descriptor.columnDefs
		},
		created() {
			this.getDownloads()
		},
		emits: ['update:visibility'],
		methods: {
			closeDialog() {
				this.$emit('update:visibility', false)
			},
			getDate(date) {
				return formatDate(date, 'LLL')
			},
			getDownloads() {
				axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/export/dataset?showAll=true').then(
					(response) => {
						console.log(response)
						this.downloadsList = response.data
					},
					(error) => console.error(error)
				)
			},
			downloadContent(data) {
				if (!data.alreadyDownloaded) {
					this.$store.commit('updateAlreadyDownloadedFiles')
					let message = {
						download: true
					}

					WEB_SOCKET.send(JSON.stringify(message))
				}
				var encodedUri = encodeURI(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/export/dataset/' + data.id)
				axios
					.get(encodedUri, {
						responseType: 'arraybuffer', // important...because we need to convert it to a blob. If we don't specify this, response.data will be the raw data. It cannot be converted to blob directly.

						headers: {
							'Content-Type': 'application/json',
							Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9'
						}
					})
					.then(
						(response) => {
							downloadDirectFromResponse(response)
						},
						(error) => console.error(error)
					)
				this.getDownloads()
			},
			deleteAllDownloads() {
				axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/export').then(
					() => {
						this.downloadsList = []
					},
					(error) => console.error(error)
				)
			}
		},
		watch: {
			visibility(newVisibility, oldVisibility) {
				if (newVisibility != oldVisibility) this.getDownloads()
			}
		}
	})
</script>
