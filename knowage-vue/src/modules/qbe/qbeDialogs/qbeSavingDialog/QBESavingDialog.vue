<template>
    <Dialog id="qbe-saving-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="descriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('qbe.savingDialog.title') }}
                </template>
            </Toolbar>
        </template>

        <TabView class="tabview-custom" v-model:activeIndex="activeTab" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.mondrianSchemasManagement.detail.title') }}</span>
                </template>
                <DetailTab :propDataset="propDataset" :scopeTypes="scopeTypes" :categoryTypes="categoryTypes" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.measureDefinition.metadata') }}</span>
                </template>
                <MetadataCard :selectedDataset="propDataset" @touched="$emit('touched')" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('qbe.savingDialog.persistence') }}</span>
                </template>

                <PersistenceTab :propDataset="propDataset" :schedulingData="scheduling" />
            </TabPanel>
        </TabView>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="$emit('close')"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary"> {{ $t('common.save') }}</Button>
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
import MetadataCard from '@/modules/managers/datasetManagement/detailView/metadataCard/DatasetManagementMetadataCard.vue'
import descriptor from './QBESavingDialogDescriptor.json'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { TabView, TabPanel, Dialog, DetailTab, PersistenceTab, MetadataCard },
    props: { propDataset: Object, visible: Boolean },
    data() {
        return {
            descriptor,
            scopeTypes: [] as any,
            categoryTypes: [] as any,
            scheduling: {
                repeatInterval: null as String | null
            } as any
        }
    },
    created() {
        this.getDomainData()
    },
    methods: {
        getDomainByType(type: string) {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=${type}`)
        },
        async getDomainData() {
            this.getDomainByType('DS_SCOPE').then((response: AxiosResponse<any>) => (this.scopeTypes = response.data))
            this.getDomainByType('CATEGORY_TYPE').then((response: AxiosResponse<any>) => (this.categoryTypes = response.data))
        }
        //#region TODO: Ove 2 metode su potrebne da bi se sacuvao scheduling, importovati dje god budemo cuvali dataset
        //  async formatCronForSave() {
        //     if (this.selectedDataset.isScheduled) {
        //         if (this.selectedDataset.startDate == null) {
        //             this.selectedDataset.startDate = new Date()
        //         }
        //         var repeatInterval = this.scheduling.repeatInterval
        //         var finalCronString = ''
        //         var secondsForCron = 0
        //         var minutesForCron = this.stringifySchedulingValues(this.scheduling.minutesSelected && this.scheduling.minutesSelected.length != 0, 'minutesSelected')
        //         var hoursForCron = this.stringifySchedulingValues(repeatInterval != 'minute' && this.scheduling.hoursSelected && this.scheduling.hoursSelected.length != 0, 'hoursSelected')
        //         var daysForCron = this.stringifySchedulingValues((repeatInterval === 'day' || repeatInterval === 'month') && this.scheduling.daysSelected && this.scheduling.daysSelected.length != 0, 'daysSelected')
        //         var monthsForCron = this.stringifySchedulingValues(repeatInterval === 'month' && this.scheduling.monthsSelected && this.scheduling.monthsSelected.length != 0, 'monthsSelected')
        //         var weekdaysForCron = this.stringifySchedulingValues(repeatInterval === 'week' && this.scheduling.weekdaysSelected && this.scheduling.weekdaysSelected.length != 0, 'weekdaysSelected')

        //         if (daysForCron == '*' && weekdaysForCron != '*') {
        //             daysForCron = '?'
        //         } else {
        //             weekdaysForCron = '?'
        //         }
        //         finalCronString = minutesForCron + ' ' + hoursForCron + ' ' + daysForCron + ' ' + monthsForCron + ' ' + weekdaysForCron

        //         return secondsForCron + ' ' + finalCronString
        //     }
        // },
        // stringifySchedulingValues(condition, selectedValue) {
        //     var stringValue = ''
        //     if (condition) {
        //         for (var i = 0; i < this.scheduling[selectedValue].length; i++) {
        //             stringValue += '' + this.scheduling[selectedValue][i]

        //             if (i < this.scheduling[selectedValue].length - 1) {
        //                 stringValue += ','
        //             }
        //         }
        //         return stringValue
        //     } else {
        //         stringValue = '*'
        //         return stringValue
        //     }
        // },
        //#endregion ===============================================================================================
    }
})
</script>
<style lang="scss">
#qbe-saving-dialog .p-dialog-content {
    padding: 0;
}
</style>
