<template>
    <div class="associations-meta-card">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start> {{ datasetProp.name }} </template>
        </Toolbar>
        <Listbox class="kn-list kn-list-border-all" v-model="selectedField" :options="datasetProp.metadata.fieldsMeta" @change="selectField">
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
    props: { datasetProp: { required: true, type: Object as any }, indexProp: { required: true, type: Number }, selectedAssociationProp: { required: true, type: Object as any } },
    emits: ['fieldSelected', 'fieldUnselected'],
    data() {
        return {
            descriptor,
            selectedField: null as any,
            associationField: null as any
        }
    },
    watch: {
        selectedAssociationProp() {
            if (this.selectedAssociationProp) {
                this.setSelectedAssociatonField()
            } else this.resetfieldSelected()
        }
    },
    setup() {
        const dashboardStore = dashStore()
        return { dashboardStore }
    },
    created() {
        if (this.selectedAssociationProp) {
            this.setSelectedAssociatonField()
        } else this.resetfieldSelected()
    },
    methods: {
        setSelectedAssociatonField() {
            // console.log('setSelectedAssociatonField - associationsCard.vue ----------------')
            this.selectedField = this.datasetProp.metadata.fieldsMeta.find((datasetField) => {
                return this.selectedAssociationProp.fields.some((associationField) => this.datasetProp.id.dsId === associationField.dataset && datasetField.alias === associationField.column)
            })
        },
        resetfieldSelected() {
            this.selectedField = null as any
        },
        selectField(event) {
            event.value ? this.$emit('fieldSelected', { column: event.value.alias, dataset: this.datasetProp.id.dsId }) : this.$emit('fieldUnselected', this.datasetProp.id.dsId)
        }
    }
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
