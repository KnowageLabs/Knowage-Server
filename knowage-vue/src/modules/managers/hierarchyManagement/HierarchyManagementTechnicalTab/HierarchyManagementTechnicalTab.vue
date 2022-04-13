<template>
    <div class="p-d-flex p-flex-row">
        <HierarchyManagementSourceCard
            :dimensions="dimensions"
            :optionsDate="optionsDate"
            @loading="$emit('loading', $event)"
            @validityDateSelected="setValidityDate"
            @dimensionSelected="setSelectedDimension"
            @nodeMetadataChanged="onNodeMetadataChange"
            @hierarchyTypeSelected="onHierarchyTypeSelected"
            @hierarchySelected="onHierarchySelected"
        ></HierarchyManagementSourceCard>
        <HierarchyManagementTargetCard :selectedDimension="selectedDimension" :validityDate="validityDate" :nodeMetadata="nodeMetadata" :selectedSourceHierarchy="selectedHierarchy" @loading="$emit('loading', $event)" @optionsDateSelected="setOptionsDate"></HierarchyManagementTargetCard>
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
            selectedHierarchy: null as iHierarchy | null,
            optionsDate: new Date()
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
        onNodeMetadataChange(metadata: iNodeMetadata | null) {
            this.nodeMetadata = metadata
        },
        onHierarchyTypeSelected(hierarchyType: string) {
            this.hierarchyType = hierarchyType
        },
        onHierarchySelected(hierarchy: iHierarchy | null) {
            this.selectedHierarchy = hierarchy
        },
        setOptionsDate(date: Date) {
            this.optionsDate = date
        }
    }
})
</script>
