<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #start>test</template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>

    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <TabView>
        <TabPanel>
            <template #header>
                <span>{{ $t('managers.layersManagement.layerTitle') }}</span>
            </template>
            <LayerTab />
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.layersManagement.filterTitle') }}</span>
            </template>
            <FilterTab />
        </TabPanel>
    </TabView>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import LayerTab from './LayersManagementLayerTab.vue'
import FilterTab from './LayersManagementFilterTab.vue'

export default defineComponent({
    components: { TabView, TabPanel, LayerTab, FilterTab },
    props: { id: { type: String, required: false } },
    computed: {},
    emits: ['touched', 'closed'],
    data() {
        return {
            touched: false
        }
    },
    async created() {},
    watch: {
        id() {}
    },
    methods: {
        closeTemplateConfirm() {
            if (!this.touched) {
                this.closeTemplate()
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.closeTemplate()
                    }
                })
            }
        },
        closeTemplate() {
            this.$router.push('/layers-management')
            this.$emit('closed')
        }
    }
})
</script>
