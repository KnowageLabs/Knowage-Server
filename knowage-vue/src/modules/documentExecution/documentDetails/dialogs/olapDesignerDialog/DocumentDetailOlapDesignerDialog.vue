<template>
    <Dialog class="full-screen-dialog" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #start>
                    <span>{{ $t('documentExecution.documentDetails.designerDialog.olapDesigner') }}</span>
                </template>
            </Toolbar>
        </template>
        <ProgressSpinner class="kn-progress-spinner" v-if="loading" />

        <div class="p-fluid p-formgrid p-grid p-m-4">
            <div class="p-field p-col-4">
                <span class="p-float-label">
                    <Dropdown id="type" class="kn-material-input" v-model="type" :options="descriptor.typeOptions" optionValue="value">
                        <template #value="slotProps">
                            <div v-if="slotProps.value">
                                <span>{{ $t(`documentExecution.documentDetails.designerDialog.${slotProps.value}`) }}</span>
                            </div>
                        </template>
                        <template #option="slotProps">
                            <div>
                                <span>{{ $t(slotProps.option.label) }}</span>
                            </div>
                        </template>
                    </Dropdown>
                    <label for="type" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.designerDialog.templateType') }}</label>
                </span>
            </div>
        </div>
        <DocumentDetailXMLAForm v-if="type === 'xmla'" :xmlModel="xmlModel"></DocumentDetailXMLAForm>
        <DocumentDetailMondrianForm v-else></DocumentDetailMondrianForm>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iDocument, iMondrianSchema, iXMLATemplate, iMondrianTemplate } from '../../DocumentDetails'
import Dialog from 'primevue/dialog'
import descriptor from './DocumentDetailOlapDesignerDialogDescriptor.json'
import DocumentDetailXMLAForm from './DocumentDetailXMLAForm.vue'
import DocumentDetailMondrianForm from './DocumentDetailMondrianForm.vue'
import Dropdown from 'primevue/dropdown'
import ProgressSpinner from 'primevue/progressspinner'

export default defineComponent({
    name: 'document-detail-olap-designer-dialog',
    components: { Dialog, DocumentDetailXMLAForm, DocumentDetailMondrianForm, Dropdown, ProgressSpinner },
    props: { visible: { type: Boolean }, selectedDocument: { type: Object as PropType<iDocument> } },
    data() {
        return {
            descriptor,
            document: null as iDocument | null,
            mondrianSchemas: [] as iMondrianSchema[],
            type: 'xmla' as string,
            xmlModel: { address: '', parameters: [] } as iXMLATemplate,
            mondrianModel: {} as iMondrianTemplate,
            loading: false
        }
    },
    watch: {
        selectedDocument() {
            this.loadDocument()
        }
    },
    async created() {
        this.loadDocument()
    },
    methods: {
        loadDocument() {
            this.document = this.selectedDocument ? { ...this.selectedDocument } : ({} as iDocument)
        },
        async loadMondrianSchemaResources() {
            this.$store.commit('setLoading', true)
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource`).then((response: AxiosResponse<any>) => (this.mondrianSchemas = response.data))
            this.$store.commit('setLoading', false)
        }
    }
})
</script>

<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{54px});
    margin: 0;
    .p-dialog-content {
        padding: 0;
        margin: 0;
        flex: 1;
        overflow: hidden;
    }
    .p-dialog-header {
        padding: 0;
        margin: 0;
    }
}
</style>
