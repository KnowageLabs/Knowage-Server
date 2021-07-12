<template>
    <Card>
        <template #content>
            <div class="p-m-3">
                <span class="p-float-label">
                    <Dropdown id="dataSource" class="kn-material-input" v-model="selectedRule.dataSource" :options="datasourcesList" optionLabel="DATASOURCE_LABEL" @change="$emit('touched')"> </Dropdown>
                    <label for="dataSourceLabel" class="kn-material-input-label">{{ $t('kpi.measureDefinition.dataSource') }}</label>
                </span>
            </div>
            <VCodeMirror ref="editor" class="flex" v-model:value="code" :options="options" />
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iRule } from '../../MeasureDefinition'
import { VCodeMirror } from 'vue3-code-mirror'
import queryCardDescriptor from './QueryCardDescriptor.json'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'query-card',
    components: { Card, Dropdown, VCodeMirror },
    props: { rule: { type: Object, required: true }, datasourcesList: { type: Array, required: true } },
    emits: ['touched'],
    data() {
        return {
            queryCardDescriptor,
            selectedRule: {} as iRule,
            code: '',
            options: {
                cursor: true,
                line: false,
                lineNumbers: true,
                mode: 'text/x-mysql',
                tabSize: 4,
                theme: 'eclipse'
            }
        }
    },
    async mounted() {
        this.loadRule()
        console.log('QueryCard Selected Rule: ', this.selectedRule)
        console.log('QueryCard Datsources: ', this.datasourcesList)
        console.log('QueryCard Options: ', this.options)
        console.log('QueryCard EDITOR: ', this.$refs.editor)
    },
    methods: {
        loadRule() {
            this.selectedRule = this.rule as iRule
            this.code = this.rule.definition ?? ''
        }
    }
})
</script>

<style lang="scss" scoped>
#dataSource {
    width: 100%;
}
</style>
