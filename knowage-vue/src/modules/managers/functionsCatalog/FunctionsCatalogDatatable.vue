<template>
    <DataTable
        id="functions-datatable"
        :value="filteredFunctions"
        :paginator="true"
        :rows="functionsCatalogDatatableDescriptor.rows"
        :loading="loading"
        class="p-datatable-sm kn-table kn-page-content"
        dataKey="id"
        :responsiveLayout="functionsCatalogDatatableDescriptor.responsiveLayout"
        :breakpoint="functionsCatalogDatatableDescriptor.breakpoint"
        @rowClick="$emit('selected', $event.data)"
    >
        <template #loading>
            {{ $t('common.info.dataLoading') }}
        </template>
        <template #empty>
            <div id="noFunctionsFound">
                {{ $t('managers.functionsCatalog.noFunctionsFound') }}
            </div>
        </template>
        <template #header>
            <div class="table-header p-d-flex">
                <span class="p-input-icon-left p-mr-3 p-col-12">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" v-model="searchWord" type="text" :placeholder="$t('common.search')" @input="searchFunctions" />
                </span>
            </div>
        </template>
        <Column class="kn-truncated" :style="col.style" v-for="col of functionsCatalogDatatableDescriptor.columns" :header="$t(col.header)" :key="col.field" :sortField="col.field" :sortable="true">
            <template #body="slotProps">
                <span v-tooltip.top="slotProps.data[col.field]"> {{ slotProps.data[col.field] }}</span>
            </template>
        </Column>
        <Column :style="functionsCatalogDatatableDescriptor.table.iconColumn.style">
            <template #body="slotProps">
                <Button icon="fa fa-play-circle" class="p-button-link" v-tooltip.top="$t('managers.functionsCatalog.executePreview')" @click.stop="previewFunction(slotProps.data)" />
                <Button v-if="canDelete(slotProps.data)" icon="pi pi-trash" class="p-button-link" v-tooltip.top="$t('common.delete')" @click.stop="deleteFunctionConfirm(slotProps.data.id)" :data-test="'delete-button-' + slotProps.data.id" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction } from './FunctionsCatalog'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import functionsCatalogDatatableDescriptor from './FunctionsCatalogDatatableDescriptor.json'

export default defineComponent({
    name: 'functions-catalog-datatable',
    components: { Column, DataTable },
    props: {
        user: { type: Object },
        propLoading: { type: Boolean },
        items: { type: Array }
    },
    emits: ['selected', 'preview', 'deleted'],
    data() {
        return {
            functionsCatalogDatatableDescriptor,
            functions: [] as iFunction[],
            filteredFunctions: [] as iFunction[],
            searchWord: '',
            loading: false
        }
    },
    watch: {
        propLoading() {
            this.setLoading()
        },
        items() {
            this.loadFunctions()
        }
    },
    computed: {
        canManageFunctionalities(): boolean {
            const index = this.user?.functionalities?.findIndex((el: string) => el === 'FunctionsCatalogManagement')
            return index !== -1
        }
    },
    async created() {
        this.setLoading()
        this.loadFunctions()
    },
    methods: {
        setLoading() {
            this.loading = this.propLoading
        },
        loadFunctions() {
            this.functions = [...(this.items as iFunction[])]
            this.filteredFunctions = [...this.functions]
            this.searchWord = ''
        },
        previewFunction(tempFunction: iFunction) {
            this.$emit('preview', tempFunction)
        },
        canDelete(tempFunction: iFunction) {
            return this.canManageFunctionalities || tempFunction?.owner === this.user?.userId
        },
        deleteFunctionConfirm(functionId: string) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deleted', functionId)
            })
        },
        searchFunctions() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredFunctions = [...this.functions] as any[]
                } else {
                    this.filteredFunctions = this.functions.filter((tempFunction: any) => {
                        return (
                            tempFunction.label.toLowerCase().includes(this.searchWord.toLowerCase()) ||
                            tempFunction.name.toLowerCase().includes(this.searchWord.toLowerCase()) ||
                            tempFunction.type.toLowerCase().includes(this.searchWord.toLowerCase()) ||
                            tempFunction.language.toLowerCase().includes(this.searchWord.toLowerCase()) ||
                            tempFunction.owner.toLowerCase().includes(this.searchWord.toLowerCase()) ||
                            (tempFunction.tags && tempFunction.tags.includes(this.searchWord.toLowerCase()))
                        )
                    })
                }
            }, 250)
        }
    }
})
</script>

<style lang="scss">
#noFunctionsFound {
    margin: 0 auto;
    border: 1px solid rgba(204, 204, 204, 0.6);
    padding: 0.5rem;
    background-color: #e6e6e6;
    text-align: center;
    text-transform: uppercase;
    font-size: 0.8rem;
    width: 80%;
}

#functions-datatable .p-datatable-wrapper {
    height: auto;
}
</style>
