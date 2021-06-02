<template>
    <div class="newsHint">
        <Card class="newsHintContent">
            <template #content>
                {{ $t('managers.newsManagement.noNewsSelected') }}
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'news-management-hint'
})
</script>

<style lang="scss" scoped>
.newsHint {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 100%;

    .newsHintContent {
        border-radius: 0;
        width: 90%;
    }
}
</style>
