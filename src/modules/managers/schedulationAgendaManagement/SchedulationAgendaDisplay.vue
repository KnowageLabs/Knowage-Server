<template>
    <div class="parent-card p-d-flex p-flex-column">
        <span class="p-d-flex flex-no-grow p-mx-2" :style="schedulationAgendaDescriptor.span.style">
            <SelectButton v-model="selectedDisplayMode" class="p-m-1" :options="displayModes" option-label="name" data-test="select-button" />
        </span>
        <div class="flex-grow overflow-auto">
            <template v-for="dataItem in groupedItemList" :key="dataItem">
                <Card class="p-mx-3 p-my-1">
                    <template #content>
                        <div class="p-grid p-m-0">
                            <div class=" p-col-3">
                                <div v-if="displayMode === 'time'" class="p-d-flex p-jc-center p-ai-center">
                                    <div class="p-d-flex p-mx-3">
                                        <p class="p-text-bold p-text-center p-large">{{ dataItem.day }}</p>
                                    </div>
                                    <div class="p-d-flex p-flex-column p-t-2">
                                        <p class="p-text-left p-text-bold p-m-1 ">{{ dataItem.dayOfWeek }}</p>
                                        <p class="p-text-left p-m-1">{{ dataItem.monthName + ' ' + dataItem.year }}</p>
                                    </div>
                                </div>
                                <div v-if="displayMode === 'package'" class="p-d-flex p-jc-center p-ai-center">
                                    <div class="p-d-flex">
                                        <h2>{{ dataItem.jobName }}</h2>
                                    </div>
                                </div>
                                <div v-if="displayMode === 'document'" class="p-d-flex p-jc-center p-ai-center">
                                    <div class="p-d-flex">
                                        <h2>{{ dataItem.document }}</h2>
                                    </div>
                                </div>
                            </div>
                            <div class="p-col-9">
                                <DataTable v-model:expandedRows="expandedRows" :value="dataItem.executions" class="custom-row-border p-datatable-sm kn-table" data-key="id" :rows="10" :row-class="rowClass" responsive-layout="stack" breakpoint="960px" data-test="data-table">
                                    <template #empty>
                                        {{ $t('common.info.noDataFound') }}
                                    </template>

                                    <Column field="date" :style="schedulationAgendaDescriptor.table.dateColumn.style">
                                        <template #body="slotProps">
                                            <div v-if="displayMode == 'time'" class="p-pl-1 color-left-border" :style="{ borderLeftColor: getRandomColor(slotProps.data.jobName) }">
                                                {{ new Date(slotProps.data.date).getHours() + ':' + new Date(slotProps.data.date).getMinutes() }}
                                            </div>
                                            <div v-if="displayMode == 'package' || displayMode == 'document'" class="p-pl-1 color-left-border" :style="{ borderLeftColor: getRandomColor(slotProps.data.jobName) }">
                                                {{ slotProps.data.date }}
                                            </div>
                                        </template></Column
                                    >
                                    <Column field="jobName"></Column>
                                    <Column field="numberOfDocuments" :style="schedulationAgendaDescriptor.table.badgeColumn.style">
                                        <template #body="slotProps">
                                            <Badge :value="slotProps.data.numberOfDocuments"></Badge>
                                        </template>
                                    </Column>
                                    <Column :style="schedulationAgendaDescriptor.table.rowExpanderColumn.style" :expander="true" />
                                    <Column :style="schedulationAgendaDescriptor.table.iconColumn.style">
                                        <template #body="slotProps">
                                            <Button icon="pi pi-pencil" class="p-button-link" :data-test="'action-button'" @click="openRedirection(slotProps.data.jobName)" />
                                        </template>
                                    </Column>
                                    <template #expansion="slotProps">
                                        <div>
                                            <DataTable :value="slotProps.data.documents">
                                                <Column>
                                                    <template #body>
                                                        <Button icon="pi pi-book" class="p-button-link" />
                                                    </template>
                                                </Column>
                                                <Column :style="schedulationAgendaDescriptor.table.iconColumn.style">
                                                    <template #body>
                                                        <Button icon="pi pi-document" class="p-button-link" />
                                                    </template>
                                                </Column>
                                                <Column>
                                                    <template #body="slotProps">
                                                        {{ slotProps.data }}
                                                    </template>
                                                </Column>
                                            </DataTable>
                                        </div>
                                    </template>
                                </DataTable>
                            </div>
                        </div>
                    </template>
                </Card>
            </template>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iSchedulation, iGroupDataItem, iDisplayMode } from './SchedulationAgenda'
