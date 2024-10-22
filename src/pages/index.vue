<script setup>
import { newMusic, songPlaylist } from "@/api/api";
const router = useRouter();
const state = reactive({
  searchKw: "",
  tableData: [],
  playlists: [],
  loading: true,
  images: [
  '../static/img/bk.png', // 确保路径正确
  '../static/img/bk.png',
  '../static/img/bk.png',
  '../static/img/bk.png'
]
});
const { tableData, playlists, loading } = toRefs(state);

onMounted(async () => {
  state.loading = true;
  // 获取新歌
  newMusic().then(({ data }) => {
    state.tableData = data.result.map((item) => {
      const names = item.song.artists.map((subItem) => subItem.name).join(",");
      return {
        cover: item.picUrl,
        title: item.name,
        singer: names,
        album: item.song.album.name,
        time: item.song.duration,
        id: item.id,
        mv: item.song.mvid,
      };
    });
    state.loading = false;
  });

  // 获取热门歌单
  songPlaylist({ limit: 30, offset: 0 }).then(({ data }) => {
    state.playlists = data.playlists;
  });
});
</script>
<template>
  <div>
    
      <el-carousel indicator-position="outside">
    <el-carousel-item >
      <img src="../static/img/xzq.webp" alt="Carousel Image" class="carousel-image" />
    </el-carousel-item>
     <el-carousel-item >
      <img src="../static/img/cyx.jpg" alt="Carousel Image" class="carousel-image" />
    </el-carousel-item>
     <el-carousel-item >
      <img src="../static/img/fdt.jpg" alt="Carousel Image" class="carousel-image" />
    </el-carousel-item>
  </el-carousel>

     
   
    <el-skeleton :rows="5" animated :loading="loading">
      <template #default>
        <div class="content-section">
          <musicTable :data="tableData"></musicTable>
        </div>
        <div style="margin-top: 50px">
          <div class="apps-card">
            <el-skeleton :rows="5" animated :loading="loading" />
            <div
              class="apps-item"
              v-for="(item, index) in playlists"
              :key="index"
              @click="
                router.push({ path: '/playList', query: { id: item.id } })
              "
            >
              <div class="app-card">
                <img :src="item.coverImgUrl + '?param=200y200'" alt="" />
              </div>
              <a href="javascript:;">{{ item.name }}</a>
            </div>
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>
<style lang="scss" scoped>

.el-carousel__item h3 {
  display: flex;
  color: #475669;
  opacity: 0.75;
  line-height: 300px;
  margin: 0;
}

.el-carousel__item:nth-child(2n) {
  background-color: #99a9bf;
}

.el-carousel__item:nth-child(2n + 1) {
  background-color: #d3dce6;
}

.carousel-image {
  width: 100%; /* 使图片自适应轮播图宽度 */
  height: 100%; /* 维持图片比例 */
}
</style>
