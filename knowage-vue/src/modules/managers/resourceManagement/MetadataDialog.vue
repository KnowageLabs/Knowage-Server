<template>
	<div class="managerDetail">
		<Dialog class="kn-dialog--toolbar--primary knMetadataDialog" v-bind:visible="visibility" footer="footer" :header="$t('metadataDialog.title')" :closable="false" modal @id="loadMetadata">
			<div class="p-grid p-m-0 p-fluid">
				<div class="p-col-8">
					<div class="p-col-4">
						<span class="p-float-label">
							<InputText id="name" class="kn-material-input" type="text" v-model="metadata.name" @change="setDirty" />
							<label class="kn-material-input-label" for="name">{{ $t('common.name') }}</label>
						</span>
					</div>
					<div class="p-col">
						<InputSwitch v-model="metadata.openSource" @change="setDirty" />
						<label class="kn-material-input-label" for="name">{{ $t(descriptor.metadata.openSource.label) }}</label>
					</div>
				</div>
				<div class="p-col-8">
					<div class="p-col-4">
						<span class="p-float-label">
							<InputText id="name" class="kn-material-input" type="text" v-model="metadata.version" @change="setDirty" />
							<label class="kn-material-input-label" for="name">{{ $t(descriptor.metadata.version.label) }}</label>
						</span>
					</div>
					<div class="p-col">
						<span class="p-float-label">
							<Dropdown id="typeOfAnalytics" class="kn-material-input" v-model="metadata.typeOfAnalytics" @change="setDirty" :options="descriptor.metadata.typeOfAnalytics.options" optionLabel="name" optionValue="value">
								<template #option="slotProps">
									<div class="p-dropdown-car-option">
										{{ $t(slotProps.option.name) }}
									</div>
								</template>
							</Dropdown>
							<label class="kn-material-input-label" for="outputType">{{ $t(descriptor.metadata.typeOfAnalytics.label) }}</label>
						</span>
					</div>
				</div>
				<div class="p-col-4 kn-height-full">
					{{ $t('common.image') }}
					<input id="inputImage" type="file" @change="uploadFile" accept="image/png, image/jpeg" />
					<label for="inputImage" v-tooltip.bottom="$t('common.upload')">
						<i class="pi pi-upload" />
					</label>
					<div class="imageContainer p-d-flex p-jc-center p-ai-center">
						<i class="far fa-image fa-5x icon" v-if="!metadata.image" />
						<img :src="metadata.image" v-if="metadata.image" height="100%" class="kn-no-select" />
					</div>
				</div>

				<div class="p-col-12">
					<span class="p-float-label">
						<Textarea v-model="metadata.description" class="kn-material-input" style="resize:none" id="description" rows="3" @change="setDirty" />
						<label class="kn-material-input-label" for="description">{{ $t('common.description') }}</label>
					</span>
				</div>
			</div>
			<div class="p-col-12">
				<TabView @change="resetSearchFilter">
					<TabPanel :header="$t(descriptor.metadata.accuracyAndPerformance.label)">
						<Textarea v-model="metadata.accuracyAndPerformance" class="kn-material-input metadataTextArea" style="resize:none" id="description" rows="3" @change="setDirty" />
					</TabPanel>
					<TabPanel :header="$t(descriptor.metadata.usageOfTheModel.label)">
						<Textarea v-model="metadata.usageOfTheModel" class="kn-material-input metadataTextArea" style="resize:none" id="description" rows="3" @change="setDirty" />
					</TabPanel>
					<TabPanel :header="$t(descriptor.metadata.formatOfData.label)">
						<Textarea v-model="metadata.formatOfData" class="kn-material-input metadataTextArea" style="resize:none" id="description" rows="3" @change="setDirty" />
					</TabPanel>
				</TabView>
			</div>
			<template #footer>
				<Button class="kn-button kn-button--primary" @click="saveMetadata" :disabled="!dirty"> {{ $t('common.save') }}</Button>
				<Button class="kn-button kn-button--secondary" @click="closeDialog">
					{{ $t('common.close') }}
				</Button>
			</template>
		</Dialog>
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	/* import Avatar from 'primevue/avatar' */
	import axios from 'axios'
	import Dialog from 'primevue/dialog'
	import Dropdown from 'primevue/dropdown'
	import { mapState } from 'vuex'
	import InputSwitch from 'primevue/inputswitch'
	import resourceManagementDescriptor from './ResourceManagementDescriptor.json'
	import Textarea from 'primevue/textarea'
	import TabView from 'primevue/tabview'
	import TabPanel from 'primevue/tabpanel'
	/* import Listbox from 'primevue/listbox'
		import axios from 'axios'

		import TabView from 'primevue/tabview'
		import TabPanel from 'primevue/tabpanel'
		import { formatDate } from '@/helpers/commons/localeHelper' */

	export default defineComponent({
		name: 'metadata-dialog',
		components: { Dialog, Dropdown, InputSwitch, TabPanel, TabView, Textarea },
		data() {
			return {
				dirty: false,
				loading: true,
				metadata: {},
				checked: false,
				descriptor: resourceManagementDescriptor
			}
		},

		created() {
			this.loadMetadata()
		},
		props: {
			id: String,
			visibility: Boolean
		},
		emits: ['update:visibility'],
		methods: {
			closeDialog() {
				this.$emit('update:visibility', false)
			},
			loadMetadata() {
				this.loading = true

				if (this.id) {
					axios
						.get(process.env.VUE_APP_API_PATH + `2.0/resources/files/metadata?key=` + this.id)
						.then((response) => {
							this.metadata = response.data
						})
						.catch(() => {
							this.$store.commit('setError', {
								title: this.$t('common.toast.metadata'),
								msg: this.$t('common.toast.metadataLoadingFailed')
							})
						})
				}
				this.loading = false
			},
			saveMetadata(): void {
				this.loading = true
				if (this.id) {
					axios
						.post(process.env.VUE_APP_API_PATH + `2.0/resources/files/metadata?key=` + this.id, this.metadata, {
							responseType: 'arraybuffer', // important...because we need to convert it to a blob. If we don't specify this, response.data will be the raw data. It cannot be converted to blob directly.

							headers: {
								'Content-Type': 'application/json'
							}
						})
						.then(() => {
							this.$store.commit('setInfo', {
								title: this.$t('common.toast.metadataTitle'),
								msg: this.$t('common.toast.metadataUpdatedSuccessfully')
							})
						})
						.catch(() => {
							this.$store.commit('setError', {
								title: this.$t('common.toast.metadataTitle'),
								msg: this.$t('common.toast.metadataLoadingFailed')
							})
						})
				}
				this.loading = false
			},
			setDirty(): void {
				this.dirty = true
			}
		},
		computed: {
			...mapState({
				locale: 'locale'
			})
		},
		watch: {
			id() {
				this.loadMetadata()
			}
		}
	})
</script>

<style scoped lang="scss">
	#inputImage {
		display: none;
	}
	.knMetadataDialog {
		min-width: 600px;
		width: 600%;
		max-width: 1200px;

		&:deep(.p-dialog-content) {
			min-width: 600px;
			width: 600%;
			max-width: 1200px;
		}

		.p-fileupload-buttonbar {
			border: none;

			.p-button:not(.p-fileupload-choose) {
				display: none;
			}

			.p-fileupload-choose {
				@extend .kn-button--primary;
			}
		}

		.metadataTextArea {
			width: 100%;
		}
	}
</style>
