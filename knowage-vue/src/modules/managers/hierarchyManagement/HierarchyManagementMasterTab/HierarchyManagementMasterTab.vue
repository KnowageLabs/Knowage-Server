<template>
    <div class="p-d-flex p-flex-row">
        <HierarchyManagementDimensionsCard class="kn-flex" :dimensions="dimensions"></HierarchyManagementDimensionsCard>
        <HierarchyManagementHierarchiesCard class="kn-flex"></HierarchyManagementHierarchiesCard>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension } from '../HierarchyManagement'
import HierarchyManagementDimensionsCard from './HierarchyManagementDimensionsCard/HierarchyManagementDimensionsCard.vue'
import HierarchyManagementHierarchiesCard from './HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesCard.vue'

export default defineComponent({
    name: 'hierarchy-management-master-tab',
    components: { HierarchyManagementDimensionsCard, HierarchyManagementHierarchiesCard },
    props: { dimensions: { type: Array as PropType<iDimension[]> } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
