<template>
    <div v-if="!loading" class="data-condition-container p-col">
        <Toolbar class="kn-toolbar kn-toolbar--default">
            <template #left>
                {{ $t('documentExecution.documentDetails.drivers.conditionsTitle') }}
            </template>
            <template #right>
                <Button :label="$t('managers.businessModelManager.addCondition')" class="p-button-text p-button-rounded p-button-plain" :style="mainDescriptor.style.white" @click="openDataConditionDialog('newCondition')" />
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <Listbox class="kn-list data-condition-list" :options="dataDependencyObjects" @change="openDataConditionDialog($event.value)">
            <template #empty>{{ $t('documentExecution.documentDetails.drivers.noDataCond') }} </template>
            <template #option="slotProps">
                <div class="kn-list-item">
                    <div class="kn-list-item-text">
                        <span class="kn-truncated" v-tooltip.top="slotProps.option.filterOperation + $t('documentExecution.documentDetails.drivers.conditionsTitle') + slotProps.option.parFatherUrlName">
                            <b>{{ slotProps.option.filterOperation }}{{ $t('documentExecution.documentDetails.drivers.conditionsTitle') }}</b> {{ slotProps.option.parFatherUrlName }}
                        </span>
                    </div>
                    <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteCondition(slotProps.option)" />
                </div>
            </template>
        </Listbox>

        <Dialog class="remove-padding" :style="driversDescriptor.style.conditionDialog" :visible="showDataConditionDialog" :modal="true" :closable="false">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--primary" :style="mainDescriptor.style.width100">
                    <template #left>
                        {{ $t('documentExecution.documentDetails.drivers.visualizationTitle') }}
                    </template>
                </Toolbar>
            </template>

            <div class="kn-details-info-div">
                {{ $t('documentExecution.documentDetails.drivers.dataHint') }}
            </div>

            <form class="p-fluid p-formgrid p-grid p-m-2">
                {{ selectedCondition }}
                <div class="p-field p-col-12 p-md-4">
                    <span class="p-float-label ">
                        <Dropdown id="driver" class="kn-material-input" v-model="selectedCondition.parFatherId" :options="filteredDrivers" optionLabel="label" optionValue="id" @change="setParFatherUrlName" />
                        <label for="driver" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.drivers.ad') }} {{ $t('documentExecution.documentDetails.drivers.adDepends') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-md-4">
                    <span class="p-float-label ">
                        <Dropdown id="filterOp" class="kn-material-input" v-model="selectedCondition.filterOperation" :options="availableOperators" />
                        <label for="filterOp" class="kn-material-input-label"> {{ $t('managers.businessModelManager.filterOperator') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-md-4">
                    <span class="p-float-label ">
                        <Dropdown id="logicalOp" class="kn-material-input" v-model="selectedCondition.logicOperator" :options="connectingOperators" />
                        <label for="logicalOp" class="kn-material-input-label"> {{ $t('managers.businessModelManager.logicOperator') }} </label>
                    </span>
                </div>
            </form>

            <template #footer>
                <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="showDataConditionDialog = false" />
                <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="saveCondition" />
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDriver, iDocument } from '@/modules/documentExecution/documentDetails/DocumentDetails'
import { AxiosResponse } from 'axios'
import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
import driversDescriptor from './DocumentDetailsDriversDescriptor.json'
import Listbox from 'primevue/listbox'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'document-drivers',
    components: { Listbox, Dialog, Dropdown },
    props: { availableDrivers: { type: Array as PropType<iDriver[]>, required: true }, selectedDocument: { type: Object as PropType<iDocument>, required: true }, selectedDriver: { type: Object as PropType<iDriver>, required: true } },
    emits: ['driversChanged'],
    data() {
        return {
            mainDescriptor,
            driversDescriptor,
            dataDependencyObjects: [] as any,
            filteredDrivers: [] as any,
            driverParuses: [] as any,
            availableOperators: driversDescriptor.dataOperators,
            connectingOperators: driversDescriptor.connectingOperators,
            selectedCondition: {} as any,
            showDataConditionDialog: false,
            loading: false
        }
    },
    watch: {
        selectedDriver() {
            this.selectedDriver.id ? this.getDataDependenciesByDriverId() : ''
            this.excludeCurrentDriverFromList()
        }
    },
    created() {
        this.selectedDriver.id ? this.getDataDependenciesByDriverId() : ''
        this.excludeCurrentDriverFromList()
    },
    methods: {
        async getDataDependenciesByDriverId() {
            this.loading = true
            this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/datadependencies?driverId=${this.selectedDriver.id}`)
                .then((response: AxiosResponse<any>) => {
                    this.dataDependencyObjects = response.data
                })
                .finally(() => (this.loading = false))
        },
        async getParusesByAnalyticalDriverId() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analyticalDrivers/${this.selectedDriver.parID}/modes`).then((response: AxiosResponse<any>) => (this.driverParuses = response.data))
        },
        excludeCurrentDriverFromList() {
            this.filteredDrivers = this.availableDrivers.filter((driver) => driver.id != this.selectedDriver.id)
        },
        setParFatherUrlName(event) {
            this.availableDrivers.filter((driver) => {
                driver.id === event.value ? (this.selectedCondition.parFatherUrlName = driver.parameterUrlName) : ''
            })
        },
        openDataConditionDialog(condition?) {
            condition != 'newCondition' ? (this.selectedCondition = { ...condition }) : (this.selectedCondition = { parId: this.selectedDriver.id } as any)
            this.showDataConditionDialog = true
        }
    }
})
</script>
<style lang="scss" scoped>
.data-condition-container {
    :deep(.p-card-body) {
        padding: 0;
        .p-card-content {
            padding: 0;
        }
    }
    .data-condition-list {
        border: 1px solid $color-borders;
        border-top: none;
    }
}
</style>
