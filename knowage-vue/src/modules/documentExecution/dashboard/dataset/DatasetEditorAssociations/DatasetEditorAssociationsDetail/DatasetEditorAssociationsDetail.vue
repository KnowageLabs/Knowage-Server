<template>
    <div id="associations-grid-container">
        <MasonryWall class="kn-flex p-mx-2" :items="selectedDatasetsProp" :column-width="300" :gap="5">
            <template #default="{ item, index }">
                <DataCard :datasetProp="item" :indexProp="index" />
            </template>
        </MasonryWall>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import dashStore from '../../../Dashboard.store'
import DataCard from './DatasetEditorAssociationsCard.vue'
import MasonryWall from '@yeger/vue-masonry-wall'

export default defineComponent({
    name: 'dataset-editor-associations-detail',
    components: { Card, Listbox, DataCard, MasonryWall },
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
    overflow: auto;
}
</style>
