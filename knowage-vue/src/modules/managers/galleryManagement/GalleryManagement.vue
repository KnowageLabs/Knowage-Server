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
        <ul class="kn-list">
          <li v-for="template in galleryTemplates" :key="template.id">
            <router-link :to="`/knowage/gallerymanagement/${template.id}`">
              <div class="p-d-flex p-ai-center p-jc-around">
                <span class="kn-list--left">
                  <img v-if="template.img" src=""/>
                  <i v-if="template.icon" :class="template.icon"></i> 
                </span>
                <span class="kn-list--text">
                  <h3>{{template.label}}</h3>
                  <p v-if="template.desc">{{template.desc}}</p>
                </span>
              </div>
            </router-link>
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
        {id:'1234', label:'test1', icon:'fas fa-star'},
        {id:'5678', label:'test2'}
      ]
    }
  }
}
</script>

<style lang="scss" scoped>
  .kn-list {
    a{
      text-decoration: none;
    }
    li {
      transition: background-color .3s ease-out;
      &:hover {
        background-color: #ccc;
      }
    }
    list-style: none;
    padding: 0;
    .kn-list--left {
      display: inline-flex;
      justify-content: center;
      align-items: center;
      width: 30px;
    }
    .kn-list--text {
      flex: 1;
      padding-left: 8px;
    }
  }
</style>
