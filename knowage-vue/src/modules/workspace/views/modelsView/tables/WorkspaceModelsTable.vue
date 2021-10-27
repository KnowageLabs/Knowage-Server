<template>
    <DataTable :value="items" id="models-table" class="p-datatable-sm kn-table" v-model:filters="filters" filterDisplay="menu" dataKey="name" :paginator="true" :rows="20" responsiveLayout="stack" breakpoint="960px">
        <template #empty>
            <Message class="p-m-2" severity="info" :closable="false" :style="workspaceModelsTableDescriptor.styles.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
        </template>

        <Column class="kn-truncated" v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true">
            <template #filter="{filterModel}"> <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText> </template
        ></Column>
        <Column :style="workspaceModelsTableDescriptor.iconColumn.style">
            <template #body="slotProps">
                <div class="p-d-flex p-flex-row">
                    <Button icon="fa fa-search" class="p-button-link" v-tooltip.left="$t('workspace.myModels.openInQBE')" @click.stop="openDatasetInQBE(slotProps.data)" />
                    <div v-if="tableMode === 'Federated'" class="p-d-flex p-flex-row">
                        <Button icon="pi pi-pencil" class="p-button-link" v-tooltip.left="$t('workspace.myModels.editDataset')" @click.stop="editDataset(slotProps.data)" />
                        <Button icon="fas fa-trash-alt" class="p-button-link" v-tooltip.left="$t('workspace.myModels.deleteDataset')" @click.stop="deleteDataset(slotProps.data)" />
                    </div>
                </div>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IBusinessModel, IFederatedDataset } from '../../../Workspace'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import workspaceModelsTableDescriptor from './WorkspaceModelsTableDescriptor.json'

export default defineComponent({
    name: 'workspace-models-table',
    components: { Column, DataTable },
    props: { tableMode: { type: String }, propItems: { type: Array } },
    data() {
        return {
            workspaceModelsTableDescriptor,
            items: [] as IBusinessModel[] | IFederatedDataset[],
            columns: [] as any[],
            filters: {
                name: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                description: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                label: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as any
        }
    },
    watch: {
        propItems() {
            this.loadTable()
        }
    },
    created() {
        this.loadTable()
    },
    methods: {
        loadTable() {
            this.loadItems()
            this.setMode()
        },
        loadItems() {
            this.items = this.propItems as IBusinessModel[] | IFederatedDataset[]
            // console.log('LOADED ITEMS: ', this.items)
        },
        setMode() {
            this.columns = this.tableMode === 'Business' ? this.workspaceModelsTableDescriptor.businessColumns : this.workspaceModelsTableDescriptor.federatedColumns
        },
        openDatasetInQBE(dataset: IBusinessModel | IFederatedDataset) {
            console.log('openDatasetInQBE clicked! ', dataset)
        },
        editDataset(dataset: IBusinessModel | IFederatedDataset) {
            console.log('editDataset clicked! ', dataset)
        },
        deleteDataset(dataset: IBusinessModel | IFederatedDataset) {
            console.log('deleteDataset clicked! ', dataset)
        }
    }
})
</script>
