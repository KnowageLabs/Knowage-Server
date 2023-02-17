<template>
    <div class="p-d-flex p-flex-column kn-flex">
        <InlineMessage v-if="selectedAssociationProp.fields.length == 0" class="p-mt-3 p-mr-3" severity="error"> {{ $t('dashboard.datasetEditor.emptyAssociationError') }} </InlineMessage>

        <div class="p-d-flex p-flex-column kn-flex kn-overflow p-my-3 p-mr-3 dashboard-scrollbar">
            <MasonryWall class="kn-flex" :items="selectedDatasetsProp" :column-width="300" :gap="10">
                <template #default="{ item, index }">
                    <DataCard :dataset-prop="item" :index-prop="index" :selected-association-prop="selectedAssociationProp" @fieldSelected="$emit('fieldSelected', $event)" @fieldUnselected="$emit('fieldUnselected', $event)" />
                </template>
            </MasonryWall>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import dashStore from '../../../Dashboard.store'
import DataCard from './DatasetEditorAssociationsCard.vue'
import MasonryWall from '@yeger/vue-masonry-wall'
import InlineMessage from 'primevue/inlinemessage'

export default defineComponent({
    name: 'dataset-editor-associations-detail',
    components: { DataCard, MasonryWall, InlineMessage },
    props: { dashboardAssociationsProp: { required: true, type: Array as any }, selectedDatasetsProp: { required: true, type: Array }, selectedAssociationProp: { required: true, type: Object } },
    emits: ['fieldSelected'],
    setup() {
        const dashboardStore = dashStore()
        return { dashboardStore }
    },
    watch: {}
})
</script>
