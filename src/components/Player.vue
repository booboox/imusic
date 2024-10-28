<script setup>
import "@/styles/Player.scss";
import { lyric, commentMusic, publishComment, Heart_Mode, songUrl} from "@/api/api";
import { ElMessage } from "element-plus";
import playerIcon from "@/static/img/aplayer/播放.svg";
import pauseIcon from "@/static/img/aplayer/暂停.svg";
import sequenceIcon from "@/static/img/aplayer/sequence.svg";
import cycleIcon from "@/static/img/aplayer/cycle.svg";
import randomIcon from "@/static/img/aplayer/random.svg";
const router = useRouter();
const musicstore = musicStore();
const audio = ref();
const state = reactive({
  list: [],
  volumes: 70,
  playing: false,
  currentIndex: 0,
  mode: 0, // 0: 顺序播放，1: 循环播放，2: 随机播放
  progress: 0,
  duration: 0,
  currentTime: 0,
  playerMode: [
    { model: "顺序播放", icon: sequenceIcon },
    { model: "单曲循环", icon: cycleIcon },
    { model: "随机播放", icon: randomIcon },
  ],
  isPlayerModel: [
    { model: "播放", icon: playerIcon },
    { model: "暂停", icon: pauseIcon },
  ],
  currenPlayerState: "",
  // 抽屉
  drawer: false,
  animationPlayState: "paused",
  parsedLyrics: [],
  currentLine: -1,
  // 歌曲评论
  commentsList: [],
  currentCommentsPage: 1,
  CommentsTotal: 0,
  loading: true,
  isRed: false,
});
const {
  list,
  volumes,
  currentTime,
  duration,
  progress,
  currenPlayerState,
  drawer,
  animationPlayState,
  parsedLyrics,
  currentLine,
  commentsList,
  currentCommentsPage,
  CommentsTotal,
  loading,
} = toRefs(state);
// 当前播放歌曲
const currentSong = computed(() => musicstore.songs[musicstore.currentIndex]);
// 当前播放模式
const modeText = computed(() => {
  return state.playerMode[state.mode];
});

onMounted(() => {
  pause();
  audio.value.src = currentSong.value.src;
  state.currenPlayerState = state.isPlayerModel[0].icon;
  getCommentList();
  state.parsedLyrics = parseLyrics(currentSong.value.Lyric);
});
// 侦听是否立即播放
watch(
  () => musicstore.currentIndex,
  () => {
    play();
    getCommentList();
  }
);
// 播放
const play = async () => {
  state.animationPlayState = "running";
  state.playing = true;
  state.currenPlayerState = state.isPlayerModel[1].icon;
  nextTick(() => {
    audio.value.play();
  });
};
// 暂停
const pause = () => {
  state.animationPlayState = "paused";
  state.playing = false;
  state.currenPlayerState = state.isPlayerModel[0].icon;
  nextTick(() => {
    audio.value.pause();
  });
};
// 暂停&播放
const togglePlay = () => {
  if (state.playing) {
    pause();
  } else {
    play();
  }
};
// 上一首
const prevSong = () => {
  if (musicstore.currentIndex === 0) {
    musicstore.currentIndex = musicstore.songs.length - 1;
  } else {
    musicstore.currentIndex--;
  }
  playSong();
};

//下一首
const nextSong = () => {
  if (musicstore.currentIndex === musicstore.songs.length - 1) {
    musicstore.currentIndex = 0;
  } else {
    musicstore.currentIndex++;
  }
  playSong();
};
// 播放歌曲
const playSong = () => {
  nextTick(() => {
    audio.value.src = currentSong.value.src;
    audio.value.play();
    state.playing = true;
    state.currenPlayerState = state.isPlayerModel[1].icon;
  });
};
// 歌曲结束后执行的方法
const handleEnded = () => {
  switch (state.mode) {
    case 0: // 顺序播放
      nextSong();
      break;
    case 1: // 单曲循环
      playSong();
      break;
    case 2: // 随机播放
      const randomIndex = Math.floor(Math.random() * musicstore.songs.length);
      musicstore.currentIndex = randomIndex;
      playSong();
      break;
  }
};
// 更新
const handleTimeUpdate = async () => {
  state.currentTime = Math.floor(audio.value.currentTime);
  state.progress = (state.currentTime / state.duration) * 100;

  // 加载歌词
  if (!currentSong.value.Lyric) {
    lyric(currentSong.value.id).then(({ data }) => {
      let isLyric = "";
      try {
        isLyric = data.lrc.lyric;
      } catch (error) {
        isLyric = "";
        console.error(error);
      }
      musicstore.updateSongs({ Lyric: isLyric });
    });
  }
  state.parsedLyrics = parseLyrics(currentSong.value.Lyric);
  // 歌词时间
  if (!state.parsedLyrics) return;
  for (let i = 0; i < state.parsedLyrics.length; i++) {
    if (
      i === state.parsedLyrics.length - 1 ||
      state.currentTime < state.parsedLyrics[i + 1].time
    ) {
      state.currentLine = i;
      break;
    }
  }
  // 侦听歌词滚动的位置
  const activeLine = document.querySelector(".lyrics-container li.active");
  if (activeLine) {
    const container = document.querySelector(".lyrics-container ul");
    container.scrollTop =
      activeLine.offsetTop -
      container.clientHeight / 2 +
      activeLine.clientHeight;
  }
};

