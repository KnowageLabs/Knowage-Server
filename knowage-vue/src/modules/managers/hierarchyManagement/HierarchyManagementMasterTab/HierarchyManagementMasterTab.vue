<template>
    <div class="p-d-flex p-flex-row">
        <HierarchyManagementDimensionsCard
            class="kn-flex"
            :dimensions="dimensions"
            :selectedHierarchy="selectedHierarchy"
            :validityTreeDate="validityTreeDate"
            @dimensionSelected="setSelectedDimension"
            @loading="$emit('loading', $event)"
            @nodeMetadataChanged="onNodeMetadataChange"
            @dimensionMetadataChanged="onDimensionMetadataChange"
            @validityDateSelected="setValidityDate"
        ></HierarchyManagementDimensionsCard>
        <HierarchyManagementHierarchiesCard
            class="kn-flex"
            :selectedDimension="selectedDimension"
            :nodeMetadata="nodeMetadata"
            :validityDate="validityDate"
            :dimensionMetadata="dimensionMetadata"
            @loading="$emit('loading', $event)"
            @hierarchyTypeSelected="onHierarchyTypeSelected"
            @hierarchySelected="onHierarchySelected"
            @dateSelected="onValidityTreeDateSelected"
        ></HierarchyManagementHierarchiesCard>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iNodeMetadata, iDimensionMetadata, iHierarchy } from '../HierarchyManagement'
import HierarchyManagementDimensionsCard from './HierarchyManagementDimensionsCard/HierarchyManagementDimensionsCard.vue'
import HierarchyManagementHierarchiesCard from './HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesCard.vue'

export default defineComponent({
    name: 'hierarchy-management-master-tab',
    components: { HierarchyManagementDimensionsCard, HierarchyManagementHierarchiesCard },
    props: { dimensions: { type: Array as PropType<iDimension[]> } },
    data() {
        return {
            selectedDimension: null as iDimension | null,
            nodeMetadata: null as iNodeMetadata | null,
            validityDate: new Date(),
            dimensionMetadata: null as iDimensionMetadata | null,
            hierarchyType: '' as string,
            selectedHierarchy: null as iHierarchy | null,
            validityTreeDate: null as Date | null
        }
    },
    async created() {},
    methods: {
        setSelectedDimension(dimension: iDimension | null) {
            this.selectedDimension = dimension
        },
        onNodeMetadataChange(metadata: iNodeMetadata | null) {
            this.nodeMetadata = metadata
        },
        setValidityDate(date: Date) {
            console.log('SET VALIDITY DATE: ', date)
            this.validityDate = date
        },
        onDimensionMetadataChange(metadata: iDimensionMetadata | null) {
            this.dimensionMetadata = metadata
        },
        onHierarchyTypeSelected(hierarchyType: string) {
            this.hierarchyType = hierarchyType
            console.log('ON HIER TYPE SELECT: ', this.hierarchyType)
        },
        onHierarchySelected(hierarchy: iHierarchy | null) {
            this.selectedHierarchy = hierarchy
            console.log('ON HIER SELECT: ', this.selectedHierarchy)
        },
        onValidityTreeDateSelected(date: Date | null) {
            this.validityTreeDate = date
            console.log('ON DATE SELECT: ', this.validityTreeDate)
        }
    }
})
</script>
