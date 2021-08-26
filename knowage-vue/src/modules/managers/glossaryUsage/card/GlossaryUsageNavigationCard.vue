<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ title }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text" @click="$emit('linkClicked', type)">{{ $t('managers.glossaryUsage.link') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable
                :value="items"
                class="p-datatable-sm kn-table"
                dataKey="id"
                v-model:selection="selectedItems"
                selectionMode="multiple"
                :metaKeySelection="false"
                v-model:filters="filters"
                :globalFilterFields="glossaryUsageNavigationCardDescriptor.globalFilterFields"
                :paginator="true"
                :rows="20"
                responsiveLayout="stack"
                breakpoint="960px"
                @rowSelect="onItemsSelected"
                @rowUnselect="onItemsSelected"
            >
                <template #header>
                    <div class="table-header p-d-flex p-ai-center">
                        <span id="search-container" class="p-input-icon-left p-mr-3">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" />
                        </span>
                    </div>
                </template>
                <template #empty>{{ $t('managers.glossaryUsage.noWordsPresent', { type: title }) }}</template>
                <Column class="kn-truncated" field="label" key="label"></Column>
                <Column class="p-text-right">
                    <template #body="slotProps">
                        <Button icon="pi pi-info-circle" class="p-button-link" @click.stop="$emit('infoClicked', slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
            <!-- <Listbox
                class="kn-list"
                v-model="selectedItems"
                :multiple="true"
                :options="items"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                filterMatchMode="contains"
                :filterFields="glossaryUsageNavigationCardDescriptor.filterFields"
                :emptyFilterMessage="$t('managers.glossaryUsage.noWordsPresent', { type: title })"
                @change="onItemsSelected"
            >
                <template #empty>{{ $t('managers.glossaryUsage.noWordsPresent', { type: title }) }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item">
                        <div class="kn-list-item-text">
                            <div>
                                <span class="label">{{ slotProps.option.label }}</span>
                                <Button class="p-button-link p-button-sm" icon="pi pi-info-circle" @click.stop="$emit('infoClicked', slotProps.option)" />
                            </div>
                        </div>
                    </div>
                </template>
            </Listbox> -->
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import glossaryUsageNavigationCardDescriptor from './GlossaryUsageNavigationCardDescriptor.json'

export default defineComponent({
    name: 'glossary-usage-navigation-card',
    components: { Card, Column, DataTable },
    props: {
        items: { type: Object },
        type: { type: String }
    },
    emits: ['infoClicked', 'linkClicked', 'selected'],
    data() {
        return {
            glossaryUsageNavigationCardDescriptor,
            filters: { global: [filterDefault] } as Object,
            selectedItems: []
        }
    },
    computed: {
        title(): string {
            switch (this.type) {
                case 'document':
                    return this.$t('managers.glossaryUsage.documents')
                case 'dataset':
                    return this.$t('managers.glossaryUsage.dataset')
                case 'businessClass':
                    return this.$t('managers.glossaryUsage.businessClass')
                case 'table':
                    return this.$t('managers.glossaryUsage.tables')
                default:
                    return ''
            }
        }
    },
    methods: {
        onItemsSelected() {
            // console.log('SELECTED ITEMS: ', this.selectedItems)
            this.$emit('selected', this.selectedItems)
        }
    }
})
</script>

<style lang="scss" scoped>
.label {
    text-transform: uppercase;
}
</style>
