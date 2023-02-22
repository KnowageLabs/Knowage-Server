<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('common.rank') }}
                </template>
            </Toolbar>
        </template>

        <div class="p-p-2">
            <label>{{ documentRank === 0 ? $t('documentExecution.main.firstToRate') : $t('documentExecution.main.currentRank') }}</label>
            <Rating v-if="documentRank !== 0" v-model="documentRank" class="document-execution-rank-stars" :disabled="true" :cancel="false"></Rating>
        </div>

        <div class="p-p-2">
            <label>{{ $t('documentExecution.main.yourRating') + ': ' }}</label>
            <Rating v-model="newRank" class="document-execution-rank-stars" :cancel="false"></Rating>
        </div>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Dialog from 'primevue/dialog'
import Rating from 'primevue/rating'

export default defineComponent({
    name: 'document-execution-help-dialog',
    components: { Dialog, Rating },
    props: { visible: { type: Boolean }, propDocumentRank: { type: Object as PropType<string | null> } },
    emits: ['close', 'saveRank'],
    data() {
        return {
            documentRank: null as string | null,
            newRank: null as string | null
        }
    },
    watch: {
        propDocumentRank() {
            this.loadDocumentRank()
        }
    },
    created() {
        this.loadDocumentRank()
    },
    methods: {
        loadDocumentRank() {
            this.newRank = null
            this.documentRank = this.propDocumentRank as any
        },
        closeDialog() {
            this.newRank = null
            this.$emit('close')
        },
        save() {
            this.$emit('saveRank', this.newRank)
        }
    }
})
</script>

<style lang="scss">
.document-execution-rank-stars .p-rating-icon {
    font-size: 2rem;
}
</style>
