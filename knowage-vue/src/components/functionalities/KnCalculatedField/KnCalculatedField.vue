<template>
    <Dialog class="kn-dialog--toolbar--primary" v-bind:visible="visibility" :header="$t('components.KnCalculatedField.title')" :closable="false" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <Message severity="info" :closable="false"> {{ $t('components.KnCalculatedField.description') }} </Message>

        <div>
            <div class="p-d-flex">
                <span class="p-float-label p-field p-ml-2 kn-flex">
                    <InputText type="text" v-model="cf.colName" class="kn-material-input" />
                    <label class="kn-material-input-label"> {{ $t('components.KnCalculatedField.columnName') }} </label>
                </span>
                <span v-if="availableOutputTypes" class="p-float-label p-field p-ml-2 kn-flex">
                    <Dropdown v-model="cf.outputType" :options="availableOutputTypes" class="kn-material-input" />
                    <label class="kn-material-input-label"> {{ $t('components.KnCalculatedField.type') }} </label>
                </span>
            </div>
        </div>
        <div>
            <span class="p-float-label">
                <Textarea v-model="cf.formula" rows="5" cols="60" class="kn-material-input" :autoResize="true" />
                <label class="kn-material-input-label"> {{ $t('components.KnCalculatedField.formula') }} </label>
            </span>
        </div>

        <template #footer>
            <Button class="p-button-text kn-button thirdButton" :label="$t('common.cancel')" @click="cancel" />
            <Button class="kn-button kn-button--primary" v-t="'common.apply'" @click="apply" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dropdown from 'primevue/dropdown'
import Textarea from 'primevue/textarea'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import { IKnCalculatedField } from '@/components/functionalities/KnCalculatedField/KnCalculatedField'

export default defineComponent({
    name: 'calculated-field',
    components: { Dropdown, Textarea, Dialog, Message },
    props: {
        fields: Array,
        visibility: Boolean,
        availableOutputTypes: Array
    },
    data() {
        return {
            cf: {} as IKnCalculatedField
        }
    },
    emits: ['save', 'cancel'],
    created() {},
    methods: {
        apply(): void {
            this.$emit('save', this.cf)
            this.clearForm()
        },
        cancel(): void {
            this.$emit('cancel', this.cf)
            this.clearForm()
        },
        clearForm(): void {
            this.cf = {} as IKnCalculatedField
        }
    }
})
</script>
