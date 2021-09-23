<template>
    <DataTable
        :value="functions"
        :paginator="true"
        :rows="15"
        :loading="loading"
        class="p-datatable-sm kn-table"
        dataKey="id"
        v-model:filters="filters"
        :globalFilterFields="functionsCatalogDatatableDescriptor.globalFilterFields"
        responsiveLayout="stack"
        breakpoint="960px"
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
                    <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                </span>
            </div>
        </template>
        <Column class="kn-truncated" :style="col.style" v-for="col of functionsCatalogDatatableDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"> </Column>
        <Column :style="functionsCatalogDatatableDescriptor.table.iconColumn.style">
            <template #body="slotProps">
                <Button icon="fa fa-play-circle" class="p-button-link" @click="previewFunction(slotProps.data)" />
                <Button icon="pi pi-trash" class="p-button-link" @click="deleteFunctionConfirm(slotProps.data.id)" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction } from './FunctionsCatalog'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import functionsCatalogDatatableDescriptor from './FunctionsCatalogDatatableDescriptor.json'

export default defineComponent({
    name: 'functions-catalog-datatable',
    components: { Column, DataTable },
    props: {
        propLoading: { type: Boolean },
        items: { type: Array }
    },
    emits: ['selected', 'deleted'],
    data() {
        return {
            functionsCatalogDatatableDescriptor,
            functions: [] as iFunction[],
            filters: { global: [filterDefault] } as Object,
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
            console.log('DATATABLE - loadFunctions() - LOADED FUNCTIOSN: ', this.functions)
        },
        previewFunction(event) {
            console.log('previewFunction() event: ', event)
        },
        deleteFunctionConfirm(functionId: string) {
            console.log('deleteFunctionConfirm() event: ', event)
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deleted', functionId)
            })
        }
    }
})
</script>

<style lang="scss" scoped>
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
</style>
