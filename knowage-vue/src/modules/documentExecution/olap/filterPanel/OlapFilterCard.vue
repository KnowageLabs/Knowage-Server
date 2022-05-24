<template>
    <div id="filterCard" class="p-d-flex p-flex-row" v-for="(filter, index) in filterCardList" :key="index" v-tooltip="{ value: $t('documentExecution.olap.filterPanel.activeLevels') + ': ' + getNumberOfActiveLevels(filter), disabled: getNumberOfActiveLevels(filter) === 0 }">
        <div :id="'filter-' + filter.name" :ref="'filter-' + filter.name" :style="panelDescriptor.style.filterCard" draggable="true" @dragstart="onDragStart($event, filter, 'filter-' + filter.name)" @dragend="removeDragClass('filter-' + filter.name)">
            <Button v-if="filter.hierarchies.length > 1" icon="fas fa-sitemap" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMultiHierarchy', filter)" />
            <span class="p-ml-1"> {{ filter.caption }} </span>
            <Button
                icon="fas fa-filter"
                :class="{ 'olap-active-filter-icon': filterIsActive(filter) }"
                class="p-button-text p-button-rounded p-button-plain p-ml-auto"
                v-tooltip="{ value: getSlicersTooltip(filter), disabled: !filter || !filter.hierarchies[0].slicers || filter.hierarchies[0].slicers.length === 0 }"
                @click="openFilterDialog(filter)"
            />
            <!-- TODO Change Request for next sprint: Tooltip for selected filters when hovering on icon and knowage magenta button color if filter is selected -->
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import panelDescriptor from './OlapFilterPanelDescriptor.json'

export default defineComponent({
    components: {},
    props: { filterCardList: { type: Array, required: true }, olapDesigner: { type: Object } },
    emits: ['dragging', 'dragend', 'showMultiHierarchy', 'openFilterDialog'],
    data() {
        return {
            panelDescriptor
        }
    },
    created() {},
    methods: {
        onDragStart(event, filter, filterId) {
            event.dataTransfer.setData('text', JSON.stringify(filter))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
            // @ts-ignore
            this.$refs[`${filterId}`].classList.add('filter-dragging')
            this.$emit('dragging')
        },
        removeDragClass(filterId) {
            // @ts-ignore
            this.$refs[`${filterId}`].classList.remove('filter-dragging')
            this.$emit('dragend')
        },
        openFilterDialog(filter: any) {
            this.$emit('openFilterDialog', { filter: filter, type: 'slicer' })
        },
        filterIsActive(filter: any) {
            let isActive = false
            for (let i = 0; i < filter.hierarchies.length; i++) {
                if (filter.hierarchies[i].slicers && filter.hierarchies[i].slicers.length > 0) {
                    isActive = true
                    break
                }
            }
            return isActive
        },
        getNumberOfActiveLevels(filter: any) {
            const dynamicSlicers = this.olapDesigner?.template?.wrappedObject.olap.DYNAMIC_SLICER
            if (!dynamicSlicers) return 0
            let numberOfActiveLevels = 0
            for (let i = 0; i < dynamicSlicers.length; i++) {
                if (dynamicSlicers[i].HIERARCHY === filter.uniqueName) numberOfActiveLevels++
            }
            return numberOfActiveLevels
        },
        getSlicersTooltip(filter: any) {
            let values = ''
            if (!filter || !filter.hierarchies[0] || !filter.hierarchies[0].slicers) return values

            for (let i = 0; i < filter.hierarchies[0].slicers.length; i++) {
                values += filter.hierarchies[0].slicers[i].name
                values += i === filter.hierarchies[0].slicers.length - 1 ? ' ' : ', '
            }

            return values
        }
    }
})
</script>
<style lang="scss" scoped>
.filter-dragging {
    background-color: #bbd6ed !important;
}

.olap-active-filter-icon {
    color: red !important;
}
</style>
