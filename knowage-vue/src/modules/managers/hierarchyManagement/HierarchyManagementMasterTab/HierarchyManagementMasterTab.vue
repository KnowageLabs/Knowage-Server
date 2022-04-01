<template>
    <div class="p-d-flex p-flex-row">
        <HierarchyManagementDimensionsCard
            :dimensions="dimensions"
            :selectedHierarchy="selectedHierarchy"
            :validityTreeDate="validityTreeDate"
            :hierarchyType="hierarchyType"
            @synchronized="onSynchronized"
            @dimensionSelected="setSelectedDimension"
            @loading="$emit('loading', $event)"
            @nodeMetadataChanged="onNodeMetadataChange"
            @dimensionMetadataChanged="onDimensionMetadataChange"
            @validityDateSelected="setValidityDate"
            @masterHierarchyCreated="onMasterHierarchyCreated"
        />
        <HierarchyManagementHierarchiesCard
            :selectedDimension="selectedDimension"
            :nodeMetadata="nodeMetadata"
            :validityDate="validityDate"
            :dimensionMetadata="dimensionMetadata"
            :synchronizationTrigger="synchronizationTrigger"
            :reloadHierarchiesTrigger="reloadHierarchiesTrigger"
            @loading="$emit('loading', $event)"
            @hierarchyTypeSelected="onHierarchyTypeSelected"
            @hierarchySelected="onHierarchySelected"
            @dateSelected="onValidityTreeDateSelected"
        />
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
            validityTreeDate: new Date() as Date | null,
            synchronizationTrigger: false,
            reloadHierarchiesTrigger: false
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
            this.validityDate = date
        },
        onDimensionMetadataChange(metadata: iDimensionMetadata | null) {
            this.dimensionMetadata = metadata
        },
        onHierarchyTypeSelected(hierarchyType: string) {
            this.hierarchyType = hierarchyType
        },
        onHierarchySelected(hierarchy: iHierarchy | null) {
            this.selectedHierarchy = hierarchy
        },
        onValidityTreeDateSelected(date: Date | null) {
            this.validityTreeDate = date
        },
        onSynchronized() {
            this.synchronizationTrigger = !this.synchronizationTrigger
        },
        onMasterHierarchyCreated() {
            this.reloadHierarchiesTrigger = !this.reloadHierarchiesTrigger
        }
    }
})
</script>
