<template>
    <div id="dataset-editor-list-card-container">
        <Card class="dataset-editor-list-card">
            <template #title>
                <Button label="Add Association" icon="pi pi-plus-circle" class="p-button-outlined p-mt-2 p-mr-2"></Button>
            </template>
            <template #content>
                <Listbox class="kn-list kn-list-no-border-right" :options="associations" :filter="true" :filterPlaceholder="$t('common.search')" optionLabel="label" filterMatchMode="contains" :filterFields="['label']" :emptyFilterMessage="$t('common.info.noDataFound')" @change="selectAssociation">
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" :style="associationListDescriptor.style.list.listItem">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option }}</span>
                            </div>
                            <div class="kn-list-item-buttons">
                                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteDatasetFromModel" />
                            </div>
                        </div>
                    </template>
                </Listbox>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import dashStore from '../../../Dashboard.store'
import associationListDescriptor from './DatasetEditorAssociationsListDescriptor.json'

export default defineComponent({
    name: 'dataset-editor-data-list',
    components: { Card, Listbox },
    props: { dashboardAssociationsProp: { required: true, type: Array as any } },
    emits: ['datasetSelected'],
    data() {
        return {
            associationListDescriptor,
            associations: []
        }
    },
    setup() {
        const dashboardStore = dashStore()
        return { dashboardStore }
    },
    created() {
        this.associations = this.dashboardAssociationsProp
    },
    methods: {
        selectAssociation(event) {
            console.log(event.value)
        }
    }
})
</script>

<style lang="scss">
.dataset-editor-list-card .p-card-title {
    display: flex;
    justify-content: end;
}
.dataset-editor-list-card .p-card-body,
.dataset-editor-list-card .p-card-content {
    padding: 0;
}
</style>
