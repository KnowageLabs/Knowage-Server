<template>
    <div class="p-d-flex p-flex-row">
        <HierarchyManagementDimensionsCard class="kn-flex" :dimensions="dimensions" @dimensionSelected="setSelectedDimension" @loading="$emit('loading', $event)" @dimensionMetadataChanged="onNodeMetadataChange"></HierarchyManagementDimensionsCard>
        <HierarchyManagementHierarchiesCard class="kn-flex" :selectedDimension="selectedDimension" :nodeMetadata="nodeMetadata" @loading="$emit('loading', $event)"></HierarchyManagementHierarchiesCard>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iNodeMetadata } from '../HierarchyManagement'
import HierarchyManagementDimensionsCard from './HierarchyManagementDimensionsCard/HierarchyManagementDimensionsCard.vue'
import HierarchyManagementHierarchiesCard from './HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesCard.vue'

export default defineComponent({
    name: 'hierarchy-management-master-tab',
    components: { HierarchyManagementDimensionsCard, HierarchyManagementHierarchiesCard },
    props: { dimensions: { type: Array as PropType<iDimension[]> } },
    data() {
        return {
            selectedDimension: null as iDimension | null,
            nodeMetadata: null as iNodeMetadata | null
        }
    },
    async created() {},
    methods: {
        setSelectedDimension(dimension: iDimension | null) {
            this.selectedDimension = dimension
        },
        onNodeMetadataChange(metadata: iNodeMetadata | null) {
            this.nodeMetadata = metadata
        }
    }
})
</script>
