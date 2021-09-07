<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="showCategoryDialog" />
            <Button class="p-button-text p-button-rounded p-button-plain" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <p>Details</p>
    <p>{{ id }}</p>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
export default defineComponent({
    name: 'cross-navigation-detail',
    props: {
        id: {
            type: String
        }
    },
    methods: {
        closeTemplate() {
            this.$emit('close')
        },
        setDirty(): void {
            this.$emit('touched')
        }
    }
})
</script>
