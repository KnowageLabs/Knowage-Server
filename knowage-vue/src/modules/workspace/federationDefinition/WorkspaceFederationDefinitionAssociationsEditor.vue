<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('workspace.federationDefinition.associationsEditor') }}
                </template>
            </Toolbar>
        </template>

        <template #content>
            <div class="p-d-flex kn-flex" style="min-width:0;overflow: hidden;">
                <div class="p-d-flex p-flex-row kn-flex overflow">
                    <WorkspaceFederationDefinitionMetafieldsList
                        style="min-width: 250px"
                        v-for="dataset in datasets"
                        class="metafield-select-list p-m-2"
                        :key="dataset.id"
                        :propDataset="dataset"
                        :selectedMetafields="selectedMetafields"
                        :resetSelectedMetafield="resetSelectedMetafield"
                    ></WorkspaceFederationDefinitionMetafieldsList>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import WorkspaceFederationDefinitionMetafieldsList from './WorkspaceFederationDefinitionMetafieldsList.vue'

export default defineComponent({
    name: 'workspace-federation-definition-associations-editor',
    components: { Card, WorkspaceFederationDefinitionMetafieldsList },
    props: { selectedDatasets: { type: Array }, selectedMetafields: { type: Array }, resetSelectedMetafield: { type: Boolean } },
    data() {
        return {
            datasets: [] as any[]
        }
    },
    watch: {
        selectedDatasets() {
            this.loadDatasets()
        }
    },
    created() {
        this.loadDatasets()
    },
    methods: {
        loadDatasets() {
            this.datasets = this.selectedDatasets as any[]
        }
    }
})
</script>

<style lang="scss" scoped>
.metafield-select-list {
    width: 250px;
}
</style>
