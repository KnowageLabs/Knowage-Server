<template>
    <KnHint :title="'managers.driversManagement.useModes.title'" :hint="'managers.driversManagement.useModes.hint'" v-if="!selectedMode.id" data-test="mode-hint"></KnHint>
    <TabView class="tabview-custom kn-page-content" v-else>
        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.useModes.details') }}</span>
            </template>
            <DetailsCard :selectedMode="mode" :selectionTypes="selectionTypes" :layers="layers" :isDate="isDate"></DetailsCard>
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.useModes.roles') }}</span>
            </template>
            <RolesCard :roles="roles" :selectedRolesProp="mode.associatedRoles"></RolesCard>
            {{ mode.associatedRoles }}
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.useModes.constraints') }}</span>
            </template>
        </TabPanel>
    </TabView>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DetailsCard from './DetailsCard.vue'
import RolesCard from './RolesCard.vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'business-model-catalogue-detail',
    components: {
        TabView,
        TabPanel,
        DetailsCard,
        RolesCard,
        KnHint
    },
    props: {
        selectedMode: {
            type: Object,
            required: false
        },
        roles: {
            type: Array,
            requierd: true
        },
        selectionTypes: {
            type: Array,
            requierd: true
        },
        layers: {
            type: Array,
            requierd: true
        },
        isDate: {
            type: Boolean,
            requierd: true
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
