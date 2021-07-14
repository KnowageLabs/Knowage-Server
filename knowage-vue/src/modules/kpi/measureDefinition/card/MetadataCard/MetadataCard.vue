<template>
    <DataTable v-if="!metadataError" class="editable-cells-tablekn-table" :value="rule.ruleOutputs" editMode="cell" dataKey="id" responsiveLayout="stack" breakpoint="960px">
        <Column>
            <template #body="slotProps">
                <i v-if="!rule.id" :class="showAliasIcon(slotProps.data.alias)"></i>
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

    <Dialog :contentStyle="metadataCardDescriptor.dialog.style" :visible="metadataError" :modal="true" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :closable="false">
        <h1>{{ $t('kpi.measureDefinition.metadataError') + ' ' + $t('kpi.measureDefinition.wrongQuery') }}</h1>
        <p>{{ metadataError }}</p>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeMetadataErrorDialog"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMeasure, iRule } from '../../MeasureDefinition'
import axios from 'axios'
import AutoComplete from 'primevue/autocomplete'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import metadataCardDescriptor from './MetadataCardDescriptor.json'

// TODO Change header for last column, add tooltips maybe?

export default defineComponent({
    name: 'metadata-card',
    components: { AutoComplete, Column, Dialog, Dropdown, DataTable },
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
        },
        availableAliases: {
            type: Array,
            required: true
        },
        notAvailableAliasList: {
            type: Array,
            required: true
        },
        changed: {
            type: Boolean
        }
    },
    emits: ['close'],
    watch: {
        async changed() {
            await this.loadMetadata()
        }
    },
    data() {
        return {
            metadataCardDescriptor,
            rule: {
                ruleOutputs: [] as iMeasure[]
            } as iRule,
            filteredCategories: [] as any[],
            columns: [] as any[],
            rows: [],
            metadataError: null
        }
    },

    async mounted() {
        this.loadRule()
        //console.log('Domains 1: ', this.tipologiesType)
        //console.log('Domains 2: ', this.domainsTemporalLevel)
        //console.log('Domains 3: ', this.categories)
        console.log('ALIASES AVAILABLE ', this.availableAliases)
        console.log('ALIASES NOT AVAILABLE : ', this.notAvailableAliasList)
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
        async loadMetadata() {
            console.log('callllled')
            const tempDatasource = this.rule.dataSource
            if (this.rule.dataSource) {
                this.rule.dataSourceId = this.rule.dataSource.DATASOURCE_ID
            }
            delete this.rule.dataSource
            const postData = { rule: this.rule, maxItem: 10 }
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/queryPreview', postData).then((response) => {
                console.log('RESPONSE!!!', response)
                if (response.data.errors) {
                    this.metadataError = response.data.errors[0].message
                } else {
                    this.columns = response.data.columns
                    this.rows = response.data.rows
                    this.columnToRuleOutputs()
                }
            })
            this.rule.dataSource = tempDatasource
            console.log('METADATA ERROR', this.metadataError)
        },
        columnToRuleOutputs() {
            const tempMetadatas = [] as any[]
            console.log('METADATA COLUMNS: ', this.columns)
            for (let index in this.columns) {
                tempMetadatas.push(this.columns[index].label.toUpperCase())
                if (this.ruleOutputIndexOfColumnName(this.columns[index].label) === -1) {
                    let type = this.tipologiesType[1]
                    if (this.columns[index].type === 'int' || this.columns[index].type == 'float') {
                        type = this.tipologiesType[0]
                    }
                    this.rule.ruleOutputs.push({
                        alias: this.columns[index].label,
                        type: type
                    })
                }
            }
            for (let index = 0; index < this.rule.ruleOutputs.length; index++) {
                if (tempMetadatas.indexOf(this.rule.ruleOutputs[index].alias.toUpperCase()) === -1) {
                    this.rule.ruleOutputs.splice(index, 1)
                    index--
                }
            }
            console.log('RULE AFTER METHOD!!!', this.rule)
        },
        ruleOutputIndexOfColumnName(columnName: string) {
            console.log('TEEEEEEEEEEST', this.rule.ruleOutputs)
            for (let i = 0; i < this.rule.ruleOutputs.length; i++) {
                if (this.rule.ruleOutputs[i].alias.toUpperCase() === columnName.toUpperCase()) {
                    return i
                }
            }
            return -1
        },
        showAliasIcon(alias: any) {
            console.log('ALIAS: ', alias)
            if (!this.aliasExists(alias) && !this.aliasUsedByMeasure(alias)) {
                console.log('ALIAS doesnt Exist!')
                return 'fa fa-exclamation-triangle icon-missing'
            }
            if (this.aliasUsedByMeasure(alias)) {
                console.log('ALIAS USED!')
                return 'fa fa-exclamation-triangle icon-used'
            }
        },
        aliasExists(name: string) {
            let exists = false
            this.availableAliases.forEach((alias: any) => {
                //console.log('Exists: ' + alias.name.toUpperCase() + ' === ' + name.toUpperCase())
                if (alias.name.toUpperCase() === name.toUpperCase()) {
                    // console.log('ALIAS Exists!')
                    exists = true
                }
            })
            return exists
        },
        aliasUsedByMeasure(name: string) {
            let used = false
            this.notAvailableAliasList.forEach((alias: any) => {
                //console.log('Used: ' + alias.name.toUpperCase() + ' === ' + name.toUpperCase())
                if (alias.name.toUpperCase() === name.toUpperCase()) {
                    //console.log('ALIAS USED!')
                    used = true
                }
            })
            return used
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
        },
        closeMetadataErrorDialog() {
            this.metadataError = null
            this.$emit('close')
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
