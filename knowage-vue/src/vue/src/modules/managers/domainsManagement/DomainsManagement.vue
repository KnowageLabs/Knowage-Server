<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('managers.domainsManagement.title') }}
            </template>
            <template #end>
                <KnFabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm()"></KnFabButton>
            </template>
        </Toolbar>
        <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
        <div class="kn-page-content p-grid p-m-0">
            <div v-if="!loading" class="p-col">
                <DataTable
                    v-model:selection="selectedDomain"
                    v-model:filters="filters"
                    :value="domains"
                    :paginator="true"
                    :loading="loading"
                    :rows="20"
                    selection-mode="single"
                    class="p-datatable-sm kn-table"
                    data-key="valueId"
                    filter-display="menu"
                    :global-filter-fields="domainsManagementDescriptor.globalFilterFields"
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
                    data-test="domains-table"
                    @rowClick="showForm($event)"
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
                    <template #loading>
                        {{ $t('common.info.dataLoading') }}
                    </template>

                    <Column v-for="col of domainsManagementDescriptor.columns" :key="col.field" :field="col.field" :header="$t(col.header)" :style="domainsManagementDescriptor.table.column.style" :sortable="true" class="kn-truncated">
                        <template #filter="{ filterModel }">
                            <InputText v-model="filterModel.value" type="text" class="p-column-filter"></InputText>
                        </template>
                        <template #body="slotProps">
                            <span :title="slotProps.data[col.field]">{{ slotProps.data[col.field] }}</span>
                        </template>
                    </Column>
                    <Column :style="domainsManagementDescriptor.table.iconColumn.style">
                        <template #body="slotProps">
                            <Button icon="pi pi-trash" class="p-button-link" :data-test="'delete-button'" @click="deleteDomainConfirm(slotProps.data.valueId)" />
                        </template>
                    </Column>
                </DataTable>
            </div>

            <div v-if="formVisible">
                <DomainsManagementDialog :model="selectedDomain" data-test="domain-form" @created="reloadDomains" @close="closeForm"></DomainsManagementDialog>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDomain } from './DomainsManagement'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '../../../helpers/commons/filterHelper'
import { AxiosResponse } from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import domainsManagementDescriptor from './DomainsManagementDescriptor.json'
import DomainsManagementDialog from './DomainsManagementDialog.vue'
import KnFabButton from '../../../components/UI/KnFabButton.vue'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'domains-management',
    components: {
        Column,
        DataTable,
        DomainsManagementDialog,
        KnFabButton
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            domainsManagementDescriptor: domainsManagementDescriptor,
            domains: [] as iDomain[],
            filters: {
                global: [filterDefault],
                valueCd: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                valueName: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                domainCode: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                domainName: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                valueDescription: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object,
            formVisible: false,
            loading: false,
            selectedDomain: null as iDomain | null
        }
    },
    created() {
        this.loadAllDomains()
    },
    methods: {
        async loadAllDomains() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/domains')
                .then((response: AxiosResponse<any>) => {
                    this.domains = response.data
                })
                .finally(() => (this.loading = false))
        },
        deleteDomainConfirm(domainId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDomain(domainId)
            })
        },
        async deleteDomain(domainId: number) {
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/domains/' + domainId).then(() => {
                this.store.setInfo({
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.loadAllDomains()
            })
        },
        showForm(event: any) {
            if (event) {
                this.selectedDomain = event.data
            }
            this.formVisible = true
        },
        closeForm() {
            this.selectedDomain = null
            this.formVisible = false
        },
        reloadDomains() {
            this.formVisible = false
            this.loadAllDomains()
        }
    }
})
</script>
