# 金山云魔方贴纸、美颜美型API文档
## 项目背景
金山魔方是一个多媒体能力提供平台，通过统一接入API、统一鉴权、统一计费等多种手段，降低客户接入多媒体处理能力的代价，提供多媒体能力供应商的效率。 本文档主要针对统一FILTER功能而说明。
## 效果展示
![Alt text](https://raw.githubusercontent.com/wiki/ksvcmc/KMCFuFilter_iOS/img.jpg)
## 名词解释
**贴纸资源**:下图右侧，选中icon后下载下来并且出现在人脸上的图案为贴纸资源。  
**缩略图**:下图左侧下方十个格子中，每个格子中的的icon即为滤镜缩略图。  
**贴纸特效动作**:贴纸里可能含有点头，摇头，张嘴等触发特效的动作，KMCArMaterial里详细定义了具体的触发动作和tips.  
## 鉴权
SDK在使用时需要用token进行鉴权后方可使用，token申请方式见**接入步骤**部分;  
token与应用包名为一一对应的关系;  
鉴权错误码见：https://github.com/ksvcmc/KMCAgoraVRTC_Android/wiki/auth_error

## 工程目录:     
**demo**:Faceunity贴纸示例工程  
**libs**: 魔方贴纸包kmcfilter.aar

**注: demo工程使用软链接引用libs目录，对于windows平台做Android开发的用户，需要手动将libs目录拷贝到demo目录下。**

此外，gradle需要依赖libksylive库:   
compile 'com.ksyun.media:libksylive-java:2.3.2'   
compile 'com.ksyun.media:libksylive-arm64:2.3.2'  
compile 'com.ksyun.media:libksylive-armv7a:2.3.2'  
compile 'com.ksyun.media:libksylive-x86:2.3.2'  

## SDK使用指南  
SDK使用指南请见[wiki](https://github.com/ksvcmc/KMCFuFilter_Android/wiki)

## 接入流程
详情请见[接入说明](https://github.com/ksvcmc/KMCFuFilter_Android/wiki/token_apply)

## Demo下载
![Alt text](https://raw.githubusercontent.com/wiki/ksvcmc/KMCFuFilter_Android/fu_android.png)
## 反馈与建议  
主页：[金山魔方](https://docs.ksyun.com/read/latest/142/_book/index.html)  
邮箱：ksc-vbu-kmc-dev@kingsoft.com  
QQ讨论群：574179720 [视频云技术交流群] 
