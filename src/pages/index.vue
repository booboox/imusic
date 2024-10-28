<script setup>
import { newMusic, songPlaylist } from "@/api/api";
import { ElCarousel, ElCarouselItem } from 'element-plus';
import img1 from '@/static/img/lbt/1.png';
import img2 from '@/static/img/lbt/2.png';
import img3 from '@/static/img/lbt/3.png';
import img4 from '@/static/img/lbt/4.png';

const router = useRouter();
const state = reactive({
  searchKw: "",
  tableData: [],
  playlists: [],
  loading: true,
});

// 导入的图片数组
const carouselImages = [img1, img2, img3, img4];

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
    <!-- 添加轮播图 -->
    <el-carousel :interval="4000" type="card" height="200px">
      <el-carousel-item v-for="(image, index) in carouselImages" :key="index">
        <img :src="image" alt="Carousel Image" style="width: 100%; height: 100%; object-fit: cover;">
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
.el-carousel__item img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 确保图片填满 */
}

.el-carousel__item h3 {
  color: #475669;
  opacity: 0.75;
  line-height: 200px;
  margin: 0;
  text-align: center;
}

.el-carousel__item:nth-child(2n) {
  background-color: #99a9bf;
}

.el-carousel__item:nth-child(2n + 1) {
  background-color: #d3dce6;
}
</style>
