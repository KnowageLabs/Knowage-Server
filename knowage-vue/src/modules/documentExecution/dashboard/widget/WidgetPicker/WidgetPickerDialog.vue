<template>
    <Dialog class="dialog-no-padding" :visible="visible" style="width: 60%" :modal="true" :closable="false">
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

        <div id="widget-card-container" class="p-grid p-m-2">
            <WidgetCard v-for="(widget, index) in widgetTypes" :key="index" :widget="widget" @click="openWidgetEditor(widget)" />
        </div>
    </Dialog>
</template>

<script lang="ts">
/**
 * ! this component is in charge of opening the correct widget editor and containing all the cards
 */
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { IWidgetPickerType } from '../../Dashboard'
import Dialog from 'primevue/dialog'
import WidgetCard from './WidgetPickerCard.vue'

export default defineComponent({
    name: 'widget-picker-dialog',
    components: { Dialog, WidgetCard },
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
