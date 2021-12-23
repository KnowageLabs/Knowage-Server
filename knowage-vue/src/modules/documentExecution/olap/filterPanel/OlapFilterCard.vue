<template>
    <div id="filterCard" class="p-d-flex p-flex-row" v-for="(filter, index) in filterCardList" :key="index">
        <div :id="'filter-' + filter.name" :ref="'filter-' + filter.name" :style="panelDescriptor.style.filterCard" draggable="true" @dragstart="onDragStart($event, filter, 'filter-' + filter.name)" @dragend="removeDragClass('filter-' + filter.name)">
            <Button v-if="filter.hierarchies.length > 1" icon="fas fa-sitemap" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMultiHierarchy', filter)" />
            <span class="p-ml-1"> {{ filter.caption }} </span>
            <Button icon="fas fa-filter" class="p-button-text p-button-rounded p-button-plain p-ml-auto" />
            <!-- TODO: Tooltip for selected filters when hovering on icon and knowage magenta button color if filter is selected -->
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import panelDescriptor from './OlapFilterPanelDescriptor.json'

export default defineComponent({
    components: {},
    props: { filterCardList: { type: Array, required: true } },
    emits: ['dragging', 'dragend', 'showMultiHierarchy'],
    data() {
        return {
            panelDescriptor
        }
    },
    created() {},
    methods: {
        onDragStart(event, filter, filterId) {
            console.log(event, filter)
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
        }
    }
})
</script>
<style lang="scss" scoped>
.filter-dragging {
    background-color: #bbd6ed !important;
}
</style>
