<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #start>{{ selectedLayer.label }}</template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>

    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <TabView>
        <TabPanel>
            <template #header>
                <span>{{ $t('managers.layersManagement.layerTitle') }}</span>
            </template>
            <LayerTab :selectedLayer="selectedLayer" :allRoles="allRoles" />
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.layersManagement.filterTitle') }}</span>
            </template>
            <FilterTab :selectedLayer="selectedLayer" />
        </TabPanel>
    </TabView>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import LayerTab from './layerTab/LayersManagementLayerTab.vue'
import FilterTab from './LayersManagementFilterTab.vue'

export default defineComponent({
    components: { TabView, TabPanel, LayerTab, FilterTab },
    props: { id: { type: String, required: false }, selectedLayer: { type: Object, required: true }, allRoles: { type: Array, required: true } },
    computed: {},
    emits: ['touched', 'closed'],
    data() {
        return {
            touched: false,
            layer: {} as any
        }
    },
    async created() {
        this.getRolesForLayer()
        this.layer = this.selectedLayer
        console.log('layerChanged: ', this.selectedLayer)
    },
    watch: {
        id() {
            this.getRolesForLayer()
            this.layer = this.selectedLayer
        },
        selectedLayer() {
            console.log('layerChanged: ', this.selectedLayer)
        }
    },
    methods: {
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
        }
    }
})
</script>
