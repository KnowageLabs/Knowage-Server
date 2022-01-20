<template>
    <Dialog id="qbe-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEFilterDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('common.filters') }}
                </template>

                <template #right>
                    <KnFabButton icon="fas fa-plus" @click="addNewFilter"></KnFabButton>
                </template>
            </Toolbar>
        </template>

        <Message v-if="filters.length === 0" class="p-m-4" severity="info" :closable="false" :style="QBEFilterDialogDescriptor.styles.message">
            {{ $t('common.info.noDataFound') }}
        </Message>
        <div v-else>
            <QbeFilterCard v-for="filter in filters" :key="filter.filterId" :propFilter="filter" :id="id" :propEntities="entities" :subqueries="filterDialogData?.query.subqueries" @removeFilter="removeFilter"></QbeFilterCard>
        </div>

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
import QbeFilterCard from './QbeFilterCard.vue'
import QBEFilterDialogDescriptor from './QBEFilterDialogDescriptor.json'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog, KnFabButton, Message, QbeFilterCard },
    props: { visible: { type: Boolean }, filterDialogData: { type: Object as PropType<{ field: iField; query: iQuery }> }, id: { type: String }, entities: { type: Array } },
    emits: ['save', 'close'],
    data() {
        return {
            QBEFilterDialogDescriptor,
            filters: [] as iFilter[],
            nextFilterIndex: -1
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
            console.log('FITLTER DIALOG DATA: ', this.filterDialogData)
            if (!this.filterDialogData || !this.filterDialogData.field || !this.filterDialogData.query) return

            this.filters = []
            this.filterDialogData.query.filters.forEach((filter: iFilter) => {
                console.log(filter.leftOperandValue + ' === ' + this.filterDialogData?.field.id)
                if (filter.leftOperandValue === this.filterDialogData?.field.id) {
                    this.filters.push({ ...filter })
                }
            })
            this.nextFilterIndex = this.filterDialogData.query.filters.length + 1
        },
        removeFilter(filter: iFilter) {
            // console.log('FILTER TO REMOVE: ', filter)
            const index = this.filters.findIndex((el: iFilter) => el.filterId === filter.filterId)
            if (index !== -1) this.filters.splice(index, 1)
        },
        addNewFilter() {
            const field = this.filterDialogData?.field
            console.log('FIELD: ', field)
            if (field) {
                this.filters.push({
                    filterId: 'Filter' + this.nextFilterIndex,
                    filterDescripion: 'Filter' + this.nextFilterIndex,
                    filterInd: this.nextFilterIndex,
                    promptable: false,
                    leftOperandValue: field.id,
                    leftOperandDescription: field.longDescription,
                    leftOperandLongDescription: field.longDescription,
                    leftOperandType: 'Field Content',
                    leftOperandDefaultValue: null,
                    leftOperandLastValue: null,
                    leftOperandAlias: field.alias,
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
                })
                this.nextFilterIndex++
            }
        },
        closeDialog() {
            this.$emit('close')
            this.nextFilterIndex = -1
        },
        save() {
            this.$emit('save', this.filters)
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
