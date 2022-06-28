<template>
    <Dialog id="scorecards-kpi-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="scorecardsKpiDialogDescriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('common.kpi') }}
                </template>
            </Toolbar>
        </template>

        <DataTable
            :value="kpis"
            :paginator="kpis.length > 20"
            :rows="20"
            class="p-datatable-sm kn-table p-p-2"
            v-model:selection="selected"
            dataKey="id"
            v-model:filters="filters"
            :globalFilterFields="scorecardsKpiDialogDescriptor.globalFilterFields"
            :scrollable="true"
            :scrollHeight="scorecardsKpiDialogDescriptor.style.dialog.scrollHeight"
        >
            <template #header>
                <div class="table-header">
                    <span class="p-input-icon-left">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>

            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>

            <Column selectionMode="multiple" :headerStyle="scorecardsKpiDialogDescriptor.selectColumnStyle"></Column>
            <Column class="kn-truncated" field="status" :header="$t('common.status')" :sortable="true">
                <template #body="slotProps">
                    <i class="fas fa-square fa-2xl p-mr-2" :class="getKpiIconColorClass(slotProps.data)"></i>
                </template>
            </Column>
            <Column class="kn-truncated" field="name" :header="$t('common.name')" :sortable="true"></Column>
            <Column class="kn-truncated" field="category.valueName" :header="$t('common.category')" :sortable="true"></Column>
            <Column class="kn-truncated" field="creationDate" :header="$t('common.creationDate')" :sortable="true">
                <template #body="slotProps">
                    <span>{{ getFormattedDate(slotProps.data.creationDate) }}</span>
                </template>
            </Column>
            <Column class="kn-truncated" field="author" :header="$t('common.author')" :sortable="true"></Column>
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iKpi } from '../Scorecards'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { formatDate } from '@/helpers/commons/localeHelper'
import { getKpiIconColorClass } from '../ScorecardsHelpers'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import moment from 'moment'
import scorecardsKpiDialogDescriptor from './ScorecardsKpiDialogDescriptor.json'

export default defineComponent({
    name: 'scorecards-kpi-dialog',
    components: { Column, DataTable, Dialog },
    props: { visible: { type: Boolean }, propKpis: { type: Array as PropType<iKpi[]>, required: true }, selectedKpis: { type: Array as PropType<iKpi[]>, required: true } },
    emits: ['close', 'kpiSelected'],
    data() {
        return {
            scorecardsKpiDialogDescriptor,
            kpis: [] as iKpi[],
            filters: { global: [filterDefault] },
            selected: [] as iKpi[],
            getKpiIconColorClass
        }
    },
    watch: {
        visible(value) {
            if (value) this.loadSelectedKpi()
        },
        propKpis() {
            this.loadKpi()
        }
    },
    created() {
        this.loadKpi()
        this.loadSelectedKpi()
    },
    methods: {
        loadKpi() {
            this.kpis = this.propKpis ?? []
        },
        loadSelectedKpi() {
            this.selected = this.selectedKpis
        },
        getFormattedDate(date: number) {
            const tempDate = moment(date).format('DD/MM/YYYY')
            return formatDate(tempDate, '', 'DD/MM/YYYY')
        },
        closeDialog() {
            this.$emit('close')
            this.selected = []
        },
        save() {
            this.$emit('kpiSelected', this.selected)
        }
    }
})
</script>

<style lang="scss" scoped>
#scorecards-kpi-dialog .p-dialog-header,
#scorecards-kpi-dialog .p-dialog-content {
    padding: 0;
}
#scorecards-kpi-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}

.table-header {
    width: 30%;
}

.scorecard-kpi-icon-red {
    color: #ff5656;
}

.scorecard-kpi-icon-yellow {
    color: #ffee58;
}

.scorecard-kpi-icon-green {
    color: #50c550;
}

.scorecard-kpi-icon-grey {
    color: #b7b7b7;
}

.scorecard-kpi-icon-light-grey {
    color: #cccccc;
}
</style>
