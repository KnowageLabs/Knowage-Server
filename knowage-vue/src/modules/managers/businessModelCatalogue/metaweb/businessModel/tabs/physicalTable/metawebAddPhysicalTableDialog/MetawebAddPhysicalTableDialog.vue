<template>
    <Dialog id="metaweb-add-physical-table-dialog" :style="metawebAddPhysicalTableDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #start>
                    {{ $t('metaweb.businessModel.addPhysicalTables') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" />

        <DataTable v-model:selection="selectedTables" v-model:filters="filters" :value="rows" class="p-datatable-sm kn-table p-m-2" data-key="name" :global-filter-fields="metawebAddPhysicalTableDialogDescriptor.globalFilterFields">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #header>
                <div class="table-header p-d-flex">
                    <span class="p-input-icon-left p-mr-3 p-col-12">
                        <i class="pi pi-search" />
                        <InputText v-model="filters['global'].value" class="kn-material-input" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>
            <Column selection-mode="multiple" :style="metawebAddPhysicalTableDialogDescriptor.selectColumnStyle" />
            <Column field="name" :header="$t('common.name')" />
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { filterDefault } from '@/helpers/commons/filterHelper'
    import Column from 'primevue/column'
    import DataTable from 'primevue/datatable'
    import Dialog from 'primevue/dialog'
    import metawebAddPhysicalTableDialogDescriptor from './MetawebAddPhysicalTableDialogDescriptor.json'

    export default defineComponent({
        name: 'metaweb-add-physical-table-dialog',
        components: { Column, DataTable, Dialog },
        props: { visible: { type: Boolean }, physicalTables: { type: Array }, propLoading: { type: Boolean }, roles: { type: Array } },
        emits: ['close', 'save'],
        data() {
            return {
                metawebAddPhysicalTableDialogDescriptor,
                rows: [] as any[],
                selectedTables: [] as any[],
                filters: {
                    global: [filterDefault]
                } as Object,
                loading: false
            }
        },
        watch: {
            physicalTables() {
                this.loadTables()
            },
            propLoading() {
                this.setLoading()
            }
        },
        created() {
            this.loadTables()
            this.setLoading()
        },
        methods: {
            loadTables() {
                this.selectedTables = []
                this.rows = this.physicalTables as any[]
            },
            setLoading() {
                this.loading = this.propLoading
            },
            closeDialog() {
                this.selectedTables = []
                this.$emit('close')
            },
            save() {
                this.$emit('save', [...this.selectedTables])
            }
        }
    })
</script>

<style lang="scss">
    #metaweb-add-physical-table-dialog .p-dialog-header,
    #metaweb-add-physical-table-dialog .p-dialog-content {
        padding: 0;
    }

    #metaweb-add-physical-table-dialog .p-dialog-content {
        display: flex;
        flex-direction: column;
        flex: 1;
    }
</style>
