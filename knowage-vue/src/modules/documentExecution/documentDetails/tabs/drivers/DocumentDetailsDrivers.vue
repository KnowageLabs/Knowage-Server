<template>
    <div class="p-grid p-m-0" :style="mainDescriptor.style.flexOne">
        <div class="p-col-4 p-sm-4 p-md-3 p-p-0" :style="mainDescriptor.style.flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('documentExecution.documentDetails.drivers.title') }}
                </template>
                <template #right>
                    <Button :label="$t('common.add')" class="p-button-text p-button-rounded p-button-plain" :style="mainDescriptor.style.white" @click="addDriver" />
                </template>
            </Toolbar>
            <div id="drivers-list-container" :style="mainDescriptor.style.flexOneRelative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <KnListBox :style="mainDescriptor.style.height100" :options="drivers" :settings="driversDescriptor.knListSettings" @click="selectDriver($event.item)" @delete.stop="deleteDriverConfirm($event)"></KnListBox>
                </div>
            </div>
        </div>
        <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0" :style="mainDescriptor.style.driverDetailsContainer">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('documentExecution.documentDetails.drivers.detailsTitle') }}
                </template>
            </Toolbar>
            <div id="driver-details-container" class="p-m-2" :style="mainDescriptor.style.flexOneRelative">
                <div class="kn-details-info-div" v-if="Object.keys(selectedDriver).length === 0">
                    {{ $t('documentExecution.documentDetails.drivers.noDriverSelected') }}
                </div>
                <Card v-else>
                    <template #content>
                        {{ selectedDriver }}
                        <form class="p-fluid p-formgrid p-grid p-m-1">
                            <div class="p-field p-col-12">
                                <span class="p-float-label">
                                    <InputText
                                        id="label"
                                        class="kn-material-input"
                                        type="text"
                                        maxLength="40"
                                        v-model="v$.selectedDriver.label.$model"
                                        :class="{
                                            'p-invalid': v$.selectedDriver.label.$invalid && v$.selectedDriver.label.$dirty
                                        }"
                                        @blur="v$.selectedDriver.label.$touch()"
                                        @change="addToChangedDrivers(selectedDriver)"
                                    />
                                    <label for="label" class="kn-material-input-label"> {{ $t('common.title') }} * </label>
                                </span>
                                <KnValidationMessages class="p-mt-1" :vComp="v$.selectedDriver.label" :additionalTranslateParams="{ fieldName: $t('common.title') }" />
                            </div>
                            <div class="p-field p-col-12">
                                <span class="p-float-label">
                                    <Dropdown
                                        id="analytical"
                                        class="kn-material-input"
                                        v-model="v$.selectedDriver.parameter.$model"
                                        :options="availableAnalyticalDrivers"
                                        :class="{
                                            'p-invalid': v$.selectedDriver.parameter.$invalid && v$.selectedDriver.parameter.$dirty
                                        }"
                                        optionLabel="label"
                                        :filter="true"
                                        :filterPlaceholder="$t('documentExecution.documentDetails.drivers.dropdownSearchHint')"
                                        @blur="v$.selectedDriver.parameter.$touch()"
                                        @change="addToChangedDrivers(selectedDriver)"
                                    >
                                        <template #value="slotProps">
                                            <div class="p-dropdown-driver-value" v-if="slotProps.value">
                                                <span>{{ slotProps.value.label }}</span>
                                            </div>
                                            <span v-else>
                                                {{ $t('common.info.noDataFound') }}
                                            </span>
                                        </template>
                                        <template #option="slotProps">
                                            <div class="p-dropdown-driver-option">
                                                <span>{{ slotProps.option.label }}</span>
                                            </div>
                                        </template>
                                    </Dropdown>
                                    <label for="analytical" class="kn-material-input-label"> {{ $t('managers.businessModelManager.analyticalDriver') }} </label>
                                </span>
                                <KnValidationMessages class="p-mt-1" :vComp="v$.selectedDriver.parameter" :additionalTranslateParams="{ fieldName: $t('managers.businessModelManager.analyticalDriver') }" />
                            </div>
                            <div class="p-field p-col-12">
                                <span class="p-float-label">
                                    <InputText
                                        id="parameterUrlName"
                                        class="kn-material-input"
                                        type="text"
                                        maxLength="20"
                                        v-model="v$.selectedDriver.parameterUrlName.$model"
                                        :class="{
                                            'p-invalid': v$.selectedDriver.parameterUrlName.$invalid && v$.selectedDriver.parameterUrlName.$dirty
                                        }"
                                        @blur="v$.selectedDriver.parameterUrlName.$touch()"
                                        @change="addToChangedDrivers(selectedDriver)"
                                    />
                                    <label for="parameterUrlName" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.drivers.parameterUrlName') }} * </label>
                                </span>
                                <KnValidationMessages class="p-mt-1" :vComp="v$.selectedDriver.label" :additionalTranslateParams="{ fieldName: $t('documentExecution.documentDetails.drivers.parameterUrlName') }" />
                            </div>
                            <span class="p-field p-col-12 p-md-4 p-jc-center p-mt-3">
                                <InputSwitch id="visible" v-model="selectedDriver.visible" @change="addToChangedDrivers(selectedDriver)" />
                                <i class="far fa-eye p-ml-2" />
                                <label for="visible" class="kn-material-input-label p-ml-2"> {{ $t('common.visible') }} </label>
                            </span>
                            <span class="p-field p-col-12 p-md-4 p-jc-center p-mt-3">
                                <InputSwitch id="required" v-model="selectedDriver.required" @change="addToChangedDrivers(selectedDriver)" />
                                <i class="fas fa-asterisk p-ml-2" />
                                <label for="required" class="kn-material-input-label p-ml-2"> {{ $t('common.required') }} </label>
                            </span>
                            <span class="p-field p-col-12 p-md-4 p-jc-center p-mt-3">
                                <InputSwitch id="multivalue" v-model="selectedDriver.multivalue" @change="addToChangedDrivers(selectedDriver)" />
                                <i class="fas fa-list p-ml-2" />
                                <label for="multivalue" class="kn-material-input-label p-ml-2"> {{ $t('managers.businessModelManager.multivalue') }} </label>
                            </span>
                        </form>
                    </template>
                </Card>
                <div v-if="drivers.length > 1 && selectedDriver.id" class="p-grid p-mt-1">
                    <DataConditions :availableDrivers="drivers" :selectedDocument="selectedDocument" :selectedDriver="selectedDriver" />
                    <VisibilityConditions v-if="selectedDocument.engine" :availableDrivers="drivers" :selectedDocument="selectedDocument" :selectedDriver="selectedDriver" />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { iDriver, iAnalyticalDriver, iDocument } from '@/modules/documentExecution/documentDetails/DocumentDetails'
