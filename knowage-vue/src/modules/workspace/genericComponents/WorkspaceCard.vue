<template>
    <div>
        <!-- {{ document }} -->
        <div class="card-container">
            <img src="img_avatar.png" alt="Avatar" style="width:100%" />
            <div class="container">
                <h4><b>John Doe</b></h4>
                <p>Architect & Engineer</p>
            </div>
        </div>
        <!-- <Card>
            <template #header>
                <img alt="user header" src="demo/images/usercard.png" />
            </template>
            <template #title>
                Advanced Card
            </template>
            <template #content>
                Lorem ipsum dolor sit amet, consectetur adipisicing elit. Inventore sed consequuntur error repudiandae numquam deserunt quisquam repellat libero asperiores earum nam nobis, culpa ratione quam perferendis esse, cupiditate neque quas!
            </template>
            <template #footer>
                <Button icon="pi pi-check" label="Save" />
                <Button icon="pi pi-times" label="Cancel" class="p-button-secondary" style="margin-left: .5em" />
            </template>
        </Card> -->
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import descriptor from './DetailSidebarDescriptor.json'
// import Card from 'primevue/card'

export default defineComponent({
    name: 'workspace-card',
    components: {
        //  Card
    },
    emits: [],
    props: { document: Array as any },
    computed: {},

    data() {
        return {
            descriptor
        }
    },
    created() {},
    watch: {},
    methods: {}
})
</script>
<style lang="css">
.card-container {
    border: gray;
}
</style>
