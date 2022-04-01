<template>
    <div class="p-d-flex p-flex-row">
        <HierarchyManagementSourceCard
            class="kn-flex"
            :dimensions="dimensions"
            @loading="$emit('loading', $event)"
            @validityDateSelected="setValidityDate"
            @dimensionSelected="setSelectedDimension"
            @dimensionMetadataChanged="onDimensionMetadataChange"
            @nodeMetadataChanged="onNodeMetadataChange"
            @hierarchyTypeSelected="onHierarchyTypeSelected"
            @hierarchySelected="onHierarchySelected"
        ></HierarchyManagementSourceCard>
        <HierarchyManagementTargetCard class="kn-flex" @loading="$emit('loading', $event)"></HierarchyManagementTargetCard>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iHierarchy, iDimensionMetadata, iNodeMetadata } from '../HierarchyManagement'
import HierarchyManagementSourceCard from './HierarchyManagementSourceCard/HierarchyManagementSourceCard.vue'
import HierarchyManagementTargetCard from './HierarchyManagementTargetCard/HierarchyManagementTargetCard.vue'

export default defineComponent({
    name: 'hierarchy-management-technical-tab',
    components: { HierarchyManagementSourceCard, HierarchyManagementTargetCard },
    props: { dimensions: { type: Array as PropType<iDimension[]> } },
    data() {
        return {
            validityDate: new Date(),
            selectedDimension: null as iDimension | null,
            dimensionMetadata: null as iDimensionMetadata | null,
            nodeMetadata: null as iNodeMetadata | null,
            hierarchyType: '' as string,
            selectedHierarchy: null as iHierarchy | null
        }
    },
    created() {},
    methods: {
        setValidityDate(date: Date) {
            this.validityDate = date
        },
        setSelectedDimension(dimension: iDimension | null) {
            this.selectedDimension = dimension
        },
        onDimensionMetadataChange(metadata: iDimensionMetadata | null) {
            this.dimensionMetadata = metadata
        },
        onNodeMetadataChange(metadata: iNodeMetadata | null) {
            this.nodeMetadata = metadata
        },
        onHierarchyTypeSelected(hierarchyType: string) {
            this.hierarchyType = hierarchyType
        },
        onHierarchySelected(hierarchy: iHierarchy | null) {
            this.selectedHierarchy = hierarchy
        }
    }
})
</script>
<style lang="scss">
.hierarchy-scrollable-card {
    height: calc(100vh - 55px);
    flex: 1 1 0;
    .p-card-body {
        flex: 1;
        overflow: auto;
    }
}
</style>
