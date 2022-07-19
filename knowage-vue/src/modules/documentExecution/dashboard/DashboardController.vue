<template>
    <div class="dashboard-container" :id="`dashboard_${model.configuration.id}`">
        <DashboardRenderer :model="model" :datasets="datasets"></DashboardRenderer>
        <WidgetPickerDialog v-if="widgetPickerVisible" :visible="widgetPickerVisible" @closeWidgetPicker="widgetPickerVisible = false" />
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the dashboard instance and to get initializing informations needed like the theme or the datasets.
 */
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { v4 as uuidv4 } from 'uuid'
import DashboardRenderer from './DashboardRenderer.vue'
import WidgetPickerDialog from './widget/WidgetPicker/WidgetPickerDialog.vue'
import mock from './DashboardMock.json'
import dashboardStore from './Dashboard.store'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'dashboard-manager',
    components: { DashboardRenderer, WidgetPickerDialog },
    data() {
        return {
            model: mock,
            widgetPickerVisible: true,
            datasets: [] as any[]
        }
    },
    provide() {
        return {
            dHash: uuidv4()
        }
    },
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    created() {
        this.loadDatasets()
    },

    unmounted() {
        this.store.removeDashboard({ id: (this as any).dHash as any })
    },
    methods: {
        async loadDatasets() {
            this.appStore.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/?asPagedList=true&seeTechnical=true`)
                .then((response: AxiosResponse<any>) => (this.datasets = response.data))
                .catch(() => {})
            this.appStore.setLoading(false)
            console.log('DASHBOARD CONTROLLER - loadDatasets() - datasets: ', this.datasets)
        }
    }
})
</script>
<style lang="scss">
.dashboard-container {
    width: 100%;
    height: 100vh;
    overflow-y: auto;
    position: relative;
}
@media screen and (max-width: 600px) {
    .dashboard-container {
        height: calc(100vh - var(--kn-mainmenu-width));
    }
}
</style>
