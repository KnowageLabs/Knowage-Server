<template>
    <grid-item :key="item.id" :x="item.x" :y="item.y" :w="item.w" :h="item.h" :i="item.i" drag-allow-from=".drag-handle">
        <div v-if="initialized" class="drag-handle"></div>
        <ProgressBar mode="indeterminate" v-if="loading" />
        <Skeleton shape="rectangle" v-if="!initialized" height="100%" border-radius="0" />
        <button @click="test">CLICK ME FOR TEST</button>
        <WidgetRenderer :widget="widget" :data="widgetData" :datasets="datasets" v-if="initialized" @interaction="manageInteraction"></WidgetRenderer>
        <WidgetButtonBar @edit-widget="toggleEditMode"></WidgetButtonBar>
    </grid-item>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget behaviour related to data and interactions, not related to view elements.
 */
import { defineComponent, PropType } from 'vue'
import { getData } from '../DataProxyHelper'
import { IDataset, IWidget } from '../Dashboard'
import { emitter } from '../DashboardHelpers'
import { mapState, mapActions } from 'pinia'
import { getAssociativeSelections } from './dataProxyHelper/DataProxyHelper'
import store from '../Dashboard.store'
import WidgetRenderer from './WidgetRenderer.vue'
import WidgetButtonBar from './WidgetButtonBar.vue'
import Skeleton from 'primevue/skeleton'
import ProgressBar from 'primevue/progressbar'

export default defineComponent({
    name: 'widget-manager',
    components: { ProgressBar, Skeleton, WidgetButtonBar, WidgetRenderer },
    inject: ['dHash'],
    props: { item: { required: true, type: Object }, activeSheet: { type: Boolean }, widget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array as PropType<IDataset[]>, required: true }, dashboardId: { type: String, required: true } },
    data() {
        return {
            loading: false,
            initialized: true,
            widgetData: [] as any,
            widgetEditorVisible: false,
            selectedWidgetId: '' as string
        }
    },
    mounted() {
        this.setEventListeners()
    },
    computed: {
        ...mapState(store, ['dashboards'])
    },
    methods: {
        // TODO
        ...mapActions(store, ['getDashboard']),
        async test() {
            const dashboardModel = this.getDashboard(this.dashboardId)
            const response = await getAssociativeSelections(dashboardModel, this.datasets, this.$http, this.dashboards)
            console.log('>>>>> RESPONSE: ', response)
        },
        setEventListeners() {
            emitter.on('interaction', async (event) => {
                /**
                 * ! this is just an example of a possible interaction.
                 * TODO: after getting the informations related to what the needed data will be, the dataProxyHelper should take care of getting the updated data.
                 */

                this.loading = true
                this.widgetData = await getData([{ event: event }])
                this.loading = false
            })
            emitter.on('openNewWidgetEditor', () => {
                this.openWidgetEditorDialog()
            })
        },
        async initializeWidget() {
            // this.widgetData = await getData([{ test: 'test' }])
            this.initialized = true
            this.loading = false
        },
        manageInteraction(e, item) {
            console.log('interaction', e, item)
            /**
             * TODO: The interaction manager will find in the widget model the interaction type, and provide the corrent event to be emitted with needed data
             */

            // @ts-ignore
            emitter.emit('interaction', { id: this.dHash, event: e })
        },
        toggleEditMode() {
            emitter.emit('openWidgetEditor', this.widget)
            // this.widgetEditorVisible = !this.widgetEditorVisible
        },
        openWidgetEditorDialog() {
            this.widgetEditorVisible = true
        },
        closeWidgetEditor() {
            this.widgetEditorVisible = false
            this.selectedWidgetId = ''
        }
    },
    updated() {
        if (!this.initialized && this.activeSheet) {
            this.$nextTick()
            this.initializeWidget()
        }
    }
})
</script>
<style lang="scss">
.editorEnter-enter-active,
.editorEnter-leave-active {
    transition: opacity 0.5s ease;
}

.editorEnter-enter-from,
.editorEnter-leave-to {
    opacity: 0;
}

.vue-resizable-handle {
    z-index: 9999;
}
</style>
