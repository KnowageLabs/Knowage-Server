<template>
    <Dialog id="qbe-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEFilterDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('common.filters') }}
                </template>

                <template #end>
                    <i v-show="temporalFiltersEnabled()" class="fa fa-calendar kn-cursor-pointer p-mr-4" @click="openTemporalFilterDialog"></i>
                    <KnFabButton icon="fas fa-plus" @click="addNewFilter"></KnFabButton>
                </template>
            </Toolbar>
        </template>

        <Message v-if="filters.length === 0 && !parameterTableVisible" class="p-m-4" severity="info" :closable="false" :style="QBEFilterDialogDescriptor.styles.message">
            {{ $t('common.info.noDataFound') }}
        </Message>
        <div v-else-if="!parameterTableVisible">
            <QBEFilterCard v-for="filter in filters" :key="filter.filterId" :propFilter="filter" :id="id" :propEntities="entities" :subqueries="filterDialogData?.query.subqueries" :field="filterDialogData?.field" :propParameters="parameters" @removeFilter="removeFilter"></QBEFilterCard>
        </div>

        <QBEFilterParameters v-else-if="parameterTableVisible" :propParameters="parameters" @parametersUpdated="onParametersUpdated"></QBEFilterParameters>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ parameterTableVisible ? $t('qbe.filters.applyParameters') : $t('common.save') }}</Button>
        </template>

        <QBETemporalFilterDialog :visible="temporalFilterDialogVisible" :temporalFilters="temporalFilters" @close="temporalFilterDialogVisible = false" @save="addTemporalFilter"></QBETemporalFilterDialog>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iField, iQuery, iFilter } from '../../QBE'
import { removeInPlace } from '../qbeAdvancedFilterDialog/treeService'
import Dialog from 'primevue/dialog'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Message from 'primevue/message'
import QBEFilterCard from './QBEFilterCard.vue'
import QBEFilterDialogDescriptor from './QBEFilterDialogDescriptor.json'
import QBETemporalFilterDialog from './QBETemporalFilterDialog.vue'
import QBEFilterParameters from './QBEFilterParameters.vue'

