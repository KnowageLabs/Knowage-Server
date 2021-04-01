<template>
	<Dialog class="kn-dialog--toolbar--primary RoleDialog" v-bind:visible="visibility" footer="footer" :header="$t('downloadsDialog.title')" :closable="false" modal>
		<DataTable :value="downloadsList" style="width:800px" :resizableColumns="true" columnResizeMode="fit | expand">
			<Column v-for="(column, index) in columnDefs" v-bind:key="index" :field="column.field" :header="$t(column.headerName)" :bodyStyle="column.bodyStyle">
				<template v-if="column.template" #body="slotProps">
					<Button icon="pi pi-download" class="p-button-text p-button-rounded p-button-plain" @click="downloadContent(slotProps.data.id)" />
				</template>
				<template v-else #body="slotProps">
					<template v-if="column.type && column.type == 'date'">{{ getDate(slotProps.data[column.field]) }}</template
					><template v-else>{{ slotProps.data[column.field] }} </template>
				</template>
			</Column>
		</DataTable>
		<template #footer>
			<Button class="kn-button kn-button--primary" @click="closeDialog">{{ $t('common.close') }}</Button>
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
	import { download } from '@/helpers/commons/fileHelper'

	interface Download {
		filename: string
		startDate: Date
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
				axios.get('/knowage/restful-services/2.0/export/dataset?showAll=true').then(
					(response) => {
						console.log(response)
						this.downloadsList = response.data

						/* 						var alreadyDownloadedCount = 0
						for (var idx in this.downloadsList) {
							if (this.downloadsList[idx].alreadyDownloaded) alreadyDownloadedCount++
						}

						this.$store.commit('setDownloads', alreadyDownloadedCount) */
					},
					(error) => console.error(error)
				)
			},
			downloadContent(id) {
				var encodedUri = encodeURI('knowage/restful-services/2.0/export/dataset/' + id)
				download(encodedUri, null, null)
			}
		}
	})
</script>
