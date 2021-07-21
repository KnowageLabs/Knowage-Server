<template>
    <DataTable v-if="!metadataError" class="p-datatable-sm kn-table" :value="rule.ruleOutputs" editMode="cell" dataKey="id" responsiveLayout="stack" breakpoint="960px" data-test="metadata-table">
        <Column :style="metadataCardDescriptor.table.iconColumn.style">
            <template #body="slotProps">
                <i v-if="slotProps.data.aliasIcon" :class="slotProps.data.aliasIcon" v-tooltip.top="alisIconTooltip(slotProps.data.aliasIcon)"></i>
            </template>
        </Column>
        <Column class="kn-truncated" field="alias" :header="$t('kpi.measureDefinition.alias')"> </Column>
        <Column class="kn-truncated" field="type" :header="$t('kpi.measureDefinition.tipology')">
            <template #editor="slotProps">
                <Dropdown class="p-mr-2" v-model="slotProps.data['type']" :options="tipologiesType" @change="$emit('touched')">
                    <template #value="slotProps">
                        <div v-if="slotProps.value">
                            <span>{{ slotProps.value['valueCd'] }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ slotProps.option.valueCd }}</span>
                        </div>
                    </template>
                </Dropdown>
                <i class="pi pi-pencil edit-icon"
            /></template>
            <template #body="slotProps">
                {{ slotProps.data.type.translatedValueName }}
                <i class="pi pi-pencil edit-icon" />
            </template>
        </Column>
        <Column :header="$t('common.category')">
            <template #editor="slotProps">
                <AutoComplete
                    class="p-inputtext-sm p-mr-2"
                    v-if="slotProps.data.type.valueCd != 'TEMPORAL_ATTRIBUTE'"
                    v-model="slotProps.data['category'].valueCd"
                    :suggestions="filteredCategories"
                    field="valueCd"
                    @complete="searchCategories($event)"
                    @input="$emit('touched')"
                    @item-select="setRuleCategory($event.value, slotProps.data)"
                />
                <Dropdown class="p-mr-2" v-else v-model="slotProps.data['hierarchy']" :options="domainsTemporalLevel" :placeholder="$t('kpi.measureDefinition.temporalAttributePlaceholder')" @change="$emit('touched')">
                    <template #value="slotProps">
                        <div v-if="slotProps.value">
                            <span>{{ slotProps.value['valueCd'] }}</span>
                        </div>
                        <span v-else>
                            {{ slotProps.placeholder }}
                        </span>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ slotProps.option.valueCd }}</span>
                        </div>
                    </template>
                </Dropdown>
                <i class="pi pi-pencil edit-icon" />
            </template>
            <template #body="slotProps">
                <span class="p-mr-2">{{ slotProps.data.type.valueCd != 'TEMPORAL_ATTRIBUTE' ? slotProps.data['category']?.valueCd : slotProps.data['hierarchy']?.valueCd }}</span>
                <i class="pi pi-pencil edit-icon" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMeasure, iRule } from '../../MeasureDefinition'
import AutoComplete from 'primevue/autocomplete'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import metadataCardDescriptor from './MeasureDefinitionMetadataCardDescriptor.json'

export default defineComponent({
    name: 'measure-definition-metadata-card',
    components: { AutoComplete, Column, Dropdown, DataTable },
    props: {
        currentRule: {
            type: Object,
            required: true
        },
        tipologiesType: {
            type: Array,
            required: true
        },
        domainsTemporalLevel: {
            type: Array
        },
        categories: {
            type: Array,
            required: true
        }
    },
    emits: ['touched'],
    data() {
        return {
            metadataCardDescriptor,
            rule: {
                ruleOutputs: [] as iMeasure[]
            } as iRule,
            filteredCategories: [] as any[],
            metadataError: null
        }
    },
    async mounted() {
        this.loadRule()
    },
    methods: {
        loadRule() {
            this.rule = this.currentRule as iRule
        },
        searchCategories(event) {
            setTimeout(() => {
                if (!event.query.trim().length) {
                    this.filteredCategories = [...this.categories] as any[]
                } else {
                    this.filteredCategories = this.categories.filter((category: any) => {
                        return category.valueCd.toLowerCase().startsWith(event.query.toLowerCase())
                    })
                }
            }, 250)
        },
        alisIconTooltip(iconClass: string) {
            if (iconClass.includes('icon-used')) {
                return this.$t('kpi.measureDefinition.aliasUsed')
            } else if (iconClass.includes('icon-missing')) {
                return this.$t('kpi.measureDefinition.aliasMissing')
            }
        },
        setRuleCategory(category: any, alias: any) {
            alias.category.valueCd = category.valueCd
        }
    }
})
</script>

<style lang="scss" scoped>
.icon-used {
    color: #f44246;
}

.icon-missing {
    color: #ffeb38;
}

.edit-icon {
    font-size: 0.7rem;
}
</style>
