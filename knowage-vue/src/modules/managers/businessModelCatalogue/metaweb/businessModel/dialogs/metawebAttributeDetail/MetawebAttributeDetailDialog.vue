<template>
    <Dialog id="metaweb-attribute-detail-dialog" class="p-fluid kn-dialog--toolbar--primary" :contentStyle="metawebAttributeDetailDialogDescriptor.dialog.style" :visible="visible" :modal="false" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('metaweb.businessModel.attributesDetail.title') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <form v-if="attribute" class="p-fluid p-formgrid p-grid p-mt-4 p-mx-2 kn-flex-0">
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <InputText id="name" class="kn-material-input" v-model.trim="attribute.name" />
                    <label for="name" class="kn-material-input-label"> {{ $t('common.name') }}</label>
                </span>
            </div>

            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <InputText id="name" class="kn-material-input" v-model.trim="attribute.description" />
                    <label for="name" class="kn-material-input-label"> {{ $t('common.description') }}</label>
                </span>
            </div>
        </form>
        <h1>IT WORKS</h1>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iBusinessModelColumn } from '../../../Metaweb'
import Dialog from 'primevue/dialog'
import metawebAttributeDetailDialogDescriptor from './MetawebAttributeDetailDialogDescriptor.json'

export default defineComponent({
    name: 'metaweb-attribute-detail-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, selectedAttribute: { type: Object as PropType<iBusinessModelColumn> } },
    data() {
        return {
            metawebAttributeDetailDialogDescriptor,
            attribute: null as iBusinessModelColumn | null,
            loading: false
        }
    },
    watch: {
        selectedAttribute() {
            this.loadAttribute()
        }
    },
    created() {
        this.loadAttribute()
    },
    methods: {
        loadAttribute() {
            this.attribute = this.selectedAttribute as iBusinessModelColumn
            console.log('LOADED ATTRIBUTE: ', this.attribute)
        }
    }
})
</script>

<style lang="scss">
#metaweb-attribute-detail-dialog #metaweb-attribute-detail-dialog .p-dialog-header,
#metaweb-attribute-detail-dialog .p-dialog-content {
    padding: 0;
}

#metaweb-attribute-detail-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
