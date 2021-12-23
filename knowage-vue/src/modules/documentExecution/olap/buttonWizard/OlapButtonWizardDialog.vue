<template>
    <Dialog id="olap-button-wizard-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="olapButtonWizardDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('documentExecution.olap.buttonsWizard.title') }}
                </template>
            </Toolbar>
        </template>

        <DataTable :value="buttons" class="p-datatable-sm kn-table p-m-2" :scrollable="true" scrollHeight="100%">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>

            <Column field="name" :header="$t('common.name')" style="flex:5"></Column>
            <Column :header="$t('common.visible')">
                <template #header>
                    <Checkbox class="p-mr-2" v-model="allVisibleSelected" :binary="true" @change="setAllChecked('visible')" />
                </template>
                <template #body="slotProps">
                    <Checkbox v-model="slotProps.data.visible" :binary="true" @change="setChecked(slotProps.data, 'visible')" />
                </template>
            </Column>
            <Column :header="$t('common.clicked')">
                <template #header>
                    <Checkbox class="p-mr-2" v-model="allClickedSelected" :binary="true" @change="setAllChecked('clicked')" />
                </template>
                <template #body="slotProps">
                    <Checkbox v-model="slotProps.data.clicked" :binary="true" :disabled="!slotProps.data.clickable" @change="setChecked(slotProps.data, 'clicked')" />
                </template>
            </Column>
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iButton } from '../Olap'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import olapButtonWizardDialogDescriptor from './OlapButtonWizardDialogDescriptor.json'

export default defineComponent({
    name: 'olap-button-wizard-dialog',
    components: { Checkbox, Column, DataTable, Dialog },
    props: { sbiExecutionId: { type: String }, propButtons: { type: Array as PropType<iButton[]> } },
    data() {
        return {
            olapButtonWizardDialogDescriptor,
            buttons: [] as iButton[],
            selected: {} as any,
            allVisibleSelected: false,
            allClickedSelected: false
        }
    },
    watch: {
        propButtons() {
            this.loadButtons()
        }
    },
    created() {
        this.loadButtons()
    },
    methods: {
        loadButtons() {
            this.buttons = this.propButtons as iButton[]
            console.log('BUTTONS LOADED IN DIALOG: ', this.buttons)
        },
        setAllChecked(type: string) {
            console.log('SET ALL CHECKED: ', type)
        },
        setChecked(button: any, type: string) {
            console.log('SET CHECKED: ', button, type)
        },
        closeDialog() {
            this.$emit('close')
        },
        save() {
            console.log('SAVE CLICKED!')
        }
    }
})
</script>

<style lang="scss">
#olap-button-wizard-dialog .p-dialog-header,
#olap-button-wizard-dialog .p-dialog-content {
    padding: 0;
}
#olap-button-wizard-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
