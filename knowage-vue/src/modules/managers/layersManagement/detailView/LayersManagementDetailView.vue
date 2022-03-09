<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #start>{{ selectedLayer.label }}</template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>

    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <TabView v-model:activeIndex="activeIndex" @tab-change="onTabChange">
        <TabPanel>
            <template #header>
                <span>{{ $t('managers.layersManagement.layerTitle') }}</span>
            </template>
            <LayerTab :selectedLayer="selectedLayer" :allRoles="allRoles" :allCategories="allCategories" />
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.layersManagement.filterTitle') }}</span>
            </template>
            <FilterTab :selectedLayer="selectedLayer" :propFilters="filters" />
        </TabPanel>
    </TabView>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iFilter } from '../LayersManagement'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import LayerTab from './layerTab/LayersManagementLayerTab.vue'
import FilterTab from './filterTab/LayersManagementFilterTab.vue'

export default defineComponent({
    components: { TabView, TabPanel, LayerTab, FilterTab },
    props: { id: { type: String, required: false }, selectedLayer: { type: Object, required: true }, allRoles: { type: Array, required: true }, allCategories: { type: Array, required: true } },
    computed: {},
    emits: ['touched', 'closed'],
    data() {
        return {
            touched: false,
            layer: {} as any,
            loading: false,
            activeIndex: 0,
            filters: [] as iFilter[]
        }
    },
    async created() {
        this.getRolesForLayer()
        this.loadLayer()
        console.log('layerChanged: ', this.selectedLayer)
    },
    watch: {
        id() {
            this.getRolesForLayer()
            this.loadLayer()
        },
        selectedLayer() {
            console.log('layerChanged: ', this.selectedLayer)
        }
    },
    methods: {
        loadLayer() {
            this.layer = this.selectedLayer
            this.layer.properties = this.layer.properties?.map((property: string) => {
                return {
                    property: property
                }
            })
        },
        async getRolesForLayer() {
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `layers/postitem`, this.selectedLayer).then((response: AxiosResponse<any>) => (this.layer.roles = response.data))
        },
        closeTemplateConfirm() {
            if (!this.touched) {
                this.closeTemplate()
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.closeTemplate()
                    }
                })
            }
        },
        closeTemplate() {
            this.$router.push('/layers-management')
            this.$emit('closed')
        },
        onTabChange() {
            if (this.activeIndex === 1) {
                this.loadFilters()
            }
        },
        async loadFilters() {
            if (this.layer) {
                this.loading = true
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `layers/getFilter?id=${this.layer.layerId}`).then((response: AxiosResponse<any>) => (this.filters = response.data))
                this.loading = false
            }
        }
    }
})
</script>
