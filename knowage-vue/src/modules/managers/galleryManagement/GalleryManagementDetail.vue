<template>
	<div class="managerDetail">
		<Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
			<template #left> Template {{ template.name }} </template>
			<template #right>
				<Button icon="pi pi-download" class="p-button-text p-button-rounded p-button-plain" @click="downloadTemplate" :disabled="!template.id" />
				<Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="!dirty" @click="saveTemplate" />
				<Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate($event)" />
			</template>
		</Toolbar>
		<ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
		<div class="p-grid p-m-0 p-fluid">
			<div class="p-col-9">
				<Card>
					<template #title>
						{{ $t('common.information') }}
					</template>
					<template #content>
						<div class="p-grid">
							<div class="p-col-6">
								<span class="p-float-label">
									<InputText id="label" class="kn-material-input" type="text" v-model="template.name" @change="setDirty" />
									<label class="kn-material-input-label" for="label">{{ $t('common.label') }}</label>
								</span>
							</div>
							<div :class="template.type === 'python' ? 'p-col-3' : 'p-col-6'">
								<span class="p-float-label">
									<Dropdown id="type" class="kn-material-input" v-model="template.type" @change="setDirty" :options="galleryDescriptor.types" optionLabel="name" optionValue="value" />
									<label class="kn-material-input-label" for="type">{{ $t('common.type') }}</label>
								</span>
							</div>
							<div class="p-col-3" v-if="template.type === 'python'">
								<span class="p-float-label">
									<Dropdown id="outputType" class="kn-material-input" v-model="template.outputType" @change="setDirty" :options="galleryDescriptor.outputTypes" optionLabel="name" optionValue="value" />
									<label class="kn-material-input-label" for="outputType">{{ $t('managers.widgetGallery.outputType') }}</label>
								</span>
							</div>
							<div class="p-col-12">
								<span class="p-float-label">
									<Textarea v-model="template.description" class="kn-material-input" :autoResize="true" id="description" rows="3" @change="setDirty" />
									<label class="kn-material-input-label" for="description">{{ $t('common.description') }}</label>
								</span>
							</div>
							<div class="p-col-12">
								<span class="p-float-label kn-material-input">
									<Chips v-model="template.tags" @add="setDirty" @remove="setDirty" :allowDuplicate="false" />
									<label class="kn-material-input-label" for="tags">{{ $t('common.tags') }}</label>
								</span>
								<small id="username1-help">{{ $t('managers.widgetGallery.tags.availableCharacters') }}</small>
							</div>
						</div>
					</template>
				</Card>
			</div>
			<div class="p-col-3 kn-height-full">
				<Card class="imageUploader">
					<template #title>
						{{ $t('common.image') }}
						<input id="inputImage" type="file" @change="uploadFile" accept="image/png, image/jpeg" />
						<label for="inputImage">
							<i class="pi pi-upload" />
						</label>
					</template>
					<template #content>
						<div class="imageContainer p-d-flex p-jc-center p-ai-center">
							<i class="far fa-image fa-5x icon" v-if="!template.image" />
							<img :src="template.image" v-if="template.image" height="100%" class="kn-no-select" />
						</div>
					</template>
				</Card>
			</div>
		</div>
		<div class="p-grid p-m-2 flex" v-if="template.type && windowWidth < windowWidthBreakPoint">
			<TabView class="tabview-custom" style="width:100%">
				<TabPanel v-for="allowedEditor in galleryDescriptor.allowedEditors[template.type]" v-bind:key="allowedEditor">
					<template #header>
						<i :class="['icon', galleryDescriptor.editor[allowedEditor].icon]"></i>&nbsp;<span style="text-transform:uppercase">{{ $t('common.codingLanguages.' + allowedEditor) }}</span>
					</template>
					<VCodeMirror class="flex" v-model:value="template.code[allowedEditor]" :options="galleryDescriptor.options[allowedEditor]" @update:value="onCmCodeChange" />
				</TabPanel>
			</TabView>
		</div>
		<div class="p-grid p-m-0 flex" v-if="template.type && windowWidth >= windowWidthBreakPoint">
			<div :class="'p-col-' + 12 / galleryDescriptor.allowedEditors[template.type].length" v-for="allowedEditor in galleryDescriptor.allowedEditors[template.type]" v-bind:key="allowedEditor" style="height:100%;display:flex;flex-direction:column">
				<h4>
					<i :class="['icon', galleryDescriptor.editor[allowedEditor].icon]"></i>
					{{ $t('common.codingLanguages.' + allowedEditor) }}
				</h4>
				<VCodeMirror class="flex" v-model:value="template.code[allowedEditor]" :options="galleryDescriptor.options[allowedEditor]" @update:value="onCmCodeChange" />
			</div>
		</div>
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import { VCodeMirror } from 'vue3-code-mirror'
	import axios from 'axios'
	import Chips from 'primevue/chips'
	import { downloadDirect } from '@/helpers/commons/fileHelper'
	import Dropdown from 'primevue/dropdown'
	import InputText from 'primevue/inputtext'
	import TabView from 'primevue/tabview'
	import TabPanel from 'primevue/tabpanel'
	import Textarea from 'primevue/textarea'
	import galleryDescriptor from './GalleryManagementDescriptor.json'
	import { IGalleryTemplate } from './GalleryManagement'

	export default defineComponent({
		name: 'gallery-management-detail',
		components: {
			Chips,
			VCodeMirror,
			Dropdown,
			InputText,
			TabView,
			TabPanel,
			Textarea
		},
		emits: ['saved'],
		props: {
			id: String
		},
		data() {
			return {
				dirty: false as Boolean,
				files: [],
				loading: false as Boolean,
				test: '' as String,
				galleryTemplates: [],
				template: {} as IGalleryTemplate,
				galleryDescriptor: galleryDescriptor,
				windowWidth: window.innerWidth,
				windowWidthBreakPoint: 1500
			}
		},
		created() {
			this.loadTemplate(this.id)
			window.addEventListener('resize', this.resizeHandler)
		},
		methods: {
			downloadTemplate(): void {
				if (this.dirty) {
					this.$confirm.require({
						message: this.$t('managers.widgetGallery.templateIsNotSaved'),
						header: this.$t('managers.widgetGallery.downloadTemplate'),
						icon: 'pi pi-exclamation-triangle',
						accept: () => {
							downloadDirect(JSON.stringify(this.template), this.template.name, 'application/json')
						}
					})
				} else {
					downloadDirect(JSON.stringify(this.template), this.template.name, 'application/json')
				}
			},
			closeTemplate(): void {
				this.$router.push('/gallerymanagement')
			},
			loadTemplate(id?: string): void {
				this.loading = true
				if (id) {
					axios
						.get(process.env.VUE_APP_API_PATH + '1.0/widgetgallery/' + (id || this.id))
						.then((response) => {
							this.template = response.data
						})
						.catch((error) => console.error(error))
						.finally(() => {
							this.loading = false
							this.dirty = false
						})
				} else {
					this.template = { type: 'html', code: { html: '', css: '', javascript: '', python: '' } } as IGalleryTemplate
					this.loading = false
					this.dirty = false
				}
			},
			onCmCodeChange(): void {
				this.setDirty()
			},
			saveTemplate(): void {
				if (this.validateTags()) {
					let postUrl = this.id ? '1.0/widgetgallery/' + this.id : '1.0/widgetgallery'
					axios
						.post(process.env.VUE_APP_API_PATH + postUrl, this.template)
						.then((response) => {
							this.$store.commit('setInfo', { title: this.$t('managers.widgetGallery.saveTemplate'), msg: this.$t('managers.widgetGallery.templateSuccessfullySaved') })
							this.$router.push('/gallerymanagement/' + response.data.id)
							this.$emit('saved')
						})
						.catch((error) => console.error(error))
				}
			},
			setDirty(): void {
				this.dirty = true
			},
			uploadFile(event): void {
				const reader = new FileReader()
				let self = this
				reader.addEventListener(
					'load',
					function() {
						self.template.image = reader.result || ''
					},
					false
				)
				if (event.srcElement.files[0] && event.srcElement.files[0].size < process.env.VUE_APP_MAX_UPLOAD_IMAGE_SIZE) {
					reader.readAsDataURL(event.srcElement.files[0])
					this.setDirty()
				} else this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('common.error.exceededSize', { size: '(200KB)' }) })
			},
			resizeHandler(): void {
				this.windowWidth = window.innerWidth
			},
			validateTags(): Boolean {
				const re = /^([a-zA-Z0-9\\-\\_])*$/g
				for (var idx in this.template.tags) {
					let currentTag = this.template.tags[idx]
					const found = currentTag.match(re)
					if (!found) {
						this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('managers.widgetGallery.tags.tagIsNotValid', { tag: currentTag }) })
						return false
					}
				}

				return true
			}
		},
		watch: {
			'$route.params.id': function(id) {
				this.loadTemplate(id)
			}
		},
		unmounted() {
			window.removeEventListener('resize', this.resizeHandler)
		}
	})
