<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary schedulerDialog" v-bind:visible="visibility" footer="footer" :header="$t('common.import')" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <div class="p-grid p-m-1 p-fluid">
            <span class="p-col-5">
                <Panel> <template #header>Schedulation</template> <KnScheduler class="p-m-2" :descriptor="schedulerDescriptor" @touched="touched = true" @cronValid="setCronValid($event)" /> </Panel>
            </span>
            <span class="p-col-7">
                <Panel
                    ><template #header>
                        Executed schedulation
                    </template>
                    <DataTable
                        :value="logs"
                        v-model:filters="filters"
                        class="p-datatable-sm kn-table"
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

                        <Column class="kn-truncated" v-for="col of schedulerDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" :selectionMode="col.field == 'selectionMode' ? 'multiple' : ''" :exportable="col.field == 'selectionMode' ? false : ''"
                            ><template #body="slotProps">
                                <span v-if="col.field === 'errorFile'"><Button :icon="col.icon" class="p-button-link" @click="downloadLog(slotProps.data)"/></span>
                                <span v-else-if="col.field === 'executionDate'"> {{ getFormattedDate(slotProps.data[col.field], 'MM/DD/YYYY') }}</span>
                                <span v-else>{{ slotProps.data[col.field] }}</span>
                            </template></Column
                        >
                    </DataTable>
                </Panel></span
            >
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
    import Panel from 'primevue/panel'

    import { formatDate } from '@/helpers/commons/localeHelper'

    import dataPreparationMonitoringDescriptor from '@/modules/workspace/dataPreparation/DataPreparationMonitoring/DataPreparationMonitoringDescriptor.json'
    import KnScheduler from '@/components/UI/KnScheduler/KnScheduler.vue'
    /*   import { AxiosResponse } from 'axios' */

    import { filterDefault } from '@/helpers/commons/filterHelper'

    export default defineComponent({
        name: 'data-preparation-scheduler-dialog',
        components: { Column, DataTable, Dialog, KnScheduler, Panel },
        props: { visibility: Boolean },
        emits: ['close'],
        data() {
            return {
                schedulerDescriptor: dataPreparationMonitoringDescriptor,
                logs: [] as any,
                filters: { global: [filterDefault] } as Object
            }
        },

        watch: {},
        created() {
            this.logs = this.schedulerDescriptor.mock.logs
        },
        methods: {
            downloadLog(item) {
                console.log(item)
            },
            getFormattedDate(date: any, format: any): String {
                return formatDate(date, format)
            },
            resetAndClose() {
                this.$emit('close')
            },
            sendSchedulation() {
                this.resetAndClose()
            }
        }
    })
</script>

<style lang="scss">
    .schedulerDialog {
        min-width: 600px;
        width: 60%;
        max-width: 1400px;
    }
</style>
