<template>
    <grid-item :key="item.i" :x="item.x" :y="item.y" :w="item.w" :h="item.h" :i="item.i" drag-allow-from=".drag-handle">
        <div v-if="initialized" class="drag-handle"></div>
        <ProgressBar mode="indeterminate" v-if="loading" />
        <Skeleton shape="rectangle" v-if="!initialized" height="100%" border-radius="0" />
        <WidgetRenderer :widget="widget" :data="widgetData" v-if="initialized" @interaction="manageInteraction"></WidgetRenderer>
    </grid-item>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget behaviour related to data and interactions, not related to view elements.
 */
import { defineComponent } from 'vue'
import { mapState } from 'vuex'
import WidgetRenderer from './WidgetRenderer.vue'
import { getData } from '../DataProxyHelper'
import Skeleton from 'primevue/skeleton'
import ProgressBar from 'primevue/progressbar'
import mitt from 'mitt'
export const emitter = mitt()

export default defineComponent({
    name: 'widget-manager',
    components: { ProgressBar, Skeleton, WidgetRenderer },
    inject: ['dHash'],
    props: {
        item: {
            required: true,
            type: Object
        },
        activeSheet: {
            type: Boolean
        },
        widget: {
            required: true,
            type: Object
        }
    },
    data() {
        return {
            loading: true,
            initialized: false,
            widgetData: [] as any
        }
    },
    mounted() {
        emitter.on('interaction', async (event) => {
            /**
             * ! this is just an example of a possible interaction.
             * TODO: after getting the informations related to what the needed data will be, the dataProxyHelper should take care of getting the updated data.
             */

            this.loading = true
            this.widgetData = await getData([{ event: event }])
            this.loading = false
        })
    },
    computed: {
        ...mapState({
            dashboard: (state: any) => state.dashboard.dashboards
        })
    },
    methods: {
        async initializeWidget() {
            this.widgetData = await getData([{ test: 'test' }])
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
<style lang="scss"></style>
