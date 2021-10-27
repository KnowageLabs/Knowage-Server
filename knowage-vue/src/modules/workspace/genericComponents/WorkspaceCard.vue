<template>
    <!-- {{ document }} -->
    <div class="card-container">
        <img class="card-image" src="https://i.imgur.com/9YqXpxc.jpeg" />
        <div class="details-container p-ml-2">
            <div class="top-container">
                <p>DASHBOARD</p>
            </div>
            <div class="middle-container">
                <h4><b>My Dashboard</b></h4>
                <p>norfolk_islang_legerage</p>
            </div>
            <div class="bottom-container">
                <i class="fas fa-ellipsis-v" />
                <i class="fas fa-info-circle" />
                <i class="fas fa-play-circle" />
            </div>
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
<style lang="scss" scoped>
.card-container {
    display: flex;
    border-style: solid;
    border-color: gray;
    border-width: 1px;
    border-radius: 10px;
    width: 350px;
    height: 175px;
}
.card-image {
    width: 70px;
    border-top-left-radius: 10px;
    border-bottom-left-radius: 10px;
}
</style>
