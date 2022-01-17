<template>
    <Dialog id="qbe-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEFilterDialogDescription.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('common.filters') }}
                </template>
            </Toolbar>
        </template>

        <Message v-if="filters.length === 0" class="p-m-2" severity="info" :closable="false" :style="QBEFilterDialogDescription.styles.message">
            {{ $t('common.info.noDataFound') }}
        </Message>
        <div v-else>
            <QbeFilterCard v-for="filter in filters" :key="filter.filterId" :propFilter="filter"></QbeFilterCard>
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
import QbeFilterCard from './QbeFilterCard.vue'
import QBEFilterDialogDescription from './QBEFilterDialogDescription.json'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog, QbeFilterCard },
    props: { visible: { type: Boolean }, filterDialogData: { type: Object as PropType<{ field: iField; query: iQuery }> } },
    data() {
        return {
            QBEFilterDialogDescription,
            filters: [] as any[]
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
