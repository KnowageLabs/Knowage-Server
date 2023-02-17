<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ title }}
                </template>
                <template #end>
                    <Button v-if="canSeeLinkTable" class="kn-button p-button-text" @click="$emit('linkClicked', type)">{{ $t('managers.glossary.glossaryUsage.link') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable
                v-model:selection="selectedItems"
                v-model:filters="filters"
                :value="items"
                class="p-datatable-sm kn-table"
                data-key="id"
                selection-mode="multiple"
                :meta-key-selection="false"
                :global-filter-fields="glossaryUsageNavigationCardDescriptor.globalFilterFields"
                :paginator="items.length > 5"
                :rows="5"
                responsive-layout="stack"
                breakpoint="960px"
                @rowSelect="onItemsSelected"
                @rowUnselect="onItemsSelected"
            >
                <template #header>
                    <div class="table-header p-d-flex p-ai-center">
                        <span id="search-container" class="p-input-icon-left p-mr-3">
                            <i class="pi pi-search" />
                            <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" data-test="search-input" />
                        </span>
                    </div>
                </template>
                <template #empty
                    ><div id="no-words-present-info">
                        <p>{{ $t('managers.glossary.glossaryUsage.noWordsPresent', { type: title }) }}</p>
                    </div></template
                >
                <Column key="label" class="kn-truncated" field="label"></Column>
                <Column class="p-text-right">
                    <template #body="slotProps">
                        <Button icon="pi pi-info-circle" class="p-button-link" :data-test="'info-button-' + slotProps.data.id" @click.stop="$emit('infoClicked', slotProps.data)" />
                    </template>
                </Column>
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
import glossaryUsageNavigationCardDescriptor from './GlossaryUsageNavigationCardDescriptor.json'
import mainStore from '../../../../App.store'

export default defineComponent({
    name: 'glossary-usage-navigation-card',
    components: { Card, Column, DataTable },
    props: {
        items: { type: Object },
        type: { type: String },
        glossaryChanged: { type: Boolean }
    },
    emits: ['infoClicked', 'linkClicked', 'selected'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            glossaryUsageNavigationCardDescriptor,
            filters: { global: [filterDefault] } as Object,
            selectedItems: [],
            user: {} as any
        }
    },
    computed: {
        title(): string {
            switch (this.type) {
                case 'document':
                    return this.$t('managers.glossary.glossaryUsage.documents')
                case 'dataset':
                    return this.$t('managers.glossary.glossaryUsage.dataset')
                case 'businessClass':
                    return this.$t('managers.glossary.glossaryUsage.businessClass')
                case 'table':
                    return this.$t('managers.glossary.glossaryUsage.tables')
                default:
                    return ''
            }
        },
        canSeeLinkTable(): boolean {
            let index = -1
            if (this.user.functionalities) {
                index = this.user.functionalities.findIndex((el: string) => el === 'ManageGlossaryTechnical')
            }
            return index !== -1
        }
    },
    watch: {
        glossaryChanged() {
            this.selectedItems = []
        }
    },
    created() {
        this.user = (this.store.$state as any).user
    },
    methods: {
        onItemsSelected() {
            this.$emit('selected', this.selectedItems)
        }
    }
})
</script>

<style lang="scss" scoped>
.label {
    text-transform: uppercase;
}

#no-words-present-info {
    margin: 0 2rem;
    font-size: 0.8rem;
    display: flex;
    justify-content: center;
    border: 1px solid rgba(59, 103, 140, 0.1);
    border-color: #c2c2c2;
    border-radius: 2px;
    background-color: #eaf0f6;
    color: var(--kn-color-primary);
    p {
        margin: 0.3rem;
    }
}
</style>
