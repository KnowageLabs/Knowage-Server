<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('managers.calendarManagement.title') }}
            </template>
            <template #end>
                <KnFabButton icon="fas fa-plus" @click="showForm()" data-test="open-form-button"></KnFabButton>
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col" v-if="!loading">
                <DataTable
                    :value="calendarDates"
                    :paginator="calendarDates.length > 20"
                    :loading="loading"
                    :rows="20"
                    class="p-datatable-sm kn-table"
                    dataKey="id"
                    v-model:filters="filters"
                    :globalFilterFields="calendarManagementDescriptor.globalFilterFields"
                    responsiveLayout="stack"
                    breakpoint="960px"
                    @rowClick="showForm($event)"
                    data-test="domains-table"
                >
                    <template #header>
                        <div class="table-header">
                            <span class="p-input-icon-left">
                                <i class="pi pi-search" />
                                <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                            </span>
                        </div>
                    </template>
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
                    </template>
                    <template #loading>
                        {{ $t('common.info.dataLoading') }}
                    </template>

                    <Column class="kn-truncated" field="name" :header="$t('common.name')" :sortable="true"></Column>
                    <Column class="kn-truncated" field="calStartDay" :header="$t('common.name')" :sortable="true">
                        <template #body="slotProps">
                            <span>{{ getFormattedDate(slotProps.data.creationDate) }}</span>
                        </template>
                    </Column>
                </DataTable>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iCalendarDate } from './CalendarManagement'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'

import calendarManagementDescriptor from './CalendarManagementDescriptor.json'

export default defineComponent({
    name: 'calendar-management',
    components: { Column, DataTable },
    data() {
        return {
            calendarManagementDescriptor,
            calendarDates: [] as iCalendarDate[],
            filters: { global: [filterDefault] },
            loading: false
        }
    },
    async created() {},
    methods: {
        showForm(calendarDate: iCalendarDate | null) {
            console.log('SHOW FORM FOR: ', calendarDate)
        }
    }
})
</script>
