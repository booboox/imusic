<script setup>
import { mvUrl, commentMV, mvDetail } from "@/api/api";
import { watch, reactive, onMounted, toRefs } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const state = reactive({
  list: [],
  mvUrls: "",
  commList: {},
  mvDetails: {
    artists: [
      {
        name: "",
      },
    ],
  },
});
const { list, mvUrls, commList, mvDetails } = toRefs(state);

// 定义一个函数来获取MV相关数据
const fetchMVData = async (id) => {
  // 获取MV地址
  const mvResponse = await mvUrl({ id });
  state.mvUrls = mvResponse.data.data.url;

  // 获取MV的详情
  const detailResponse = await mvDetail(id);
  state.mvDetails = detailResponse.data.data;

  // 获取MV评论
  const commentResponse = await commentMV({ id, limit: 30, offset: 0 });
  state.commList = commentResponse.data;
};

// 在组件挂载时获取初始的MV数据
onMounted(() => {
  fetchMVData(route.params.id);
});

// 监听route.params.id的变化
watch(() => route.params.id, (newId) => {
  fetchMVData(newId); // 重新获取数据
});
</script>

<template>
  <div class="content-section menuBar-mv">
    <video
      controls
      loop
      autoplay
      :src="mvUrls"
      style="width: 100%; border-radius: 10px"
    ></video>
    <div class="content-section-title">
      <h2>
        {{ mvDetails.artists.map((item) => item.name).join() }}
        <span style="margin: 0px 10px">-</span>
        <span> {{ mvDetails.name }}</span>
      </h2>
      {{ route.query.id }}
      <!-- 评论 -->
      <div class="content-section-title comstitle">
        <h2>
          评论<span>共{{ commList.total }}条 </span>
        </h2>
      </div>
      <div class="content-section" style="margin-top: 0">
        <div class="apps-card">
          <div
            class="app-card usercom"
            v-for="(item, idx) in commList.comments"
            :key="idx"
          >
            <span>
              <img
                class="useravatar"
                :src="item.user.avatarUrl + '?param=32y32'"
                alt=""
              />
              {{ item.user.nickname }}
            </span>
            <div class="app-card__subtext">{{ item.content }}</div>
            <div class="com-time">
              <span>{{ item.ipLocation.location }}</span>
              <span class="usercountlike">{{ item.timeStr }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style lang="scss" scoped></style>
