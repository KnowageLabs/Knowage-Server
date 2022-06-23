<template>
    <div id="filterPanelContainer" ref="filterPanelContainer" :style="panelDescriptor.style.filterPanelContainer">
        <div id="filterPanel" ref="filterPanel" class="p-d-flex filterPanel p-ai-center" :style="panelDescriptor.style.filterPanel" @drop="onDrop($event)" @dragover.prevent @dragenter="displayDropzone">
            <Button v-if="scrollContainerWidth < scrollContentWidth" icon="fas fa-arrow-circle-left" class="p-button-text p-button-rounded p-button-plain p-ml-1" @click="scrollLeft" />
            <div ref="filterItemsContainer" class="p-d-flex p-ai-center kn-flex" :style="panelDescriptor.style.containerScroll" @dragover.prevent @dragenter.prevent @dragleave="hideDropzone">
                <div v-if="filterCardList?.length == 0" class="p-d-flex p-flex-row p-jc-center kn-flex">
                    <InlineMessage class="kn-flex p-m-1" :style="panelDescriptor.style.noFilters" severity="info" closable="false">{{ $t('documentExecution.olap.filterPanel.filterPanelEmpty') }}</InlineMessage>
                </div>
                <FilterCard v-else :filterCardList="filterCardList" :olapDesigner="olapDesigner" @showMultiHierarchy="emitMultiHierarchy" @openFilterDialog="$emit('openFilterDialog', $event)" />
                <div ref="axisDropzone" class="kn-flex kn-truncated p-mr-1" :style="panelDescriptor.style.filterAxisDropzone">{{ $t('documentExecution.olap.filterPanel.drop') }}</div>
            </div>
            <Button v-if="scrollContainerWidth < scrollContentWidth" icon="fas fa-arrow-circle-right" class="p-button-text p-button-rounded p-button-plain p-mr-1" @click="scrollRight" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iOlapFilter, iOlap } from '@/modules/documentExecution/olap/Olap'
import panelDescriptor from './OlapFilterPanelDescriptor.json'
import InlineMessage from 'primevue/inlinemessage'
import FilterCard from './OlapFilterCard.vue'
import mainStore from '../../../../App.store'

export default defineComponent({
    components: { InlineMessage, FilterCard },
    props: { olapProp: { type: Object as PropType<iOlap | null>, required: true }, olapDesigner: { type: Object } },
    emits: ['putFilterOnAxis', 'showMultiHierarchy', 'openFilterDialog'],
    data() {
        return {
            panelDescriptor,
            filterCardList: [] as iOlapFilter[],
            scrollContainerWidth: 0,
            scrollContentWidth: 0
        }
    },
    watch: {
        olapProp() {
            this.loadData()
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.filterCardList = this.olapProp?.filters as iOlapFilter[]
            window.addEventListener('resize', this.assignScrollValues)
            this.assignScrollValues()
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
                    this.store.setInfo({ title: this.$t('common.toast.warning'), msg: this.$t('documentExecution.olap.filterToolbar.noMeasure') })
                    return null
                }
                if (fromAxis != -1) {
                    if ((fromAxis === 0 && topLength == 1) || (fromAxis === 1 && leftLength == 1)) {
                        this.store.setInfo({ title: this.$t('common.toast.warning'), msg: this.$t('documentExecution.olap.filterToolbar.dragEmptyWarning') })
                    } else {
                        data.positionInAxis = this.filterCardList.length
                        data.axis = -1
                        this.$emit('putFilterOnAxis', fromAxis, data)
                    }
                }
            }
        },
        emitMultiHierarchy(filter) {
            this.$emit('showMultiHierarchy', filter)
        },
        scrollLeft() {
            // @ts-ignore
            this.$refs.filterItemsContainer.scrollLeft -= 50
        },
        scrollRight() {
            // @ts-ignore
            this.$refs.filterItemsContainer.scrollLeft += 50
        },
        assignScrollValues() {
            // @ts-ignore
            this.scrollContainerWidth = this.$refs?.filterPanelContainer?.clientWidth
            // @ts-ignore
            this.scrollContentWidth = this.$refs?.filterItemsContainer?.scrollWidth
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
