### 安装ruby
1. 官网下载ruby-2.4.2.tar.gz
2. 解压：tar -zxvf ruby-2.4.2.tar.gz
3. cd ruby-2.4.2
4. ./configure
5. make
6. make install
7. 检查ruby版本：ruby -v

### 安装redis
1. 官网下载redis-4.0.1.tar.gz
2. 解压：tar -zxvf redis-4.0.1.tar.gz
3. cd redis-4.0.4
4. 编译：make
5. 在用户目录下面建立一个文件夹：mkdir redis-cluster
6. cd redis-cluster；mkdir 7000 7001 7002 7003 7004 7005；mkdir bin
7. 拷贝4中src目录的redis-server、redis-cli、redis-trib.rb到6中创建的bin目录下面
8. 拷贝4中redis.conf到6中创建的7000 7001 7002 7003 7004 7005目录
9. 修改配置文件，这里以7000为例，其他的就只改port.
10. vi redis.conf
        
        #bind后面的ip是访问redis的IP地址，这里填写本机的ip（非本机能连）
        bind 127.0.0.1

        #保护模式
        protected-mode no
        
        #端口号
        port 7000
        
        #是否后台启动
        daemonize yes
        
        #redis执行目录
        dir /home/redis/redis-cluster/7000
        
        #进程文件路径
        pidfile /home/redis/redis-cluster/7000/redis_7000.pid
        
        #日志文件路径
        logfile "/home/redis/redis-cluster/7000/redis.log"
        
        #是否开启AOF
        appendonly yes
        
        #Redis Cluster配置
        cluster-enabled yes
        cluster-config-file nodes-7000.conf
        cluster-node-timeout 15000
11. 分别启动redis：bin/redis-server 7000/redis.conf。注：如果日志文件遇到:You requested maxclients of 10000 requiring at least 10032 max file descriptors，调大系统文件打开数（ulimit -a查看）
12. 建立集群：bin/redis-trib.rb create --replicas 1 192.168.150.133:7000 192.168.150.133:7001 192.168.150.133:7002 192.168.150.133:7003 192.168.150.133:7004 192.168.150.133:7005
        
        如果出现：/usr/local/lib/ruby/2.4.0/rubygems/core_ext/kernel_require.rb:55:in `require': cannot load such file -- redis (LoadError)
        使用root用户执行：gem install redis（需要ruby环境，前面已安装好）
13. 连接集群模式：bin/redis-cli -h 192.168.150.129 -p 7001 -c（这个参数不加，不能设置别的槽点的key-value）