<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" />
        </template>
    </Toolbar>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
export default defineComponent({
    name: 'alert-details'
})
</script>
