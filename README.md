# 分布式id生成器-ids

1. 简介
> 生成全局唯一的id（流水号），是很多公司都需要解决的问题。如果还是采用时间戳+随机数形式生成，在并发量大时，很有可能会生成重复的id。重复id的危害就是会导致一系列问题，比如幂等性。

> ids专门用来高效的生成全局唯一id，支持多数据中心，每个应用实例的tps可达到100万，而且服务端毫无压力。即使服务端和zookeeper都宕机了，id生成依然可用（ids弱依赖zookeeper）。

2. 环境要求：
> * jdk1.8
> * zookeeper


> 注意：本项目已经上传到[maven中央库](http://search.maven.org/#search%7Cga%7C1%7Corg.antframework.ids)

3. 技术交流和支持
> 欢迎加我微信（zhong_xun_），进行技术交流和支持。如果本项目对你有帮助，欢迎Star和Fork。

## 1. 整体设计
ids是结合开源项目[“idcenter”](https://github.com/zhongxunking/idcenter)和[“本地id生成”](https://github.com/zhongxunking/ant-common-util#7-%E6%9C%AC%E5%9C%B0id%E7%94%9F%E6%88%90%E5%99%A8)，进行组合出的适用于绝大多数公司的分布式id生成器。如果ids生成的id不满足你的需求，你完全可以自己根据“idcenter”和“本地id生成”组装出适合自己的分布式id生成器。

### 1.1 整体设计图
<img src="https://note.youdao.com/yws/api/personal/file/WEB54a4eae4524569272aadcc611a4355f2?method=download&shareKey=0776adfb2e6be8406898b74e19c58ffa" width=700 />

### 1.2 id生成过程
分为以下两种情况：
1. idcenter可用时：ids会优先使用从idcenter获取到的id，ids平均每5分钟请求一次idcenter（对服务端压力非常小），并且会预存足够使用10至15分钟的id存量。
2. idcenter不可用时：idcenter不可用的接下来10到15分钟使用的依然是从idcenter获取到的id（因为有id存量）。只有当从idcenter获取到的id使用完时，ids才会使用“本地id生成”进行生成的id。ids会先从zookeeper获取workerId（只会获取一次，对zookeeper压力非常小），并把workerId保存到缓存文件，当以后zookeeper不可用时，会直接从缓存文件读取workerId（弱依赖zookeeper）。

### 1.3 id结构
整体结构：
- id长度：20 + 数据中心编码的长度。
- 生成的id样例：20180908120000000001（无多数据中心情况）、2018090812010000000001（数据中心编码为：01）

id结构详解（无多数据中心情况）:
1. 从idcenter获取的id：id前10位为生成id时间的yyyyMMddHH，后10位为0到100亿趋势递增的数字。样例：20180908120000000123
2. 从“本地id生成”获取的id：id前8位为生成id时间的yyyyMMdd，中间5位为应用的workerId+25000，最后7位为0到1000万趋势递增的数字。样例：20180908250010000123

id结构详解（多数据中心情况）:<br/>
根据上面无多数据中心情况生成的id，然后在id中间插入数据中心的编码。比如数据中心编码为01生成的id样例：2018090812010000000123（从idcenter获取）、2018090825010010000123（从“本地id生成”获取）

### 1.4 限制
1. idcenter生成的id：每小时可生成的id数量上限为100亿个（对于绝大多数公司完全足够），如果超过100亿，则会预先透支下一个小时的id。
2. “本地id生成”的id：每个应用节点每天可生成的id数量上限为1000万个，如果超过1000万，则会预先透支下一天的id。
3. 应用节点的数量上限是37500个（因为workerId的长度限制为5位，再减去25000，还有为了效率考虑做了一些措施）。如果你的应用实例超过了37500，则可以考虑使用多数据中心，或者参考ids自己实现一个id位数更长的分布式id生成器（比如25或30位）。

### 1.5 其他
1. ids有安全控制，即使系统突然宕机、时钟被回拨，都不会出现问题，生成的id永远都是唯一的。
2. 总的来说生成的id基本上是从idcenter获取的，“本地id生成”只是一种保底措施，保证idcenter不可用时，依然可以正常生成id。
3. 生成的id的时间前缀有5分钟误差，比如20180908170000000001这个id真正的生成时间是2018-09-08 17:00至
2018-09-08 18:05这期间。

## 2. 部署
### 2.1 部署idcenter
1. 按照[“idcenter”](https://github.com/zhongxunking/idcenter#2-%E6%9C%8D%E5%8A%A1%E7%AB%AF%E9%83%A8%E7%BD%B2)的文档部署服务端。
2. 通过idcenter的[后台管理页面](https://github.com/zhongxunking/idcenter#4-id%E7%AE%A1%E7%90%86%E4%BB%8B%E7%BB%8D)添加一个id提供者：
```
id编码：common-uid
周期类型：每小时
id最大值：10000000000（100亿）
单次最大数量：1000000（100万）
```
<img src="https://note.youdao.com/yws/api/personal/file/WEBbfaa8afe3aaeb4694a48356ff081748a?method=download&shareKey=afecc5ea5a633f72049535911cc40f66" width=400 />
<img src="https://note.youdao.com/yws/api/personal/file/WEB57aeac85c2f65615483f563011c732b3?method=download&shareKey=b2565bf36898cd27145e97860812a760" width=400 />

### 2.2 部署zookeeper
1. 如果你所在的公司有现有的zookeeper，则直接使用现有的zookeeper就行，ids只会操作“/ids/workerId”路径下的节点。
2. 如果你所在的公司没有部署zookeeper，则需要部署zookeeper。对于怎么部署zookeeper，网上有很多介绍，在此就不论述了。

## 3. 使用ids
1. 引入ids依赖：
```xml
<dependency>
    <groupId>org.antframework.ids</groupId>
    <artifactId>ids</artifactId>
    <version>1.2.0.RELEASE</version>
</dependency>
```
2. 系统启动阶段设置ids的参数：
```java
// 设置idcenter服务端地址
System.setProperty(IdsParams.SERVER_URL_PROPERTY_NAME, "http://localhost:6210");
// 设置缓存文件夹路径
System.setProperty(IdsParams.HOME_PATH_PROPERTY_NAME, "/var/ids");
// 设置应用实例的编码（每个实例必须唯一），比如可以使用ip+端口
System.setProperty(IdsParams.WORKER_PROPERTY_NAME, "192.168.0.1:8080");
// 设置zookeeper地址，存在多个则以“,”分隔（比如：192.168.0.1:2181,192.168.0.2:2181）
System.setProperty(IdsParams.ZK_URLS_PROPERTY_NAME, "localhost:2181");
// 设置数据中心编码（如果不存在多数据中心这种情况，则不需要设置该参数）
System.setProperty(IdsParams.IDC_ID_PROPERTY_NAME, "01");
```
3. 获取id
```java
// 使用UID获取全局唯一id
String id1 = UID.newId();  // 2018090812010000000001
String id2 = UID.newId();  // 2018090812010000000002
```
