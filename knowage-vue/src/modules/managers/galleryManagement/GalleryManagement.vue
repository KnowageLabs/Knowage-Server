<template>
	<div class="kn-page">
		<div class="kn-page-content p-grid p-m-0">
			<div class="p-col-4 p-sm-4 p-md-3 p-p-0 kn-page">
				<Toolbar class="kn-toolbar kn-toolbar--primary">
					<template #left>
						{{ $t('managers.widgetGallery.title') }}
					</template>
					<template #right>
						<FabButton icon="fas fa-plus" @click="toggleAdd" />
						<Menu ref="menu" :model="addMenuItems" popup="true" />
					</template>
				</Toolbar>
				<KnInputFile label="" :changeFunction="uploadTemplate" accept="application/json,application/zip" :triggerInput="triggerInput" />
				<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
				<KnListBox :options="galleryTemplates" :settings="typeDescriptor.knListSettings" @delete="deleteTemplate($event, item)"></KnListBox>
			</div>
			<div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
				<router-view @saved="savedElement" />
			</div>
		</div>
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import axios from 'axios'
	import FabButton from '@/components/UI/KnFabButton.vue'
	import KnInputFile from '@/components/UI/KnInputFile.vue'
	import { IGalleryTemplate } from './GalleryManagement'
	import Menu from 'primevue/menu'
	import galleryDescriptor from './GalleryManagementDescriptor.json'

	import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'

	export default defineComponent({
		name: 'gallery-management',
		components: {
			FabButton,
			KnInputFile,
			KnListBox,
			Menu
		},
		data() {
			return {
				galleryTemplates: [] as Array<IGalleryTemplate>,
				loading: false,
				typeDescriptor: galleryDescriptor,
				triggerInput: false,
				addMenuItems: [
					{ label: this.$t('managers.widgetGallery.newTemplate'), icon: 'fas fa-plus', command: () => this.newTemplate() },
					{
						label: this.$t('managers.widgetGallery.importTemplate'),
						icon: 'fas fa-file-import',
						command: () => {
							this.triggerInputFile(true)
						}
					}
				],
				importingTemplate: {} as string | ArrayBuffer
			}
		},
		created() {
			this.loadAllTemplates()
		},
		methods: {
			triggerInputFile(value) {
				this.triggerInput = value
			},
			loadAllTemplates(): void {
				this.loading = true
				this.axios
					.get(process.env.VUE_APP_API_PATH + '1.0/widgetgallery')
					.then((response) => {
						this.galleryTemplates = response.data.map((item) => {
							// TODO remove after backend implementation
							item.label = item.label || item.name
							return item
						})
					})
					.catch((error) => console.error(error))
					.finally(() => (this.loading = false))
			},
			deleteTemplate(e, templateId): void {
				e.preventDefault()
				if (e.item && e.item.id) templateId = e.item.id
				this.$confirm.require({
					message: this.$t('managers.widgetGallery.templateDoYouWantToDeleteTemplate'),
					header: this.$t('managers.widgetGallery.deleteTemplate'),
					icon: 'pi pi-exclamation-triangle',
					accept: () => {
						this.axios
							.delete(process.env.VUE_APP_API_PATH + '1.0/widgetgallery/' + templateId)
							.then(() => {
								this.$store.commit('setInfo', { title: this.$t('managers.widgetGallery.deleteTemplate'), msg: this.$t('managers.widgetGallery.templateSuccessfullyDeleted') })
								this.loadAllTemplates()
								if (templateId === this.$route.params.id) this.$router.push('/gallery-management')
							})
							.catch((error) => console.error(error))
					}
				})
			},
			newTemplate() {
				this.$router.push('/gallery-management/new-template')
			},
			savedElement() {
				this.loadAllTemplates()
			},
			toggleAdd(event) {
				// eslint-disable-next-line
				// @ts-ignore
				this.$refs.menu.toggle(event)
				this.triggerInputFile(false)
			},
			uploadTemplate(event): void {
				var reader = new FileReader()
				reader.onload = this.onReaderLoad
				reader.readAsText(event.target.files[0])
				this.triggerInputFile(false)
				event.target.value = ''
			},
			onReaderLoad(event) {
				let json = JSON.parse(event.target.result)

				if (!json.id || json.id === '') {
					this.$confirm.require({
						message: this.$t('importExport.import.itemWithoutIdConfirm'),
						header: this.$t('common.import'),
						icon: 'pi pi-exclamation-triangle',
						accept: () => {
							json.id = ''
							this.importWidget(json)
						}
					})
				}
			},
			importWidget(json: JSON) {
				axios.post(process.env.VUE_APP_API_PATH + '1.0/widgetgallery/import', json).then(() => {
					this.$store.commit('setInfo', { title: this.$t('managers.widgetGallery.uploadTemplate'), msg: this.$t('managers.widgetGallery.templateSuccessfullyUploaded') })

					this.loadAllTemplates()
				})
			}
		}
	})
</script>

<style lang="scss" scoped>
	.kn-list-column {
		border-right: 1px solid #ccc;
	}
</style>
