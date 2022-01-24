<template>
    <Dialog id="qbe-having-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="QBEHavingDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #left>
                    {{ $t('qbe.having.title') }}
                </template>

                <template #right>
                    <KnFabButton icon="fas fa-plus" @click="addNewHaving"></KnFabButton>
                </template>
            </Toolbar>
        </template>

        <Message class="p-mx-4" severity="info" :closable="false" :style="QBEHavingDialogDescriptor.styles.message">
            {{ $t('qbe.having.infoMessage') }}
        </Message>

        <Message v-if="havings.length === 0" class="p-mx-4 p-my-2" severity="info" :closable="false" :style="QBEHavingDialogDescriptor.styles.message">
            {{ $t('qbe.having.noHavings') }}
        </Message>
        <div>
            <QBEHavingCard v-for="having in havings" :key="having.filterId" :propHaving="having" :havings="havingDialogData?.query.havings" @removeHaving="removeHaving"></QBEHavingCard>
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
import QBEHavingDialogDescriptor from './QBEHavingDialogDescriptor.json'
import QBEHavingCard from './QBEHavingCard.vue'

export default defineComponent({
    name: 'qbe-having-dialog',
    components: { Dialog, KnFabButton, Message, QBEHavingCard },
    props: { visible: { type: Boolean }, havingDialogData: { type: Object as PropType<{ field: iField; query: iQuery }> } },
    emits: ['save', 'close'],
    data() {
        return {
            QBEHavingDialogDescriptor,
            havings: [] as any[],
            nextHavingIndex: -1
        }
    },
    watch: {
        havingDialogData() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            console.log('Having Dialog - loadData() - HAVING DIALOG DATA: ', this.havingDialogData)
            if (!this.havingDialogData || !this.havingDialogData.field || !this.havingDialogData.query) return

            this.havings = []
            this.havingDialogData.query.havings.forEach((filter: iFilter) => {
                console.log('Having Dialog - loadData() - COMPARE: ', filter.leftOperandValue, ' === ', this.havingDialogData?.field.id)
                if (filter.leftOperandValue === this.havingDialogData?.field.id) {
                    this.havings.push({ ...filter })
                }
            })
            this.nextHavingIndex = this.getHavingNextIndex()
        },
        getHavingNextIndex() {
            let maxIndex = 0
            this.havings.forEach((having: iFilter) => {
                if (having.filterInd > maxIndex) maxIndex = having.filterInd
            })
            return maxIndex + 1
        },
        addNewHaving() {
            const field = this.havingDialogData?.field
            console.log('Having Dialog - addNewHaving() - FIELD: ', field)
            if (field) {
                this.havings.push({
                    filterId: 'having' + this.nextHavingIndex,
                    filterDescripion: 'having1' + this.nextHavingIndex,
                    filterInd: this.nextHavingIndex,
                    promptable: false,
                    leftOperandAggregator: field.funct,
                    leftOperandValue: field.id,
                    leftOperandDescription: field.entity + ':' + field.funct + ' (' + field.alias + ')',
                    leftOperandLongDescription: field.entity + ':' + field.funct + ' (' + field.alias + ')',
                    leftOperandType: 'Field Content',
                    leftOperandDefaultValue: null,
                    leftOperandDataType: '',
                    leftOperandLastValue: null,
                    operator: 'EQUALS TO',
                    rightOperandAggregator: '',
                    rightOperandValue: [],
                    rightOperandDescription: '',
                    rightOperandLongDescription: '',
                    rightOperandType: '',
                    rightType: 'manual',
                    rightOperandDefaultValue: [''],
                    rightOperandLastValue: [''],
                    booleanConnector: 'AND',
                    deleteButton: false,
                    color: '',
                    entity: field.entity
                })
                this.nextHavingIndex++
            }
        },
        removeHaving(having: iFilter) {
            console.log('QBE Having Dialog - removeHaving() - HAVING TO REMOVE: ', having)
            const index = this.havings.findIndex((el: iFilter) => el.filterId === having.filterId)
            console.log('QBE Having Dialog - removeHaving() - INDEX: ', index)
            if (index !== -1) this.havings.splice(index, 1)
        },
        closeDialog() {
            this.$emit('close')
        },
        save() {
            this.$emit('save', this.havings, this.havingDialogData?.field)
        }
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
