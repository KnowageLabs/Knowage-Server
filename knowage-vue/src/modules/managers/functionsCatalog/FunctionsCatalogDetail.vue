<template>
    <Dialog id="function-catalog-detail-dialog" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :contentStyle="functionsCatalogDetailDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ selectedFunction.name }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text p-m-2" :label="$t('common.close')" @click="$emit('close')"></Button>
                    <Button class="kn-button p-button-text" :label="$t('common.save')" :disabled="readonly" @click="onSave"></Button>
                </template>
            </Toolbar>
        </template>

        <h4>{{ selectedFunction }}</h4>

        <TabView>
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.functionsCatalog.general') }}</span>
                </template>

                <FunctionCatalogGeneralTab :propFunction="selectedFunction" :readonly="readonly" :functionTypes="filteredFunctionTypes" :propKeywords="keywords"></FunctionCatalogGeneralTab>
            </TabPanel>
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.functionsCatalog.input') }}</span>
                </template>

                <FunctionCatalogInputTab :propFunction="selectedFunction" :readonly="readonly"></FunctionCatalogInputTab>
            </TabPanel>
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.functionsCatalog.script') }}</span>
                </template>

                <FunctionCatalogScriptTab :propFunction="selectedFunction" :readonly="readonly"></FunctionCatalogScriptTab>
            </TabPanel>
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.functionsCatalog.output') }}</span>
                </template>

                <FunctionCatalogOutputTab :propFunction="selectedFunction" :readonly="readonly"></FunctionCatalogOutputTab>
            </TabPanel>
        </TabView>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction, iFunctionType, iInputColumn, iInputVariable, iOutputColumn } from './FunctionsCatalog'
import Dialog from 'primevue/dialog'
import functionsCatalogDetailDescriptor from './FunctionsCatalogDetailDescriptor.json'
import FunctionCatalogGeneralTab from './tabs/FunctionCatalogGeneralTab/FunctionCatalogGeneralTab.vue'
import FunctionCatalogInputTab from './tabs/FunctionCatalogInputTab/FunctionCatalogInputTab.vue'
import FunctionCatalogScriptTab from './tabs/FunctionCatalogScriptTab/FunctionCatalogScriptTab.vue'
import FunctionCatalogOutputTab from './tabs/FunctionCatalogOutputTab/FunctionCatalogOutputTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'functions-catalog-detail',
    components: { Dialog, FunctionCatalogGeneralTab, FunctionCatalogInputTab, FunctionCatalogScriptTab, FunctionCatalogOutputTab, TabView, TabPanel },
    props: {
        visible: { type: Boolean },
        propFunction: { type: Object },
        functionTypes: { type: Array },
        keywords: { type: Array }
    },
    data() {
        return {
            functionsCatalogDetailDescriptor,
            selectedFunction: {} as iFunction,
            filteredFunctionTypes: [] as iFunctionType[],
            missingFields: [] as String[]
        }
    },
    watch: {
        propFunction() {
            this.loadFunction()
        },
        functionTypes() {
            this.loadFunctionTypes()
        }
    },
    computed: {
        // TODO proveriti uslov
        readonly(): boolean {
            return !(this.$store.state as any).user.isSuperadmin || this.selectedFunction?.owner !== (this.$store.state as any).user.userId
        }
    },
    created() {
        this.loadFunction()
        this.loadFunctionTypes()
    },
    methods: {
        loadFunction() {
            this.selectedFunction = this.propFunction
                ? ({ ...this.propFunction } as iFunction)
                : ({
                      description: '',
                      owner: (this.$store.state as any).user.userId,
                      language: '',
                      inputColumns: [] as iInputColumn[],
                      inputVariables: [] as iInputVariable[],
                      outputColumns: [] as iOutputColumn[],
                      family: 'online'
                  } as iFunction)
            console.log('READONLY: ', this.readonly)
        },
        loadFunctionTypes() {
            this.filteredFunctionTypes = this.functionTypes?.filter((el: any) => el.valueCd !== 'All') as iFunctionType[]
            console.log('FILTERED FUNCTION TYPES: ', this.filteredFunctionTypes)
        },
        validateArguments() {
            let valid = true

            if (!this.validateInputColumns()) {
                valid = false
            }

            if (!this.validateInputVariables()) {
                valid = false
            }

            if (!this.validateOutputColumns()) {
                valid = false
            }

            if (!this.validateFunctionInfo()) {
                valid = false
            }

            if (!this.validateCode()) {
                valid = false
            }

            console.log('MISSING FIELDS: ', this.missingFields)
            return valid
        },
        validateInputColumns() {
            let valid = true

            for (let i = 0; i < this.selectedFunction.inputColumns.length; i++) {
                if (!this.selectedFunction.inputColumns[i].name) {
                    valid = false
                    this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.inputColumnName', { number: i + 1 }))
                }
                if (!this.selectedFunction.inputColumns[i].type) {
                    valid = false
                    this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.inputColumnType', { number: i + 1 }))
                }
            }

            return valid
        },
        validateInputVariables() {
            let valid = true

            for (let i = 0; i < this.selectedFunction.inputVariables.length; i++) {
                if (!this.selectedFunction.inputVariables[i].name) {
                    valid = false
                    this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.inputVariableName', { number: i + 1 }))
                }
                if (!this.selectedFunction.inputVariables[i].type) {
                    valid = false
                    this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.inputVariableType', { number: i + 1 }))
                }
            }

            return valid
        },
        validateOutputColumns() {
            let valid = true

            if (this.selectedFunction.outputColumns.length === 0) {
                valid = false
                this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.outputColumnMissing'))
            }

            for (let i = 0; i < this.selectedFunction.outputColumns.length; i++) {
                if (!this.selectedFunction.outputColumns[i].name) {
                    valid = false
                    this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.outputColumnName', { number: i + 1 }))
                }
                if (!this.selectedFunction.outputColumns[i].fieldType) {
                    valid = false
                    this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.outputColumnFieldType', { number: i + 1 }))
                }
                if (!this.selectedFunction.outputColumns[i].type) {
                    valid = false
                    this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.outputColumnType', { number: i + 1 }))
                }
            }

            return valid
        },
        validateFunctionInfo() {
            let valid = true

            if (!this.selectedFunction.description) {
                valid = false
                this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.functionDescription'))
            }

            return valid
        },
        validateCode() {
            let valid = true

            if (this.selectedFunction.family === 'online' && this.selectedFunction.onlineScript === '') {
                valid = false
                this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.onlineScript'))
            } else if (this.selectedFunction.family === 'offline') {
                if (!this.selectedFunction.offlineScriptTrainModel) {
                    valid = false
                    this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.offlineTrainingScript'))
                }
                if (!this.selectedFunction.offlineScriptUseModel) {
                    valid = false
                    this.missingFields.push(this.$t('managers.functionsCatalog.missingFields.offlineUseScript'))
                }
            }

            return valid
        },
        onSave() {
            console.log('onSave() selectedFunction: ', this.selectedFunction)
            this.validateArguments()
        }
    }
})
</script>

<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
}

.full-screen-dialog.p-dialog .p-dialog-content {
    padding: 0;
}

#function-catalog-detail-dialog .p-toolbar-group-right {
    height: 100%;
}
</style>
