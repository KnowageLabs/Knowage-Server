<template>
    <Dialog id="qbe-saving-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="descriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('qbe.savingDialog.title') }}
                </template>
            </Toolbar>
        </template>

        <TabView v-model:activeIndex="activeTab" class="tabview-custom" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.mondrianSchemasManagement.detail.title') }}</span>
                </template>
                <DetailTab :prop-dataset="propDataset" :scope-types="scopeTypes" :category-types="categoryTypes" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.measureDefinition.metadata') }}</span>
                </template>
                <MetadataCard :prop-metadata="propMetadata" @touched="$emit('touched')" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('qbe.savingDialog.persistence') }}</span>
                </template>

                <PersistenceTab :prop-dataset="propDataset" :scheduling-data="scheduling" />
            </TabPanel>
        </TabView>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="$emit('close')"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="buttonDisabled" @click="saveDataset"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DetailTab from './QBESavingDialogDetailTab.vue'
import PersistenceTab from './QBESavingDialogPersistence.vue'
import MetadataCard from './QbeSavingDialogMetadata.vue'
import useValidate from '@vuelidate/core'
import descriptor from './QBESavingDialogDescriptor.json'
import mainStore from '../../../../App.store'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { TabView, TabPanel, Dialog, DetailTab, PersistenceTab, MetadataCard },
    props: { propDataset: { type: Object, required: true }, propMetadata: { type: Array, required: true }, visible: Boolean },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            descriptor,
            v$: useValidate() as any,
            scopeTypes: [] as any,
            selectedDataset: {} as any,
            selectedDatasetId: null as any,
            categoryTypes: [] as any,
            fieldsMetadata: [] as any,
            scheduling: {
                repeatInterval: null as string | null
            } as any
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    watch: {
        propDataset: {
            handler() {
                this.selectedDataset = this.propDataset
                this.setEndUserScope()
            },
            deep: true
        }
    },
    created() {
        this.getDomainData()
        this.selectedDataset = this.propDataset
    },
    methods: {
        getDomainByType(type: string) {
            return this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=${type}`)
        },
        async getDomainData() {
            await this.getDomainByType('DS_SCOPE').then((response: AxiosResponse<any>) => (this.scopeTypes = response.data))
            await this.getDomainByType('DATASET_CATEGORY').then((response: AxiosResponse<any>) => (this.categoryTypes = response.data))
        },

        async saveDataset() {
            const dsToSave = { ...this.selectedDataset } as any
            dsToSave.pars ? '' : (dsToSave.pars = [])

            dsToSave.pythonEnvironment ? (dsToSave.pythonEnvironment = JSON.stringify(dsToSave.pythonEnvironment)) : ''
            dsToSave.meta ? (dsToSave.meta = await this.manageDatasetFieldMetadata(this.propMetadata)) : (dsToSave.meta = [])

            dsToSave.isScheduled ? (dsToSave.schedulingCronLine = await this.formatCronForSave()) : ''

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/datasets/`, dsToSave, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
                })
                .then((response: AxiosResponse<any>) => {
                    this.store.setInfo({ title: this.$t('common.toast.createTitle'), msg: this.$t('common.toast.success') })
                    this.selectedDataset.meta = response.data.meta
                    if (!this.selectedDataset.id) {
                        this.selectedDataset.id = response.data.id
                        this.$emit('created', response)
                    } else this.$emit('updated')
                    this.$emit('datasetSaved')
                    this.$emit('close')
                })
                .catch()
        },
        async manageDatasetFieldMetadata(metadata) {
            const metaToSave = metadata.map((meta: any) => {
                return {
                    name: meta.column,
                    displayedName: meta.fieldAlias,
                    type: meta.Type,
                    fieldType: meta.fieldType,
                    decrypt: meta.decrypt,
                    personal: meta.personal,
                    subjectId: meta.subjectId
                }
            })

            return metaToSave
        },
        async manageDatasetFieldMetadata1(fieldsColumns) {
            if (fieldsColumns.columns != undefined && fieldsColumns.columns != null) {
                const columnsArray = []

                let columnsNames = []

                for (let i = 0; i < fieldsColumns.columns.length; i++) {
                    const element = fieldsColumns.columns[i]
                    columnsNames.push(element.column)
                }

                columnsNames = this.removeDuplicates(columnsNames)

                for (let i = 0; i < columnsNames.length; i++) {
                    const columnObject = { displayedName: '', name: '', fieldType: '', type: '', personal: false, decrypt: false, subjectId: false }
                    const currentColumnName = columnsNames[i]

                    if (currentColumnName.indexOf(':') != -1) {
                        const arr = currentColumnName.split(':')
                        columnObject.displayedName = arr[1]
                    } else {
                        columnObject.displayedName = currentColumnName
                    }

                    columnObject.name = currentColumnName
                    for (let j = 0; j < fieldsColumns.columns.length; j++) {
                        const element = fieldsColumns.columns[j]
                        if (element.column == currentColumnName) {
                            if (element.pname.toUpperCase() == 'type'.toUpperCase()) {
                                columnObject.type = element.pvalue
                            } else if (element.pname.toUpperCase() == 'fieldType'.toUpperCase()) {
                                columnObject.fieldType = element.pvalue
                            } else if (element.pname.toUpperCase() == 'personal'.toUpperCase()) {
                                columnObject.personal = element.pvalue
                            } else if (element.pname.toUpperCase() == 'decrypt'.toUpperCase()) {
                                columnObject.decrypt = element.pvalue
                            } else if (element.pname.toUpperCase() == 'subjectId'.toUpperCase()) {
                                columnObject.subjectId = element.pvalue
                            }
                        }
                    }
                    columnsArray.push(columnObject)
                }

                return columnsArray
            }
        },
        removeDuplicates(array) {
            const index = {}
            for (let i = array.length - 1; i >= 0; i--) {
                if (array[i] in index) {
                    array.splice(i, 1)
                } else {
                    index[array[i]] = true
                }
            }
            return array
        },
        async formatCronForSave() {
            if (this.selectedDataset.isScheduled) {
                if (this.selectedDataset.startDate == null) {
                    this.selectedDataset.startDate = new Date()
                }
                const repeatInterval = this.scheduling.repeatInterval
                let finalCronString = ''
                const secondsForCron = 0
                const minutesForCron = this.stringifySchedulingValues(this.scheduling.minutesSelected && this.scheduling.minutesSelected.length != 0, 'minutesSelected')
                const hoursForCron = this.stringifySchedulingValues(repeatInterval != 'minute' && this.scheduling.hoursSelected && this.scheduling.hoursSelected.length != 0, 'hoursSelected')
                let daysForCron = this.stringifySchedulingValues((repeatInterval === 'day' || repeatInterval === 'month') && this.scheduling.daysSelected && this.scheduling.daysSelected.length != 0, 'daysSelected')
                const monthsForCron = this.stringifySchedulingValues(repeatInterval === 'month' && this.scheduling.monthsSelected && this.scheduling.monthsSelected.length != 0, 'monthsSelected')
                let weekdaysForCron = this.stringifySchedulingValues(repeatInterval === 'week' && this.scheduling.weekdaysSelected && this.scheduling.weekdaysSelected.length != 0, 'weekdaysSelected')

                if (daysForCron == '*' && weekdaysForCron != '*') {
                    daysForCron = '?'
                } else {
                    weekdaysForCron = '?'
                }
                finalCronString = minutesForCron + ' ' + hoursForCron + ' ' + daysForCron + ' ' + monthsForCron + ' ' + weekdaysForCron

                return secondsForCron + ' ' + finalCronString
            }
        },
        stringifySchedulingValues(condition, selectedValue) {
            let stringValue = ''
            if (condition) {
                for (let i = 0; i < this.scheduling[selectedValue].length; i++) {
                    stringValue += '' + this.scheduling[selectedValue][i]

                    if (i < this.scheduling[selectedValue].length - 1) {
                        stringValue += ','
                    }
                }
                return stringValue
            } else {
                stringValue = '*'
                return stringValue
            }
        },
        setEndUserScope() {
            if (this.selectedDataset && !this.selectedDataset.id && !(this.store.$state as any).user.functionalities.includes('QbeAdvancedSaving')) {
                const userScope = this.scopeTypes.find((scope) => scope.VALUE_CD === 'USER')
                this.selectedDataset.scopeCd = userScope.VALUE_CD
                this.selectedDataset.scopeId = userScope.VALUE_ID
            }
        }
    }
})
</script>
<style lang="scss">
#qbe-saving-dialog .p-dialog-content {
    padding: 0;
}
</style>
