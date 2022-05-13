<template>
    <div class="kn-page">
        <ProgressSpinner class="kn-progress-spinner" v-if="loading" />

        <div class="kn-page-content">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.eventsManagement.title') }}
                </template>
            </Toolbar>
            <div id="input-container">
                <Card class="p-m-2 events-input-card">
                    <template #content>
                        <form class="p-fluid p-formgrid p-grid p-m-1">
                            <div class="p-float-label p-col">
                                <Calendar id="startDate" class="kn-material-input" v-model="startDate" :showIcon="true" />
                                <label for="endDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.startDate') }} </label>
                            </div>
                            <div class="p-float-label p-col">
                                <Calendar id="endDate" class="kn-material-input" v-model="endDate" :showIcon="true" />
                                <label for="endDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.endDate') }} </label>
                            </div>
                            <span class="p-field p-float-label p-col">
                                <Dropdown id="eventModel" class="kn-material-input" v-model="selectedEventModel" :options="eventModel" />
                                <label for="eventModel" class="kn-material-input-label"> {{ $t('managers.eventsManagement.eventModel') }} </label>
                            </span>
                            <Button icon="pi pi-search" class="p-button-text kn-button thirdButton" @click="getEvents" />
                        </form>
                    </template>
                </Card>
                <DataTable class="p-datatable-sm kn-table" :value="events" dataKey="id" :scrollable="true" scrollHeight="flex" v-model:filters="filters" :globalFilterFields="globalFilterFields">
                    <template #header>
                        <div class="table-header p-d-flex p-ai-center">
                            <span id="search-container" class="p-input-icon-left p-mr-3">
                                <i class="pi pi-search" />
                                <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                            </span>
                        </div>
                    </template>
                    <Column field="user" :header="$t('managers.eventsManagement.user')" :sortable="true"></Column>
                    <Column field="formattedDate" :header="$t('cron.date')" :sortable="true"></Column>
                    <Column field="type" :header="$t('common.type')" :sortable="true"></Column>
                </DataTable>
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { filterDefault } from '@/helpers/commons/filterHelper'
import descriptor from './EventsManagementDescriptor.json'
import ProgressSpinner from 'primevue/progressspinner'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    name: 'dataset-management',
    components: { ProgressSpinner, Card, Dropdown, Calendar, DataTable, Column },
    data() {
        return {
            descriptor,
            loading: false,
            events: [] as any,
            fetchSize: 23,
            offset: 0,
            eventModel: descriptor.eventModel,
            startDate: null as any,
            endDate: null as any,
            selectedEventModel: '' as String,
            filters: { global: [filterDefault] } as Object,
            globalFilterFields: ['user', 'type']
        }
    },
    created() {
        this.getEvents()
    },
    methods: {
        async getEvents() {
            this.loading = true
            let url = ''
            if (this.selectedEventModel === '') {
                url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/events/?fetchsize=${this.fetchSize}&offset=${this.offset}`
            } else {
                url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/events/?fetchsize=${this.fetchSize}&offset=${this.offset}&type=${this.selectedEventModel}`
            }
            await this.$http
                .get(url)
                .then((response: AxiosResponse<any>) => (this.events = response.data.results))
                .finally(() => (this.loading = false))
        }
    }
})
</script>
<style lang="scss">
// .events-input-card .p-card-body,
// .events-input-card .p-card-content {
//     padding: 0;
// }
</style>