// 当前时间更改
const handleDurationChange = () => {
  state.duration = Math.floor(audio.value.duration);
};
// 更改播放模式
const toggleMode = () => {
  if (state.mode === 2) {
    state.mode = 0;
  } else {
    state.mode++;
  }
  ElMessage.success({
    message: state.playerMode[state.mode].model,
    type: "success",
  });
};
// 格式化时间
const formatTime = (time) => {
  const minutes = Math.floor(time / 60);
  const seconds = time % 60;
  return `${minutes}:${seconds.toString().padStart(2, "0")}`;
};
// 改变进度
const seek = (event) => {
  let ct = (state.progress * state.duration) / 100;
  if (!isNaN(ct)) {
    audio.value.currentTime = ct;
  }
};
//心动模式
const heartmode = async () => {
  console.log("点击心动模式")
  state.isRed = !state.isRed; // 切换 isRed 的值
  console.log(state.isRed);
  const currentSongId = musicstore.songs[musicstore.currentIndex]['id'];
  console.log("当前音乐播放的id", currentSongId);
  // 调用心动模式接口
  const response = await Heart_Mode(currentSongId);
  console.log(response.data.data);
  response.data.data.forEach(song => {
    console.log("song", song);
  });
  // 提取所需字段并构建新的歌曲数组
  const newSongs = response.data.data.map(song => ({
    id: song.id,
    title: song.songInfo?.name || 'Unknown Title',
    singer: song.songInfo?.ar[0]?.name || 'Unknown Singer',
    album: song.songInfo?.al?.name || 'Unknown Album',
    cover: song.songInfo?.al?.picUrl || 'No Cover',
    src: null,
    time: null,
  }));
  console.log("获得的心动模式歌曲数", newSongs.length);
  console.log("获得的心动模式歌曲", newSongs);
  // 然后逐个获取 src 并更新歌曲数据
  /*for (let i = 0; i < 12; i++) {
    const songId = newSongs[i].id;
    console.log("每个歌曲的id", songId);
    const srcResponse = await songUrl({ id: songId });
    console.log("每个歌曲的数据", srcResponse.data);
    newSongs[i].src = srcResponse.data.data[0].url;  // 更新 src
    console.log("每个歌曲的src", newSongs[i].src);
    newSongs[i].time = srcResponse.data.data[0].time
    console.log("每个歌曲的time", newSongs[i].time);
  }*/
 // 使用 Promise.all 并行获取每首歌曲的 src 和 time
  const srcPromises = newSongs.map(async (song) => {
    const srcResponse = await songUrl({ id: song.id });
    song.src = srcResponse.data.data[0].url;
    song.time = srcResponse.data.data[0].time;
  });
  // 清空原来的数据并存入新的数据
  //musicstore.songs = []; // 清空原来的歌曲数据
  //musicstore.songs.push(...newSongs); // 将新的歌曲数据存入 songs 中
  musicstore.songs = newSongs; 
  console.log(musicstore.songs); // 打印更新后的歌曲数据
  musicstore.currentIndex = 0;
  playSong();
  // 等待所有歌曲的 src 获取完成
  await Promise.all(srcPromises);
};

// 控制音量
const changeVolumes = (val) => {
  audio.value.volume = val / 100;
};

const parseLyrics = (lyric) => {
  if (!lyric) return;
  const lines = lyric.split("\n");
  return lines
    .map((line) => {
      const matches = line.match(/^\[(\d{2}):(\d{2}\.\d{2,3})\](.*)/);
      if (matches) {
        const minutes = parseInt(matches[1]);
        const seconds = parseFloat(matches[2]);
        const text = matches[3].trim();
        return { time: minutes * 60 + seconds, text };
      } else {
        return null;
      }
    })
    .filter((line) => line !== null);
};
const newComment = ref(''); // 用于存储新的评论内容

