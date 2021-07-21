<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('kpi.kpiScheduler.executionType') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-field-radiobutton">
                <RadioButton id="delta-with-update" name="delta" :value="true" v-model="schedule.delta" />
                <label for="delta-with-update">{{ $t('kpi.kpiScheduler.insertAndUpdate') }}</label>
            </div>
            <div class="p-field-radiobutton">
                <RadioButton id="delta-with-delete" name="delta" :value="false" v-model="schedule.delta" />
                <label for="delta-with-delete">{{ $t('kpi.kpiScheduler.deleteAndInsert') }}</label>
            </div>
        </template>
    </Card>
    <Card class="p-mt-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('kpi.kpiScheduler.logExecution') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable :value="executionList" :paginator="true" :rowsPerPageOptions="[10, 20, 50]" :rows="10" :loading="loading" class="p-datatable-sm kn-table p-m-1" dataKey="id" responsiveLayout="stack" breakpoint="960px" @rowClick="showForm($event.data, false)">
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>
                <template #header>
                    <div class="table-header">
                        <div class="p-d-flex p-ai-center">
                            <span class="p-d-flex p-flex-column  p-mr-2">
                                <label for="numberOfLogs" class="kn-material-input-label"> {{ $t('kpi.kpiScheduler.numberOfExecutions') }}</label>
                                <InputNumber id="numberOfLogs" inputClass="kn-material-input" v-model="numberOfLogs" />
                            </span>
                            <Button id="load-button" class="kn-button kn-button--primary" :label="$t('common.load')" @click="loadLogExecutionList"></Button>
                        </div>
                    </div>
                </template>
                <Column :style="executeCardDescriptor.table.columns.style" field="timeRun" :header="$t('kpi.kpiScheduler.timeRun')" :sortable="true">
                    <template #body="slotProps">
                        {{ getFormatedDate(slotProps.data.timeRun) }}
                    </template>
                </Column>
                <Column class="kn-truncated" :style="executeCardDescriptor.table.columns.style" v-for="col of executeCardDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"> </Column>
                <Column :style="executeCardDescriptor.table.iconColumn.style">
                    <template #body="slotProps">
                        <Button v-if="slotProps.data.outputPresent" icon="pi pi-download" class="p-button-link" @click="downloadFile(slotProps.data.id)" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { formatDate } from '@/helpers/commons/localeHelper'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import axios from 'axios'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputNumber from 'primevue/inputnumber'
import RadioButton from 'primevue/radiobutton'
import executeCardDescriptor from './ExecuteCardDescriptor.json'

export default defineComponent({
    name: 'execute-card',
    components: { Card, Column, DataTable, InputNumber, RadioButton },
    props: {
        selectedSchedule: {
            type: Object
        },
        logExecutionList: {
            type: Array
        }
    },
    data() {
        return {
            executeCardDescriptor,
            schedule: {} as any,
            executionList: [] as any[],
            numberOfLogs: 10
        }
    },
    created() {
        this.loadSelectedSchedule()
        this.loadLogExecutionList()
        console.log('SELECTED SCHEDULE', this.selectedSchedule)
        console.log('LOG EXEC LIST', this.logExecutionList)
    },
    methods: {
        loadSelectedSchedule() {
            this.schedule = this.selectedSchedule as any
            console.log('SELECTED SCH. DELTA', this.schedule.delta)
        },
        loadLogExecutionList() {
            if (this.selectedSchedule) {
                axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.selectedSchedule.id}/${this.numberOfLogs}/logExecutionList`).then((response) => (this.executionList = response.data))
            }
        },
        getFormatedDate(date: any) {
            return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
        },
        async downloadFile(id: number) {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${id}/logExecutionListOutputContent`).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.downloading'),
                        msg: this.$t('common.error.downloading')
                    })
                } else {
                    downloadDirect(JSON.stringify(response.data.output), this.schedule.name + 'ErrorLog', 'text/plain')
                    this.$store.commit('setInfo', { title: this.$t('common.toast.success') })
                }
            })
        }
    }
})
</script>

<style lang="scss" scoped>
#load-button {
    height: 2.5rem;
}
</style>
