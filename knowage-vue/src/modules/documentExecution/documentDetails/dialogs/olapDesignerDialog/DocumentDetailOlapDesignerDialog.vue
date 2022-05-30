<template>
    <Dialog class="full-screen-dialog" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #start>
                    <span>{{ $t('documentExecution.documentDetails.designerDialog.olapDesigner') }}</span>
                </template>
                <template #end>
                    <Button class="kn-button kn-button--primary olap-designer-toolbar-button" @click="closeDialog"> {{ $t('common.back') }}</Button>
                    <Button class="kn-button kn-button--primary olap-designer-toolbar-button" @click="start"> {{ $t('common.start') }}</Button>
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
        <DocumentDetailXMLAForm v-if="type === 'xmla'" class="p-m-4" :xmlModel="xmlModel"></DocumentDetailXMLAForm>
        <DocumentDetailMondrianForm v-else class="p-m-4" :sbiExecutionId="sbiExecutionId" :mondrianModel="mondrianModel" :mondrianSchemas="mondrianSchemas"></DocumentDetailMondrianForm>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iDocument, iMondrianSchema, iXMLATemplate, iMondrianTemplate } from '../../DocumentDetails'
import { mapState } from 'vuex'
import Dialog from 'primevue/dialog'
import descriptor from './DocumentDetailOlapDesignerDialogDescriptor.json'
import DocumentDetailXMLAForm from './DocumentDetailXMLAForm.vue'
import DocumentDetailMondrianForm from './DocumentDetailMondrianForm.vue'
import Dropdown from 'primevue/dropdown'
import ProgressSpinner from 'primevue/progressspinner'

const crypto = require('crypto')

export default defineComponent({
    name: 'document-detail-olap-designer-dialog',
    components: { Dialog, DocumentDetailXMLAForm, DocumentDetailMondrianForm, Dropdown, ProgressSpinner },
    props: { visible: { type: Boolean }, selectedDocument: { type: Object as PropType<iDocument> } },
    emits: ['designerStarted', 'close'],
    data() {
        return {
            descriptor,
            document: null as iDocument | null,
            mondrianSchemas: [] as iMondrianSchema[],
            type: 'xmla' as string,
            xmlModel: { address: '', parameters: [] } as iXMLATemplate,
            mondrianModel: {} as iMondrianTemplate,
            sbiExecutionId: '',
            loading: false
        }
    },
    computed: {
        ...mapState({
            user: 'user'
        })
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
        async loadDocument() {
            this.document = this.selectedDocument ? { ...this.selectedDocument } : ({} as iDocument)
            this.sbiExecutionId = crypto.randomBytes(16).toString('hex')

            this.initialize()
            this.loadMondrianSchemaResources()
        },
        async initialize() {
            if (!this.document) return
            this.$store.commit('setLoading', true)
            let language = this.user.locale.split('_')[0]
            let country = this.user.locale.split('_')[1]
            await this.$http
                .get(
                    process.env.VUE_APP_RESTFUL_SERVICES_PATH +
                        `knowagewhatifengine/restful-services/olap/startolap/edit?SBI_LANGUAGE=${language}&SBI_COUNTRY=${country}&DOCUMENT_LABEL=${this.document.label}&mode=edit&user_id=$${this.user.userUniqueIdentifier}&document=${this.document.id}&ENGINE=knowageolapengine&SBI_EXECUTION_ID=${this.sbiExecutionId}`,
                    { headers: { Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9' } }
                )
                .then(() => {})
            this.$store.commit('setLoading', false)
        },
        async loadMondrianSchemaResources() {
            this.$store.commit('setLoading', true)
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource`).then((response: AxiosResponse<any>) => (this.mondrianSchemas = response.data))
            this.$store.commit('setLoading', false)
        },
        closeDialog() {
            this.$emit('close')
            this.type = 'xmla'
            this.xmlModel = { address: '', parameters: [] }
            this.mondrianModel = {} as iMondrianTemplate
        },
        async start() {
            console.log(' >>> START xmlModel: ', this.xmlModel)
            console.log(' >>> START mondrianModel: ', this.mondrianModel)
            // TODO - REMOVE HARCODED SBI_EXECUTION_ID
            const postData = this.type === 'xmla' ? { ...this.xmlModel } : { ...this.mondrianModel }
            this.$store.commit('setLoading', true)
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/designer/cubes?SBI_EXECUTION_ID=${this.sbiExecutionId}`, postData, { headers: { Accept: 'application/json, text/plain, */*' } })
                .then((response: AxiosResponse<any>) => {
                    console.log('RESPONSE DATA: ', response.data)
                    this.$emit('designerStarted', this.selectedDocument)
                })
                .catch(() => {})
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

.olap-designer-toolbar-button {
    font-size: 0.75rem;
}
</style>
