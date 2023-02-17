<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('workspace.federationDefinition.associationsEditor') }}
                </template>
            </Toolbar>
        </template>

        <template #content>
            <div class="p-d-flex kn-flex" :style="workspaceFederationDatasetListDescriptor.styles.metaContainer">
                <div class="p-d-flex p-flex-row kn-flex kn-overflow">
                    <WorkspaceFederationDefinitionMetafieldsList
                        v-for="dataset in datasets"
                        :key="dataset.id"
                        :style="workspaceFederationDatasetListDescriptor.styles.metaList"
                        class="metafield-select-list p-mx-2 p-mb-2"
                        :prop-dataset="dataset"
                        :selected-metafields="selectedMetafields"
                        :reset-selected-metafield="resetSelectedMetafield"
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
    import workspaceFederationDatasetListDescriptor from './WorkspaceFederationDatasetListDescriptor.json'

    export default defineComponent({
        name: 'workspace-federation-definition-associations-editor',
        components: { Card, WorkspaceFederationDefinitionMetafieldsList },
        props: { selectedDatasets: { type: Array }, selectedMetafields: { type: Array }, resetSelectedMetafield: { type: Boolean } },
        data() {
            return {
                datasets: [] as any[],
                workspaceFederationDatasetListDescriptor
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
