<template>
    <Dialog class="bsdialog" :style="bsDescriptor.style.bsDialog" :visible="showBusinessViewDialog" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #left>
                    {{ $t('metaweb.businessModel.newView') }}
                </template>
            </Toolbar>
        </template>

        <StepOne v-if="wizardStep === 1" :physicalModels="physicalModels" :showBusinessViewDialog="showBusinessViewDialog" :bnssViewObject="tmpBnssView" />

        <form v-if="wizardStep === 2" ref="bvForm" class="p-fluid p-formgrid p-grid p-mt-4 p-mx-2 kn-flex-0">
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label ">
                    <Dropdown id="source" class="kn-material-input" v-model="sourceTable" :options="tmpBnssView.physicalModels" optionLabel="name" />
                    <label for="source" class="kn-material-input-label"> {{ $t('metaweb.businessModel.sourceTable') }}</label>
                </span>
            </div>
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label ">
                    <Dropdown id="target" class="kn-material-input" v-model="targetTable" :options="tmpBnssView.physicalModels" optionLabel="name" />
                    <label for="target" class="kn-material-input-label"> {{ $t('metaweb.businessModel.targetTable') }}</label>
                </span>
            </div>
        </form>

        <TableAssociator class="kn-flex" v-if="wizardStep === 2" :sourceArray="sourceTable.columns" :targetArray="targetTable.columns" :useMultipleTablesFromSameSource="true" @drop="updateSummary" @relationshipDeleted="updateSummary" />

        <div v-if="wizardStep === 2" id="summary-container" class="p-m-3 p-d-flex p-flex-column kn-flex-05">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('metaweb.businessModel.summary') }}
                </template>
                <template #right>
                    <Button v-if="!expandSummary" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandSummary = true" />
                    <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandSummary = false" />
                </template>
            </Toolbar>
            <div class="kn-relative kn-flex">
                <Listbox v-show="expandSummary" class="kn-list data-condition-list kn-absolute kn-height-full kn-width-full kn-overflow" :options="summary">
                    <template #empty>{{ $t('metaweb.businessModel.summaryHint') }} </template>
                    <template #option="slotProps">
                        <div v-for="(rel, index) in slotProps.option.links" v-bind:key="index" class="associator-target-list-item">
                            <span class="p-d-flex p-flex-row kn-width-full">
                                <span class="kn-truncated kn-flex-05">
                                    {{ rel.name }}
                                </span>
                                <i class="fas fa-link kn-flex-05" />
                                <span class="kn-truncated kn-flex">
                                    {{ slotProps.option.name }}
                                </span>
                                <Button icon="far fa-trash-alt kn-flex-0" class="p-button-text p-button-rounded p-button-plain" v-if="slotProps.option[associatedItem] && slotProps.option[associatedItem].length > 0" />
                            </span>
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteRelationship(slotProps.option)" />
                        </div>
                    </template>
                </Listbox>
            </div>
        </div>

        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="closeDialog" />
            <Button v-if="wizardStep == 2" class="kn-button kn-button--secondary" :label="$t('common.back')" :disabled="buttonDisabled || summary.length > 0" @click="previousStep" />
            <Button v-if="wizardStep == 1" class="kn-button kn-button--primary" :label="$t('common.next')" :disabled="buttonDisabled" @click="nextStep" />
            <Button v-if="wizardStep == 2" class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="buttonDisabled" @click="saveBusinessView" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import useValidate from '@vuelidate/core'
import Dialog from 'primevue/dialog'
import bsDescriptor from '../MetawebBusinessModelDescriptor.json'
import StepOne from './businessViewWizard/MetawebBusinessViewWizardStepOne.vue'
import TableAssociator from '@/modules/managers/businessModelCatalogue/metaweb/businessModel/tableAssociator/MetawebTableAssociator.vue'
import Dropdown from 'primevue/dropdown'
import Listbox from 'primevue/listbox'

const { observe, generate, applyPatch } = require('fast-json-patch')

