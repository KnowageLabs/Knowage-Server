<template>
    <Dialog class="bsdialog" :style="bsDescriptor.style.bsDialog" :visible="showBusinessViewDialog" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #start>
                    {{ $t('metaweb.businessModel.newView') }}
                </template>
            </Toolbar>
        </template>

        <StepOne v-if="wizardStep === 1" :physical-models="physicalModels" :show-business-view-dialog="showBusinessViewDialog" :bnss-view-object="tmpBnssView" />

        <form v-if="wizardStep === 2" ref="bvForm" class="p-fluid p-formgrid p-grid p-mt-4 p-mx-2 kn-flex-0">
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <Dropdown id="source" v-model="sourceTable" class="kn-material-input" :options="tmpBnssView.physicalModels" option-label="name" />
                    <label for="source" class="kn-material-input-label"> {{ $t('metaweb.businessModel.sourceTable') }}</label>
                </span>
            </div>
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <Dropdown id="target" v-model="targetTable" class="kn-material-input" :options="tmpBnssView.physicalModels" option-label="name" />
                    <label for="target" class="kn-material-input-label"> {{ $t('metaweb.businessModel.targetTable') }}</label>
                </span>
            </div>
        </form>

        <TableAssociator v-if="wizardStep === 2" class="kn-flex" :source-array="sourceTable.columns" :target-array="targetTable.columns" :use-multiple-tables-from-same-source="true" @drop="updateSummary" @relationshipDeleted="updateSummary" />

        <div v-if="wizardStep === 2" id="summary-container" class="p-m-3 p-d-flex p-flex-column kn-flex-05">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('metaweb.businessModel.summary') }}
                </template>
                <template #end>
                    <Button v-if="!expandSummary" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color: white" @click="expandSummary = true" />
                    <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color: white" @click="expandSummary = false" />
                </template>
            </Toolbar>
            <div class="kn-relative kn-flex">
                <Listbox v-show="expandSummary" class="kn-list data-condition-list kn-absolute kn-height-full kn-width-full kn-overflow" :options="summary">
                    <template #empty>{{ $t('metaweb.businessModel.summaryHint') }} </template>
                    <template #option="slotProps">
                        <div class="associator-block-hover p-d-flex p-flex-row p-ai-center kn-width-full">
                            <span class="kn-truncated kn-flex-05 p-ml-2">
                                {{ slotProps.option.name }}
                            </span>
                            <i v-if="slotProps.option['links'] && slotProps.option['links'].length > 0" class="fas fa-link kn-flex-05" />
                            <div v-if="slotProps.option['links'] && slotProps.option['links'].length > 0" class="p-d-flex p-flex-column kn-flex" :class="{ 'p-mb-1': slotProps.option['links'].length > 1 }">
                                <span v-for="(link, index) in slotProps.option['links']" :key="index" class="p-d-flex p-flex-row p-ai-center">
                                    <span class="kn-truncated">
                                        {{ link.name }}
                                    </span>
                                    <Button v-if="slotProps.option['links'].length > 1" icon="fas fa-times" class="associator-enable-hover p-button-text p-button-rounded p-button-plain" @click.stop="deleteRelationship(slotProps.option, link)" />
                                </span>
                            </div>
                            <Button v-if="slotProps.option['links'] && slotProps.option['links'].length > 0" icon="far fa-trash-alt kn-flex-0" class="associator-enable-hover p-button-text p-button-rounded p-button-plain" @click.stop="deleteRelationship(slotProps.option)" />
                        </div>
                    </template>
                </Listbox>
            </div>
        </div>

        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="closeDialog" />
            <Button v-if="wizardStep == 2 && !editMode" class="kn-button kn-button--secondary" :label="$t('common.back')" :disabled="buttonDisabled || summary.length > 0" @click="previousStep" />
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

import { generate, applyPatch } from 'fast-json-patch'

