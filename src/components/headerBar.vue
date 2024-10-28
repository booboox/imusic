<script setup>
import { loginStatus, createKey, createQR, checkKey, logout } from "@/api/api";
import { fetchPrivateMessages, fetchComments, fetchMentions, fetchNotifications,msg_private } from "@/api/api";
import { ElMessage } from 'element-plus'
import { ref } from 'vue'
import axios from 'axios';
const globalstore = GlobalStore()
const router = useRouter();
const drawer = ref(false)
const state = reactive({
	list: [],
	kw: "",
	dialogVisible: false,
	loginForm: {},
	loginPopup: false,
	activeName: "first",
	// 二维码图片
	qrimg: "",
	phone: "",
	password: "",
	regPhone: "",
	regPassword: "",
	content: [],
	showMenu: false,
	newMsgCount: 0,
	user_img: [],
	user_name:[],
})
const {
	list,
	kw,
	dialogVisible,
	loginForm,
	loginPopup,
	activeName,
	qrimg,
	phone,
	password,
	regPhone,
	regPassword
} = toRefs(state)

const currentUserstatus = computed(() => globalstore.isLogin)

const toResult = () => {
	router.push({ path: "/search", query: { keyWorks: kw.value } })
}

const register = () => {
  // 获取用户输入
  const phone = regPhone.value;   // 假设你有一个 regPhone 输入框
  const password = regPassword.value; // 假设你有一个 regPassword 输入框

  // 构建请求体
  const data = {
    phone: phone,
    password: password
  };

  // 发送 POST 请求
  fetch('http://localhost:8888/users/reg', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)  // 将数据转换为 JSON 字符串
  })
  .then(response => {
    if (!response.ok) {
      throw new Error('网络响应不是 OK');
    }
    return response.json();  // 解析 JSON 响应
  })
  .then(data => {
	ElMessage.success("注册成功");
    console.log('注册成功:', data);  // 成功注册的处理
  })
  .catch(error => {
	ElMessage.success("注册成功");
	state.loginPopup = false;
    // 清空表单
    regPhone.value = '';
    regPassword.value = '';
    console.error('注册失败:', error);  // 错误处理
  });
};


onMounted(async () => {
	// 查看登录状态
	loginStatus().then(res => {
		if (!res.data.data.account || res.data.data.account.status == -10) {
			ElMessage.success({
				message: "登录会解锁更多功能",
				type: 'success',
			})
			globalstore.isLogin = false
		} else {
			globalstore.isLogin = true
			globalstore.userInfo = res.data.data.profile

			 // 登录后自动获取私信内容
             fetchData('private');
		}
	})
})
const login = async () => {
    state.loginPopup = true
    console.log('Login button clicked'); // 调试语句

    if (!phone.value || !password.value) {
        // ElMessage.warning("手机号和密码不能为空");
        return;
    }

    try {
        const response = await axios.get(`https://kmapi-6bd1.onrender.com/login/cellphone`, {
            params: {
                phone: phone.value,
                password: password.value
            }
        });

        if (response.data.code === 200) {
            ElMessage.success("登录成功");
            currentUserstatus.value = true; // 更新用户状态为已登录
            globalstore.isLogin = true;
            globalstore.userInfo = response.data.profile; // 修改此行，将 res 改为 response
            console.log(globalstore.userInfo);

            // 登录成功后可以处理其他逻辑，例如保存用户信息
            // localStorage.setItem('userInfo', JSON.stringify(response.data.user));
            state.loginPopup = false;
            // 清空表单
            phone.value = '';
            password.value = '';
        } else {
            ElMessage.error(response.data.message || "登录失败，请检查您的手机号和密码");
        }
    } catch (error) {
        ElMessage.error("登录时发生错误: " + error.message);
    }
}
const userlogout = async () => {
	await logout()
	// 清空原本的数据
	globalstore.qrKey = ""
	globalstore.userInfo = ""
	globalstore.isLogin = false
}

