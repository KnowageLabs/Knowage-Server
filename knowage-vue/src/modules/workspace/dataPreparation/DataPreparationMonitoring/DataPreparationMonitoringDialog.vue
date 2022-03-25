<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary schedulerDialog" v-bind:visible="visibility" footer="footer" :header="$t('common.import')" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }" :closable="false">
        <div class="p-grid p-d-flex p-m-1 p-fluid">
            <div class="p-col-5">
                <Card class="kn-card full-height">
                    <template #content><KnScheduler class="p-m-1" :formula="currentCronExpression" :descriptor="schedulerDescriptor" @touched="touched = true" :readOnly="true" /> </template
                ></Card>
            </div>
            <div class="p-col-7">
                <Card class="kn-card full-height">
                    <template #header
                        ><Toolbar class="kn-toolbar kn-toolbar--secondary">
                            <template #start>
                                {{ $t('managers.workspaceManagement.dataPreparation.monitoring.executionLogs') }}
                            </template>
                        </Toolbar></template
                    ><template #content>
                        <DataTable
                            :value="logs"
                            v-model:filters="filters"
                            class="p-datatable-sm kn-table"
                            columnResizeMode="fit | expand"
                            dataKey="id"
                            :paginator="true"
                            :rows="10"
                            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                            responsiveLayout="stack"
                            breakpoint="960px"
                            :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
                            :globalFilterFields="['name', 'type', 'tags']"
                            :loading="loading"
                        >
                            <template #empty>
                                {{ $t('common.info.noDataFound') }}
                            </template>
                            <template #loading>
                                {{ $t('common.info.dataLoading') }}
                            </template>

                            <Column
                                class="kn-truncated"
                                v-for="col of schedulerDescriptor.columns"
                                :field="col.field"
                                :header="col.field !== 'errorFile' ? $t(col.header) : ''"
                                :key="col.field"
                                :sortable="col.field !== 'errorFile'"
                                :selectionMode="col.field == 'selectionMode' ? 'multiple' : ''"
                                :exportable="col.field == 'selectionMode' ? false : ''"
                                ><template #body="slotProps">
                                    <span v-if="col.field === 'start' || col.field === 'stop'"> {{ getFormattedDate(slotProps.data[col.field], 'MM/DD/YYYY') }}</span>
                                    <span v-else>{{ slotProps.data[col.field] }}</span>
                                </template></Column
                            >

                            <Column class="kn-truncated" field="errorFile" :header="''" :sortable="false" :selectionMode="false" :exportable="false">
                                <template #body="slotProps">
                                    <span><Button icon="pi pi-download" class="p-button-link" v-if="slotProps.data['status'] === 'KO'" @click="downloadLog(slotProps.data)"/></span> </template
                            ></Column> </DataTable
                    ></template>
                </Card>
            </div>
        </div>
        <template #footer>
            <Button v-bind:visible="visibility" class="p-button-text kn-button--secondary" :label="$t('common.cancel')" @click="resetAndClose" />

            <Button v-bind:visible="visibility" class="kn-button kn-button--primary" v-t="'common.save'" @click="sendSchedulation" />
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'

    import Column from 'primevue/column'
    import DataTable from 'primevue/datatable'
    import Dialog from 'primevue/dialog'
    import Card from 'primevue/card'

    import { formatDate } from '@/helpers/commons/localeHelper'

    import dataPreparationMonitoringDescriptor from '@/modules/workspace/dataPreparation/DataPreparationMonitoring/DataPreparationMonitoringDescriptor.json'
    import KnScheduler from '@/components/UI/KnScheduler/KnScheduler.vue'
    import { IDataPrepLog } from '@/modules/workspace/dataPreparation/DataPreparationMonitoring/DataPreparationMonitoring'
    import { AxiosResponse } from 'axios'

    import { downloadDirectFromResponse } from '@/helpers/commons/fileHelper'
    import { filterDefault } from '@/helpers/commons/filterHelper'

    export default defineComponent({
        name: 'data-preparation-scheduler-dialog',
        components: { Card, Column, DataTable, Dialog, KnScheduler },
        props: { visibility: Boolean, dataset: Object },
        emits: ['close'],
        data() {
            return {
                schedulerDescriptor: dataPreparationMonitoringDescriptor,
                logs: Array<IDataPrepLog>(),

                filters: { global: [filterDefault] } as Object,
                validSchedulation: Boolean,

                currentCronExpression: ''
            }
        },

        watch: {
            visibility(newVisibility) {
                if (newVisibility) {
                    this.loadLogs()
                } else {
                    this.logs = []
                }
            }
        },

        methods: {
            async downloadLog(item: IDataPrepLog) {
                await this.$http.post(process.env.VUE_APP_DATA_PREPARATION_PATH + '1.0/process/' + item.id + '/log/download').then((response: AxiosResponse<any>) => {
                    downloadDirectFromResponse(response)
                })
            },
            getFormattedDate(date: any, format: any): String {
                return formatDate(date, format)
            },
            async loadLogs() {
                if (this.dataset && this.dataset.label) {
                    await this.$http.get(process.env.VUE_APP_DATA_PREPARATION_PATH + '1.0/process/by-destination-data-set/' + this.dataset.label).then((response: AxiosResponse<any>) => {
                        let instance = response.data.instance
                        if (instance) {
                            this.currentCronExpression = instance.config.cron

                            this.$http.get(process.env.VUE_APP_DATA_PREPARATION_PATH + '1.0/process/logs/' + instance.id).then((response: AxiosResponse<any>) => {
                                this.logs = response.data
                            })
                        }
                    })
                }
            },
            resetAndClose() {
                this.currentCronExpression = ''
                this.$emit('close')
            },
            sendSchedulation() {
                this.resetAndClose()
            },
            setCronValid(event) {
                this.validSchedulation = event.item
            }
        }
    })
</script>

<style lang="scss">
    .schedulerDialog {
        min-width: 600px;
        width: 1200px;
        max-width: 1400px;
    }
</style>