import { createValidations } from '@/helpers/commons/validationHelper'
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
import driversDescriptor from './DocumentDetailsDriversDescriptor.json'
import DataConditions from './DocumentDetailsDataConditions.vue'
import VisibilityConditions from './DocumentDetailsVisibilityConditions.vue'
import useValidate from '@vuelidate/core'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import InputSwitch from 'primevue/inputswitch'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'document-drivers',
    components: { DataConditions, VisibilityConditions, KnListBox, KnValidationMessages, InputSwitch, Dropdown },
    props: { selectedDocument: { type: Object as PropType<iDocument>, required: true }, availableDrivers: { type: Array as PropType<iDriver[]>, required: true }, availableAnalyticalDrivers: { type: Array as PropType<iAnalyticalDriver[]>, required: true } },
    emits: ['driversChanged'],
    data() {
        return {
            mainDescriptor,
            driversDescriptor,
            v$: useValidate() as any,
            drivers: [] as iDriver[],
            driversToChange: [] as iDriver[],
            driversToDelete: [] as iDriver[],
            selectedDriver: {} as iDriver,
            driverParuses: [] as any,
            lovIdAndColumns: [] as any,
            visusalDependencyObjects: [] as any,
            dataDependencyObjects: [] as any,
            transformedObj: {} as any
        }
    },
    created() {
        this.drivers = this.availableDrivers
    },
    validations() {
        const validationObject = { selectedDriver: createValidations('driver', driversDescriptor.validations.driver) }
        return validationObject
    },
    methods: {
        async getDataDependenciesByDriverId() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/datadependencies?driverId=${this.selectedDriver.id}`).then((response: AxiosResponse<any>) => {
                this.dataDependencyObjects = response.data
                this.transformedObj = {}
                console.log('getDataDependenciesByDriverId', this.dataDependencyObjects)
                // this.transformingCorrelations(response.data)
            })
        },
        // transformingCorrelations(correlations, transformKey?, fromPost?) {
        //     // eslint-disable-next-line no-prototype-builtins
        //     if (this.transformedObj[transformKey] && !fromPost) {
        //         delete this.transformedObj[transformKey]
        //     }
        //     for (var i = 0; i < correlations.length; i++) {
        //         var fatherIdfilterOperation = correlations[i].parFatherId + correlations[i].filterOperation

        //         if (this.transformedObj[fatherIdfilterOperation] == undefined) {
        //             this.transformedObj[fatherIdfilterOperation] = []
        //         }
        //         if (correlations[i].id && correlations[i].deleteItem == undefined) {
        //             this.transformedObj[fatherIdfilterOperation].push(correlations[i])
        //         }
        //     }
        //     if (this.transformedObj[fatherIdfilterOperation] != undefined && this.transformedObj[fatherIdfilterOperation].length == 0) delete this.transformedObj[fatherIdfilterOperation]

        //     console.log('transformingCorrelations', this.transformedObj)
        //     return this.transformedObj
        // },
        async getParusesByAnalyticalDriverId() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analyticalDrivers/${this.selectedDriver.parID}/modes`).then((response: AxiosResponse<any>) => (this.driverParuses = response.data))
        },
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
            console.log(lovIdAndColumns)
            return lovIdAndColumns
        },
        selectDriver(driver) {
            this.selectedDriver = driver
            this.setParameterInfo(this.selectedDriver)
            if (this.selectedDriver.parID) {
                this.getParusesByAnalyticalDriverId()
                this.getLovsByAnalyticalDriverId()
            }
            if (this.selectedDriver.id) {
                // this.getDataDependenciesByDriverId()
            }
        },
        setQuerryParameters(driverID) {
            return '?driverId=' + driverID
        },
        setParameterInfo(driver) {
            console.log('etParameterInfo(driver) {')
            if (this.availableAnalyticalDrivers) {
                for (var i = 0; i < this.availableAnalyticalDrivers.length; i++) {
                    if ((driver.parameter && this.availableAnalyticalDrivers[i].id == driver.parID) || (driver.parameter && this.availableAnalyticalDrivers[i].name == driver.parameter.name)) {
                        driver.parameter = { ...this.availableAnalyticalDrivers[i] }
                        driver.parID = this.availableAnalyticalDrivers[i].id
                    }
                }
            }
        },
        addDriver() {
            if (this.selectedDocument.modelLocked) {
                if (this.selectedDocument.id) {
                    if (this.drivers) {
                        this.drivers.push({ label: '', parameter: {} as any, parameterUrlName: '', priority: this.drivers.length == 0 ? 1 : this.drivers.length + 1, newDriver: 'true', biMetaModelID: this.selectedDocument.id, visible: true, required: true, multivalue: false } as iDriver)
                        var index = this.drivers.length
                        this.selectDriver(this.drivers[index - 1])
                    } else {
                        this.drivers = [{ label: '', parameter: {} as any, parameterUrlName: '', priority: 1, newDriver: 'true', biMetaModelID: this.selectedDocument.id, visible: true, required: true, multivalue: false } as iDriver]
                        this.selectDriver(this.drivers[1])
                    }
                }
            } else {
                this.transformedObj = {}
                if (this.selectedDocument.id) {
                    if (this.drivers) {
                        this.drivers.push({ label: '', parameter: {} as any, parameterUrlName: '', priority: this.drivers.length == 0 ? 1 : this.drivers.length + 1, newDriver: 'true', biObjectID: this.selectedDocument.id, visible: true, required: true, multivalue: false } as iDriver)
                        index = this.drivers.length
                        this.selectDriver(this.drivers[index - 1])
                    } else {
                        this.drivers = [{ label: '', parameter: {} as any, parameterUrlName: '', priority: 1, newDriver: 'true', biObjectID: this.selectedDocument.id, visible: true, required: true, multivalue: false } as iDriver]
                        this.selectDriver(this.drivers[1])
                    }
                }
            }
        },
        addToChangedDrivers(driver) {
            this.setInfoForChangedDriver(driver)
            this.driversToChange.indexOf(driver) == -1 ? this.driversToChange.push(driver) : ''
            console.log('changed One', driver)
            console.log('all changed drivers', this.driversToChange)
            this.$emit('driversChanged')
        },
        setInfoForChangedDriver(driver) {
            if (this.availableAnalyticalDrivers) {
                for (var i = 0; i < this.availableAnalyticalDrivers.length; i++) {
                    if (driver.parameter && this.availableAnalyticalDrivers[i].label == driver.parameter.label) {
                        driver.parameter = { ...this.availableAnalyticalDrivers[i] }
                        driver.parID = this.availableAnalyticalDrivers[i].id
                    }
                }
            }
        },
        movePriority(priority, direction) {
            // priority = event.item.priority
            var cur, next, prev
            for (var p in this.drivers) {
                if (this.drivers[p].priority == priority) cur = p
                if (direction == 'up' && this.drivers[p].priority == priority - 1) prev = p
                this.addToChangedDrivers(this.drivers[p])
                if (direction == 'down' && this.drivers[p].priority == priority + 1) next = p
                this.addToChangedDrivers(this.drivers[p])
            }
            if (direction == 'up') {
                this.drivers[cur].priority--
                this.drivers[prev].priority++
                this.addToChangedDrivers(this.drivers[cur])
                this.addToChangedDrivers(this.drivers[prev])
            }
            if (direction == 'down') {
                this.drivers[cur].priority++
                this.drivers[next].priority--
                this.addToChangedDrivers(this.drivers[cur])
                this.addToChangedDrivers(this.drivers[prev])
            }
        },
        deleteDriverConfirm(event) {
            this.$confirm.require({
                header: this.$t('common.toast.deleteConfirmTitle'),
                message: this.$t('documentExecution.documentDetails.drivers.deleteMessage'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.addDriverForDeletion(event.item)
            })
        },
        addDriverForDeletion(driver) {
            for (var i = 0; i < this.drivers.length; i++) {
                if (this.drivers[i].id == driver.id) {
                    if (!driver.newDriver) {
                        this.driversToDelete.push(driver)
                    }
                    this.drivers.splice(i, 1)
                }
            }
            if (this.drivers.length > 0) {
                var priorityOfDeletedDriver = driver.priority
                for (var d in this.drivers) {
                    if (this.drivers[d].priority > priorityOfDeletedDriver) {
                        this.drivers[d].priority--
                    }
                }
            }
            console.log('deleted: ', driver)
            console.log('all to delete: ', this.driversToDelete)
            console.log('check priority: ', this.drivers)
        },
        logEvent(event) {
            console.log(event)
        }
    }
})
</script>
<style lang="scss">
.kn-details-info-div {
    margin: 8px !important;
    border: 1px solid rgba(204, 204, 204, 0.6);
    padding: 8px;
    background-color: #e6e6e6;
    text-align: center;
    position: relative;
    text-transform: uppercase;
    font-size: 0.8rem;
}
</style>
