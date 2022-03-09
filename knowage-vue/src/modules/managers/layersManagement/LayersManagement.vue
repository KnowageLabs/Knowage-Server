<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.layersManagement.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click="showDetail" data-test="open-form-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <KnListBox :options="allLayers" :settings="descriptor.knListSettings" @click="showDetail" @delete.stop="deleteLayerConfirm" @download.stop="downloadLayerFile" />
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
                <router-view :selectedLayer="selectedLayer" :allRoles="allRoles" :allCategories="allCategories" @closed="onDetailClose" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iLayer } from './LayersManagement'
import FabButton from '@/components/UI/KnFabButton.vue'
import descriptor from './LayersManagementDescriptor.json'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'

const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'roles-management',
    components: { FabButton, KnListBox },
    data() {
        return {
            descriptor,
            allLayers: [] as iLayer[],
            allRoles: [] as any,
            allCategories: [] as any,
            selectedLayer: {} as iLayer,
            touched: false,
            loading: false
        }
    },
    async created() {
        this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            await Promise.all([await this.getAllLayers(), await this.getAllRoles(), await this.getAllCategories()])
            this.loading = false
        },
        async getAllLayers() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `layers`).then((response: AxiosResponse<any>) => (this.allLayers = response.data.root))
        },
        async getAllRoles() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `layers/getroles?`).then((response: AxiosResponse<any>) => (this.allRoles = response.data))
        },
        async getAllCategories() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=GEO_CATEGORY`).then((response: AxiosResponse<any>) => (this.allCategories = response.data))
        },
        showDetail(event) {
            const path = event.item ? `/layers-management/${event.item.layerId}` : '/layers-management/new-layer'
            if (!this.touched) {
                this.selectedLayer = deepcopy(event.item) as iLayer
                this.$router.push(path)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.selectedLayer = deepcopy(event.item) as iLayer
                        this.$router.push(path)
                    }
                })
            }
        },
        deleteLayerConfirm(event) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteLayer(event.item.layerId)
            })
        },
        async deleteLayer(layerId: number) {
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `layers/deleteLayer?id=${layerId}`).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/layers-management')
                this.getAllLayers()
            })
        },
        onDetailClose() {
            this.touched = false
        }
    }
})
</script>
