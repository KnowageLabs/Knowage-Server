<template>
  <Toolbar>
        <template #left>
            Gallery Management
        </template>

        <template #right>
            <Button icon="pi pi-search" class="p-mr-2" />
            <Button icon="pi pi-times" class="p-button-danger p-button-text" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-1">
      <div class="p-col-2">
        <ul>
          <li v-for="template in galleryTemplates" :key="template.id">
            <router-link :to="template.id">{{template.label}}</router-link>
          </li>
        </ul>
      </div>
      <div class="p-col-10">
        <router-view />
      </div>
    </div>
</template>

<script>
export default {
  name: 'gallery-management',
  data (){
    return{
      galleryTemplates: [
        {id:'1234', label:'test1'},
        {id:'5678', label:'test2'}
      ]
    }
  }
}
</script>
