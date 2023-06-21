<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #start> Template {{ template.label }} </template>
        <template #end>
            <Button v-tooltip.bottom="$t('common.download')" icon="pi pi-download" class="p-button-text p-button-rounded p-button-plain" :disabled="!template.id" @click="downloadTemplate" />
            <Button v-tooltip.bottom="$t('common.save')" icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="isSaveButtonDisabled" @click="saveTemplate" />
            <Button v-tooltip.bottom="$t('common.close')" icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm()" />
        </template>
    </Toolbar>
    <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" />

    <div class="managerDetail">
        <div class="p-grid p-m-0 p-fluid">
            <div class="p-col-9">
                <Card>
                    <template #content>
                        <div class="p-grid">
                            <div class="p-col-3">
                                <span class="p-float-label">
                                    <InputText id="label" v-model="v$.template.label.$model" class="kn-material-input" type="text" @change="setDirty" />
                                    <label class="kn-material-input-label" for="label">{{ $t('common.label') }}</label>
                                </span>
                                <KnValidationMessages class="p-mt-1" :v-comp="v$.template.label" :additional-translate-params="{ fieldName: $t('common.label') }" />
                            </div>
                            <div class="p-col-3">
                                <span class="p-float-label">
                                    <InputText id="name" v-model="v$.template.name.$model" class="kn-material-input" type="text" @change="setDirty" />
                                    <label class="kn-material-input-label" for="name">{{ $t('common.name') }}</label>
                                </span>
                                <KnValidationMessages class="p-mt-1" :v-comp="v$.template.name" :additional-translate-params="{ fieldName: $t('common.name') }" />
                            </div>
                            <div :class="template.type === 'python' ? 'p-col-3' : 'p-col-6'">
                                <span class="p-float-label">
                                    <Dropdown id="type" v-model="v$.template.type" class="kn-material-input" :options="galleryDescriptor.types" option-label="name" option-value="value" @change="setDirty" />
                                    <label class="kn-material-input-label" for="type">{{ $t('common.type') }}</label>
                                </span>
                            </div>
                            <div v-if="template.type === 'python'" class="p-col-3">
                                <span class="p-float-label">
                                    <Dropdown id="outputType" v-model="v$.template.outputType.$model" class="kn-material-input" :options="galleryDescriptor.outputTypes" option-label="name" option-value="value" @change="setDirty" />
                                    <label class="kn-material-input-label" for="outputType">{{ $t('managers.widgetGallery.outputType') }}</label>
                                </span>
                            </div>
                            <div class="p-col-12">
                                <span class="p-float-label">
                                    <Textarea id="description" v-model="v$.template.description.$model" class="kn-material-input" style="resize: none" rows="3" @change="setDirty" />
                                    <label class="kn-material-input-label" for="description">{{ $t('common.description') }}</label>
                                </span>
                            </div>
                            <div class="p-col-12 kn-truncated">
                                <span class="p-float-label kn-material-input" :title="$t('managers.widgetGallery.tags.availableCharacters')">
                                    <Chips v-model="v$.template.tags.$model" :allow-duplicate="false" @add="setDirty" @remove="setDirty" />
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
                        <input id="inputImage" type="file" accept="image/png, image/jpeg" @change="uploadFile" />
                        <label v-tooltip.bottom="$t('common.upload')" for="inputImage">
                            <i class="pi pi-upload" />
                        </label>
                    </template>
                    <template #content>
                        <div class="imageContainer p-d-flex p-jc-center p-ai-center">
                            <i v-if="!template.image" class="far fa-image fa-5x icon" />
                            <img v-if="template.image" :src="template.image" height="100%" class="kn-no-select" />
                        </div>
                    </template>
                </Card>
            </div>
        </div>
        <div v-if="template.type && windowWidth < windowWidthBreakPoint" class="p-grid p-m-2 flex">
            <TabView class="tabview-custom" style="width: 100%">
                <TabPanel v-for="(allowedEditor, index) in galleryDescriptor.allowedEditors[template.type]" :key="allowedEditor">
                    <template #header>
                        <i :class="['icon', galleryDescriptor.editor[allowedEditor].icon]"></i>&nbsp;<span style="text-transform: uppercase">{{ $t('common.codingLanguages.' + allowedEditor) }}</span>
                    </template>
                    <VCodeMirror :ref="'editor_' + index" class="flex" v-model:value="template.code[allowedEditor]" :options="galleryDescriptor.options[allowedEditor]" @update:value="onCmCodeChange" />
                </TabPanel>
            </TabView>
        </div>
        <div v-if="template.type && windowWidth >= windowWidthBreakPoint" class="p-grid p-m-0 flex">
            <div v-for="allowedEditor in galleryDescriptor.allowedEditors[template.type]" :key="allowedEditor" class="multiViewer" :class="'p-col-' + 12 / galleryDescriptor.allowedEditors[template.type].length">
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
import { AxiosResponse } from 'axios'
import Chips from 'primevue/chips'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Textarea from 'primevue/textarea'
import galleryDescriptor from './GalleryManagementDescriptor.json'
import { IGalleryTemplate } from './GalleryManagement'
import useValidate from '@vuelidate/core'
import { createValidations } from '@/helpers/commons/validationHelper'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    name: 'gallery-management-detail',
    components: {
        Chips,
        VCodeMirror,
        Dropdown,
        KnValidationMessages,
        InputText,
        TabView,
        TabPanel,
        Textarea
    },
    props: {
        id: String
    },
    emits: ['saved'],
    data() {
        return {
            dirty: false as boolean,
            files: [],
            loading: false as boolean,
            test: '' as string,
            galleryTemplates: [],
            template: {} as IGalleryTemplate,
            galleryDescriptor: galleryDescriptor,
            windowWidth: window.innerWidth,
            windowWidthBreakPoint: 1500,
            v$: useValidate() as any
        }
    },
    validations() {
        return {
            template: createValidations('template', this.galleryDescriptor.validations.template)
        }
    },
    computed: {
        isSaveButtonDisabled(): any {
            return this.v$.$invalid || !this.dirty
        }
    },
    watch: {
        '$route.params.id': function(id) {
            this.loadTemplate(id)
        }
    },
    created() {
        this.loadTemplate(this.id)
        window.addEventListener('resize', this.resizeHandler)
    },
    unmounted() {
        window.removeEventListener('resize', this.resizeHandler)
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
        closeTemplateConfirm(): void {
            if (!this.dirty) {
                this.closeTemplate()
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.dirty = false
                        this.closeTemplate()
                    }
                })
            }
        },
        closeTemplate(): void {
            this.$router.push('/gallery-management')
        },
        loadTemplate(id?: string): void {
            this.loading = true
            if (id) {
                this.$http
                    .get(process.env.VUE_APP_API_PATH + '1.0/widgetgallery/' + (id || this.id))
                    .then((response: AxiosResponse<any>) => {
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
                const postUrl = this.id ? '1.0/widgetgallery/' + this.id : '1.0/widgetgallery'
                const label = this.template.label
                const name = this.template.name
                if (!label && !name) {
                    this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('managers.widgetGallery.fieldIsMandatory', { field: this.$t('common.name') }) })
                    return
                }
                if (!label && name) this.template.label = name
                if (label && !name) this.template.name = label
                this.$http
                    .post(process.env.VUE_APP_API_PATH + postUrl, this.template)
                    .then((response: AxiosResponse<any>) => {
                        this.$store.commit('setInfo', { title: this.$t('managers.widgetGallery.saveTemplate'), msg: this.$t('managers.widgetGallery.templateSuccessfullySaved') })
                        this.$router.push('/gallery-management/' + response.data.id)
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
            const self = this
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
        validateTags(): boolean {
            const validationRegex = /^([a-zA-Z0-9-_])*$/g
            for (const idx in this.template.tags) {
                const currentTag = this.template.tags[idx]
                const valid = currentTag.match(validationRegex)
                if (!valid) {
                    this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: this.$t('common.error.tags.tagIsNotValid', { tag: currentTag }) })
                    return false
                }
            }
            return true
        },
        tabChange(e) {
            let ref = 'editor_' + e.index
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs[ref].editor.refresh()
        }
    }
})
</script>

<style lang="scss" scoped>
.managerDetail {
    overflow: auto;
    flex: 1;

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
            background-color: var(--kn-color-secondary);
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
            color: var(--kn-color-secondary);
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
        height: 220px;
    }
}
</style>