const handlePublishComment = async () => {
  // 验证评论内容是否为空
  if (!newComment.value.trim()) {
    alert('评论内容不能为空');
    return;
  }

  const t = 1; // 替换为实际的 t
  const type = 0; // 根据你的需求设置类型
  const id = currentSong.value.id; // 假设数据对象有一个 id 属性

  try {
    // 调用 publishComment 方法发布评论
    await publishComment(t, type, id, newComment.value);
    
    // 清空评论框
    newComment.value = '';

    // 发布成功后重新加载评论列表
    await getCommentList(); // 重新获取评论列表

  } catch (error) {
    console.error('发布评论失败:', error);
    alert('发布评论失败，请重试');
  }
};

// 评论分页
// 改变页数触发
const handleCurrentChange = () => {
  getCommentList();
};
// 获取歌曲评论
const getCommentList = () => {
  state.loading = true;
  // 1.清空评论数据
  state.commentsList = [];
  // 2.加载歌曲的评论
  commentMusic({
    id: currentSong.value.id,
    offset: (state.currentCommentsPage - 1) * 20,
    limit: 20,
  }).then((res) => {
    state.commentsList = res.data;
    state.CommentsTotal = res.data.total;
    state.loading = false;
  });
};
</script>
<template>
  <audio
    ref="audio"
    :src="currentSong.src"
    @timeupdate="handleTimeUpdate"
    @durationchange="handleDurationChange"
    @ended="handleEnded"
  ></audio>
  <div class="playerbox">
    <div class="m-tandc">
      <img
        :src="currentSong.cover + '?param=91y90'"
        alt=""
        @click="drawer = !drawer"
      />
      <div class="titleandsinger">
        <a href="javascript:;">{{ currentSong.title }}</a>
        <a href="javascript:;" style="font-size: 13px">{{
          currentSong.singer
        }}</a>
      </div>
    </div>
    <div class="btns">
      <div class="aplayer">
        <!-- 歌曲列表 -->
        <div class="m-btns">
          <a href="javascript:;" @click="toggleMode">
            <img :src="modeText.icon" alt="" class="Musicice" />
          </a>
          <a href="javascript:;" @click="prevSong">
            <img
              src="../static/img/aplayer/上一首.svg"
              alt=""
              class="Musicice"
            />
          </a>
          
          <a href="javascript:;" @click="togglePlay">
            <img :src="currenPlayerState" alt="" />
          </a>
          <a href="javascript:;" @click="nextSong">
            <img
              src="../static/img/aplayer/下一首.svg"
              alt=""
              class="Musicice"
            />
          </a>
          <a href="javascript:;" @click="heartmode">
            <img
              src="../static/img/aplayer/心动模式.svg"
              alt=""
              class="Musicice"
              :class="{ red: isRed }"
               style="width: 26px; height:26px;"
            />
          </a>
        </div>
        <div class="slider-demo-block rightvoice">
          <a href="javascript:;"
            ><img src="../static/img/aplayer/声音.svg" alt="" width="20"
          /></a>
          <!-- 音量条 -->
          <el-slider
            v-model="volumes"
            @change="changeVolumes"
            style="width: 70px"
            :show-tooltip="true"
          />
          <a
            href="javascript:;"
            @click="router.push({ path: '/currentPlaylist' })"
          >
            <span class="notification-number">{{
              musicstore.songs.length
            }}</span>
            <img src="../static/img/aplayer/列表.svg" alt="" class="Musicice" />
          </a>
        </div>
      </div>
      <!-- 进度条 -->
      <div class="m-slider">
        <span class="prismColor">{{ formatTime(currentTime) }}</span>
        <div class="slider-demo-block">
          <el-slider
            v-model="progress"
            :show-tooltip="false"
            @change="seek"
            style="width: 400px"
          />
        </div>
        <span class="prismColor" style="margin-left: 18px">{{
          formatTime(duration)
        }}</span>
      </div>
    </div>
    <el-drawer
      title=""
      :modal="false"
      v-model="drawer"
      direction="ttb"
      destroy-on-close
      size="89.5%"
    >
      <div>
        <div class="showMusicBox">
          <!-- 封面 -->
          <div style="margin-right: 150px">
            <div style="position: relative">
              <img
                class="vinyl rotate"
                :style="{ animationPlayState: animationPlayState }"
                src="../static/img/vinyl.png"
                alt=""
              />
              <el-image
                class="elImg"
                :src="currentSong.cover + '?param=350y350'"
              ></el-image>
            </div>
            <!-- <el-image class="rotate" :style="{ animationPlayState: animationPlayState }"
							:src="currentSong.cover + '?param=350y350'"></el-image> -->
          </div>
          <!-- 歌词 -->
          <div class="lyrics-container">
            <ul>
              <li
                v-for="(line, index) in parsedLyrics"
                :key="index"
                :class="{ active: currentLine === index }"
              >
                {{ line.text }}
              </li>
            </ul>
          </div>
        </div>
    <div class="comments">
          <div class="content-section" style="margin-top: 0">
            <!-- 输入评论框和提交按钮 -->
            <div class="comment-input">
              <el-input
                v-model="newComment"
                placeholder="说点什么吧..."
                class="input-box"
                type="textarea"
                :rows="3"
                @keyup.enter="submitComment"
              ></el-input>
              <el-button type="primary" @click="handlePublishComment">评论</el-button>
            </div>

          <div class="apps-card">
            <el-skeleton :rows="5" animated :loading="loading">
              <template #default>
                <div
                  class="app-card usercom"
                  v-for="(item, index) in commentsList.comments"
                  :key="index"
                >
                  <span>
                    <el-image
                      class="useravatar"
                      :src="item.user.avatarUrl + '?param=32y32'"
                      alt=""
                    ></el-image>
                    {{ item.user.nickname }}
                  </span>
                  <div class="app-card__subtext">{{ item.content }}</div>
                  <div
                    class="app-card__footer"
                    v-if="item.ipLocation.location"
                  >
                    <span>IP:{{ item.ipLocation.location }}</span>
                    <span>{{ item.timeStr }}</span>
                  </div>
                </div>
              </template>
            </el-skeleton>
            </div>
          </div>
        </div>
      </div>
      <div class="pagination">
        <el-pagination
          @current-change="handleCurrentChange"
          v-model:currentPage="currentCommentsPage"
          :page-size="30"
          layout="prev, pager, next, jumper"
          :total="CommentsTotal"
        >
        </el-pagination>
      </div>
    </el-drawer>
  </div>