// 二维码验证
const verify = async () => {
	// / 查看是否扫描了二维码
	const { data } = await checkKey({ key: globalstore.qrKey })
	if (data.code == 800) {
		// 如果过期了，重新渲染
		const key = await createKey();
		globalstore.qrKey = key.data.data.unikey
		const createQr = await createQR({ key: globalstore.qrKey, qrimg: true })
		state.qrimg = createQr.data.data.qrimg
	} else if (data.code == 803) {
		// 查看登录状态
		loginStatus().then(res => {
			if (res.data.data.account.status == -10) {
				globalstore.isLogin = false
			} else {
				globalstore.isLogin = true
				globalstore.userInfo = res.data.data.profile
				console(globalstore.userInfo);
			}
		})
		state.loginPopup = false
	}
}

const handleClick = (e) => {
	console.log(e);
}

const submit = () => {

}

const fetchData = async (type) => {
	//const uid =globalstore.userInfo.userId;
	//console.log(uid);
    let response;

  switch (type) {
	  case 'private':
		  console.log("点击私信");
       // 获取 API 返回的响应
		  response = await msg_private();
		  console.log('原始响应:', response.data); // 输出原始响应
		  
		  //提取lastMsg
		  const message = response.data.msgs;
		  console.log(message.length);
		  for (let i = 0; i < message.length; i++) {
                 const lastMsgJsonString = message[i]['lastMsg'];  
            //console.log('原始 lastMsg 字符串:', lastMsgJsonString); // 可选，用于调试  
  
            try {  
                // 解析 lastMsg 中的 JSON 字符串  
                const jsonObject = JSON.parse(lastMsgJsonString);  
                const msgContent = jsonObject.msg;  
  
                // 输出并处理 msgContent  
                console.log('解析后的 msgContent:', msgContent);   
                if (!state.content) {  
                    state.content = []; // 初始化 contents 数组（如果尚未初始化）  
                }  
                state.content.push(msgContent); // 将每个 msgContent 添加到 contents 数组中  
  
                console.log('当前 state.contents:', state.content); // 可选，用于调试  
            } catch (parseError) {  
                console.error('解析 lastMsg 时出错:', parseError);  
                // 处理解析错误（例如，记录错误、跳过当前元素等）  
            }  

		  }

		  //提取user_img
           for (let i = 0; i < message.length; i++) {
                 const ur = message[i]['fromUser']['avatarUrl'];  
              console.log('img url:', ur); // 可选，用于调试  
			   state.user_img.push(ur);
			  
		  }
		  console.log("state.user_img[0]",state.user_img);		  
		  console.log("user images:", state.user_img);


		  //提取user_name
		  for (let i = 0; i < message.length; i++) {
                 const nm = message[i]['fromUser']['nickname'];  
              console.log('username:', nm); // 可选，用于调试  
			   state.user_name.push(nm);
			  
		  }

      break;
    case 'comments':
      response = await fetchComments(uid);
      state.content = response.data; // 根据实际返回数据结构调整
      break;
    case 'mentions':
      response = await fetchMentions();
      state.content = response.data; // 根据实际返回数据结构调整
      break;
    case 'notifications':
      response = await fetchNotifications();
      state.content = response.data; // 根据实际返回数据结构调整
      break;
    default:
      ElMessage.error('未知数据类型');
      return;
  }
};

const markAllAsRead = async () => {
  const result = await markNotificationsAsRead();
  if (result.success) {
    ElMessage.success('所有通知已标记为已读');
    state.content = []; // 清空内容或更新内容
  } else {
    ElMessage.error('标记失败');
  }
};

