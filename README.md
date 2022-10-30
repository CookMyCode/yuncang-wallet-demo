# yuncang-wallet-demo
钱包支付服务

## 模块说明
wallet-common: 服务基础层  
wallet-client: 公共接口层, 服务对外暴露的接口  
wallet-server: 钱包服务, 具体实现  

## 设计思路
### 表设计
ddl 在 wallet-server模块下路径 src/main/resources/db/migration/
#### 用户钱包表 user_wallet
存放钱包id、所属用户、钱包余额等信息。

#### 进行中的钱包动账交易表 wallet_transaction_pool
用来临时存放钱包交易信息的表，充值、提现、消费、退款、冲正等交易行为都会尝试在该表插入数据，插入成功才会进入下一步交易流程，表中有唯一的订单号字段，订单号来自于调起方。  
除了在交易处理过程中发生异常，否则交易结束后不会主动删除表中数据，主要是为了防止出现重复消费问题。  
交易完成的数据会保留在表中，直到被定时任务删除，删除条件是数据的创建时间超过一定时间，这个时间需根据订单待支付时间等业务环境进行调整。
交易完成后数据会转存到 wallet_transaction_info 表（见下一节）。  
1. 为什么需要这个表？  
1）这个表可以理解为一个锁，表中不允许存在订单号和交易类型都相同的两条数据，保证了数据的唯一性，防止出现重复消费问题；  
2）其次，表中数据由于定时的清理，数据量始终在一个可控的范围，在对表进行查询，对表中索引字段进行修改时，相对于操作一个大表，执行效率更高。  
2. 为什么不在交易结束时清理表数据？  
1）删除操作会降低接口的效率，使用定时器在其他线程上进行删除操作更加合适；  
2）最主要的还是为了保证不会发生重复消费的问题，比如因为网络原因，重复调用了两次钱包支付接口，第一次很快到达并执行完成，第二次1分钟后才到达，如果订单马上被清理还是会出现重复消费。  
3. 为什么不用redis？  
1）redis的事务功能不完善；  
2）不是热点数据没有必要放redis；  
3）redis基于内存实现，占用内存资源。  

#### 钱包动账交易信息表 wallet_transaction_info
交易结束时，交易信息会转存到这个表，原则上本表中的数据不允许再被修改，表中加入了自增的索引，当数据量非常庞大时，深度分页可以用上该索引。  
该表提供给用户查看交易记录，以及给后台运营人员进行对账、管理。  

#### 其他
如果支付流程逐渐复杂，比如加入了消费券、混合支付等，可能还需要有历史表记录每个流程步骤，方便以后对问题的溯源。    

### 模块以及框架
我将钱包支付和其他支付方式视为同一级别的服务，也是为了方便维护，所以放到一个单独的微服务中。  
使用的仍然是传统的三层结构，每层都需要进行bean的转换，持久层返回PO，服务层返回DTO，接口层返回VO。  
主要使用了 springboot、myBatis、mySql。    

### 接口设计
接口入口在 wallet-server 模块下 src/main/java/com/example/wallet/client  
这里主要介绍支付和退款接口。  
交易接口要注意三个点：  
1. 防重；  
2. 事务，防幻读；  
3. 金额计算。  

先来说一下第一点，在前面介绍表设计时有说到，防重复请求是通过innodb的唯一索引来实现的。一个订单在有效期内，必须保证只能有一个正在处理的支付或退款交易，所以支付接口只接收一个订单id做为参数，支付接口在获取到订单号后，先是尝试在pool表中插入数据，插入成功后，需要去调用订单相关服务的接口，确认订单的有效性，再进行下一步操作。  
第二点防幻读，这是在修改钱包余额时要注意的点，我的解决方案是使用 lock in share mode，由于使用了索引字段，所以是一个行级锁，一般业务场景中不会用户同时使用同一个钱包支付多笔订单的情况，但是为了防止特例，还是要做好防幻读。  
第三点金额计算，我直接使用了BigDecimal，通过String和BigDecimal的转换来保证计算的准确性，因时间问题没有做进一步封装，但最好还是使用第三方的Money工具类。  
钱包交易归根结底是对钱包余额的修改，不管是退款还是支付，所以我把防重和事务都剥离出来，统一抽象为交易操作，而金额如何进行增减计算需要在支付和付款接口进行具体实现。  
获取钱包交易信息列表则是需要注意深度分页的问题，对于一个电商平台，订单和支付记录是最重要的数据之一，随着时间推移，表中会存放海量数据，设计初期要尽可能地做好冗余，所以在表设计时我不会吝啬索引的创建，同时表中加入了自增的索引，在进行深度分页时将会发挥作用。

由于时间关系，很多基础组件都没有做好实现，如有遗漏的地方欢迎指正，感谢。