export default defineComponent({
    components: { Dialog, StepOne, TableAssociator, Dropdown, Listbox },
    emits: ['closeDialog'],
    props: { physicalModels: Array, showBusinessViewDialog: Boolean, meta: Object },
    computed: {
        buttonDisabled(): boolean {
            if (this.v$.$invalid || this.tmpBnssView.physicalModels.length < 2) {
                return true
            } else return false
        }
    },
    data() {
        return {
            bsDescriptor,
            v$: useValidate() as any,
            tmpBnssView: { physicalModels: [], name: '', description: '' } as any,
            observer: null as any,
            metaObserve: {} as any,
            wizardStep: 1,
            expandSummary: true,
            summary: [] as any,
            sourceTable: { columns: [] } as any,
            targetTable: { columns: [] } as any
        }
    },
    created() {
        this.loadMeta()
    },
    watch: {
        meta() {
            console.log(this.meta)
            this.loadMeta()
        }
    },
    methods: {
        async loadMeta() {
            this.meta ? (this.metaObserve = this.meta) : ''
            this.meta ? (this.observer = observe(this.metaObserve.businessModels)) : ''
        },
        resetPhModel() {
            this.tmpBnssView.physicalModels = []
        },
        closeDialog() {
            this.$emit('closeDialog')
            this.tmpBnssView = { physicalModels: [], name: '', description: '' } as any
        },
        nextStep() {
            this.wizardStep++
        },
        previousStep() {
            this.wizardStep--
        },
        updateSummary() {
            this.summary = []
            for (var i = 0; i < this.tmpBnssView.physicalModels.length; i++) {
                for (var col = 0; col < this.tmpBnssView.physicalModels[i].columns.length; col++) {
                    // eslint-disable-next-line no-prototype-builtins
                    if (this.tmpBnssView.physicalModels[i].columns[col].hasOwnProperty('links') && this.tmpBnssView.physicalModels[i].columns[col].links.length > 0) {
                        this.summary.push(this.tmpBnssView.physicalModels[i].columns[col])
                    }
                }
            }
            console.log(this.summary)
        },
        logEvent(event) {
            console.log(event)
        },
        async saveBusinessView() {
            var tmpData = {} as any
            tmpData.name = this.tmpBnssView.name
            tmpData.description = this.tmpBnssView.description
            tmpData.sourceBusinessClass = this.tmpBnssView.sourceBusinessClass
            tmpData.physicaltable = []
            tmpData.relationships = {}

            for (var i = 0; i < this.tmpBnssView.physicalModels.length; i++) {
                var tmpDataObj = this.tmpBnssView.physicalModels[i]
                tmpData.physicaltable.push(tmpDataObj.name)
                for (var col = 0; col < this.tmpBnssView.physicalModels[i].columns.length; col++) {
                    // eslint-disable-next-line no-prototype-builtins
                    if (this.tmpBnssView.physicalModels[i].columns[col].hasOwnProperty('links') && this.tmpBnssView.physicalModels[i].columns[col].links.length > 0) {
                        //check if the table is present in the relationships object
                        // eslint-disable-next-line no-prototype-builtins
                        if (!tmpData.relationships.hasOwnProperty(tmpDataObj.name)) {
                            tmpData.relationships[tmpDataObj.name] = {}
                        }
                        var tabObj = tmpData.relationships[tmpDataObj.name]
                        var tmpColObj = this.tmpBnssView.physicalModels[i].columns[col]
                        //check if table has column object
                        // eslint-disable-next-line no-prototype-builtins
                        if (!tabObj.hasOwnProperty(tmpColObj.name)) {
                            tabObj[tmpColObj.name] = {}
                        }
                        var colObj = tabObj[tmpColObj.name]
                        for (var rel = 0; rel < tmpColObj.links.length; rel++) {
                            //check if column  has has target table object
                            // eslint-disable-next-line no-prototype-builtins
                            if (!colObj.hasOwnProperty(tmpColObj.links[rel].tableName)) {
                                colObj[tmpColObj.links[rel].tableName] = []
                            }
                            var targetTableObj = colObj[tmpColObj.links[rel].tableName]
                            targetTableObj.push(tmpColObj.links[rel].name)
                        }
                    }
                }
            }
            const postData = { data: tmpData, diff: generate(this.observer) }
            await this.$http
                .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/addBusinessView`, postData)
                .then(async (response: AxiosResponse<any>) => {
                    this.metaObserve = applyPatch(this.metaObserve, response.data)
                    this.closeDialog()
                })
                .catch(() => {})
        }
    }
})
</script>
<style lang="scss">
.data-condition-list {
    border: 1px solid $color-borders !important;
    border-top: none;
}
</style>
