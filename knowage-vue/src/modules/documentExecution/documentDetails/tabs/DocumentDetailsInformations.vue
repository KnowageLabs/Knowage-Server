<template>
    <div class="p-grid p-m-0" :style="mainDescriptor.style.flexOne">
        <div class="p-col-7 p-m-0 p-p-0 right-border">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('documentExecution.documentDetails.info.infoTitle') }}
                </template>
            </Toolbar>
            <div class="informations-content">
                <Card class="p-m-2">
                    <template #content>
                        <form class="p-fluid p-formgrid p-grid p-m-1">
                            <span class="p-field p-col-12 p-lg-6 p-float-label">
                                <InputText id="label" class="kn-material-input" type="text" maxLength="100" v-model="document.label" />
                                <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                            </span>
                            <span class="p-field p-col-12 p-lg-6 p-float-label">
                                <InputText id="name" class="kn-material-input" type="text" maxLength="200" v-model="document.name" />
                                <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                            </span>
                            <span class="p-field p-col-12 p-float-label">
                                <Textarea id="description" class="kn-material-input" rows="3" maxLength="400" v-model="document.description" :autoResize="true" />
                                <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                            </span>
                            <div class="p-field p-col-12 p-d-flex">
                                <div style="width:100%">
                                    <span class="p-float-label">
                                        <InputText id="fileName" class="kn-material-input" v-model="test" :disabled="true" />
                                        <label for="fileName" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.previewImage') }} </label>
                                    </span>
                                </div>
                                <Button icon="fas fa-upload fa-2x" class="p-button-text p-button-plain p-ml-2" @click="setUploadType" />
                                <Button icon="fas fa-download fa-2x" class="p-button-text y p-button-plain p-ml-2" @click="downloadDatasetFile" />
                                <KnInputFile :changeFunction="uploadDatasetFile" accept=".png, .jpg, .jpeg" :triggerInput="triggerUpload" />
                            </div>
                            <span class="p-field p-float-label p-col-12 p-lg-6">
                                <Dropdown id="type" class="kn-material-input" :options="cities" optionLabel="name" optionValue="code" v-model="selectedCity" />
                                <label for="type" class="kn-material-input-label"> {{ $t('importExport.catalogFunction.column.type') }} </label>
                            </span>
                            <span class="p-field p-float-label p-col-12 p-lg-6">
                                <Dropdown id="engine" class="kn-material-input" :options="cities" optionLabel="name" optionValue="code" v-model="selectedCity" />
                                <label for="engine" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.engine') }} </label>
                            </span>
                            <span class="p-field p-float-label p-col-12">
                                <Dropdown id="datasource" class="kn-material-input" :options="cities" optionLabel="name" optionValue="code" v-model="selectedCity" />
                                <label for="datasource" class="kn-material-input-label"> {{ $t('managers.businessModelManager.dataSource') }} </label>
                            </span>
                            <span class="p-field p-float-label p-col-12 p-lg-6">
                                <Dropdown id="state" class="kn-material-input" :options="cities" optionLabel="name" optionValue="code" v-model="selectedCity" />
                                <label for="state" class="kn-material-input-label"> {{ $t('common.state') }} *</label>
                            </span>
                            <span class="p-field p-col-12 p-lg-6 p-float-label">
                                <InputText id="refresh" class="kn-material-input" type="text" maxLength="50" v-model="test" data-test="label-input" />
                                <label for="refresh" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.refresh') }} * </label>
                            </span>
                            <span class="p-field p-col-12 p-lg-6 p-jc-center p-mt-3">
                                <InputSwitch id="visible" v-model="document.visible" />
                                <i class="far fa-eye p-ml-2" />
                                <label for="visible" class="kn-material-input-label p-ml-2"> {{ $t('common.visible') }} </label>
                            </span>
                            <span class="p-field p-col-12 p-lg-6 p-mt-3">
                                <InputSwitch id="locked" v-model="document.lockedByUser" />
                                <i class="fas fa-lock p-ml-2" />
                                <label for="locked" class="kn-material-input-label p-ml-2"> {{ $t('common.locked') }} </label>
                            </span>
                        </form>
                    </template>
                </Card>
            </div>
        </div>
        <div class="p-col-5 p-m-0 p-p-0">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('documentExecution.documentDetails.info.positionTitle') }}
                </template>
            </Toolbar>
            <div class="position-content" style="overflow:auto">
                <Card>
                    <template #content>
                        <form class="p-fluid p-formgrid p-grid p-m-1">
                            <InlineMessage severity="info" class="p-col-12">VISIBILITY RESTRICTIONS</InlineMessage>
                        </form>
                    </template>
                </Card>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
// import { AxiosResponse } from 'axios'
import mainDescriptor from '../DocumentDetailsDescriptor.json'
import Card from 'primevue/card'
import Textarea from 'primevue/textarea'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import InlineMessage from 'primevue/inlinemessage'

export default defineComponent({
    name: 'document-details-informations',
    components: { Card, Textarea, Dropdown, InputSwitch, InlineMessage },
    props: { selectedDocument: { type: Object } },
    data() {
        return {
            mainDescriptor,
            test: '',
            selectedCity: null,
            cities: [
                { name: 'New York', code: 'NY' },
                { name: 'Rome', code: 'RM' },
                { name: 'London', code: 'LDN' },
                { name: 'Istanbul', code: 'IST' },
                { name: 'Paris', code: 'PRS' }
            ],
            document: {} as any
        }
    },
    watch: {
        document() {
            this.document = this.selectedDocument
        }
    },
    created() {
        this.document = this.selectedDocument
    },
    //analyticalDrivers: http://localhost:8080/knowage/restful-services/2.0/datasources
    //datasources: http://localhost:8080/knowage/restful-services/2.0/analyticalDrivers
    //document: http://localhost:8080/knowage/restful-services/2.0/documents/${id}
    //drivers: http://localhost:8080/knowage/restful-services/2.0/documentdetails/${id}/drivers
    //engines: http://localhost:8080/knowage/restful-services/2.0/engines

    //folderId: ??
    //resourcePath: ??
    //states: ??
    //template: ??
    //types: ??
    methods: {}
})
</script>
