<template>
    <div v-if="crossNavigationModel">
        {{ crossNavigationModel }}
        {{ 'CROSS NAVS: ' }}
        {{ crossNavigationOptions }}
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetSelection } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import dashboardStore from '@/modules/documentExecution/Dashboard/Dashboard.store'

export default defineComponent({
    name: 'table-widget-cross-navigation',
    components: {},
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            descriptor,
            crossNavigationModel: null as ITableWidgetSelection | null,
            crossNavigationOptions: [] as string[]
        }
    },
    setup() {
        const store = dashboardStore()
        return { store }
    },
    created() {
        this.loadSelectionModel()
        this.loadCrossNavigationOptions()
    },
    methods: {
        loadSelectionModel() {
            if (this.widgetModel?.settings?.interactions?.crosssNavigation) this.crossNavigationModel = this.widgetModel.settings.interactions.crosssNavigation
        },
        loadCrossNavigationOptions() {
            const temp = this.store.getCrossNavigations()
            if (temp) this.crossNavigationOptions = temp.map((crossNavigation: any) => crossNavigation.crossName)
        }
    }
})
</script>
