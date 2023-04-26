<template>
    <div class="p-fluid p-formgrid p-grid">
        <div class="p-field p-col-12">
            <span class="p-float-label">
                <Textarea v-model="calc" class="kn-material-input kn-width-full" rows="4" :auto-resize="true" maxlength="150" @change="onColumnChanged" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.editorTags.calc') }}</label>
            </span>
        </div>
        <div class="p-field p-col-4">
            <span class="p-float-label">
                <InputText v-model="min" class="kn-material-input" @change="onColumnChanged" />
                <label class="kn-material-input-label">{{ $t('common.min') }}</label>
            </span>
        </div>
        <div class="p-field p-col-4">
            <span class="p-float-label">
                <InputText v-model="max" class="kn-material-input" @change="onColumnChanged" />
                <label class="kn-material-input-label">{{ $t('common.max') }}</label>
            </span>
        </div>
        <div class="p-field p-col-4">
            <span class="p-float-label">
                <InputNumber v-model="precision" class="kn-material-input" @change="onColumnChanged" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.precision') }}</label>
            </span>
        </div>
        <div class="p-field p-d-flex p-col-12 p-mt-2">
            <InputSwitch v-model="format" class="" @change="onColumnChanged" />
            <label class="kn-material-input-label p-mx-2">{{ $t('dashboard.widgetEditor.editorTags.toLocale') }}</label>
            <i v-tooltip.right="$t('dashboard.widgetEditor.editorTags.hint.toLocale')" class="p-button-text p-button-rounded p-button-plain fas fa-circle-question" style="color: rgba(0, 0, 0, 0.6)" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import descriptor from '../WidgetTagsDialogDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'widget-editor-calculator',
    components: { Textarea, InputNumber, InputSwitch },
    emits: ['insertChanged'],
    data() {
        return {
            descriptor,
            calc: '',
            min: '',
            max: '',
            precision: null as any,
            format: false as boolean
        }
    },
    methods: {
        onColumnChanged() {
            let forInsert = `[kn-calc=(${this.calc}) min='${this.min}' max='${this.max}'`
            if (this.precision) forInsert += ` precision='${this.precision}`
            forInsert += `${this.format ? ' format' : ''}]`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
