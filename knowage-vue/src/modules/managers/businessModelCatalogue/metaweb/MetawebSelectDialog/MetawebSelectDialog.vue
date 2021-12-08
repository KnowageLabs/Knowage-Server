<template>
    <Dialog id="metaweb-select-dialog" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :contentStyle="metawebSelectDialogDescriptor.dialog.style" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ businessModel?.name }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text p-m-2" :label="$t('common.close')" @click="closeDialog"></Button>
                    <Button class="kn-button p-button-text" :label="$t('common.continue')" @click="onContinue"></Button>
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="true" />
        </template>

        <DataTable :value="rows" class="p-datatable-sm kn-table" responsiveLayout="stack" breakpoint="960px">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>

            <Column field="value" :header="$t('common.name')"></Column>
        </DataTable>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iBusinessModel } from '../../BusinessModelCatalogue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import metawebSelectDialogDescriptor from './MetawebSelectDialogDescriptor.json'

export default defineComponent({
    name: 'metaweb-select-dialog',
    components: { Column, DataTable, Dialog },
    props: { visible: { type: Boolean }, selectedBusinessModel: { type: Object as PropType<iBusinessModel> } },
    data() {
        return {
            metawebSelectDialogDescriptor,
            businessModel: null as iBusinessModel | null,
            datasourceStructure: null as any,
            rows: [] as any[],
            loading: false
        }
    },
    watch: {
        async businessModel() {
            await this.loadData()
        }
    },
    async created() {
        await this.loadData()
    },
    methods: {
        async loadData() {
            this.loadBusinessModel()
            await this.loadDatasourceStructure()
        },
        loadBusinessModel() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel
            console.log('LOADED BUSINESS MODEL: ', this.businessModel)
        },
        async loadDatasourceStructure() {
            this.loading = true
            if (this.businessModel?.dataSourceId) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasources/structure/${this.businessModel.dataSourceId}`).then((response: AxiosResponse<any>) => (this.datasourceStructure = response.data))
            }
            this.loading = false
            console.log('LOADED DATASOURCE STRUCTURE: ', this.datasourceStructure)
        },
        closeDialog() {
            this.$emit('close')
        },
        onContinue() {
            console.log('CONTINUE CLICKED!')
        }
    }
})
</script>

<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{$mainmenu-width});
    margin: 0;
}
.full-screen-dialog.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}
#metaweb-select-dialog .p-toolbar-group-right {
    height: 100%;
}
</style>
