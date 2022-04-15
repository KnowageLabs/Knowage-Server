<template>
    <DataTable :value="[]" class="p-datatable-sm kn-table" responsiveLayout="stack" breakpoint="960px">
        <template #empty>{{ $t('common.info.noDataFound') }}</template>

        <Column :style="olapFilterDialogDescriptor.iconColumnStyle">
            <template #body="slotProps">
                <Button icon="pi pi-trash" class="p-button-link" @click="remove(slotProps.data)" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import olapFilterDialogDescriptor from './OlapFilterDialogDescriptor.json'

export default defineComponent({
    name: 'olap-filter-table',
    components: { Column, DataTable },
    props: {},
    data() {
        return {
            olapFilterDialogDescriptor,
            levels: [] as any[]
        }
    },
    watch: {},
    created() {},
    methods: {
        remove(level: any) {}
    }
})
</script>

<style lang="scss" scoped></style>
