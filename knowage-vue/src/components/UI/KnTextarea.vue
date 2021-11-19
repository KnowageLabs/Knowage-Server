<template>
    <span>
        <span class="p-float-label">
            <InputText :id="name" type="text" :modelValue="modelValue" v-bind="$attrs" :class="[cssClass ? cssClass + ' kn-truncated' : 'kn-material-input kn-truncated', required && !modelValue ? 'p-invalid' : '']" :disabled="true" />

            <label :for="name" :class="cssClass ? cssClass : 'kn-material-input-label'">{{ label }}</label>
            <Button class="p-button-text p-button-rounded p-button-plain pi pi-pencil" @click="switchVisibility" /> </span
    ></span>

    <Dialog class="kn-dialog--toolbar--primary knTextareaDialog" v-bind:visible="visible" footer="footer" :header="$t('common.edit')" :closable="false" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <div>
            <span class="p-float-label">
                <Textarea v-model="localCopy" :rows="rows ? rows : 5" :cols="cols ? cols : 30" :class="[cssClass ? cssClass : 'kn-material-input', required && !locaCopy ? 'p-invalid' : '']" :autoResize="false" v-bind="$attrs" />
                <label :for="name" :class="cssClass ? cssClass : 'kn-material-input-label'">{{ label }}</label>
            </span>
        </div>
        <template #footer>
            <Button class="p-button-text kn-button thirdButton" :label="$t('common.cancel')" @click="switchVisibility(false)" />
            <Button class="kn-button kn-button--primary" v-t="'common.save'" @click="switchVisibility(true)" />
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Dialog from 'primevue/dialog'
    import Textarea from 'primevue/textarea'

    export default defineComponent({
        name: 'kn-text-area',
        components: { Dialog, Textarea },

        props: {
            name: { type: String, required: true },
            label: { type: String, required: true },
            modelValue: { type: String, required: true },
            required: { type: Boolean, required: true },
            cssClass: { type: String, required: false },
            rows: { type: Number, required: false },
            cols: { type: Number, required: false }
        },
        data() {
            return { visible: false, localCopy: '' }
        },
        emits: ['update:visibility'],
        created() {
            if (this.modelValue) this.localCopy = JSON.parse(JSON.stringify(this.modelValue))
        },
        methods: {
            switchVisibility(save: Boolean): void {
                if (!save && this.localCopy != this.modelValue) {
                    console.log('ciao')
                }
                this.visible = !this.visible
            }
        }
    })
</script>

<style lang="scss">
    .knTextareaDialog {
        min-width: 600px;
        max-width: 900px;
        min-height: 150px;

        &:deep(.p-dialog-content) {
            @extend .knTextareaDialog;

            padding: 16px;
        }
    }
</style>
