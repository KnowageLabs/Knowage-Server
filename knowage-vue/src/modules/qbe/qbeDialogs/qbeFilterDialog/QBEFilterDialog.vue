<template>
    <Dialog id="qbe-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEFilterDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('common.filters') }}
                </template>

                <template #right>
                    <i v-show="temporalFiltersEnabled" class="fa fa-calendar kn-cursor-pointer p-mr-4" @click="openTemporalFilterDialog"></i>
                    <KnFabButton icon="fas fa-plus" @click="addNewFilter"></KnFabButton>
                </template>
            </Toolbar>
        </template>

        <Message v-if="filters.length === 0 && !parameterTableVisible" class="p-m-4" severity="info" :closable="false" :style="QBEFilterDialogDescriptor.styles.message">
            {{ $t('common.info.noDataFound') }}
        </Message>
        <div v-else-if="!parameterTableVisible">
            <QBEFilterCard v-for="filter in filters" :key="filter.filterId" :propFilter="filter" :id="id" :propEntities="entities" :subqueries="filterDialogData?.query.subqueries" :field="filterDialogData?.field" :propParameters="parameters" @removeFilter="removeFilter"></QBEFilterCard>
        </div>

        <QBEFilterParameters v-else-if="parameterTableVisible" :propParameters="propParameters"></QBEFilterParameters>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ parameterTableVisible ? $t('qbe.filters.applyParameters') : $t('common.save') }}</Button>
        </template>

        <QBETemporalFilterDialog :visible="temporalFilterDialogVisible" :temporalFilters="temporalFilters" @close="temporalFilterDialogVisible = false" @save="addTemporalFilter"></QBETemporalFilterDialog>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iField, iQuery, iFilter } from '../../QBE'
import Dialog from 'primevue/dialog'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Message from 'primevue/message'
import QBEFilterCard from './QBEFilterCard.vue'
import QBEFilterDialogDescriptor from './QBEFilterDialogDescriptor.json'
import QBETemporalFilterDialog from './QBETemporalFilterDialog.vue'
import QBEFilterParameters from './QBEFilterParameters.vue'

