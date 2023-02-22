<template>
    <Dialog id="metaweb-inccorect-relationships-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="metawebInvalidRelationshipsDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false" :base-z-index="10" :auto-z-index="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ $t('common.warning') }}
                </template>
            </Toolbar>
        </template>

        <div class="p-m-4">
            <h3>{{ $t('metaweb.incorrectRelationships.title') }}</h3>
            <p>{{ $t('metaweb.incorrectRelationships.warningMessage.partOne') }}</p>
            <p>{{ $t('metaweb.incorrectRelationships.warningMessage.partTwo') }}</p>
            <p>{{ $t('metaweb.incorrectRelationships.warningMessage.partThree') }}</p>
        </div>

        <div class="p-d-flex p-flex-column p-m-2">
            <div v-for="(relation, index) in incorrectRelationships" :key="index" class="p-m-4">
                <h2>{{ relation.businessRelationshipName }}</h2>
                <div>{{ relation.sourceTableName }} <i class="fa fa-arrow-right" aria-hidden="true"></i> {{ relation.destinationTableName }}</div>
                <Chip class="p-m-2">{{ $t('metaweb.incorrectRelationships.requiredColumns') + ': ' + relation.requiredNumberOfColumns }}</Chip>
            </div>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Chip from 'primevue/chip'
    import Dialog from 'primevue/dialog'
    import metawebInvalidRelationshipsDialogDescriptor from './MetawebInvalidRelationshipsDialogDescriptor.json'

    export default defineComponent({
        name: 'metaweb-invalid-relationships-dialog',
        components: { Chip, Dialog },
        props: { visible: { type: Boolean }, propIncorrectRelationships: { type: Array } },
        emits: ['close', 'save'],
        data() {
            return {
                metawebInvalidRelationshipsDialogDescriptor,
                incorrectRelationships: [] as any[]
            }
        },
        watch: {
            propIncorrectRelationships() {
                this.loadIncorrectRelationships()
            }
        },
        created() {
            this.loadIncorrectRelationships()
        },
        methods: {
            loadIncorrectRelationships() {
                this.incorrectRelationships = this.propIncorrectRelationships as any[]
            },
            closeDialog() {
                this.$emit('close')
            },
            save() {
                this.$emit('save')
            }
        }
    })
</script>

<style lang="scss">
    #metaweb-inccorect-relationships-dialog .p-dialog-header,
    #metaweb-inccorect-relationships-dialog .p-dialog-content {
        padding: 0;
    }

    #metaweb-inccorect-relationships-dialog .p-dialog-content {
        display: flex;
        flex-direction: column;
        flex: 1;
    }
</style>
