<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.buisnessModelCatalogue.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="new-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <div class="p-col">
                    <h1>TREE</h1>
                </div>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <h1>OSTALO</h1>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunctionality } from './FunctionalitiesManagement'
// import axios from 'axios'

export default defineComponent({
    name: 'functionalities-management',
    components: {},
    data() {
        return {
            functionalities: [] as iFunctionality[]
        }
    },
    async created() {},
    methods: {}
})
</script>
