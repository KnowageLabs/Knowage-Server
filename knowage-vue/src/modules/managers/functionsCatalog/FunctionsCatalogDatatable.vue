<template>
    <DataTable
        id="functions-datatable"
        :value="filteredFunctions"
        :paginator="true"
        :rows="functionsCatalogDatatableDescriptor.rows"
        :loading="loading"
        class="p-datatable-sm kn-table kn-page-content"
        data-key="id"
        :responsive-layout="functionsCatalogDatatableDescriptor.responsiveLayout"
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
                    <InputText v-model="searchWord" class="kn-material-input" type="text" :placeholder="$t('common.search')" @input="searchFunctions" />
                </span>
            </div>
        </template>
        <Column v-for="col of functionsCatalogDatatableDescriptor.columns" :key="col.field" class="kn-truncated" :style="col.style" :header="$t(col.header)" :sort-field="col.field" :sortable="true">
            <template #body="slotProps">
                <span v-tooltip.top="slotProps.data[col.field]"> {{ slotProps.data[col.field] }}</span>
            </template>
        </Column>
        <Column :style="functionsCatalogDatatableDescriptor.table.iconColumn.style">
            <template #body="slotProps">
                <Button v-tooltip.top="$t('managers.functionsCatalog.executePreview')" icon="fa fa-play-circle" class="p-button-link" @click.stop="previewFunction(slotProps.data)" />
                <Button v-if="canDelete(slotProps.data)" v-tooltip.top="$t('common.delete')" icon="pi pi-trash" class="p-button-link" :data-test="'delete-button-' + slotProps.data.id" @click.stop="deleteFunctionConfirm(slotProps.data.id)" />
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
    computed: {
        canManageFunctionalities(): boolean {
            const index = this.user?.functionalities?.findIndex((el: string) => el === 'FunctionsCatalogManagement')
            return index !== -1
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
