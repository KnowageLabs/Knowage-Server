<template>
    <Dialog class="dialog-no-padding" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #start>
                    {{ $t('documentExecution.olap.showMdxQuery') }}
                </template>
                <template #end>
                    <Button icon="pi pi-times" class="p-button-link" @click="$emit('closeWidgetPicker')" />
                </template>
            </Toolbar>
        </template>

        <div class="widget-card-container" v-for="(widget, index) in widgetTypes" :key="index">
            <Button :label="widget.type" class="p-button-link" @click="openWidgetEditor(widget)" />
        </div>
    </Dialog>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget behaviour related to data and interactions, not related to view elements.
 */
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { IWidgetPickerType } from '../../Dashboard'
import Dialog from 'primevue/dialog'

export default defineComponent({
    name: 'widget-picker-dialog',
    components: { Dialog },
    emits: ['closeWidgetPicker'],
    inject: [],
    props: {},
    data() {
        return {
            widgetTypes: [] as IWidgetPickerType[]
        }
    },
    created() {
        this.getWidgetTypes()
    },
    computed: {},
    methods: {
        async getWidgetTypes() {
            // await this.$http.get(import.meta.env.VITE_DASHBOARD_PATH + `1.0/engine/widget`).then((response: AxiosResponse<any>) => (this.widgetTypes = response.data))
        },
        openWidgetEditor(widget) {
            //TODO: logic that opens widget editor
            console.log(widget)
        }
    }
})
</script>
<style lang="scss">
.dialog-no-padding.p-dialog .p-dialog-header,
.dialog-no-padding.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}
</style>
