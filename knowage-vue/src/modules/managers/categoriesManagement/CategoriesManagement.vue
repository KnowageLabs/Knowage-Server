<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('managers.categoriesManagement.title') }}
            </template>
            <template #end>
                <KnFabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm" />
            </template>
        </Toolbar>
        <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
        <div class="kn-page-content p-grid p-m-0">
            <div v-if="!loading" class="p-col">
                <DataTable
                    v-model:selection="selectedCategory"
                    v-model:filters="filters"
                    class="p-datatable-sm kn-table"
                    :loading="loading"
                    :value="categories"
                    :paginator="true"
                    :rows="20"
                    selection-mode="single"
                    data-key="id"
                    filter-display="menu"
                    :global-filter-fields="descriptor.globalFilterFields"
                    :rows-per-page-options="[10, 15, 20]"
                    responsive-layout="stack"
                    breakpoint="960px"
                    :current-page-report-template="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
                    @row-click="showForm($event)"
                >
                    <template #header>
                        <div class="table-header">
                            <span class="p-input-icon-left">
                                <i class="pi pi-search" />
                                <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                            </span>
                        </div>
                    </template>
                    <template #empty> {{ $t('common.info.noDataFound') }} </template>
                    <template #loading> {{ $t('common.info.dataLoading') }} </template>

                    <Column v-for="col of descriptor.columns" :key="col.field" :field="col.field" :header="$t(col.header)" :style="col.field === 'occurences' ? descriptor.table.column.badgeStyle : descriptor.table.column.style" :sortable="true" class="kn-truncated">
                        <template #filter="{ filterModel }">
                            <InputText v-model="filterModel.value" type="text" class="p-column-filter" />
                        </template>
                        <template #body="slotProps">
                            <span v-if="col.field === 'occurrences'" class="categories-badge" :style="getBadgeColor(slotProps.data[col.field])">{{ slotProps.data[col.field] }}</span>
                            <span v-else :title="slotProps.data[col.field]">{{ slotProps.data[col.field] }}</span>
                        </template>
                    </Column>
                    <Column :style="descriptor.table.iconColumn.style">
                        <template #body="slotProps">
                            <Button icon="pi pi-trash" class="p-button-link" :data-test="'delete-button'" @click="deleteCategoryConfirm(slotProps.data)" />
                        </template>
                    </Column>
                </DataTable>
            </div>

            <CategoriesManagementDialog v-if="formVisible" :prop-category="selectedCategory" data-test="domain-form" @created="reloadCategories" @close="closeForm" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iCategory } from './CategoriesManagement'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '../../../helpers/commons/filterHelper'
import { AxiosResponse } from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import descriptor from './CategoriesManagementDescriptor.json'
import CategoriesManagementDialog from './CategoriesManagementDialog.vue'
import KnFabButton from '../../../components/UI/KnFabButton.vue'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'categories-management',
    components: { Column, DataTable, CategoriesManagementDialog, KnFabButton },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            descriptor: descriptor,
            categories: [] as iCategory[],
            selectedCategory: null as iCategory | null,
            filters: {
                global: [filterDefault],
                code: { operator: FilterOperator.AND, constraints: [filterDefault] },
                name: { operator: FilterOperator.AND, constraints: [filterDefault] },
                occurences: { operator: FilterOperator.AND, constraints: [filterDefault] },
                type: { operator: FilterOperator.AND, constraints: [filterDefault] }
            } as Object,
            formVisible: false,
            loading: false
        }
    },
    created() {
        this.loadCategories()
    },
    methods: {
        async loadCategories() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '3.0/category/categories')
                .then((response: AxiosResponse<any>) => (this.categories = response.data))
                .finally(() => (this.loading = false))
        },
        deleteCategoryConfirm(category: iCategory) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteCategory(category)
            })
        },
        async deleteCategory(category: iCategory) {
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '3.0/category', { data: category }).then(() => {
                this.store.setInfo({ title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                this.loadCategories()
            })
        },
        showForm(event: any) {
            if (event) {
                this.selectedCategory = event.data
            }
            this.formVisible = true
        },
        closeForm() {
            this.selectedCategory = null
            this.formVisible = false
        },
        reloadCategories() {
            this.formVisible = false
            this.loadCategories()
        },
        getBadgeColor(value) {
            if (value > 0) return 'background: #3B82F6'
            else return 'background: #EF4444'
        }
    }
})
</script>
<style lang="scss">
.categories-badge {
    font-size: 0.75rem;
    font-weight: 700;
    min-width: 1.5rem;
    height: 1.5rem;
    line-height: 1.5rem;
    display: inline-block;
    text-align: center;
    padding: 0;
    border-radius: 50%;
}
</style>
