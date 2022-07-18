<template>
    <div class="dashboard-container" :id="`dashboard_${model.configuration.id}`">
        <DashboardRenderer :model="model"></DashboardRenderer>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the dashboard instance and to get initializing informations needed like the theme or the datasets.
 */
import { defineComponent } from 'vue'
import DashboardRenderer from './DashboardRenderer.vue'
import mock from './DashboardMock.json'
import { v4 as uuidv4 } from 'uuid'
import dashboardStore from './Dashboard.store'

export default defineComponent({
    name: 'dashboard-manager',
    components: { DashboardRenderer },
    data() {
        return {
            model: mock
        }
    },
    setup() {
        const store = dashboardStore()
        return { store }
    },
    provide() {
        return {
            dHash: uuidv4()
        }
    },
    unmounted() {
        this.store.removeDashboard({ id: (this as any).dHash as any })
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
