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
            <Listbox
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
            </Listbox>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import glossaryUsageNavigationCardDescriptor from './GlossaryUsageNavigationCardDescriptor.json'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'glossary-usage-navigation-card',
    components: { Card, Listbox },
    props: {
        items: { type: Object },
        type: { type: String }
    },
    emits: ['infoClicked', 'linkClicked', 'selected'],
    data() {
        return {
            glossaryUsageNavigationCardDescriptor,
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
