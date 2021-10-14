<template>
    <Dialog :visible="dialogVisible" :modal="true" class="kn-dialog--toolbar--primary" :closable="false" :style="userAttributesLovValueDialogDescriptor.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col">
                <template #left>
                    {{ attribute.attributeName }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" data-test="submit-button" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeDialog" />
                </template>
            </Toolbar>
        </template>
        <DataTable :value="lovValues" v-model:selection="selectedLovValues" dataKey="id" responsiveLayout="scroll" v-model:filters="filters" filterDisplay="row" :globalFilterFields="userAttributesLovValueDialogDescriptor.globalFilterFields">
            <template #header>
                <div class="p-col-8 p-input-icon-left">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input p-col-12" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                </div>
            </template>
            <Column :selectionMode="selectionMode" dataKey="id" :exportable="false"></Column>
            <Column field="value" header="Value" :sortable="true"> </Column>
        </DataTable>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { iAttribute } from '../UsersManagement'
import userAttributesLovValueDialogDescriptor from './UserAttributesLovValueDialogDescriptor.json'
import axios from 'axios'

export default defineComponent({
    name: 'lovs-value-dialog',
    components: { Dialog, DataTable, Column },
    emits: ['closeDialog', 'saveLovValues'],
    props: {
        attribute: {
            type: Object as PropType<iAttribute>,
            required: true
        },
        dialogVisible: Boolean,
        selection: Object as PropType<any>
    },
    watch: {
        attribute() {
            if (!this.attribute) return
            this.loadAttributeValue()
            this.selectionMode = this.attribute.multivalue ? 'multiple' : 'single'
            this.selectedLovValues = this.attribute.multivalue ? [] : {}
        }
    },
    created() {
        this.loadAttributeValue()
    },
    data() {
        return {
            selectedLovValues: [] as Array<any> | Object,
            lovValues: [
                { id: 1, value: 'Bla bla' },
                { id: 2, value: 'ohoooo' }
            ],
            userAttributesLovValueDialogDescriptor: userAttributesLovValueDialogDescriptor,
            selectionMode: 'multiple',
            filters: {
                global: [filterDefault],
                value: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object
        }
    },
    methods: {
        async loadAttributeValue() {
            if (this.attribute?.lovId) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/lovs/${this.attribute.lovId}/preview`).then((response) => {
                    this.lovValues = response.data.map((lovValue, index) => {
                        return { value: lovValue, id: index }
                    })
                    this.loadSelectedValues()
                })
            }
        },
        loadSelectedValues() {
            if (this.attribute.multivalue) {
                this.selectedLovValues = []
                if (Array.isArray(this.selection)) {
                    const values = [] as Array<any>
                    this.selection.forEach((selValue) => {
                        const ind = this.lovValues.findIndex((lovValue) => lovValue.value == selValue)
                        if (ind >= 0) {
                            values.push(this.lovValues[ind])
                        }
                    })
                    this.selectedLovValues = values
                }
            } else {
                this.selectedLovValues = {}
                const ind = this.lovValues.findIndex((lovValue) => lovValue.value == this.selection)
                if (ind >= 0) {
                    this.selectedLovValues = this.lovValues[ind]
                }
            }
        },
        closeDialog() {
            this.$emit('closeDialog')
        },
        handleSubmit() {
            this.$emit('saveLovValues', this.selectedLovValues)
        }
    }
})
</script>
<style lang="scss" scoped>
.kn-toolbar button {
    color: white;
}
</style>
