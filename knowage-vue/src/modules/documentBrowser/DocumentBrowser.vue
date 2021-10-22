<template>
    <div class="kn-page">
        <TabView id="document-browser-tab-view">
            <TabPanel>
                <template #header>
                    <i class="fa fa-folder-open"></i>
                </template>
            </TabPanel>
        </TabView>

        <router-view />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'document-browser',
    components: { TabView, TabPanel },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>

<style lang="scss">
#document-browser-tab-view .p-tabview-panels {
    padding: 0;
}
</style>
