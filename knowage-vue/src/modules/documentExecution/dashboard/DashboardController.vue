<template>
    <div class="dashboard-container">
        <DashboardRenderer :model="model"></DashboardRenderer>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DashboardRenderer from './DashboardRenderer.vue'
import mock from './DashboardMock.json'
import { v4 as uuidv4 } from 'uuid'

export default defineComponent({
    name: 'dashboard-manager',
    components: { DashboardRenderer },
    data() {
        return {
            model: mock
        }
    },
    provide() {
        return {
            dHash: uuidv4()
        }
    },
    unmounted() {
        // @ts-ignore
        this.$store.commit('dashboard/removeDashboard', { id: this.dHash as any })
    }
})
</script>
<style lang="scss">
.dashboard-container {
    width: 100%;
    height: 100vh;
    overflow-y: auto;
}
@media screen and (max-width: 600px) {
    .dashboard-container {
        height: calc(100vh - var(--kn-mainmenu-width));
    }
}
</style>
