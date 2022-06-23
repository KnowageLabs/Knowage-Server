<template>
    <DataTable :value="items" id="models-table" class="p-datatable-sm kn-table" v-model:filters="filters" filterDisplay="menu" dataKey="name" responsiveLayout="stack" breakpoint="960px">
        <template #empty>
            <Message class="p-m-2" severity="info" :closable="false" :style="workspaceModelsTableDescriptor.styles.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
        </template>

        <Column class="kn-truncated" v-for="col of workspaceModelsTableDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true">
            <template #filter="{ filterModel }"> <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText> </template
        ></Column>
        <Column :style="workspaceModelsTableDescriptor.iconColumn.style">
            <template #body="slotProps">
                <div class="p-d-flex p-flex-row p-jc-end">
                    <div v-if="slotProps.data.type === 'federatedDataset'" class="p-d-flex p-flex-row">
                        <Button icon="fas fa-ellipsis-v" class="p-button-link" @click="toggle($event, slotProps.data)" />
                        <Menu ref="menu" :model="menuItems" :popup="true" />
                    </div>
                    <Button icon="fas fa-info-circle" class="p-button-link" v-tooltip.left="$t('workspace.myModels.showInfo')" @click.stop="$emit('selected', slotProps.data)" :data-test="'info-button-' + slotProps.data.name" />
                    <Button icon="fas fa-search" class="p-button-link" v-tooltip.left="$t('workspace.myModels.openInQBE')" @click.stop="openDatasetInQBE(slotProps.data)" />
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
import Menu from 'primevue/menu'
import workspaceModelsTableDescriptor from './WorkspaceModelsTableDescriptor.json'

export default defineComponent({
    name: 'workspace-models-table',
    components: { Column, DataTable, Menu },
    props: { propItems: { type: Array } },
    emits: ['openDatasetInQBEClick', 'editDatasetClick', 'deleteDatasetClick', 'selected'],
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
                }
            } as any,
            menuItems: [] as any[],
            user: null as any
        }
    },
    watch: {
        propItems() {
            this.loadTable()
        }
    },
    created() {
        this.user = (this.store.state as any).user
        this.loadTable()
    },
    methods: {
        loadTable() {
            this.loadItems()
        },
        loadItems() {
            this.items = this.propItems as IBusinessModel[] | IFederatedDataset[]
        },
        canDeleteFederation(federation: IFederatedDataset) {
            return this.user.isSuperadmin || this.user.userId === federation.owner
        },
        openDatasetInQBE(dataset: IBusinessModel | IFederatedDataset) {
            this.$emit('openDatasetInQBEClick', dataset)
        },
        editDataset(dataset: IBusinessModel | IFederatedDataset) {
            this.$emit('editDatasetClick', dataset)
        },
        deleteDataset(dataset: IBusinessModel | IFederatedDataset) {
            this.$emit('deleteDatasetClick', dataset)
        },
        toggle(event: any, dataset: IFederatedDataset) {
            this.createMenuItems(dataset)
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems(dataset: IFederatedDataset) {
            this.menuItems = []
            this.menuItems.push({ icon: 'pi pi-pencil', label: this.$t('workspace.myModels.editDataset'), command: () => this.editDataset(dataset) })
            if (this.canDeleteFederation(dataset)) {
                this.menuItems.push({ icon: 'fas fa-trash-alt', label: this.$t('workspace.myModels.deleteDataset'), command: () => this.deleteDataset(dataset) })
            }
        }
    }
})
</script>
