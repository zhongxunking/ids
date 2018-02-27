# 分布式id生成器-ids

1. 简介
> 生成全局唯一的id（流水号），是很多公司都需要解决的问题。如果还是采用时间戳+随机数形式生成，在并发量大时，很有可能会生成重复的id。重复id的危害就是会导致一系列问题，比如幂等性。ids专门用来高效的生成全局唯一id，每个应用实例的tps可达到100万，而且服务端无压力。即使服务端和zookeeper都挂掉了，id生成依然可用。（zookeeper只做为系统启动阶段时生成workerId用，id生成时不会使用到。）

2. 环境要求：
> * jdk1.8
> * zookeeper


> 注意：ids已经上传到[maven中央库](http://search.maven.org/#search%7Cga%7C1%7Corg.antframework.ids)

3. 感谢
> 感谢[美团的Leaf系统](https://tech.meituan.com/MT_Leaf.html)设计思路介绍，里面介绍了几种全局id生成技术，建议读者先阅读它。ids借鉴了leaf的一些设计思路，同时也进行了优化。

## 1. 使用ids
> 下面先介绍ids的使用，然后再介绍整体设计和部署。

1. 引入ids依赖：
```
<dependency>
    <groupId>org.antframework.ids</groupId>
    <artifactId>ids</artifactId>
    <version>1.1.0.RELEASE</version>
</dependency>
```
2. 系统启动阶段设置ids的参数：
```
System.setProperty(IdsParams.APP_CODE_PROPERTY_NAME, "mytest");   // 设置应用的编码
System.setProperty(IdsParams.APP_PORT_PROPERTY_NAME, "8080");   // 设置应用使用的端口
System.setProperty(IdsParams.SERVER_URL_PROPERTY_NAME, "http://localhost:6210");   // 设置idcenter服务端地址
System.setProperty(IdsParams.ZK_URLS_PROPERTY_NAME, "localhost:2181");  // 设置zookeeper地址，存在多个则以“,”分隔（比如：192.168.0.1:2181,192.168.0.2:2181）
System.setProperty(IdsParams.HOME_PATH_PROPERTY_NAME, "/var/ids");  // 设置缓存文件夹路径
```
3. 使用ids
```
// 上面已经设置了ids的参数，下面就可以直接生成20位全局唯一的id了，比如：20180214170000000001
String id1 = UID.newId();
String id2 = UID.newId();
// 。。。
```

## 2. 整体设计
> ids是结合开源项目[“idcenter”](https://github.com/zhongxunking/idcenter)和[“本地id生成”](https://github.com/zhongxunking/ant-common-util#7-本地id生成器)，进行组合出的适用于绝大多数公司的分布式id生成器，建议读者先阅读“idcenter”和“本地id生成”的文档。如果ids不满足你的需求，你完全可以自己根据“idcenter”和“本地id生成”组装出适合自己的分布式id生成器。

### 2.1 生成的id长度为20位：
1. 从idcenter获取到的id：id前10位为生成id时间的yyyyMMddHH，后10位为从idcenter获取到的id。例如：20180214170000000001
2. “本地id生成”生成的id：id前8位为生成id时间的yyyyMMdd，中间5位为应用的workerId+25000，最后7位为通过“本地id生成”生成的id。例如：20180214250010000001

### 2.2 id生成过程如下：
1. idcenter可用时：ids会优先使用从idcenter获取到的id，ids平均每5分钟请求一次idcenter，并且会预存足够使用10至15分钟的id量。
2. idcenter不可用时：idcenter不可用的接下来10到15分钟使用的依然是从idcenter获取到的id，因为有id存量。只有当从idcenter获取到的id使用完时，ids才会使用从“本地id生成”生成的id，直到idcenter的服务恢复。

### 2.3 限制
1. idcenter生成的id：每小时生成的id数量上限为100亿个，如果超过100亿，则会预先透支下一个小时的id。
2. “本地id生成”的id：每个应用节点每天生成的id数量上限为1000万个，如果超过1000万，则会预先透支下一天的id。
3. 应用节点的数量上限是37500个（因为workerId的长度限制为5位，再减去25000，还有为了效率考虑做了一些措施），如果你的应用实例超过了37500，则可以考虑参考ids自己实现一个id位数更长的分布式id生成器（比如25或30位）。

### 2.4 其他
1. idcenter和“本地id生成”都有并发控制，多线程情况下不会出现问题。
2. idcenter和“本地id生成”都有安全控制，即使系统突然宕机、时钟被回拨，都不会出现问题，生成的id永远都是唯一的。
3. 总的来说生成的id基本上是从idcenter获取的，“本地id生成”只是一种保底措施，保证idcenter不可用时，依然可以正常生成id。
4. 从idcenter获取到的id第9至10位小于24，从“本地id生成”生成的id第9至10位大于24，没有等于24这种情况。
5. 从idcenter生成的id的时间前缀有5分钟误差，比如20180214170000000001这个id真正的生成时间是2018-02-14 17:00至
2018-02-14 18:05这期间。

### 2.5 整体设计图
![](https://note.youdao.com/yws/api/personal/file/WEB54a4eae4524569272aadcc611a4355f2?method=download&shareKey=0776adfb2e6be8406898b74e19c58ffa)

## 3. 部署
### 3.1 部署idcenter
1. 按照[“idcenter”](https://github.com/zhongxunking/idcenter#2-服务端部署)的文档部署服务端。
2. 通过idcenter的后台管理页面添加一个id提供者：
```
id编码：common-uid
周期类型：每小时
id最大值：10000000000（100亿）
单次获取id最多数量：1000000（100万，可根据各自情况可以做调整）
```
![](https://note.youdao.com/yws/api/personal/file/WEB896a835676108bdf7dcfd792dd4e5764?method=download&shareKey=b050ceb4de5991523295c38aaa0c2e6f)
### 3.2 部署zookeeper
1. 如果你所在的公司有现有的zookeeper，则直接使用现有的zookeeper就行，ids只会操作“/ids/workerId”路径下的节点。
2. 如果你所在的公司没有部署zookeeper，则需要部署zookeeper。对于怎么部署zookeeper，网上有很多介绍，在此就不论述了。
