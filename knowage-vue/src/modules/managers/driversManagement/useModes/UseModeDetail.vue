<template>
    <TabView class="tabview-custom kn-page-content">
        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.details') }}</span>
            </template>
            <DetailsCard :selectedMode="mode"></DetailsCard>
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.roles') }}</span>
            </template>
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.constraints') }}</span>
            </template>
        </TabPanel>
    </TabView>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DetailsCard from './DetailsCard.vue'

export default defineComponent({
    name: 'business-model-catalogue-detail',
    components: {
        TabView,
        TabPanel,
        DetailsCard
    },
    props: {
        selectedMode: {
            type: Object,
            required: false
        }
    },
    data() {
        return {
            mode: {} as any
        }
    },
    watch: {
        selectedMode() {
            //this.v$.$reset()
            this.mode = { ...this.selectedMode } as any
        }
    },
    mounted() {
        if (this.selectedMode) {
            this.mode = { ...this.selectedMode } as any
        }
    }
})
</script>
