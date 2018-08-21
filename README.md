# http-httpserver

用netty4.x写的一个http的服务端，支持get和post的请求

###启动方法
		int port = 9876;
		// 启动服务端
		HttpServer server = new HttpServer(port);
		new ScheduledThreadPoolExecutor(1).schedule(server, 5, TimeUnit.SECONDS);
    
###使用者可以对MyServerHandler类进行重写，重写其channelRead方法

target中，包含了可执行的jar包
