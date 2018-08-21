package robin.main;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import robin.netty.server.HttpServer;

public class MainApp {

	public static Logger logger = Logger.getLogger(MainApp.class);

	public static void main(String[] args) {
		// 读取端口
		int port = 9876;
		// 启动服务端
		HttpServer server = new HttpServer(port);
		new ScheduledThreadPoolExecutor(1).schedule(server, 5, TimeUnit.SECONDS);
	}
}
