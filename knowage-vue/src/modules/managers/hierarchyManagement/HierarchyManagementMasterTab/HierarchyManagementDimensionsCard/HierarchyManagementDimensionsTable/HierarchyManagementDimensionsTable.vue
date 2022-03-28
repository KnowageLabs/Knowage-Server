<template>
    <h1>IT WORKS</h1>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import hierarchyManagementDimensionsTableDescriptor from './HierarchyManagementDimensionsTableDescriptor.json'

export default defineComponent({
    name: 'hierarchy-management-dimensions-table',
    components: {},
    props: { dimensionData: { type: Object } },
    data() {
        return {
            hierarchyManagementDimensionsTableDescriptor
        }
    },
    watch: {
        dimensionData() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            console.log('LOADED DIMENSION DATA TABLE: ', this.dimensionData)
        }
    }
})
</script>