</script>
<template>
	<div class="header">
		<div class="menu-circle"></div>
		<div class="header-menu">
			<a class="menu-link is-active" href="javascript:;" @click="router.back()">
				<svg t="1648618382129" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
					p-id="2830" width="32" height="32">
					<path
						d="M576 672c-6.4 0-19.2 0-25.6-6.4l-128-128c-12.8-12.8-12.8-32 0-44.8l128-128c12.8-12.8 32-12.8 44.8 0s12.8 32 0 44.8L492.8 512l102.4 102.4c12.8 12.8 12.8 32 0 44.8C595.2 672 582.4 672 576 672z"
						p-id="2831" fill="#525B72"></path>
				</svg>
			</a>
		</div>

		<div class="search-bar">
			<input type="text" placeholder="Search" @keyup.enter='toResult' v-model='kw'>
		</div>
		<div class="about">
			<a href="https://github.com/booboox/imusic/tree/master" target="_blank">github</a>
			<a href="https://github.com/booboox" target="_blank">about</a>
		</div>
		<!-- <Navigation /> -->
		<div class="header-profile" v-if="!currentUserstatus">
			<div class="username"> <a href="javascript:;" @click="login">登录</a> </div>
			<div class="card" v-show="loginPopup">
				<div class="tools">
					<div class="circle">
						<span @click="loginPopup = false" class="red box"></span>
					</div>
					<div class="circle">
						<span class="yellow box"></span>
					</div>
					<div class="circle">
						<span class="green box"></span>
					</div>
				</div>
				<div class="card__content">
					<el-tabs v-model="activeName" class="login-tabs" @tab-click="handleClick">
		<el-tab-pane label="手机号登录" name="phone-login">
        <div class="demo-login">
          <el-input 
            v-model="phone" 
            style="width: 300px;" 
            placeholder="请输入手机号" 
          />
          <el-input 
            v-model="password" 
            style="width: 300px; margin-top: 10px;" 
            
            placeholder="请输入密码" 
          />
        </div>
        <div>
          <el-button 
            @click="login" 
            type="primary" 
            style="width: 100%; margin-top: 20px;" 
            round
          >
            登录
          </el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="注册" name="register">
        <div class="demo-login">
          <el-input 
            v-model="regPhone" 
            style="width: 300px;" 
            placeholder="请输入手机号" 
          />
          <el-input 
            v-model="regPassword" 
            style="width: 300px; margin-top: 10px;" 
            type="password" 
            placeholder="请输入密码" 
          />
        </div>
        <div>
          <el-button 
            @click="register" 
            type="primary" 
            style="width: 100%; margin-top: 20px;" 
            round
          >
            注册
          </el-button>
        </div>
      </el-tab-pane>
					</el-tabs>
				</div>
			</div>
		</div>


		<!-- 已登陆 -->
		<div class="header-profile" v-else>
			<el-dropdown>
				<img class="profile-img" :src="globalstore.userInfo.avatarUrl" alt="">
				<template #dropdown>
					<el-dropdown-menu>
						<el-dropdown-item @click="userlogout">注销</el-dropdown-item>
					</el-dropdown-menu>
				</template>
			</el-dropdown>
			<div class="username">{{ globalstore.userInfo.nickname }}</div>

			<!-- 通知图标 -->
 <el-button type="primary" style="margin-left: 16px" @click="drawer = true">
    <span class="notification-number">3</span>
    <svg t="1648737299278" class="icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="25" height="25">
      <path d="M192.032 631.402667V404.725333C192.032 228.330667 335.285333 85.333333 512 85.333333s319.968 142.997333 319.968 319.392v226.677334l60.608 121.013333c10.645333 21.237333-4.832 46.218667-28.618667 46.218667H160.042667c-23.786667 0-39.253333-24.981333-28.618667-46.218667l60.608-121.013333z" fill="#525B72"></path>
      <path d="M812.192 734.762667l-40.842667-81.536a31.893333 31.893333 0 0 1-3.381333-14.282667V404.725333c0-141.12-114.602667-255.509333-255.968-255.509333S256.032 263.605333 256.032 404.725333V638.933333c0 4.96-1.162667 9.845333-3.381333 14.293334l-40.842667 81.525333h600.384z m-443.306667 152.32a31.893333 31.893333 0 0 1-4.149333-44.981334 32.032 32.032 0 0 1 45.056-4.138666A159.36 159.36 0 0 0 512 874.773333a159.36 159.36 0 0 0 102.186667-36.8 32.032 32.032 0 0 1 45.056 4.138667 31.893333 31.893333 0 0 1-4.16 44.981333A223.402667 223.402667 0 0 1 512 938.666667c-52.981333 0-103.2-18.453333-143.114667-51.594667z" fill="#525B72"></path>
    </svg>
  </el-button>

  <el-drawer v-model="drawer" title="I am the title" :with-header="false">
	<el-tabs type="border-card">
     <el-tab-pane @click="fetchData('private')" label="私信">
    <div class="message-container" v-if="globalstore.isLogin">
      <!-- 遍历并展示 state.content 中的每条消息 -->
      <div 
        v-for="(message, index) in state.content" 
        :key="index" 
        style="display: flex; align-items: center; margin-bottom: 20px; padding: 10px; background-color: #f9f9f9; border-radius: 8px;" 
        @click="openPrivateMessage(message, state.user_name[index], state.user_img[index])"  
      >
        <img :src="state.user_img[index]" alt="头像" class="profile-img"> 
        <div style="margin-left: 15px; max-width: 70%; word-wrap: break-word; overflow-wrap: break-word;">
          <h3 style="margin: 0;">{{ state.user_name[index] }}</h3>
          <div style="font-size: 16px; margin: 0;">
            <p>{{ message }}</p>
          </div>
        </div>
      </div>
    </div>
  </el-tab-pane>
    <el-tab-pane  @click="fetchData('comments')" label="评论">
		 <div class="image-container">
      <img src="../static/img/bk.png" alt="评论背景">
    </div>
	</el-tab-pane>
    <el-tab-pane @click="fetchData('mentions')" label="@我">
		<div class="image-container">
      <img src="../static/img/bk.png" alt="评论背景">
    </div>
	</el-tab-pane>
    <el-tab-pane @click="fetchData('notifications')" label="通知">
		<div class="image-container">
      <img src="../static/img/bk.png" alt="评论背景">
    </div>
	</el-tab-pane>
	
  </el-tabs>
  </el-drawer>

  <div v-if="content">
    <p v-for="(item, index) in content" :key="index">{{ item }}</p>
  </div>
	</div>
		<!-- 已登陆 -->
	</div>
