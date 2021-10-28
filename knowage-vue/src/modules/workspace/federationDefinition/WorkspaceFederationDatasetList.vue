<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ mode === 'available' ? $t('workspace.federationDefinition.availableDatasets') : $t('workspace.federationDefinition.selectedDatasets') }}
                </template>
            </Toolbar>
        </template>

        <template #content>
            <Message class="p-m-4" severity="info" :closable="false" :style="workspaceFederationDatasetListDescriptor.styles.message">
                {{ mode === 'available' ? $t('workspace.federationDefinition.availableDatasetsMessage') : $t('workspace.federationDefinition.selectedDatasetsMessage') }}
            </Message>

            <Listbox class="federation-dataset-list" :options="dataset" :filter="true" optionLabel="name" @change="selectDataset($event.value)">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item p-d-flex p-flex-row">
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.name }}</span>
                        </div>
                        <i v-if="mode === 'available'" class="fa fa-info-circle dataset-info-icon" @click.stop="$emit('showInfo', slotProps.option)"></i>
                    </div>
                </template>
            </Listbox>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import Message from 'primevue/message'
import workspaceFederationDatasetListDescriptor from './WorkspaceFederationDatasetListDescriptor.json'

export default defineComponent({
    name: 'workspace-federation-dataset-list',
    components: { Card, Listbox, Message },
    props: { mode: { type: String }, propDatasets: { type: Array } },
    emits: ['showInfo', 'datasetSelected'],
    data() {
        return {
            workspaceFederationDatasetListDescriptor,
            dataset: [] as any[]
        }
    },
    watch: {
        propDatasets() {
            this.loadDatasets()
        }
    },
    created() {
        this.loadDatasets()
    },
    methods: {
        loadDatasets() {
            this.dataset = this.propDatasets as any[]
        },
        selectDataset(dataset: any) {
            this.$emit('datasetSelected', { dataset: dataset, mode: this.mode })
        }
    }
})
</script>

<style lang="scss">
.federation-dataset-list {
    border: none;
    height: 70vh;
    overflow-y: scroll;
}

.dataset-info-icon {
    margin-left: auto;
}
</style>
