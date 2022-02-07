<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-mb-1">
        <template #left>
            Example Primary toolbar
        </template>
        <template #right>
            <FabButton icon="fas fa-plus" />
        </template>
    </Toolbar>

    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #left>
            Example Secondary toolbar
        </template>
        <template #right> </template>
    </Toolbar>

    <Card class="p-mt-4">
        <template #content>
            <form class="p-fluid p-m-5 p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText id="exampleTextInput" class="kn-material-input" type="text" />
                        <label for="exampleTextInput" class="kn-material-input-label"> Example Text Input </label>
                    </span>
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText id="exampleNumberInput" class="kn-material-input" type="number" />
                        <label for="exampleNumberInput" class="kn-material-input-label"> Example Number Input </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import Card from 'primevue/card'

export default defineComponent({
    name: 'theme-management-examples',
    components: { Card, FabButton },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
