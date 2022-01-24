<template>
    <Dialog id="qbe-having-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEHavingDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('qbe.having.title') }}
                </template>

                <template #right>
                    <KnFabButton icon="fas fa-plus" @click="addNewFilter"></KnFabButton>
                </template>
            </Toolbar>
        </template>

        <Message v-if="filters.length === 0" class="p-m-4" severity="info" :closable="false" :style="QBEHavingDialogDescriptor.styles.message">
            {{ $t('common.info.noDataFound') }}
        </Message>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iField, iQuery, iFilter } from '../../QBE'
import Dialog from 'primevue/dialog'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Message from 'primevue/message'
import QBEHavingDialogDescriptor from './QBEHavingDialogDescriptor.json'

export default defineComponent({
    name: 'qbe-having-dialog',
    components: { Dialog, KnFabButton, Message },
    props: { visible: { type: Boolean }, filterDialogData: { type: Object as PropType<{ field: iField; query: iQuery }> } },
    emits: ['save', 'close'],
    data() {
        return {
            QBEHavingDialogDescriptor,
            filters: [] as iFilter[]
        }
    },
    watch: {
        filterDialogData() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            console.log('Having Dialog - loadData() - FITLTER DIALOG DATA: ', this.filterDialogData)
            if (!this.filterDialogData || !this.filterDialogData.field || !this.filterDialogData.query) return

            this.filters = []
            this.filterDialogData.query.filters.forEach((filter: iFilter) => {
                console.log(filter.leftOperandValue + ' === ' + this.filterDialogData?.field.id)
                if (filter.leftOperandValue === this.filterDialogData?.field.id) {
                    this.filters.push({ ...filter })
                }
            })
        },
        addNewFilter() {},
        closeDialog() {
            this.$emit('close')
        },
        save() {}
    }
})
</script>

<style lang="scss">
#qbe-having-dialog .p-dialog-header,
#qbe-having-dialog .p-dialog-content {
    padding: 0;
}
#qbe-having-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