</template>
<style lang="scss" scoped>
.rotate {
  animation-name: rotate;
  animation-duration: 50s;
  animation-iteration-count: infinite;
  animation-timing-function: linear;
}
.comment-input {
  display: flex;
  align-items: flex-start; /* Align items at the start */
  margin-bottom: 20px; /* 调整输入框和提交按钮之间的间距 */
}

.input-box {
  flex: 1; /* 让输入框占据可用空间 */
  margin-right: 10px; /* 输入框和按钮之间的间距 */
  border-radius: 20px; /* 圆角 */
  background-color: #f0f0f0; /* 背景颜色 */
  padding: 10px; /* 内边距 */
  font-size: 14px; /* 字体大小 */
  resize: none; /* 禁止改变大小 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

.input-box::placeholder {
  color: #999; /* 占位符颜色 */
}
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.lyrics-container {
  height: 500px;
  position: relative;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 50%;
    background: linear-gradient(
      to top,
      rgba(255, 255, 255, 0) 0%,
      rgba(255, 255, 255, 1) 100%
    );
    z-index: 2;
    pointer-events: none;
    /* make sure the overlay doesn't interfere with mouse events */
    opacity: 1;
    /* set initial opacity to 1 */
    transition: opacity 0.5s ease-in-out;
    /* add a transition for the opacity property */
  }

  &::after {
    content: "";
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 50%;
    background: linear-gradient(
      to bottom,
      rgba(255, 255, 255, 0) 0%,
      rgba(255, 255, 255, 1) 100%
    );
    z-index: 2;
    pointer-events: none;
    opacity: 1;
    transition: opacity 0.5s ease-in-out;
  }

  ul {
    overflow: auto;
    height: 100%;
    list-style: none;
    margin: 0;
    padding: 0;
    z-index: 1;

    .active {
      color: var(--el-color-primary);
      font-size: 20px;
      font-weight: bold;
    }

    /* 自定义滚动条样式 */
    // 隐藏滚动条
    &::-webkit-scrollbar {
      width: 0;
    }

    li {
      text-align: center;
      padding: 15px 0px;
      // opacity: 0.75;
      /* 过渡动画，可根据需要调整 */
      transition: all 0.3s ease-out;
    }
  }
}

.lyrics-container.fade-out::before,
.lyrics-container.fade-out::after {
  opacity: 0;
}
/* 当 isRed 为 true 时，应用红色样式 */
.red {
  filter: invert(22%) sepia(98%) saturate(7504%) hue-rotate(1deg) brightness(99%) contrast(112%);
}
</style>
