<template>
    <Message v-if="!widgetModel.dataset" class="p-mb-2" severity="warn" :closable="false" :style="descriptor.hintStyle">
        {{ $t(`managers.functionsCatalog.noDatasetSelected`) }}
    </Message>
    <span v-else class="p-float-label">
        <InputText class="kn-material-input" v-model="repeaterLimit" @change="onColumnChanged" />
        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.editorTags.limit') }}</label>
    </span>
    <!-- <Button icon="fas fa-terminal" class="p-button-text p-button-rounded p-button-plain" @click="logModel" /> -->
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import descriptor from '../WidgetTagsDialogDescriptor.json'
import Message from 'primevue/message'

export default defineComponent({
    name: 'widget-editor-repeater',
    components: { Message },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    emits: ['insertChanged'],
    data() {
        return {
            descriptor,
            repeaterLimit: ''
        }
    },
    methods: {
        onColumnChanged() {
            let limit = null as any
            if (this.repeaterLimit) limit = this.repeaterLimit
            const forInsert = `<div kn-repeat="true" limit="${limit}"></div>`
            this.$emit('insertChanged', forInsert)
        }
        // logModel() {
        //     console.log(this.widgetModel)
        // }
    }
})
</script>
