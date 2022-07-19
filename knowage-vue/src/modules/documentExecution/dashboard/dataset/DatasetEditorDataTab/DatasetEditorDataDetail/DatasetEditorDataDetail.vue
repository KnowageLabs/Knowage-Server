<template>
    <Card>
        <template #header>
            <img alt="user header" src="demo/images/usercard.png" />
        </template>
        <template #title> Advanced Card </template>
        <template #content> Lorem ipsum dolor sit amet, consectetur adipisicing elit. Inventore sed consequuntur error repudiandae numquam deserunt quisquam repellat libero asperiores earum nam nobis, culpa ratione quam perferendis esse, cupiditate neque quas! </template>
        <template #footer>
            <Button icon="pi pi-check" label="Save" />
            <Button icon="pi pi-times" label="Cancel" class="p-button-secondary" style="margin-left: 0.5em" />
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
// import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'

export default defineComponent({
    name: 'dataset-editor-data-detail',
    components: { Card },
    props: {},
    emits: [],
    data() {
        return {}
    },
    setup() {},
    async created() {},
    methods: {}
})
</script>
