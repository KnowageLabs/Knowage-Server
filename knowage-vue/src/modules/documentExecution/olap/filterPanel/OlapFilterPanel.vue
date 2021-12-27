<template>
    <div id="filterPanel" class="p-d-flex filterPanel p-ai-center p-flex-wrap" @drop="onDrop($event)" @dragover.prevent @dragenter="displayDropzone" @dragleave="hideDropzone">
        <div v-if="filterCardList?.length == 0" class="p-d-flex p-flex-row kn-flex p-jc-center">
            <InlineMessage class="kn-flex p-m-1" :style="panelDescriptor.style.noFilters" severity="info" closable="false">{{ $t('documentExecution.olap.filterPanel.filterPanelEmpty') }}</InlineMessage>
        </div>
        <FilterCard v-else :filterCardList="filterCardList" @showMultiHierarchy="emitMultiHierarchy" />
        <div ref="axisDropzone" class="kn-flex kn-truncated p-mr-1" :style="panelDescriptor.style.filterAxisDropzone">{{ $t('documentExecution.olap.filterPanel.drop') }}</div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iOlapFilter, iOlap } from '@/modules/documentExecution/olap/Olap'
import panelDescriptor from './OlapFilterPanelDescriptor.json'
import InlineMessage from 'primevue/inlinemessage'
import FilterCard from './OlapFilterCard.vue'

export default defineComponent({
    components: { InlineMessage, FilterCard },
    props: { olapProp: { type: Object as PropType<iOlap | null>, required: true } },
    emits: ['putFilterOnAxis', 'showMultiHierarchy'],
    data() {
        return {
            panelDescriptor,
            filterCardList: [] as iOlapFilter[]
        }
    },
    watch: {
        olapProp() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },

    methods: {
        loadData() {
            this.filterCardList = this.olapProp?.filters as iOlapFilter[]
        },
        displayDropzone() {
            // @ts-ignore
            this.$refs.axisDropzone.classList.add('display-axis-dropzone')
        },
        hideDropzone() {
            // @ts-ignore
            this.$refs.axisDropzone.classList.remove('display-axis-dropzone')
        },
        onDrop(event) {
            // @ts-ignore
            this.$refs.axisDropzone.classList.remove('display-axis-dropzone')
            var data = JSON.parse(event.dataTransfer.getData('text/plain'))

            var topLength = this.olapProp?.columns.length
            var leftLength = this.olapProp?.rows.length
            var fromAxis
            if (data != null) {
                fromAxis = data.axis
                if (data.measure) {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.warning'), msg: this.$t('documentExecution.olap.filterToolbar.noMeasure') })
                    return null
                }
                if (fromAxis != -1) {
                    if ((fromAxis === 0 && topLength == 1) || (fromAxis === 1 && leftLength == 1)) {
                        this.$store.commit('setInfo', { title: this.$t('common.toast.warning'), msg: this.$t('documentExecution.olap.filterToolbar.dragEmptyWarning') })
                    } else {
                        data.positionInAxis = this.filterCardList.length
                        data.axis = -1
                        this.$emit('putFilterOnAxis', fromAxis, data)
                    }
                }
            }
            //TODO: Ne znam cemu sluzi ostaviti za kasnije pa pogledati....FilterPanel.js linija 164 clearLoadedData
            // data != null ? this.clearLoadedData(data.uniqueName) : ''
        },
        emitMultiHierarchy(filter) {
            this.$emit('showMultiHierarchy', filter)
        }
    }
})
</script>
<style lang="scss" scoped>
#filterPanel {
    min-height: 45px;
}
.display-axis-dropzone {
    display: flex !important;
}
</style>
