<template>
    <Dialog id="function-catalog-detail-dialog" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" contentStyle="height:100vh" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="9999" :autoZIndex="true">
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

        <TabView class="document-details-tabview" style="flex:1">
            <TabPanel>
                <template #header>
                    <span>{{ $t('documentExecution.documentDetails.info.infoTitle') }}</span>
                </template>
                <!-- <div class="p-grid p-m-0" style="height: calc(100vh - 35px - 39px - 35px);"> -->
                <div class="p-grid p-m-0" style="height:100%">
                    <div class="p-col-7 p-m-0 p-p-0 right-border" style="height:100%">
                        <Toolbar class="kn-toolbar kn-toolbar--secondary">
                            <template #left>
                                {{ $t('documentExecution.documentDetails.info.infoTitle') }}
                            </template>
                        </Toolbar>
                        <div class="informations-content" style="height:100%;overflow:auto">
                            <Card class="p-m-2">
                                <template #content>
                                    <form class="p-fluid p-formgrid p-grid">
                                        <span class="p-field p-col-12 p-lg-6 p-float-label">
                                            <InputText id="label" class="kn-material-input" type="text" maxLength="50" v-model="test" data-test="label-input" />
                                            <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                                        </span>
                                        <span class="p-field p-col-12 p-lg-6 p-float-label">
                                            <InputText id="name" class="kn-material-input" type="text" maxLength="50" v-model="test" data-test="name-input" />
                                            <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                                        </span>
                                        <span class="p-field p-col-12 p-float-label">
                                            <Textarea id="description" class="kn-material-input" v-model="test" rows="3" :autoResize="true" />
                                            <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                                        </span>
                                        <div class="p-field p-col-12 p-d-flex">
                                            <div style="width:100%">
                                                <span class="p-float-label">
                                                    <InputText id="fileName" class="kn-material-input" v-model="test" :disabled="true" />
                                                    <label for="fileName" class="kn-material-input-label"> {{ $t('downloadsDialog.columns.fileName') }} </label>
                                                </span>
                                            </div>
                                            <Button icon="fas fa-upload fa-2x" class="p-button-text p-button-plain p-ml-2" @click="setUploadType" />
                                            <Button icon="fas fa-download fa-2x" class="p-button-text y p-button-plain p-ml-2" @click="downloadDatasetFile" />
                                            <KnInputFile :changeFunction="uploadDatasetFile" accept=".csv, .xls, .xlsx" :triggerInput="triggerUpload" />
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
                                    </form>
                                </template>
                            </Card>
                        </div>
                    </div>
                    <div class="p-col-5 p-m-0 p-p-0" style="height:100%">
                        <Toolbar class="kn-toolbar kn-toolbar--secondary">
                            <template #left>
                                {{ $t('documentExecution.documentDetails.info.positionTitle') }}
                            </template>
                        </Toolbar>
                        <div class="position-content" style="height:100%;overflow:auto">
                            <Card>
                                <template #content>
                                    Position Content
                                    <!-- #region -->
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <p>a</p>
                                    <!-- #endregion  -->
                                    end
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
    </Dialog>
    <!-- <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #left>
                {{ $t('documentExecution.documentDetails.title') }}
            </template>
            <template #right>
                <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" />
            </template>
        </Toolbar>
        <TabView class="document-details-tabview">
            <TabPanel>
                <template #header>
                    <span>{{ $t('documentExecution.documentDetails.info.infoTitle') }}</span>
                </template>
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
    </div> -->
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Card from 'primevue/card'
import Textarea from 'primevue/textarea'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'document-details',
    components: { TabView, TabPanel, Dialog, Card, Textarea, Dropdown },
    props: { id: { type: String, required: true }, visible: { type: Boolean, required: false } },
    emits: ['closeDetails'],
    computed: {},
    created() {},

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
            ]
        }
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
</style>
<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
    width: calc(100vw - #{$mainmenu-width});
    margin: 0;
}

.full-screen-dialog.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}

#function-catalog-detail-dialog .p-toolbar-group-right {
    height: 100%;
}
</style>
