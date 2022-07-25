<template>
    <div id="associations-grid-container">
        <DataCard v-for="(dataset, index) of selectedDatasetsProp" :key="index" :datasetProp="dataset" :indexProp="index" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import dashStore from '../../../Dashboard.store'
import DataCard from './DatasetEditorAssociationsCard.vue'

export default defineComponent({
    name: 'dataset-editor-associations-detail',
    components: { Card, Listbox, DataCard },
    props: { dashboardAssociationsProp: { required: true, type: Array as any }, selectedDatasetsProp: { required: true, type: Array } },
    emits: [],
    data() {
        return {
            associations: []
        }
    },
    setup() {
        const dashboardStore = dashStore()
        return { dashboardStore }
    },
    created() {},
    methods: {}
})
</script>

<style lang="scss" scoped>
#associations-grid-container {
    flex: 3;
    gap: 10px;
    display: flex;
}
</style>
