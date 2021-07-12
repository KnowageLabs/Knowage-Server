<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col-4 p-sm-4 p-md-3 p-p-0">
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
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="galleryTemplates"
                    listStyle="max-height:calc(100% - 62px)"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="name"
                    filterMatchMode="contains"
                    :filterFields="['name', 'type', 'tags']"
                    :emptyFilterMessage="$t('managers.widgetGallery.noResults')"
                >
                    <template #option="slotProps">
                        <router-link class="kn-decoration-none" :to="{ name: 'gallery-detail', params: { id: slotProps.option.id } }" exact>
                            <div class="kn-list-item">
                                <Avatar :icon="typeDescriptor.iconTypesMap[slotProps.option.type].className" shape="circle" size="medium" :style="typeDescriptor.iconTypesMap[slotProps.option.type].style" v-tooltip.bottom="slotProps.option.type" />
                                <div class="kn-list-item-text">
                                    <span>{{ slotProps.option.name }}</span>
                                    <span class="kn-list-item-text-secondary kn-truncated" v-tooltip="slotProps.option.description">{{ slotProps.option.description }}</span>
                                </div>
                                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click="deleteTemplate($event, slotProps.option.id)" v-tooltip.bottom="$t('common.delete')" />
                            </div>
                        </router-link>
                    </template>
                </Listbox>
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
import Avatar from 'primevue/avatar'
import FabButton from '@/components/UI/KnFabButton.vue'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import { IGalleryTemplate } from './GalleryManagement'
import Listbox from 'primevue/listbox'
import Menu from 'primevue/menu'
import galleryDescriptor from './GalleryManagementDescriptor.json'

export default defineComponent({
    name: 'gallery-management',
    components: {
        Avatar,
        FabButton,
        KnInputFile,
        Listbox,
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
                .then((response) => (this.galleryTemplates = response.data))
                .catch((error) => console.error(error))
                .finally(() => (this.loading = false))
        },
        deleteTemplate(e, templateId): void {
            e.preventDefault()
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
        },
        uploadTemplate(event): void {
            var reader = new FileReader()
            reader.onload = this.onReaderLoad
            reader.readAsText(event.target.files[0])
            this.triggerInputFile(false)
        },
        onReaderLoad(event) {
            try {
                let json = JSON.parse(event.target.result)
                axios.post(process.env.VUE_APP_API_PATH + '1.0/widgetgallery/import', json).then(
                    () => {
                        this.$store.commit('setInfo', { title: this.$t('managers.widgetGallery.uploadTemplate'), msg: this.$t('managers.widgetGallery.templateSuccessfullyUploaded') })

                        this.loadAllTemplates()
                    },
                    (error) => this.$store.commit('setError', { title: this.$t('common.error.uploading'), msg: error.message })
                )
            } catch (e) {
                console.log(e)
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.kn-list-column {
    border-right: 1px solid #ccc;
}
</style>
