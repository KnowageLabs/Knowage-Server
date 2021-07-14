<template>
    <DataTable v-if="!metadataError" class="editable-cells-tablekn-table" :value="rule.ruleOutputs" editMode="cell" dataKey="id" responsiveLayout="stack" breakpoint="960px">
        <Column>
            <template #body="slotProps">
                <i v-if="slotProps.data.aliasIcon" :class="slotProps.data.aliasIcon"></i>
            </template>
        </Column>
        <Column class="kn-truncated" field="alias" :header="$t('kpi.measureDefinition.alias')"> </Column>
        <Column class="kn-truncated" field="type" :header="$t('kpi.measureDefinition.tipology')">
            <template #editor="slotProps">
                <Dropdown v-model="slotProps.data['type']" :options="tipologiesType">
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
                </Dropdown></template
            >
            <template #body="slotProps">
                {{ slotProps.data.type.translatedValueName }}
            </template>
        </Column>
        <Column :header="$t('common.category')">
            <template #editor="slotProps">
                <AutoComplete v-if="slotProps.data.type.valueCd != 'TEMPORAL_ATTRIBUTE'" v-model="slotProps.data['category'].valueCd" :suggestions="filteredCategories" field="valueCd" @complete="searchCategories($event)" />
                <Dropdown v-else v-model="slotProps.data['hierarchy']" :options="domainsTemporalLevel">
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
            </template>
            <template #body="slotProps">
                {{ slotProps.data.type.valueCd != 'TEMPORAL_ATTRIBUTE' ? slotProps.data['category']?.valueCd : slotProps.data['hierarchy']?.valueCd }}
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
import metadataCardDescriptor from './MetadataCardDescriptor.json'

// TODO Change header for last column, add tooltips maybe?

export default defineComponent({
    name: 'metadata-card',
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
        //console.log('Domains 1: ', this.tipologiesType)
        //console.log('Domains 2: ', this.domainsTemporalLevel)
        //console.log('Domains 3: ', this.categories)
    },
    methods: {
        loadRule() {
            this.rule = this.currentRule as iRule
            if (this.rule.ruleOutputs) {
                this.rule.ruleOutputs.forEach((ruleOutput: any) => {
                    if (!ruleOutput.category) {
                        ruleOutput.category = { valueCd: '' }
                    }
                    if (!ruleOutput.hierarchy) {
                        ruleOutput.hierarchy = { valueCd: '' }
                    }
                })
            }
            console.log('RULE: ', this.rule)
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
            // console.log('FILTERED CATEGORIES: ', this.filteredCategories)
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
</style>
