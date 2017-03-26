# 利用编译时注解实现一个ButterKnife

ButterKnife 这个框架真的很好用，简化了大量的代码，在效率上和手写的代码相比几乎没有损失。虽然会用 ButterKnife，同时 ButterKnife 的原理也要明白。

于是，找到 ButterKnife 最早的 [1.0 版本](https://github.com/JakeWharton/butterknife/releases/tag/butterknife-parent-1.0.0)，学习一下它的原理，并模仿一下。

## 最终效果

在 Activity 中使用注解绑定控件

```java
public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.tv)
    public TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Views.inject(this);
        mTextView.setText("TextView");
    }
}
```

框架根据注解生成的代码如下

```java
public class MainActivity$$ViewInjector {
  public static void inject(MainActivity activity) {
    activity.mTextView = (android.widget.TextView) activity.findViewById(2131427413);
  }
}
```

## 项目结构

* app
* annotation
* compiler
* api

### app

就是我们的android项目

### annotation

定义注解，`InjectView` 就放在这个 module 中

### compiler

注解解析器，生成代码

### api

定义 Butterkinfe 的 API，比如我们常用的 Butterknife.bind() 

## annotation 

在 Android Studio 点击 File - New Module - Java Library 新建一个 Java Module

build.gradle 内容如下

```
apply plugin: 'java'

sourceCompatibility = JavaVersion.VERSION_1_7
targetCompatibility = JavaVersion.VERSION_1_7
```

然后定义一个注解

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface InjectView {
    int value();
}
```

## compiler

有了注解，接着就需要一个注解解析器，去解析注解然后生成代码。

同样的，建立一个 Java Library，build.gradle 的定义如下

```
apply plugin: 'java'

sourceCompatibility = JavaVersion.VERSION_1_7
targetCompatibility = JavaVersion.VERSION_1_7

dependencies {
    compile 'com.google.auto.service:auto-service:1.0-rc2'
    compile project(':annotation')
}
```

在这里引入了 Google 的 auto-service 依赖。它可以用来帮助生成 META-INF 文件

接着就是创建注解解析器，实现 `process` 方法

```java
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
	@Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    	return false;
    }
}
```

这个方法中，主要做了几件事

1. 找到所有使用 InjectView 注解的类
2. 解析 InjectView 注解的信息
3. 根据信息生成代码

## api

创建一个 Android Library，建立一个 `Views` 类

```java
import android.app.Activity;

import java.lang.reflect.Method;

public class Views {

    private Views() {
        // No instances.
    }

    public static void inject(Activity activity) {
        try {
            Class<?> injector = Class.forName(activity.getClass().getName() + "$$ViewInjector");
            Method inject = injector.getMethod("inject", activity.getClass());
            inject.invoke(null, activity);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject views for activity " + activity, e);
        }
    }
}
```

## app

build.gradle 定义如下

```
apply plugin: 'com.android.application'
apply plugin: 'com.neenbedankt.android-apt'

android {
    compileSdkVersion 24
    buildToolsVersion "25.0.2"
    defaultConfig {
        applicationId "com.okada.viewinject"
        minSdkVersion 14
        targetSdkVersion 24
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }

}

dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    compile 'com.android.support:appcompat-v7:24.2.1'
    compile project(':annotation')
    apt project(':compiler')
    compile project(':api')
}
```

同时在项目的 build.gradle 定义如下

```java
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.2.0'
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'  // 加上这句话
    }
}

allprojects {
    repositories {
        jcenter()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

现在可以使用注解了

```java
public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.tv)
    public TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Views.inject(this);
        mTextView.setText("TextView");
    }
}
```

然后点击 Build - Rebuild Project，就可以在 app - build - generated - source - apt 文件夹中看到生成的代码了

## 项目源码

https://github.com/okadaNana/ViewInject

## 参考来源

* [Android APT（编译时代码生成）最佳实践](https://github.com/taoweiji/DemoAPT)
* [Android 如何编写基于编译时注解的项目](http://blog.csdn.net/lmj623565791/article/details/51931859)
* [Android之使用apt编写编译时注解](http://www.cnblogs.com/linux007/p/5777963.html)
* [ButterKnife](https://github.com/JakeWharton/butterknife/releases/tag/butterknife-parent-1.0.0)
