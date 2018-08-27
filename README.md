# SakuApng
Apng的解析器和播放器

# 1.  概述
在深入了解Apng动画播放之前，我们需要对Apng的结构有所了解，具体参见[**Apng动画介绍**](http://www.jianshu.com/p/5333bcc20ba7)，对Apng的整体结构有所了解后，下面我们来讲讲Apng动画的播放，主要包括Apng解析和Apng渲染两个过程。
# 2.  Apng动画播放流程
Apng动画播放流程包括Apng解析和Apng渲染两个过程，Apng解析主要有两种方法，下面我们将会介绍，而Apng渲染主要包括三个步骤：**消除（dispose）、合成（blend）、绘制（draw）**，由此得到Apng动画播放流程图如下：
![Apng动画播放流程](http://upload-images.jianshu.io/upload_images/3427834-aa0b26e40be1d556.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
# 3.  Apng的解析
Apng的解析主要是将Apng文件转化成Apng序列帧Frame-n，从上面的流程图可知，Apng文件的解析列出了两种方案，下面来分别说说：

1）Apng文件首先经过一个解压（**ApngExact**）的过程，生成png序列帧保存在本地，然后经过加载（**LoadPng**）处理生成序列帧Frame-n。

假设Apng动画文件总共有90帧，那么经过ApngExact处理后，会生成90张png序列帧保存在本地，每帧通过LoadPng处理生成Bitmap并供后面的Apng渲染使用。

2）Apng是一个独立的文件，我们自己编写读取Apng文件的代码类：ApngReader，当渲染第i帧时，通过ApngReader直接获取第i帧的Bitmap。

**比较：**
1）方案一是将Apng文件全部解压成png序列图片保存在本地，方案二是把Apng文件当做一个整体去处理，需要第几帧直接读取第几帧，并将该帧以Bitmap的形似保存到内存。

2）方案一解压得到的png图片在后面的渲染中需要转化成Bitamp，而方案二直接就获取了第几帧的Bitmap，相比于方案一，方案二减少了一个从SD卡读取png文件的操作。

#ApngReader的实现
方案一的具体实现大家可以参考github上面的一个项目[**apng-view**](https://github.com/sahasbhop/apng-view)，下面我们来讲讲方案二的具体实现，即ApngReader的具体实现。


# 4.  Apng的渲染
# 解析Apng的
