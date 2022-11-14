<template>
    <div class="dashboard-card-shadow kn-height-full p-ml-1">
        <div class="p-d-flex p-flex-row p-ai-center p-flex-wrap">
            <InputText class="kn-material-input p-m-3 model-search" v-model="searchWord" type="text" :placeholder="$t('common.search')" />
        </div>
        <div class="p-grid p-m-2" data-test="card-container">
            <Message v-if="htmlGalleryProp.length == 0" class="kn-flex p-m-2" severity="info" :closable="false">
                {{ $t('common.info.noDataFound') }}
            </Message>
            <template v-else> amount of items: {{ htmlGalleryProp.length }} </template>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IGalleryItem } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from '../HTMLWidgetSettingsDescriptor.json'
import Message from 'primevue/message'

export default defineComponent({
    name: 'html-widget-settings-container',
    components: { Message },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        htmlGalleryProp: { type: Array as PropType<IGalleryItem[]>, required: true }
    },
    data() {
        return {
            descriptor,
            searchWord: ''
        }
    },
    watch: {},
    created() {},
    methods: {}
})
</script>

<style lang="scss"></style>