export default defineComponent({
    name: 'qbe-filter-dialog',
    components: { Dialog, KnFabButton, Message, QBEFilterCard, QBETemporalFilterDialog, QBEFilterParameters },
    props: { visible: { type: Boolean }, filterDialogData: { type: Object as PropType<{ field: iField; query: iQuery }> }, id: { type: String }, entities: { type: Array }, propParameters: { type: Array, required: true } },
    emits: ['save', 'close'],
    data() {
        return {
            QBEFilterDialogDescriptor,
            filters: [] as iFilter[],
            nextFilterIndex: -1,
            temporalFilters: [] as any[],
            temporalFilterDialogVisible: false,
            parameters: [] as any[],
            parameterTableVisible: false
        }
    },
    watch: {
        filterDialogData() {
            this.loadData()
        },
        propParameters() {
            this.loadParameters()
        }
    },
    created() {
        this.loadData()
        this.loadParameters()
    },
    methods: {
        loadData() {
            console.log('FITLTER DIALOG DATA: ', this.filterDialogData)
            if (!this.filterDialogData || !this.filterDialogData.field || !this.filterDialogData.query) return

            this.filters = []
            this.filterDialogData.query.filters.forEach((filter: iFilter) => {
                console.log(filter.leftOperandValue + ' === ' + this.filterDialogData?.field.id)
                if (filter.leftOperandValue === this.filterDialogData?.field.id) {
                    this.filters.push({ ...filter })
                }
            })
            this.nextFilterIndex = this.getFilterNextIndex()
        },
        getFilterNextIndex() {
            let maxIndex = 0
            this.filters.forEach((filter: iFilter) => {
                if (filter.filterInd > maxIndex) maxIndex = filter.filterInd
            })
            return maxIndex + 1
        },
        loadParameters() {
            this.parameters = this.propParameters ? [...this.propParameters] : []
        },
        removeFilter(filter: iFilter) {
            // console.log('FILTER TO REMOVE: ', filter)
            const index = this.filters.findIndex((el: iFilter) => el.filterId === filter.filterId)
            if (index !== -1) this.filters.splice(index, 1)
        },
        addNewFilter() {
            const field = this.filterDialogData?.field
            console.log('FIELD: ', field)
            if (field) {
                this.filters.push({
                    filterId: 'Filter' + this.nextFilterIndex,
                    filterDescripion: 'Filter' + this.nextFilterIndex,
                    filterInd: this.nextFilterIndex,
                    promptable: false,
                    leftOperandValue: field.id,
                    leftOperandDescription: field.longDescription,
                    leftOperandLongDescription: field.longDescription,
                    leftOperandType: 'Field Content',
                    leftOperandDefaultValue: null,
                    leftOperandLastValue: null,
                    leftOperandAlias: field.alias,
                    leftOperandDataType: '',
                    operator: 'EQUALS TO',
                    rightOperandDescription: '',
                    rightOperandLongDescription: '',
                    rightOperandValue: [''],
                    rightOperandType: 'Static Content',
                    rightType: 'manual',
                    rightOperandDefaultValue: [''],
                    rightOperandLastValue: [''],
                    rightOperandAlias: '',
                    rightOperandDataType: '',
                    booleanConnector: 'AND',
                    deleteButton: false,
                    color: '',
                    entity: field.entity
                })
                this.nextFilterIndex++
            }
        },
        temporalFiltersEnabled() {
            return ((this.$store.state as any).user.functionalities.includes('Timespan') && this.filterDialogData?.field.dataType.toLowerCase() === 'java.sql.data') || this.filterDialogData?.field.dataType.toLowerCase() === 'java.sql.timestamp'
        },
        async openTemporalFilterDialog() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/timespan/listTimespan/?types=DAY_OF_WEEK&types=DAY_OF_WEEK&types=DAY_OF_WEEK`).then((response: AxiosResponse<any>) => (this.temporalFilters = response.data.data))
            this.temporalFilterDialogVisible = true

            // TODO remove mock
            if (this.temporalFilters.length === 0) {
                this.temporalFilters = [
                    {
                        staticFilter: false,
                        name: 'TEST',
                        id: 1,
                        type: 'temporal',
                        definition: [
                            {
                                from: '01/10/2019',
                                to: '31/10/2019',
                                fromLocalized: '10/01/2019',
                                toLocalized: '10/31/2019'
                            },
                            {
                                from: '04/11/2019',
                                to: '24/12/2019',
                                fromLocalized: '11/04/2019',
                                toLocalized: '12/24/2019'
                            }
                        ],
                        commonInfo: 'it.eng.spagobi.commons.metadata.SbiCommonInfo@10537804'
                    },
                    {
                        staticFilter: false,
                        name: 'Janaury 1997',
                        id: 2,
                        type: 'temporal',
                        definition: [
                            {
                                from: '01/01/1997',
                                to: '31/01/1997',
                                fromLocalized: '01/01/1997',
                                toLocalized: '01/31/1997'
                            }
                        ],
                        commonInfo: 'it.eng.spagobi.commons.metadata.SbiCommonInfo@26451795'
                    },
                    {
                        staticFilter: false,
                        name: 'LunchTime',
                        id: 3,
                        type: 'time',
                        definition: [
                            {
                                from: '12:30',
                                to: '14:00'
                            }
                        ],
                        commonInfo: 'it.eng.spagobi.commons.metadata.SbiCommonInfo@24517265'
                    }
                ]
            }

            console.log('LOADED TEMPORAL FILTERS MAIN: ', this.temporalFilters)
        },
        addTemporalFilter(temporalFilter: any) {
            console.log('TEMPORAL FILTER TO ADD: ', temporalFilter)
            for (let i = 0; i < temporalFilter.definition.length; i++) {
                this.filters.push({
                    filterId: 'Filter' + this.nextFilterIndex,
                    filterDescripion: 'Filter' + this.nextFilterIndex,
                    filterInd: this.nextFilterIndex,
                    promptable: false,
                    leftOperandValue: this.filterDialogData?.field.id,
                    leftOperandDescription: this.filterDialogData?.field.longDescription,
                    leftOperandLongDescription: this.filterDialogData?.field.longDescription,
                    leftOperandType: 'Field Content',
                    leftOperandDefaultValue: null,
                    leftOperandLastValue: null,
                    leftOperandAlias: this.filterDialogData?.field.alias,
                    leftOperandDataType: '',
                    operator: 'BETWEEN',
                    rightType: 'manual',
                    rightOperandValue: [temporalFilter.definition[i].from, temporalFilter.definition[i].to],
                    rightOperandDescription: this.createRightOperandDescription(temporalFilter.definition[i].from, temporalFilter.definition[i].to, this.filterDialogData?.field.dataType),
                    rightOperandLongDescription: this.createRightOperandDescription(temporalFilter.definition[i].from, temporalFilter.definition[i].to, this.filterDialogData?.field.dataType),
                    rightOperandType: 'Static Content',
                    rightOperandDefaultValue: [''],
                    rightOperandLastValue: [''],
                    rightOperandAlias: null,
                    rightOperandDataType: '',
                    booleanConnector: 'OR',
                    deleteButton: false
                } as any)
                this.nextFilterIndex++
            }
            this.temporalFilterDialogVisible = false
        },
        createRightOperandDescription(from: string, to: string, dataType: any) {
            if (dataType === 'java.sql.Timestamp') {
                return from + ' 00:00:00 ' + ' ---- ' + to + ' 00:00:00 '
            }
            return from + ' ---- ' + to
        },

        closeDialog() {
            this.$emit('close')
            this.nextFilterIndex = -1
            this.parameters = []
            this.parameterTableVisible = true
        },
        save() {
            if (this.propParameters.length > 0 && !this.parameterTableVisible) {
                this.parameterTableVisible = true
            } else {
                this.$emit('save', this.filters, this.filterDialogData?.field, this.parameters)
                this.parameterTableVisible = false
            }
        }
    }
})
</script>

<style lang="scss">
#qbe-filter-dialog .p-dialog-header,
#qbe-filter-dialog .p-dialog-content {
    padding: 0;
}
#qbe-filter-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
