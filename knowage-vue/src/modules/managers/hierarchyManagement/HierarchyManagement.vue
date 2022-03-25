<template>
    <div class="kn-page">
        <div class="kn-page-content">
            <KnOverlaySpinnerPanel :visibility="loading" />

            <TabView class="p-d-flex p-flex-column kn-flex kn-tab">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('managers.hierarchyManagement.master') }}</span>
                    </template>
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('managers.hierarchyManagement.technical') }}</span>
                    </template>
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('managers.hierarchyManagement.backup') }}</span>
                    </template>
                </TabPanel>
            </TabView>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDimension } from './HierarchyManagement.d'
import { AxiosResponse } from 'axios'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'hierarchy-management',
    components: { KnOverlaySpinnerPanel, TabView, TabPanel },
    data() {
        return {
            dimensions: [] as iDimension[],
            loading: false
        }
    },
    async created() {
        await this.loadDimensions()
    },
    methods: {
        async loadDimensions() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dimensions/getDimensions`).then((response: AxiosResponse<any>) => (this.dimensions = response.data))
            this.loading = false
            console.log('LOADED DIMENSIONS: ', this.dimensions)
        }
    }
})
</script>
