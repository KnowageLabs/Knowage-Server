<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ $t('managers.newsManagement.detailTitle') }}</template>
    </Toolbar>
    <div class="p-d-flex p-ai-center p-jc-center hint-container">
        <div class="p-d-flex p-flex-column">
            <img :src="require('@/assets/images/news/newspaper.svg')" height="100" width="100" />
            <div>
                {{ $t('managers.newsManagement.noNewsSelected') }}
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'news-management-hint'
})
</script>

<style lang="scss" scoped>
.hint-container {
    height: 70vh;
}
</style>
