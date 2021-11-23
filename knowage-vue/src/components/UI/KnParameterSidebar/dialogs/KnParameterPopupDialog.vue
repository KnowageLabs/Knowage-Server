<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="knParameterPopupDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left> {{ $t('common.parameter') + ': ' }} {{ popupData ? popupData.idParam : '' }} </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <KnParameterPopupTable v-if="parameterPopUpData && !loading" :parameterPopUpData="parameterPopUpData" :multivalue="multivalue" :multipleSelectedRows="multipleSelectedRows" @selected="onRowSelected"></KnParameterPopupTable>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import knParameterPopupDialogDescriptor from './KnParameterPopupDialogDescriptor.json'
import KnParameterPopupTable from './KnParameterPopupTable.vue'

export default defineComponent({
    name: 'kn-parameter-popup-dialog',
    components: { Dialog, KnParameterPopupTable },
    props: { visible: { type: Boolean }, selectedParameter: { type: Object }, propLoading: { type: Boolean }, parameterPopUpData: { type: Object } },
    emits: ['close', 'save'],
    data() {
        return {
            knParameterPopupDialogDescriptor,
            parameter: null as any,
            popupData: null as any,
            selectedRow: null as any,
            multipleSelectedRows: [] as any,
            multivalue: false,
            loading: false
        }
    },
    watch: {
        visible() {
            this.loadParamaterData()
        },
        selectedParameter() {
            this.loadParamaterData()
        },
        propLoading() {
            this.setLoading()
        },
        parameterPopUpData() {
            this.loadPopupData()
        }
    },
    created() {
        this.setLoading()
        this.loadParamaterData()
    },
    methods: {
        loadParamaterData() {
            this.loading = true
            this.loadParameter()
            this.loadPopupData()
            this.loading = false
        },
        loadParameter() {
            this.parameter = this.selectedParameter
            this.multivalue = this.selectedParameter?.multivalue

            if (this.multivalue) {
                this.setMultipleSelectedRows()
            }
            // console.log('LOADED PARAMETER: ', this.parameter)
        },
        setMultipleSelectedRows() {
            // console.log('SELECTED PARAMETER: ', this.parameter)
            // console.log('SELECTED ROWS: ', this.multipleSelectedRows)
            this.multipleSelectedRows = this.parameter.parameterValue
        },
        loadPopupData() {
            this.popupData = this.parameterPopUpData
            // console.log('LOADED DATA: ', this.popupData)
        },

        setLoading() {
            this.loading = this.propLoading
        },
        closeDialog() {
            this.$emit('close')
            this.loadParameter()
            this.popupData = null
            this.selectedRow = null
        },
        onRowSelected(selectedRows: any) {
            // console.log('MULTIVALUE? ', this.multivalue)
            // console.log('SELECTED ROW: ', selectedRows)
            if (!this.multivalue) {
                this.selectedRow = selectedRows
                // console.log('SELECTED ROW: ', this.selectedRow)
            } else {
                this.multipleSelectedRows = selectedRows
                // console.log('SELECTED MULTIPLE ROWS AFTER PUSH: ', this.multipleSelectedRows)
            }
        },
        save() {
            // console.log('PARAMETER: ', this.parameter)
            if (this.selectedRow && !this.multivalue) {
                this.parameter.parameterValue = [{ value: this.selectedRow.value, description: this.selectedRow.description }]

                this.selectedRow = null
            } else {
                this.parameter.parameterValue = []
                this.multipleSelectedRows?.forEach((el: any) => this.parameter.parameterValue.push({ value: el.value, description: el.description }))
            }

            this.popupData = null
            this.$emit('save', this.parameter)
        }
    }
})
</script>
