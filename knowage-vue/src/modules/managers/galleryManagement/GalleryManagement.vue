<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.widgetGallery.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click="toggleAdd" />
                        <Menu ref="menu" :model="addMenuItems" popup="true" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
                <Listbox v-if="!loading" class="kn-list" :options="galleryTemplates" :filter="true" :filterPlaceholder="$t('common.search')" optionLabel="name" filterMatchMode="contains" :filterFields="['name', 'type', 'tags']" :emptyFilterMessage="$t('managers.widgetGallery.noResults')">
                    <template #option="slotProps">
                        <router-link class="kn-decoration-none" :to="{ name: 'gallerydetail', params: { id: slotProps.option.id } }" exact>
                            <div class="kn-list-item">
                                <Avatar :icon="typeDescriptor.iconTypesMap[slotProps.option.type].className" shape="circle" size="medium" :style="typeDescriptor.iconTypesMap[slotProps.option.type].style" />
                                <div class="kn-list-item-text">
                                    <span>{{ slotProps.option.name }}</span>
                                    <span class="kn-list-item-text-secondary">{{ slotProps.option.author }}</span>
                                </div>
                                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain kn-gallery-slotProps.option.type" @click="deleteTemplate($event, slotProps.option.id)" />
                            </div>
                        </router-link>
                    </template>
                </Listbox>
            </div>
            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view @saved="savedElement" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Avatar from 'primevue/avatar'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import Menu from 'primevue/menu'
import galleryDescriptor from './GalleryManagementDescriptor.json'

export default defineComponent({
    name: 'gallery-management',
    components: {
        Avatar,
        FabButton,
        Listbox,
        Menu
    },
    data() {
        return {
            galleryTemplates: [],
            loading: false,
            typeDescriptor: galleryDescriptor,
            addMenuItems: [
                { label: this.$t('managers.widgetGallery.newTemplate'), icon: 'fas fa-plus', command: () => this.newTemplate() },
                { label: this.$t('managers.widgetGallery.importTemplate'), icon: 'fas fa-file-import', command: () => {} }
            ]
        }
    },
    created() {
        this.loadAllTemplates()
    },
    methods: {
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
                message: 'Are you sure you want to delete the selected template?',
                header: 'Confirmation',
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.axios
                        .delete(process.env.VUE_APP_API_PATH + '1.0/widgetgallery/' + templateId)
                        .then(() => {
                            this.$store.commit('setInfo', { title: 'Deleted template', msg: 'template deleted' })
                            this.loadAllTemplates()
                            if (templateId === this.$route.params.id) this.$router.push('/gallerymanagement')
                        })
                        .catch((error) => console.error(error))
                }
            })
        },
        newTemplate() {
            this.$router.push('/gallerymanagement/newtemplate')
        },
        savedElement() {
            this.loadAllTemplates()
        },
        toggleAdd(event) {
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.menu.toggle(event)
        }
    }
})
</script>

<style lang="scss" scoped>
.kn-list-column {
    border-right: 1px solid #ccc;
}
</style>