export default defineComponent({
    components: { Dialog, StepOne, TableAssociator, Dropdown, Listbox },
    props: { showBusinessViewDialog: Boolean, meta: { type: Object, required: true }, observer: { type: Object }, selectedBusinessModel: { type: Object, required: true }, editMode: Boolean },
    emits: ['closeDialog'],
    data() {
        return {
            bsDescriptor,
            v$: useValidate() as any,
            tmpBnssView: { physicalModels: [], name: '', description: '' } as any,
            metaObserve: {} as any,
            wizardStep: 1,
            expandSummary: true,
            summary: [] as any,
            sourceTable: { columns: [] } as any,
            targetTable: { columns: [] } as any,
            physicalModels: [] as any
        }
    },
    computed: {
        buttonDisabled(): boolean {
            if (this.v$.$invalid || this.tmpBnssView.physicalModels.length < 2) {
                return true
            } else return false
        }
    },
    watch: {
        meta() {
            this.loadMeta()
            this.setEditModeData()
        }
    },
    created() {
        this.loadMeta()
        this.setEditModeData()
    },
    methods: {
        async loadMeta() {
            this.meta ? (this.metaObserve = this.meta) : ''
            this.meta ? (this.physicalModels = JSON.parse(JSON.stringify(this.meta.physicalModels))) : ''
        },
        closeDialog() {
            this.tmpBnssView = null as any
            this.targetTable = null
            this.sourceTable = null
            this.$emit('closeDialog')
        },
        nextStep() {
            this.wizardStep++
        },
        previousStep() {
            this.wizardStep--
        },
        getItemIndex(list, name) {
            for (let i = 0; i < list.length; i++) {
                if (list[i].name === name) {
                    return i
                }
            }
            return -1
        },
        setEditModeData() {
            if (this.editMode == true) {
                this.wizardStep = 2
                for (let pti = 0; pti < this.selectedBusinessModel.physicalTables.length; pti++) {
                    let tmppt = {}
                    tmppt = JSON.parse(JSON.stringify(this.meta.physicalModels[this.selectedBusinessModel.physicalTables[pti].physicalTableIndex]))
                    this.tmpBnssView.physicalModels.push(tmppt)
                }

                for (let x = 0; x < this.tmpBnssView.physicalModels.length; x++) {
                    for (let y = 0; y < this.tmpBnssView.physicalModels[x].columns.length; y++) {
                        this.tmpBnssView.physicalModels[x].columns[y].$parent = this.tmpBnssView.physicalModels[x]
                    }
                }

                for (let i = 0; i < this.selectedBusinessModel.joinRelationships.length; i++) {
                    const rel = this.selectedBusinessModel.joinRelationships[i]
                    const destTab = this.tmpBnssView.physicalModels[this.getItemIndex(this.tmpBnssView.physicalModels, rel.destinationTable.name)]
                    const sourceTab = this.tmpBnssView.physicalModels[this.getItemIndex(this.tmpBnssView.physicalModels, rel.sourceTable.name)]
                    for (let dc = 0; dc < rel.destinationColumns.length; dc++) {
                        const destCol = destTab.columns[this.getItemIndex(destTab.columns, rel.destinationColumns[dc].name)]
                        const sourceCol = sourceTab.columns[this.getItemIndex(sourceTab.columns, rel.sourceColumns[dc].name)]
                        // eslint-disable-next-line no-prototype-builtins
                        if (!destCol.hasOwnProperty('links')) {
                            destCol.links = []
                        }
                        destCol.links.push(sourceCol)
                    }
                }

                this.updateSummary()
            }
        },
        updateSummary() {
            this.summary = []
            for (let i = 0; i < this.tmpBnssView.physicalModels.length; i++) {
                for (let col = 0; col < this.tmpBnssView.physicalModels[i].columns.length; col++) {
                    // eslint-disable-next-line no-prototype-builtins
                    if (this.tmpBnssView.physicalModels[i].columns[col].hasOwnProperty('links') && this.tmpBnssView.physicalModels[i].columns[col].links.length > 0) {
                        this.summary.push(this.tmpBnssView.physicalModels[i].columns[col])
                        // eslint-disable-next-line no-prototype-builtins
                    } /* elseif (this.tmpBnssView.physicalModels[i].columns[col].hasOwnProperty('links') && this.tmpBnssView.physicalModels[i].columns[col].links.length > 0) {
                        delete this.tmpBnssView.physicalModels[i].columns[col].links
                    } */
                }
            }
        },
        async saveBusinessView() {
            const tmpData = {} as any
            if (this.editMode) {
                tmpData.viewUniqueName = this.selectedBusinessModel.uniqueName
            } else {
                tmpData.name = this.tmpBnssView.name
                tmpData.description = this.tmpBnssView.description
                tmpData.sourceBusinessClass = this.tmpBnssView.sourceBusinessClass
                tmpData.physicaltable = []
            }

            tmpData.relationships = {}

            for (let i = 0; i < this.tmpBnssView.physicalModels.length; i++) {
                const tmpDataObj = this.tmpBnssView.physicalModels[i]
                this.editMode ? '' : tmpData.physicaltable.push(tmpDataObj.name)
                for (let col = 0; col < this.tmpBnssView.physicalModels[i].columns.length; col++) {
                    // eslint-disable-next-line no-prototype-builtins
                    if (this.tmpBnssView.physicalModels[i].columns[col].hasOwnProperty('links') && this.tmpBnssView.physicalModels[i].columns[col].links.length > 0) {
                        // eslint-disable-next-line no-prototype-builtins
                        if (!tmpData.relationships.hasOwnProperty(tmpDataObj.name)) {
                            tmpData.relationships[tmpDataObj.name] = {}
                        }
                        const tabObj = tmpData.relationships[tmpDataObj.name]
                        const tmpColObj = this.tmpBnssView.physicalModels[i].columns[col]
                        // eslint-disable-next-line no-prototype-builtins
                        if (!tabObj.hasOwnProperty(tmpColObj.name)) {
                            tabObj[tmpColObj.name] = {}
                        }
                        const colObj = tabObj[tmpColObj.name]
                        for (let rel = 0; rel < tmpColObj.links.length; rel++) {
                            // eslint-disable-next-line no-prototype-builtins
                            if (!colObj.hasOwnProperty(tmpColObj.links[rel].tableName)) {
                                colObj[tmpColObj.links[rel].tableName] = []
                            }
                            const targetTableObj = colObj[tmpColObj.links[rel].tableName]
                            targetTableObj.push(tmpColObj.links[rel].name)
                        }
                    }
                }
            }
            const postData = { data: tmpData, diff: [] }
            await this.$http
                .post(import.meta.env.VITE_META_API_URL + `/1.0/metaWeb/addBusinessView`, postData)
                .then(async (response: AxiosResponse<any>) => {
                    this.metaObserve = applyPatch(this.metaObserve, response.data)
                    generate(this.observer)
                })
                .catch(() => {})
                .finally(() => this.closeDialog())
        },
        deleteRelationship(item, rel?) {
            rel == undefined ? (item.links = []) : item.links.splice(rel, 1)
            this.updateSummary()
        }
    }
})
</script>
<style lang="scss">
.data-condition-list {
    border: 1px solid var(--kn-color-borders) !important;
    border-top: none;
}
</style>
