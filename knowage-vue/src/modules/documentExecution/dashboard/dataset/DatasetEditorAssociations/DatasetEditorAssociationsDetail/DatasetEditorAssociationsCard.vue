<template>
    <div class="associations-meta-card" :style="descriptor.style.metaCard.card">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start> {{ datasetProp.name }} - Index: {{ indexProp }} </template>
        </Toolbar>
        <Listbox class="kn-list kn-list-border-all" :options="datasetProp.metadata.fieldsMeta" @change="selectAssociation">
            <template #option="slotProps">
                <div class="kn-list-item" :style="descriptor.style.metaCard.listItem">
                    <div class="kn-list-item-text">
                        <span>{{ slotProps.option.name }}</span>
                    </div>
                    <div class="kn-list-item-buttons">
                        <b>{{ descriptor.datasetTypes[slotProps.option.type] }}</b>
                    </div>
                </div>
            </template>
        </Listbox>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import dashStore from '../../../Dashboard.store'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import descriptor from './DatasetEditorAssociationsDescriptor.json'

export default defineComponent({
    name: 'dataset-editor-associations-detail',
    components: { Card, Listbox },
    props: { datasetProp: { required: true, type: Object as any }, indexProp: { required: true, type: Number } },
    emits: [],
    data() {
        return {
            descriptor
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

<style lang="scss">
.associations-meta-card .p-card-title {
    display: flex;
    justify-content: end;
}
.associations-meta-card .p-card-body,
.associations-meta-card .p-card-content {
    padding: 0;
}
</style>
