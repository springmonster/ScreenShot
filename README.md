# Android:手机投屏

## Android几种截屏方式

* View.getDrawingCache()
* 读取/dev/graphics/fb0
* adb shell screencap/screenshot -p xxx.png
* 反射调用Surface.screenshot()/SurfaceControl.screenshot()
* MediaProjection,VirtualDisplay(Android Version >= 5.0）

最后两种方式也是Vysor这款软件所采用的。


## Android Annotation @hide

* 对于@hide标记的class，在/system/framework/framework.jar中是存在的。但是在开发中引入Android Sdk的时候，引用的为sdk/platforms/android-X/android.jar（例如X为21），这里面被标记为@hide的class已经不存在了。例如SurfaceControl.java。
* 对于@hide标记的field和method，可通过反射进行调用。


## Adb命令
### app_process
#### am命令

```
adb shell am start -n com.android.browser/com.android.browser.BrowserActivity
```

#### pm命令

```
adb shell pm list packages
```

#### 如何启动自定义的Java类获取类似am命令的效果

```
package com.wise.main;

public class HelloWorld {
    public static void main(String[]args){
        System.out.println("Hello, world!");
    }
}
```
```
dx --dex --output=D:\HelloWorld.dex HelloWorld.class
adb push D:\HelloWorld.dex /sdcard
```
```
adb shell
export CLASSPATH=/sdcard/HelloWorld.dex
exec app_process /system/bin com.wise.main.HelloWorld
```

### LocalSocketServer

### adb forward 协议:port 协议:port

## 如何运行
* 运行ComputerClientFrame.java
* 点击Phone -> Prepare
* 点击Phone -> Connect Phone
* 如果使用鼠标进行点击操作，打开开发者选项里面的“模拟点击”

## 数据格式
### 手机端到电脑端
> 发送
```
手机屏幕的宽：DataOutputStream
手机屏幕的高：DataOutputStream
手机图像大小：DataOutputStream
手机图像字节数组：BufferedOutputStream
```

> 接收
```
字符串：BufferedReader
UP302#492：操作类型x#y
UP
DOWN
MOVE
MENU
HOME
BACK
```
