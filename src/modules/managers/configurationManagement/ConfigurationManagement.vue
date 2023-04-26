<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('managers.configurationManagement.title') }}
            </template>
            <template #end>
                <KnFabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm()"></KnFabButton>
            </template>
        </Toolbar>
        <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
        <div class="kn-page-content p-grid p-m-0">
            <div v-if="!loading" class="p-col">
                <DataTable
                    v-model:filters="filters"
                    v-model:selection="selectedConfiguration"
                    :value="configurations"
                    :paginator="true"
                    :loading="loading"
                    :rows="20"
                    class="p-datatable-sm kn-table"
                    data-key="id"
                    filter-display="menu"
                    :global-filter-fields="configurationManagementDescriptor.globalFilterFields"
                    :rows-per-page-options="[10, 15, 20]"
                    responsive-layout="stack"
                    breakpoint="960px"
                    :current-page-report-template="
                        $t('common.table.footer.paginated', {
                            first: '{first}',
                            last: '{last}',
                            totalRecords: '{totalRecords}'
                        })
                    "
                    data-test="configurations-table"
                    selection-mode="single"
                    @rowSelect="showForm"
                >
                    <template #header>
                        <div class="table-header">
                            <span class="p-input-icon-left">
                                <i class="pi pi-search" />
                                <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                            </span>
                        </div>
                    </template>
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
                    </template>
                    <template v-if="loading" #loading>
                        {{ $t('common.info.dataLoading') }}
                    </template>

                    <Column v-for="col of columns" :key="col.field" :field="col.field" :header="$t(col.header)" :sortable="true" :style="[col.style, [col.field == 'valueCheck' ? 'max-width: 200px' : '']]" class="kn-truncated">
                        <template #filter="{ filterModel }">
                            <InputText v-model="filterModel.value" type="text" class="p-column-filter"></InputText>
                        </template>
                        <template #body="slotProps">
                            <span v-if="slotProps.data['label'].toLowerCase().endsWith('.password') && col.field == 'valueCheck'">●●●●●●●●●●●●</span>
                            <span v-else :title="slotProps.data[col.field]">{{ slotProps.data[col.field] }}</span>
                        </template>
                    </Column>
                    <Column :style="configurationManagementDescriptor.table.iconColumn.style" @rowClick="false">
                        <template #body="slotProps">
                            <Button icon="pi pi-trash" class="p-button-link" :data-test="'delete-button'" @click="showDeleteDialog(slotProps.data.id)" />
                        </template>
                    </Column>
                </DataTable>
            </div>
            <div v-if="formVisible">
                <ConfigurationManagementDialog :model="selectedConfiguration" data-test="configuration-form" @created="reload" @close="closeForm"></ConfigurationManagementDialog>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iConfiguration } from './ConfigurationManagement'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '@/helpers/commons/filterHelper'
import configurationManagementDescriptor from './ConfigurationManagementDescriptor.json'
import { AxiosResponse } from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import ConfigurationManagementDialog from './ConfigurationManagementDialog.vue'
import mainStore from '../../../App.store'
export default defineComponent({
    name: 'configuration-management',
    components: {
        Column,
        DataTable,
        KnFabButton,
        ConfigurationManagementDialog
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            configurationManagementDescriptor: configurationManagementDescriptor,
            configurations: [] as iConfiguration[],
            selectedConfiguration: null as iConfiguration | null,
            columns: configurationManagementDescriptor.columns,
            formVisible: false,
            loading: false,
            filters: {
                global: [filterDefault],
                label: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                name: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                category: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                valueCheck: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                active: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object
        }
    },
    created() {
        this.loadConfigurations()
    },
    methods: {
        async loadConfigurations() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/configs')
                .then((response: AxiosResponse<any>) => {
                    this.configurations = response.data
                })
                .finally(() => (this.loading = false))
        },
        showDeleteDialog(configurationId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteConfiguration(configurationId)
            })
        },
        async deleteConfiguration(configurationId: number) {
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/configs/' + configurationId).then(() => {
                this.store.setInfo({
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.loadConfigurations()
            })
        },
        showForm(event) {
            if (event) {
                this.selectedConfiguration = event.data
            }
            this.formVisible = true
        },
        closeForm() {
            this.selectedConfiguration = null
            this.formVisible = false
        },
        reload() {
            this.formVisible = false
            this.loadConfigurations()
        }
    }
})
</script>

<style lang="scss" scoped></style>
