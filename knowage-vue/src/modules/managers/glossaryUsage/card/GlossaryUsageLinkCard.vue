<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ title }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable :value="items" class="p-datatable-sm kn-table" dataKey="field" :paginator="true" :rows="20" responsiveLayout="stack" breakpoint="960px">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <Column class="kn-truncated" v-for="col of glossaryUsageLinkCardDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"></Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import glossaryUsageLinkCardDescriptor from './GlossaryUsageLinkCardDescriptor.json'

export default defineComponent({
    name: 'glossary-usage-link-card',
    components: { Card, Column, DataTable },
    props: { title: { type: String }, items: { type: Array } },
    data() {
        return {
            glossaryUsageLinkCardDescriptor
        }
    },
    async created() {},
    methods: {}
})
</script>
