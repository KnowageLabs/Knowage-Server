<template>
    <div class="p-col federation-listbox-container">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ mode === 'available' ? $t('workspace.federationDefinition.availableDatasets') : $t('workspace.federationDefinition.selectedDatasets') }}
            </template>
        </Toolbar>
        <Message class="p-m-4" severity="info" :closable="false" :style="workspaceFederationDatasetListDescriptor.styles.message">
            {{ mode === 'available' ? $t('workspace.federationDefinition.availableDatasetsMessage') : $t('workspace.federationDefinition.selectedDatasetsMessage') }}
        </Message>
        <Listbox class="kn-list listbox-container" :listStyle="workspaceFederationDatasetListDescriptor.styles.dsList" :options="dataset" :filter="true" optionLabel="name" @change="selectDataset($event.value)">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div class="kn-list-item p-d-flex p-flex-row">
                    <div class="kn-list-item-text">
                        <span>{{ slotProps.option.name }}</span>
                    </div>
                    <i v-if="mode === 'available'" class="fas fa-info-circle dataset-info-icon" @click.stop="$emit('showInfo', slotProps.option)"></i>
                </div>
            </template>
        </Listbox>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Listbox from 'primevue/listbox'
import Message from 'primevue/message'
import workspaceFederationDatasetListDescriptor from './WorkspaceFederationDatasetListDescriptor.json'

export default defineComponent({
    name: 'workspace-federation-dataset-list',
    components: {
        Listbox,
        Message
    },
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

<style lang="scss" scoped>
.federation-listbox-container {
    :deep(.p-card-body) {
        padding: 0;
        .p-card-content {
            padding: 0;
        }
    }
    .listbox-container {
        border: 1px solid $color-borders;
        border-top: none;
        border-radius: 0;
    }
}

.dataset-info-icon {
    margin-left: auto;
    color: #a7a7a7;
}
</style>
