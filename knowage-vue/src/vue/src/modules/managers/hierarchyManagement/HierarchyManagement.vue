<template>
    <div class="kn-page">
        <div class="kn-page-content">
            <ProgressSpinner v-if="loading" class="kn-progress-spinner" data-test="spinner" />

            <TabView class="hierarchy-tabview kn-height-full">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('managers.hierarchyManagement.master') }}</span>
                    </template>

                    <HierarchyManagementMasterTab :dimensions="dimensions" @loading="setLoading" />
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('managers.hierarchyManagement.technical') }}</span>
                    </template>

                    <HierarchyManagementTechnicalTab :dimensions="dimensions" @loading="setLoading"></HierarchyManagementTechnicalTab>
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('managers.hierarchyManagement.backup') }}</span>
                    </template>

                    <HierarchyManagementBackupTab :dimensions="dimensions" />
                </TabPanel>
            </TabView>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDimension } from './HierarchyManagement.d'
import { AxiosResponse } from 'axios'
import HierarchyManagementMasterTab from './HierarchyManagementMasterTab/HierarchyManagementMasterTab.vue'
import HierarchyManagementTechnicalTab from './HierarchyManagementTechnicalTab/HierarchyManagementTechnicalTab.vue'
import HierarchyManagementBackupTab from './HierarchyManagementBackupTab/HierarchyManagementBackupTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import ProgressSpinner from 'primevue/progressspinner'
import descriptor from './HierarchyManagementDescriptor.json'

export default defineComponent({
    name: 'hierarchy-management',
    components: { ProgressSpinner, HierarchyManagementMasterTab, HierarchyManagementTechnicalTab, HierarchyManagementBackupTab, TabView, TabPanel },
    data() {
        return {
            dimensions: [] as iDimension[],
            loading: false,
            descriptor
        }
    },
    async created() {
        await this.loadDimensions()
    },
    methods: {
        async loadDimensions() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `dimensions/getDimensions`)
                .then((response: AxiosResponse<any>) => (this.dimensions = response.data))
                .catch(() => {})
            this.loading = false
        },
        setLoading(value: boolean) {
            this.loading = value
        }
    }
})
</script>
<style lang="scss">
.hierarchy-tabview,
.hierarchy-tabview .p-tabview-panels,
.hierarchy-tabview .p-tabview-panel {
    display: flex;
    flex-direction: column;
    flex: 1;
}

.hierarchy-tabview .p-tabview-panels {
    padding: 0 !important;
}

.hierarchy-scrollable-card {
    height: calc(100vh - 55px);
    flex: 1 1 0;
    .p-card-body {
        flex: 1;
        overflow: auto;
    }
}
</style>
