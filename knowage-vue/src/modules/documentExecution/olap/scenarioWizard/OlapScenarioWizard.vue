<template>
    <Dialog id="olap-scenario-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="descriptor.style.dialog" :visible="visible" :modal="true" :closable="false" :draggable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('documentExecution.olap.scenarioWizard.title') }}
                </template>
                <template #end>
                    <Button icon="fas fa-info-circle" class="p-button-text p-button-rounded p-button-plain" @click="showInfo = !showInfo" />
                </template>
            </Toolbar>
        </template>

        <div class="p-d-flex p-flex-column">
            <InlineMessage v-if="showInfo" class="p-m-2" severity="info">{{ $t('documentExecution.olap.scenarioWizard.longInfo') }}</InlineMessage>

            <div class="p-float-label p-col-12 p-mt-2">
                <Dropdown id="selectedCube" class="kn-material-input" v-model="selectedCube" :options="cubes" optionLabel="name" @change="getAllMeasures" />
                <label for="selectedCube" class="kn-material-input-label"> {{ $t('documentExecution.olap.scenarioWizard.selectedCube') }} </label>
            </div>

            <Card class="p-m-2">
                <template #header>
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #start>
                            {{ $t('documentExecution.olap.scenarioWizard.editableMeasures') }}
                        </template>
                    </Toolbar>
                </template>
                <template #content>
                    <DataTable :value="measures" v-model:selection="selectedMeasures" class="p-datatable-sm kn-table" dataKey="name" responsiveLayout="stack" breakpoint="960px" :scrollable="true" scrollHeight="flex">
                        <template #empty>
                            {{ $t('common.info.noDataFound') }}
                        </template>
                        <Column class="kn-column-checkbox" selectionMode="multiple" dataKey="name"></Column>
                        <Column field="name" :header="$t('common.name')"></Column>
                    </DataTable>
                </template>
            </Card>

            <Toolbar class="kn-toolbar kn-toolbar--secondary p-mx-2">
                <template #start>
                    <Button v-if="!expandParamsCard" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandParamsCard = true" />
                    <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandParamsCard = false" />
                    {{ $t('managers.datasetManagement.params') }}
                </template>
                <template #end>
                    <Button icon="fas fa-plus" class="p-button-text p-button-rounded p-button-plain" @click="addNewParam" />
                    <Button icon="fas fa-eraser" class="p-button-text p-button-rounded p-button-plain" :disabled="disableDeleteAll" @click="removeAllParams" />
                </template>
            </Toolbar>
            <Card v-show="expandParamsCard" class="p-mx-2 p-mb-2">
                <template #content>
                    <DataTable class="p-datatable-sm kn-table" editMode="cell" :value="variables" :scrollable="true" scrollHeight="250px" responsiveLayout="stack" breakpoint="960px">
                        <template #empty>
                            {{ $t('managers.datasetManagement.tableEmpty') }}
                        </template>
                        <Column field="name" :header="$t('documentExecution.olap.scenarioWizard.parameterName')">
                            <template #body="{data}">
                                <InputText class="kn-material-input" v-model="data.name" :style="descriptor.style.columnStyle" />
                            </template>
                        </Column>
                        <Column field="value" :header="$t('documentExecution.olap.scenarioWizard.parameterValue')">
                            <template #body="{data}">
                                <InputText class="kn-material-input" v-model="data.value" :style="descriptor.style.columnStyle" />
                            </template>
                        </Column>
                        <Column @rowClick="false" :style="descriptor.style.iconColumn">
                            <template #body="slotProps">
                                <Button icon="pi pi-trash" class="p-button-link" @click="removeParam(slotProps)" />
                            </template>
                        </Column>
                    </DataTable>
                </template>
            </Card>
        </div>

        <template #footer>
            <Button class="kn-button"> {{ $t('documentExecution.olap.scenarioWizard.clearData') }}</Button>
            <Button class="kn-button kn-button--secondary" @click="$emit('close')"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import descriptor from './OlapScenarioWizardDescriptor.json'
import InlineMessage from 'primevue/inlinemessage'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'

export default defineComponent({
    name: 'olap-scenario-wizard',
    components: { Dialog, InlineMessage, Dropdown, Card, Column, DataTable },
    props: { hiddenFormDataProp: { type: Object, required: true }, sbiExecutionId: { type: String } },
    emits: ['close'],
    computed: {},
    data() {
        return {
            descriptor,
            showInfo: false,
            loading: false,
            expandParamsCard: true,
            selectedCube: null as any,
            cubes: [] as any,
            measures: [] as any,
            selectedMeasures: [] as any,
            variables: [] as any
        }
    },
    watch: {
        async visible() {
            await this.getAllCubes()
        }
    },
    created() {
        this.getAllCubes()
    },
    methods: {
        async getAllCubes() {
            const currentContentId = this.hiddenFormDataProp.get('SBI_ARTIFACT_VERSION_ID')
            await this.$http
                .get(process.env.VUE_APP_OLAP_PATH + `1.0/designer/cubes/${currentContentId}?SBI_EXECUTION_ID=${this.sbiExecutionId}`)
                .then((response: AxiosResponse<any>) => {
                    this.cubes = response.data.map((cube) => ({ name: cube }))
                })
                .catch(() => {})
                .finally(() => (this.loading = false))
        },
        async getAllMeasures() {
            const currentContentId = this.hiddenFormDataProp.get('SBI_ARTIFACT_VERSION_ID')
            await this.$http
                .get(process.env.VUE_APP_OLAP_PATH + `1.0/designer/measures/${currentContentId}/${this.selectedCube.name}?SBI_EXECUTION_ID=${this.sbiExecutionId}`)
                .then((response: AxiosResponse<any>) => {
                    this.measures = response.data.map((cube) => ({ name: cube }))
                })
                .catch(() => {})
                .finally(() => (this.loading = false))
        },
        addNewParam() {
            const newParam = { ...descriptor.newVariable }
            this.variables.push(newParam)
        },
        removeAllParams() {
            this.$confirm.require({
                message: this.$t('managers.datasetManagement.deleteAllRequestHeaderMsg'),
                header: this.$t('managers.datasetManagement.deleteAllRequestHeaderTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => (this.variables = [])
            })
        },
        removeParam(slotProps) {
            this.variables.splice(slotProps.index, 1)
        }
    }
})
</script>

<style lang="scss">
#olap-scenario-dialog .p-dialog-header,
#olap-scenario-dialog .p-dialog-content {
    padding: 0;
}
#olap-scenario-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
