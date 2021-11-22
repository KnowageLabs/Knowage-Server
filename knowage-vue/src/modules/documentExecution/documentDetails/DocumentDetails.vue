<template>
    <Dialog class="document-details-dialog p-fluid kn-dialog--toolbar--primary" contentStyle="display:flex;flex-direction:column;flex:1" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="9999" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('documentExecution.documentDetails.title') }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="$emit('closeDetails')" />
                </template>
            </Toolbar>
        </template>
        <div class="document-details-tab-container p-d-flex p-flex-column" style="flex:1">
            <TabView class="document-details-tabview" style="display:flex;flex-direction:column;flex:1">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.info.infoTitle') }}</span>
                    </template>
                    <!-- <div class="p-grid p-m-0" style="height: calc(100vh - 35px - 39px - 35px);"> -->
                    <div class="p-grid p-m-0" style="flex:1">
                        <div class="p-col-7 p-m-0 p-p-0 right-border">
                            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                                <template #left>
                                    {{ $t('documentExecution.documentDetails.info.infoTitle') }}
                                </template>
                            </Toolbar>
                            <div class="informations-content" style="overflow:auto">
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
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.drivers.title') }}</span>
                    </template>
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.outputParams.title') }}</span>
                    </template>
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.dataLineage.title') }}</span>
                    </template>
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.history.title') }}</span>
                    </template>
                </TabPanel>
            </TabView>
        </div>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
// import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Card from 'primevue/card'
import Textarea from 'primevue/textarea'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import InlineMessage from 'primevue/inlinemessage'

export default defineComponent({
    name: 'document-details',
    components: { TabView, TabPanel, Dialog, Card, Textarea, Dropdown, InputSwitch, InlineMessage },
    props: { selectedDocument: { type: Object }, visible: { type: Boolean, required: false } },
    emits: ['closeDetails'],
    data() {
        return {
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
    methods: {}
})
</script>

<style lang="scss">
.right-border {
    border-right: 1px solid #ccc;
}
.document-details-tabview .p-tabview-panels {
    padding: 0 !important;
}
.document-details-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{$mainmenu-width});
    margin: 0;
}

.document-details-dialog.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}

.document-details-tab-container .p-tabview .p-tabview-panel,
.document-details-tab-container .p-tabview .p-tabview-panels {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
