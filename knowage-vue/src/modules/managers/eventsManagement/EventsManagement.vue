<template>
    <div class="kn-page">
        <ProgressSpinner class="kn-progress-spinner" v-if="loading" data-test="progress-spinner" />
        <div class="p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.eventsManagement.title') }}
                </template>
            </Toolbar>
            <div id="input-container" class="p-d-flex p-flex-column kn-flex">
                <Card class="p-m-2 events-input-card">
                    <template #content>
                        <form class="p-fluid p-formgrid p-grid p-m-1">
                            <div class="p-float-label p-col">
                                <Calendar ref="test" id="startDate" class="kn-material-input" v-model="startDate" :showIcon="true" @date-select="setFocusOnSearchButton" />
                                <label for="endDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.startDate') }} </label>
                            </div>
                            <div class="p-float-label p-col">
                                <Calendar id="endDate" class="kn-material-input" v-model="endDate" :showIcon="true" @date-select="setFocusOnSearchButton" />
                                <label for="endDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.endDate') }} </label>
                            </div>
                            <span class="p-field p-float-label p-col">
                                <Dropdown id="eventModel" class="kn-material-input" v-model="selectedEventModel" :options="eventModel" @change="setFocusOnSearchButton" />
                                <label for="eventModel" class="kn-material-input-label"> {{ $t('managers.eventsManagement.eventModel') }} </label>
                            </span>
                            <Button ref="search-button" id="search-button" icon="pi pi-search" class="p-button-text kn-button thirdButton" @click="onSearchClicked" data-test="search-button" />
                        </form>
                    </template>
                </Card>
                <Card class="domainCard" style="height: calc(100vh - 125px)">
                    <template #content>
                        <DataTable
                            class="p-datatable-sm kn-table"
                            v-model:first="first"
                            :value="events"
                            dataKey="id"
                            :paginator="true"
                            :lazy="true"
                            :totalRecords="lazyParams.size"
                            paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
                            :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
                            :rows="20"
                            responsiveLayout="stack"
                            breakpoint="960px"
                            :scrollable="true"
                            scrollHeight="flex"
                            :stripedRows="true"
                            v-model:filters="filters"
                            :globalFilterFields="globalFilterFields"
                            @page="onPage($event)"
                        >
                            <template #header>
                                <div class="table-header p-d-flex p-ai-center">
                                    <span id="search-container" class="p-input-icon-left p-mr-3">
                                        <i class="pi pi-search" />
                                        <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                                    </span>
                                </div>
                            </template>
                            <template #loading>
                                {{ $t('common.info.dataLoading') }}
                            </template>
                            <template #empty>
                                <div id="noDatasetsFound">
                                    {{ $t('common.info.noDataFound') }}
                                </div>
                            </template>
                            <Column field="user" class="kn-truncated" :header="$t('managers.eventsManagement.user')" :sortable="true"></Column>
                            <Column field="formattedDate" class="kn-truncated" :header="$t('cron.date')" :sortable="true"></Column>
                            <Column field="type" class="kn-truncated" :header="$t('common.type')" :sortable="true"></Column>
                            <Column :header="$t('common.description')" class="kn-truncated" :sortable="true">
                                <template #body="slotProps">
                                    <span class="kn-truncated" v-tooltip.top="slotProps.data.desc"> {{ slotProps.data.desc }}</span>
                                </template>
                            </Column>
                        </DataTable>
                    </template>
                </Card>
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { filterDefault } from '@/helpers/commons/filterHelper'
import moment from 'moment'
import descriptor from './EventsManagementDescriptor.json'
import ProgressSpinner from 'primevue/progressspinner'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    name: 'events-management',
    components: { ProgressSpinner, Card, Dropdown, Calendar, DataTable, Column },
    data() {
        return {
            descriptor,
            loading: false,
            events: [] as any,
            eventModel: descriptor.eventModel,
            startDate: null as any,
            endDate: null as any,
            selectedEventModel: '' as String,
            filters: { global: [filterDefault] } as Object,
            globalFilterFields: ['user', 'type'],
            lazyParams: { size: 20, paginationStart: 0 } as any,
            first: 0
        }
    },
    created() {
        this.getEvents()
    },
    methods: {
        async getEvents() {
            this.loading = true
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/events/?fetchsize=${this.lazyParams.size}&offset=${this.lazyParams.paginationStart}`
            this.selectedEventModel != '' ? (url += `&type=${this.selectedEventModel}`) : ''
            this.startDate ? (url += `&startDate=${encodeURIComponent(moment(this.startDate).format('YYYY-MM-DD+HH:mm:ss'))}`) : ''
            this.endDate ? (url += `&endDate=${encodeURIComponent(moment(this.endDate).format('YYYY-MM-DD+HH:mm:ss'))}`) : ''
            await this.$http
                .get(url)
                .then((response: AxiosResponse<any>) => {
                    this.events = response.data.results
                    this.lazyParams.size = response.data.total
                    this.lazyParams.paginationStart = response.data.start
                })
                .finally(() => (this.loading = false))
        },
        async onPage(event: any) {
            this.lazyParams = { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows, size: this.lazyParams.size }
            await this.getEvents()
        },
        async onSearchClicked() {
            this.first = 0
            this.lazyParams = { size: 20, paginationStart: 0 }
            await this.getEvents()
        },
        setFocusOnSearchButton() {
            this.$nextTick(() => {
                const searchButton = this.$refs['search-button'] as any
                searchButton.$el.focus()
            })
        }
    }
})
</script>
<style lang="scss" scoped>
.domainCard {
    &:deep(.p-card-body) {
        height: calc(100% - 35px);
        .p-card-content {
            height: 100%;
            padding-bottom: 0;
            .p-paginator-bottom {
                border: none;
            }
        }
    }
}

.thirdButton:focus {
    color: #c2c2c2 !important;
}
</style>
