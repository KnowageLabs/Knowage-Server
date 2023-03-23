<template>
    <div>VEGA CONTAINER</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IWidget, ISelection } from '../../../Dashboard'

export default defineComponent({
    name: 'vega-container',
    components: {},
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        dataToShow: { type: Object as any, required: true },
        dashboardId: { type: String, required: true },
        editorMode: { type: Boolean },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true }
    },
    data() {
        return {}
    },
    computed: {},
    watch: {},
    mounted() {},
    unmounted() {},
    methods: {}
})
</script>