import DataTable from 'primevue/datatable'
import Badge from 'primevue/badge'
import Column from 'primevue/column'
import SelectButton from 'primevue/selectbutton'
import schedulationAgendaDescriptor from './SchedulationAgendaDescriptor.json'
import { formatDate, formatDateWithLocale } from '@/helpers/commons/localeHelper'
import Card from 'primevue/card'
import moment from 'moment'

export default defineComponent({
    name: 'schedulation-agenda-display',
    components: {
        Card,
        Badge,
        Column,
        DataTable,
        SelectButton
    },
    props: {
        itemList: Array
    },
    data() {
        return {
            selectedItem: {},
            displayMode: 'time',
            expandedRows: [],
            schedulationAgendaDescriptor,
            dataItemList: [] as iSchedulation[],
            groupedItemList: [] as iGroupDataItem[],
            colorDictionary: {},
            selectedDisplayMode: null as iDisplayMode | null,
            displayModes: [
                { name: this.$t('managers.schedulationAgendaManagement.detail.time'), code: 'time' },
                { name: this.$t('managers.schedulationAgendaManagement.detail.package'), code: 'package' },
                { name: this.$t('managers.schedulationAgendaManagement.detail.document'), code: 'document' }
            ]
        }
    },
    watch: {
        itemList() {
            this.dataItemList = this.itemList as iSchedulation[]
            if (this.displayMode) {
                this.updateDisplayData(this.displayMode)
            }
        },
        selectedDisplayMode() {
            if (this.selectedDisplayMode) {
                this.displayMode = this.selectedDisplayMode.code
                this.updateDisplayData(this.displayMode)
            }
        }
    },
    methods: {
        updateDisplayData(displayType: string) {
            this.runDisplay(displayType)
        },
        runDisplay(displayType: string) {
            this.groupedItemList = [] as iGroupDataItem[]
            this.displayMode = displayType
            switch (displayType) {
                case 'time':
                    this.groupedItemList = this.createTimeItemList()
                    break
                case 'package':
                    this.groupedItemList = this.createPackageItemList()
                    break
                case 'document':
                    this.groupedItemList = this.createDocumentItemList()
                    break
            }
        },
        openRedirection(jobName: string) {
            if (jobName) {
                this.$router.push(`/scheduler/edit-package-schedule?id=${jobName}&clone=false`)
            }
        },
        getRandomColor(inputName: any) {
            let currentColor = this.colorDictionary[inputName]
            if (!currentColor) {
                const letters = '0123456789ABCDEF'
                let color = '#'
                for (let i = 0; i < 6; i++) {
                    color += letters[Math.floor(Math.random() * 16)]
                }
                currentColor = color
                this.colorDictionary[inputName] = currentColor
            }
            return currentColor
        },
        createDocumentItemList(): iGroupDataItem[] {
            let index = 0
            const groupedItemList: iGroupDataItem[] = []
            for (let i = 0; i < this.dataItemList.length; i++) {
                if (this.dataItemList[i].triggers && this.dataItemList[i].triggers[0].executions) {
                    for (let j = 0; j < this.dataItemList[i].triggers.length; j++) {
                        for (let k = 0; k < this.dataItemList[i].triggers[j].executions.length; k++) {
                            for (let l = 0; l < this.dataItemList[i].triggers[j].documents.length; l++) {
                                let item = groupedItemList.find((x) => x.document === this.dataItemList[i].triggers[j].documents[l])
                                if (!item) {
                                    item = {
                                        jobName: '',
                                        color: 'red',
                                        document: this.dataItemList[i].triggers[j].documents[l],
                                        executions: [] as any
                                    }
                                    groupedItemList.push(item)
                                }

                                let execution = item.executions.find((x) => x.date == new Date(this.dataItemList[i].triggers[j].executions[k]))

                                if (!execution) {
                                    execution = {
                                        id: index++,
                                        date: this.formatDateTime(this.dataItemList[i].triggers[j].executions[k]),
                                        jobName: this.dataItemList[i].triggers[j].jobName,
                                        numberOfDocuments: this.dataItemList[i].triggers[j].documents.length,
                                        documents: this.dataItemList[i].triggers[j].documents
                                    }
                                    item.executions.push(execution)
                                }
                            }
                        }
                    }
                }
            }
            return groupedItemList
        },
        createPackageItemList(): iGroupDataItem[] {
            let index = 0
            const groupedItemList: iGroupDataItem[] = []
            for (let i = 0; i < this.dataItemList.length; i++) {
                if (this.dataItemList[i].triggers && this.dataItemList[i].triggers[0].executions) {
                    const item = {
                        jobName: this.dataItemList[i].name,
                        color: 'red',
                        document: '',
                        executions: [] as any
                    }
                    for (let j = 0; j < this.dataItemList[i].triggers.length; j++) {
                        for (let k = 0; k < this.dataItemList[i].triggers[j].executions.length; k++) {
                            const execution = {
                                id: index++,
                                rawDate: new Date(this.dataItemList[i].triggers[j].executions[k]),
                                date: this.formatDateTime(this.dataItemList[i].triggers[j].executions[k]),
                                jobName: this.dataItemList[i].triggers[j].jobName,
                                numberOfDocuments: this.dataItemList[i].triggers[j].documents.length,
                                documents: this.dataItemList[i].triggers[j].documents
                            }
                            const executionMomentDate = moment(execution.rawDate)
                            const ind = item.executions.findIndex((item) => executionMomentDate.isBefore(item.rawDate))
                            if (ind >= 0) {
                                item.executions.splice(ind, 0, execution)
                            } else {
                                item.executions.push(execution)
                            }
                        }
                    }
                    groupedItemList.push(item)
                }
            }
            return groupedItemList
        },
        createTimeItemList(): iGroupDataItem[] {
            let index = 0
            const groupedItemList: iGroupDataItem[] = []
            for (let i = 0; i < this.dataItemList.length; i++) {
                if (this.dataItemList[i].triggers && this.dataItemList[i].triggers[0].executions) {
                    for (let j = 0; j < this.dataItemList[i].triggers.length; j++) {
                        for (let k = 0; k < this.dataItemList[i].triggers[j].executions.length; k++) {
                            let item = groupedItemList.find((x) => moment(x.date).isSame(this.dataItemList[i].triggers[j].executions[k], 'day'))
                            if (!item) {
                                item = this.createTimeGrupItem(this.dataItemList[i].triggers[j].executions[k])
                                //search for position of group item
                                const itemMomentDate = moment(item.rawDate)
                                const ind = groupedItemList.findIndex((groupItem) => itemMomentDate.isBefore(groupItem.rawDate))
                                if (ind >= 0) {
                                    groupedItemList.splice(ind, 0, item)
                                } else {
                                    groupedItemList.push(item)
                                }
                            }
                            const execution = {
                                id: index++,
                                rawDate: new Date(this.dataItemList[i].triggers[j].executions[k]),
                                date: this.dataItemList[i].triggers[j].executions[k],
                                jobName: this.dataItemList[i].triggers[j].jobName,
                                numberOfDocuments: this.dataItemList[i].triggers[j].documents.length,
                                documents: this.dataItemList[i].triggers[j].documents
                            }
                            const execItemMomentDate = moment(execution.rawDate)
                            const execItemInd = item.executions.findIndex((groupItem) => execItemMomentDate.isBefore(groupItem.rawDate))
                            if (execItemInd >= 0) {
                                item.executions.splice(execItemInd, 0, execution)
                            } else {
                                item.executions.push(execution)
                            }
                        }
                    }
                }
            }
            return groupedItemList
        },
        createTimeGrupItem(date: string) {
            return {
                jobName: '',
                color: 'red',
                rawDate: new Date(date),
                date: formatDateWithLocale(date),
                dayOfWeek: formatDateWithLocale(date, { weekday: 'long' }),
                monthName: formatDateWithLocale(date, { month: 'long' }),
                year: new Date(date).getFullYear(),
                day: new Date(date).getDate(),
                document: '',
                executions: [] as any
            }
        },
        returnTime(inputDate: any) {
            const date = new Date(inputDate)
            if (date) {
                return date.toLocaleTimeString(navigator.language, {
                    hour: '2-digit',
                    minute: '2-digit'
                })
            }
        },
        formatDateTime(date: any) {
            return formatDate(date, 'DD/MM/YYYY HH:MM')
        }
    }
})
</script>

<style lang="scss" scoped>
.p-card:nth-child(even) {
    background-color: #f2f2f2;
    &:deep(.p-datatable) {
        .p-datatable-tbody tr {
            background-color: inherit;
        }
        .p-datatable-thead tr th {
            background-color: inherit;
        }
    }
}
.p-large {
    font-size: 2rem;
}
.color-left-border {
    border-left: 5px solid;
}
.parent-card {
    overflow-y: auto;
    overflow-x: hidden;
    flex-grow: 1;
}

.overflow-auto {
    overflow: auto;
}
.flex-no-grow {
    flex-grow: 0;
}
.flex-grow {
    flex-grow: 1;
}
</style>