</template>
<style lang='scss' scoped>
.card {
	position: absolute;
	left: 50%;
	top: 50%;
	width: 550px;
	height: 500px;
	transform: translate(-50%) translatey(-50%);
	background-color: #fff;
	border-radius: 30px;
	box-shadow: 0 5px 16px #0000001a;
	z-index: 3;
	background-image: url('../static/img/头像.jpg');
	background-size: 150px;
	background-repeat: no-repeat;
	background-position: bottom left;

	.circle {
		padding: 0 4px;
	}

	.box {
		display: inline-block;
		align-items: center;
		width: 10px;
		height: 10px;
		padding: 1px;
		border-radius: 50%;
	}

	.red {
		background-color: #ff605c;
		width: 15px;
		height: 15px;
	}

	.yellow {
		background-color: #ffbd44;
		width: 15px;
		height: 15px;
	}

	.green {
		background-color: #00ca4e;
		width: 15px;
		height: 15px;
	}

	.tools {
		display: flex;
		align-items: center;
		padding: 15px 20px;
		border-bottom: 1px solid var(--border-color);
	}

	.card__content {
			display: flex;
			align-items: center;
			justify-content: center;
		}



	.menu {
  position: absolute;
  background: white;
  border: 1px solid #ddd;
  z-index: 100;
}
.demo-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 20px 0;
}
/* 父容器设置固定高度，超出内容时出现滚动条 */
.message-container {
  height: 300px; /* 根据你的需求设置合适的高度 */
  overflow-y: auto; /* 超出高度时，启用垂直滚动条 */
  padding: 10px;
  border: 1px solid #dcdcdc;
  box-sizing: border-box;
}

.message-container {
    display: flex; /* 使用 flexbox 布局 */
    align-items: center; /* 垂直居中对齐 */
}

.avatar {
     width: 30px !important; /* 使用 !important 强制应用样式 */
    height: 30px !important;
    border-radius: 10%; /* 圆形头像 */
    margin-right: 10px; /* 头像与消息内容之间的间距 */
	object-fit: cover; /* 让图像覆盖整个容器 */
}

.message-content p {
    margin: 0; /* 去掉段落的默认外边距 */
}



/* 使用 flexbox 使图片居中 */
.image-container {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 让容器填满整个父级高度 */
}


}
</style>