const crypto = require('crypto')
const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'qbe-filter-dialog',
    components: { Dialog, KnFabButton, Message, QBEFilterCard, QBETemporalFilterDialog, QBEFilterParameters },
    props: { visible: { type: Boolean }, filterDialogData: { type: Object as PropType<{ field: iField; query: iQuery }> }, id: { type: String }, entities: { type: Array }, propParameters: { type: Array, required: true }, propExpression: { type: Object } },
    emits: ['save', 'close', 'parametersUpdated'],
    data() {
        return {
            QBEFilterDialogDescriptor,
            filters: [] as iFilter[],
            nextFilterIndex: -1,
            temporalFilters: [] as any[],
            temporalFilterDialogVisible: false,
            parameters: [] as any[],
            parameterTableVisible: false,
            expression: {} as any,
            updatedParameters: [] as any[]
        }
    },
    watch: {
        filterDialogData() {
            this.loadData()
        },
        propParameters: {
            handler() {
                this.loadParameters()
            },
            deep: true
        },
        propExpression() {
            this.loadExpression()
        }
    },
    created() {
        this.loadData()
        this.loadParameters()
        this.loadExpression()
    },
    methods: {
        loadData() {
            if (!this.filterDialogData || !this.filterDialogData.field || !this.filterDialogData.query) return

            this.filters = []
            this.filterDialogData.query.filters.forEach((filter: iFilter) => {
                if (filter.leftOperandValue === this.filterDialogData?.field.id) {
                    this.filters.push({ ...filter })
                }
            })
            this.nextFilterIndex = crypto.randomBytes(16).toString('hex')
            if (this.filterDialogData.field.type === 'inline.calculated.field') {
                this.setCalculatedFieldLongDescription(this.filterDialogData.field, this.filterDialogData.field.originalId as string)
            } else if (this.filterDialogData.field.attributes?.type === 'inLineCalculatedField') {
                this.setCalculatedFieldLongDescription(this.filterDialogData.field, this.filterDialogData.field.id)
            }
        },
        setCalculatedFieldLongDescription(field: any, id: string | null) {
            if (id) {
                const temp = id.substring(id.lastIndexOf('.') + 1)
                const tempSplitted = temp.split(':')
                field.longDescription = tempSplitted[0] + ' : ' + tempSplitted[1]
            }
        },
        loadParameters() {
            this.parameters = this.propParameters ? [...this.propParameters] : []
            this.updatedParameters = deepcopy(this.parameters)
        },
        loadExpression() {
            this.expression = this.propExpression as any
        },
        removeFilter(filter: iFilter) {
            const index = this.filters.findIndex((el: iFilter) => el.filterId === filter.filterId)
            if (index !== -1) this.filters.splice(index, 1)
            removeInPlace(this.expression, '$F{' + filter.filterId + '}')
        },
        addNewFilter() {
            const field = this.filterDialogData ? this.filterDialogData.field : ({} as any)

            const filter = {
                filterId: 'Filter' + this.nextFilterIndex,
                filterDescripion: 'Filter' + this.nextFilterIndex,
                filterInd: this.nextFilterIndex,
                promptable: false,
                leftOperandValue: this.filterDialogData?.field.attributes?.type === 'inLineCalculatedField' ? this.filterDialogData?.field.attributes.formState : field.id,
                leftOperandDescription: field.longDescription ?? field.attributes.longDescription,
                leftOperandLongDescription: field.longDescription ?? field.attributes.longDescription,
                leftOperandType: this.filterDialogData?.field.type === 'inline.calculated.field' || this.filterDialogData?.field.attributes?.type === 'inLineCalculatedField' ? 'inline.calculated.field' : 'Field Content',
                leftOperandDefaultValue: null,
                leftOperandLastValue: null,
                leftOperandAlias: field.alias ?? field.attributes.field,
                leftOperandDataType: '',
                operator: 'EQUALS TO',
                rightOperandDescription: '',
                rightOperandLongDescription: '',
                rightOperandValue: [''],
                rightOperandType: 'Static Content',
                rightType: 'manual',
                rightOperandDefaultValue: [''],
                rightOperandLastValue: [''],
                rightOperandAlias: '',
                rightOperandDataType: '',
                booleanConnector: 'AND',
                deleteButton: false,
                color: '',
                entity: field.entity
            }
            if (field) {
                this.filters.push(filter)
                this.nextFilterIndex = crypto.randomBytes(16).toString('hex')
            }
            this.push(filter)
        },
        push(filter: iFilter) {
            var newConst = {
                type: 'NODE_CONST',
                value: '$F{' + filter.filterId + '}',
                childNodes: [],
                details: {
                    leftOperandAlias: filter.leftOperandAlias,
                    operator: filter.operator,
                    entity: filter.entity,
                    rightOperandValue: filter.rightOperandValue.join(', ')
                }
            }

            let newRoot = {} as any

            if (this.expression && Object.keys(this.expression).length === 0 && Object.getPrototypeOf(this.expression) === Object.prototype) {
                newRoot = {
                    type: 'NODE_OP',
                    childNodes: [],
                    value: filter.booleanConnector || 'AND'
                }
                newRoot.childNodes.push(newConst)
                this.expression = JSON.parse(JSON.stringify(newRoot))
            } else if (this.expression.childNodes && this.expression.childNodes.length <= 1) {
                newRoot = this.expression
                newRoot.childNodes.unshift(newConst)
            } else {
                newRoot = {
                    type: 'NODE_OP',
                    childNodes: [],
                    value: filter.booleanConnector || 'AND'
                }
                newRoot.childNodes.push(newConst)
                newRoot.childNodes.push(JSON.parse(JSON.stringify(this.expression)))
                this.expression = JSON.parse(JSON.stringify(newRoot))
            }
        },
        temporalFiltersEnabled() {
            return (this.$store.state as any).user.functionalities.includes('Timespan') && (this.filterDialogData?.field.dataType?.toLowerCase() === 'java.sql.date' || this.filterDialogData?.field.dataType?.toLowerCase() === 'java.sql.timestamp')
        },
        async openTemporalFilterDialog() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/timespan/listTimespan/?types=DAY_OF_WEEK&types=DAY_OF_WEEK&types=DAY_OF_WEEK`).then((response: AxiosResponse<any>) => (this.temporalFilters = response.data.data))
            this.temporalFilterDialogVisible = true
        },
        addTemporalFilter(temporalFilter: any) {
            if (temporalFilter) {
                for (let i = 0; i < temporalFilter.definition.length; i++) {
                    const tempFilter = {
                        filterId: 'Filter' + this.nextFilterIndex,
                        filterDescripion: 'Filter' + this.nextFilterIndex,
                        filterInd: this.nextFilterIndex,
                        promptable: false,
                        leftOperandValue: this.filterDialogData?.field.attributes?.type === 'inLineCalculatedField' ? this.filterDialogData?.field.attributes.formState : this.filterDialogData?.field.id,
                        leftOperandDescription: this.filterDialogData?.field.longDescription ?? this.filterDialogData?.field.attributes.longDescription,
                        leftOperandLongDescription: this.filterDialogData?.field.longDescription ?? this.filterDialogData?.field.attributes.longDescription,
                        leftOperandType: this.filterDialogData?.field.type === 'inline.calculated.field' || this.filterDialogData?.field.attributes?.type === 'inLineCalculatedField' ? 'inline.calculated.field' : 'Field Content',
                        leftOperandDefaultValue: null,
                        leftOperandLastValue: null,
                        leftOperandAlias: this.filterDialogData?.field.alias ?? this.filterDialogData?.field.attributes.field,
                        leftOperandDataType: '',
                        operator: 'BETWEEN',
                        rightType: 'manual',
                        rightOperandValue: [temporalFilter.definition[i].from, temporalFilter.definition[i].to],
                        rightOperandDescription: this.createRightOperandDescription(temporalFilter.definition[i].from, temporalFilter.definition[i].to, this.filterDialogData?.field.dataType),
                        rightOperandLongDescription: this.createRightOperandDescription(temporalFilter.definition[i].from, temporalFilter.definition[i].to, this.filterDialogData?.field.dataType),
                        rightOperandType: 'Static Content',
                        rightOperandDefaultValue: [''],
                        rightOperandLastValue: [''],
                        rightOperandAlias: null,
                        rightOperandDataType: '',
                        booleanConnector: 'OR',
                        deleteButton: false
                    } as any
                    this.filters.push(tempFilter)
                    this.push(tempFilter)
                    this.nextFilterIndex = crypto.randomBytes(16).toString('hex')
                }
            }
            this.temporalFilterDialogVisible = false
        },
        createRightOperandDescription(from: string, to: string, dataType: any) {
            if (dataType === 'java.sql.Timestamp') {
                return from + ' 00:00:00 ' + ' ---- ' + to + ' 00:00:00 '
            }
            return from + ' ---- ' + to
        },
        closeDialog() {
            this.$emit('close')
            this.nextFilterIndex = crypto.randomBytes(16).toString('hex')
            this.updatedParameters = []
            this.parameterTableVisible = false
            this.removeFiltersOnCancel()
        },
        removeFiltersOnCancel() {
            this.filters.forEach((filter: iFilter) => {
                const index = this.filterDialogData?.query.filters.findIndex((tempFilter: iFilter) => filter.filterId === tempFilter.filterId)
                if (index === -1) removeInPlace(this.expression, '$F{' + filter.filterId + '}')
            })
        },
        save() {
            if (this.propParameters.length > 0 && !this.parameterTableVisible && this.filterUsesParameters()) {
                this.parameterTableVisible = true
            } else {
                this.$emit('save', this.filters, this.filterDialogData?.field, this.updatedParameters, this.expression)
                this.parameterTableVisible = false
            }
        },
        onParametersUpdated(updatedParameters: any[]) {
            this.updatedParameters = updatedParameters
        },
        filterUsesParameters() {
            let usesParameters = false
            for (let i = 0; i < this.filters.length; i++) {
                if (this.filters[i].rightType === 'parameter') {
                    usesParameters = true
                    break
                }
            }

            return usesParameters
        }
    }
})
</script>

<style lang="scss">
#qbe-filter-dialog .p-dialog-header,
#qbe-filter-dialog .p-dialog-content {
    padding: 0;
}
#qbe-filter-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