</script>

<style lang="scss" scoped>
	.managerDetail {
		#inputImage {
			display: none;
		}
		label[for='inputImage'] {
			float: right;
			transition: background-color 0.3s linear;
			border-radius: 50%;
			width: 2.25rem;
			line-height: 1rem;
			top: -5px;
			height: 2.25rem;
			padding: 0.571rem;
			position: relative;
			cursor: pointer;
			user-select: none;
			&:hover {
				background-color: $color-secondary;
			}
		}
		&:deep(.p-tabview) {
			display: flex;
			flex-direction: column;
			.p-tabview-panels {
				padding: 0;
				flex: 1;
				.p-tabview-panel {
					height: 100%;
					.v-code-mirror {
						height: 100%;
					}
				}
			}
		}
		&:deep(.CodeMirror) {
			font-size: 0.8rem;
		}
		display: flex;
		height: 100%;
		flex-direction: column;
		.flex {
			flex: 1;
		}
		h4 {
			margin: 0;
			padding: 8px;
			background-color: #1a1b1f;
			color: #aaaebc;
			text-transform: uppercase;
		}
		&:deep(.imageUploader) {
			.p-fileupload {
				display: inline-block;
				float: right;
				.p-button {
					background-color: transparent;
					color: black;
				}
			}
		}
		.imageContainer {
			height: 100%;
			.icon {
				color: $color-secondary;
			}
			img {
				height: auto;
				max-height: 100%;
				max-width: 100%;
			}
		}
		.codemirrorContainer {
			width: 100%;
			display: inline-flex;
			.editorContainer {
				flex: 1;
			}
		}
		&:deep(.p-card-content) {
			height: 210px;
		}
	}
</style>
