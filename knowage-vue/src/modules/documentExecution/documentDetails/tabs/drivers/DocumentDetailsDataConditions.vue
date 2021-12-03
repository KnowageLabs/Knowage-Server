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
        <Listbox class="kn-list data-condition-list" :options="transformedObj" @change="openDataConditionDialog($event.value)">
            <template #empty>{{ $t('documentExecution.documentDetails.drivers.noDataCond') }} </template>
            <template #option="slotProps">
                <div class="kn-list-item">
                    <div class="kn-list-item-text">
                        <!-- {{ slotProps }} -->
                        <span class="kn-truncated" v-tooltip.top="slotProps.option[0].filterOperation + $t('documentExecution.documentDetails.drivers.dataConditionsValue') + slotProps.option[0].parFatherUrlName">
                            <b>{{ slotProps.option[0].filterOperation }} {{ $t('documentExecution.documentDetails.drivers.dataConditionsValue') }}</b> {{ slotProps.option[0].parFatherUrlName }}
                        </span>
                    </div>
                    <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteDataCondition(slotProps.index)" />
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
                <!-- {{ $t('documentExecution.documentDetails.drivers.dataHint') }} -->
                {{ selectedCondition }}
            </div>

            <form class="p-fluid p-formgrid p-grid p-m-2">
                <div class="p-field p-col-12 p-md-4">
                    <span class="p-float-label ">
                        <Dropdown id="driver" class="kn-material-input" v-model="selectedCondition[0].parFatherId" :options="filteredDrivers" optionLabel="label" optionValue="id" @change="setParFatherUrlName" />
                        <label for="driver" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.drivers.ad') }} {{ $t('documentExecution.documentDetails.drivers.adDepends') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-md-4">
                    <span class="p-float-label ">
                        <Dropdown id="filterOp" class="kn-material-input" v-model="selectedCondition[0].filterOperation" :options="availableOperators" />
                        <label for="filterOp" class="kn-material-input-label"> {{ $t('managers.businessModelManager.filterOperator') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-md-4">
                    <span class="p-float-label ">
                        <Dropdown id="logicalOp" class="kn-material-input" v-model="selectedCondition[0].logicOperator" :options="connectingOperators" />
                        <label for="logicalOp" class="kn-material-input-label"> {{ $t('managers.businessModelManager.logicOperator') }} </label>
                    </span>
                </div>
                <div v-for="(paruse, index) of driverParuses" :key="index">
                    <div class="kn-details-info-div">
                        {{ paruse }}
                    </div>
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
    components: {
        Listbox,
        Dialog,
        Dropdown
    },
    props: { availableDrivers: { type: Array as PropType<iDriver[]>, required: true }, selectedDocument: { type: Object as PropType<iDocument>, required: true }, selectedDriver: { type: Object as PropType<iDriver>, required: true } },
    emits: ['driversChanged'],
    data() {
        return {
            mainDescriptor,
            driversDescriptor,
            dataDependencyObjects: [] as any,
            dataDependenciesForDeleting: [] as any,
            filteredDrivers: [] as any,
            driverParuses: [] as any,
            availableOperators: driversDescriptor.dataOperators,
            connectingOperators: driversDescriptor.connectingOperators,
            lovIdAndColumns: [] as any,
            selectedCondition: {} as any,
            transformedObj: {} as any,
            showDataConditionDialog: false,
            loading: false
        }
    },
    watch: {
        selectedDriver() {
            this.excludeCurrentDriverFromList()
            this.getParusesByAnalyticalDriverId()
            this.getLovsByAnalyticalDriverId()
            this.selectedDriver.id ? this.getDataDependenciesByDriverId() : ''
        }
    },
    created() {
        this.excludeCurrentDriverFromList()
        this.getParusesByAnalyticalDriverId()
        this.getLovsByAnalyticalDriverId()
        this.selectedDriver.id ? this.getDataDependenciesByDriverId() : ''
    },
    methods: {
        //#region ===================== Get Dependencies and Transform them  ====================================================
        async getDataDependenciesByDriverId() {
            this.loading = true
            this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/datadependencies?driverId=${this.selectedDriver.id}`)
                .then((response: AxiosResponse<any>) => {
                    this.dataDependencyObjects[this.selectedDriver.id] = response.data
                    this.transformedObj = {} as any
                    this.transformingCorrelations(response.data)
                })
                .finally(() => (this.loading = false))
        },
        transformingCorrelations(correlations, transformKey?, fromPost?) {
            // eslint-disable-next-line no-prototype-builtins
            if (this.transformedObj[transformKey] && !fromPost) {
                delete this.transformedObj[transformKey]
            }
            for (var i = 0; i < correlations.length; i++) {
                var fatherIdfilterOperation = correlations[i].parFatherId + correlations[i].filterOperation

                if (this.transformedObj[fatherIdfilterOperation] == undefined) {
                    this.transformedObj[fatherIdfilterOperation] = []
                }
                if (correlations[i].id && correlations[i].deleteItem == undefined) {
                    this.transformedObj[fatherIdfilterOperation].push(correlations[i])
                }
            }
            if (this.transformedObj[fatherIdfilterOperation] != undefined && this.transformedObj[fatherIdfilterOperation].length == 0) {
                delete this.transformedObj[fatherIdfilterOperation]
            }

            return this.transformedObj
        },
        //#endregion ===============================================================================================
        async getParusesByAnalyticalDriverId() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analyticalDrivers/${this.selectedDriver.parID}/modes`).then((response: AxiosResponse<any>) => (this.driverParuses = response.data))
        },

        //#region ===================== Get Lovs for Dropdown and set the values  ====================================================
        async getLovsByAnalyticalDriverId() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analyticalDrivers/${this.selectedDriver.parID}/lovs`).then((response: AxiosResponse<any>) => {
                for (var i = 0; i < response.data.length; i++) {
                    this.lovIdAndColumns.push(this.setLovColumns(response.data[i]))
                }
            })
        },
        setLovColumns(lov) {
            var lovIdAndColumns = {} as any
            var lovColumns = [] as any
            var lovObject = JSON.parse(lov.lovProviderJSON)
            if (lovObject != [] && lovObject.QUERY) {
                var visibleColumns = lovObject.QUERY['VISIBLE-COLUMNS']
                var invisibleColumns = lovObject.QUERY['INVISIBLE-COLUMNS']

                var visibleColumnsArr = visibleColumns ? visibleColumns.split(',') : []
                var invisibleColumnsArr = invisibleColumns ? invisibleColumns.split(',') : []
                for (var i of visibleColumnsArr) {
                    lovColumns.push(i)
                }
                for (var j of invisibleColumnsArr) {
                    lovColumns.push(j)
                }

                lovIdAndColumns.id = lov.id
                lovIdAndColumns.columns = lovColumns
                    .filter(function(el) {
                        return '' !== el
                    })
                    .sort()
            }
            return lovIdAndColumns
        },
        //#endregion ===============================================================================================

        getLovColumnsForParuse(paruse) {
            for (var i = 0; i < this.lovIdAndColumns.length; i++) {
                if (paruse.idLov == this.lovIdAndColumns[i].id) {
                    if (this.lovIdAndColumns[i].columns != undefined) {
                        return this.lovIdAndColumns[i].columns
                    } else return ['VALUE', 'DESCRIPTION']
                }
            }
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
            condition != 'newCondition' ? (this.selectedCondition = { ...condition }) : (this.selectedCondition = [{ parId: this.selectedDriver.id, filterOperation: this.availableOperators[0], logicOperator: this.connectingOperators[0] }])
            this.getLovColumnsForParuse(this.selectedCondition)
            this.showDataConditionDialog = true
        },

        //#region ===================== Delete Functionality  ====================================================
        deleteDataCondition(transformKey) {
            this.dataDependenciesForDeleting = [...this.transformedObj[transformKey]]
            this.deleteDataDependencies(this.selectedDocument.id)
        },
        async deleteDataDependencies(driverableObjectId) {
            for (var i = 0; i < this.dataDependenciesForDeleting.length; i++) {
                delete this.dataDependenciesForDeleting[i].deleteItem
                console.log(driverableObjectId)
                await this.deleteDriverDataDependency(this.dataDependenciesForDeleting[i])
            }
            this.dataDependenciesForDeleting = []
        },
        async deleteDriverDataDependency(dataDependency) {
            this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/datadependencies/delete`, dataDependency)
                .then(() => {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                })
                .catch((error) => {
                    this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: error })
                })
        }
        //#endregion ===============================================================================================
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
