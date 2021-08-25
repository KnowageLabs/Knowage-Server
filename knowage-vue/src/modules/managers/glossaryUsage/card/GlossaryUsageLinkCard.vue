<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ title }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text" @click="$emit('close')">{{ $t('common.close') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable :value="items" class="p-datatable-sm kn-table" dataKey="id" v-model:filters="filters" :globalFilterFields="glossaryUsageLinkCardDescriptor.globalFilterFields" :paginator="true" :rows="20" responsiveLayout="stack" breakpoint="960px">
                <template #header>
                    <div class="table-header p-d-flex p-ai-center">
                        <span id="search-container" class="p-input-icon-left p-mr-3">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" />
                        </span>
                    </div>
                </template>
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <Column class="kn-truncated" v-for="col of glossaryUsageLinkCardDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"></Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
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
            glossaryUsageLinkCardDescriptor,
            filters: { global: [filterDefault] } as Object
        }
    },
    async created() {},
    methods: {}
})
</script>
