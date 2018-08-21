package robin.netty.server;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.handler.codec.http.multipart.MixedAttribute;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

public class MyServerHandler extends ChannelInboundHandlerAdapter {
	private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
	private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
	private static final AsciiString CONNECTION = new AsciiString("Connection");
	private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

	public static Logger logger = Logger.getLogger(MyServerHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// 如果不是HTTP请求
		if (!(msg instanceof FullHttpRequest)) {
			logger.error("不是HTTP请求");
			return;
		}
		FullHttpRequest req = (FullHttpRequest) msg;

		// 解析url
		String uri = req.uri();
		logger.info("请求url=[" + uri + "]");
		JSONObject ret = new JSONObject();
		if (req.method() == HttpMethod.GET) {
			logger.info("get请求");
			ret.put("ret", "true");
			ret.put("msg", "来自get请求的返回");
			responseJson(ctx, req, ret.toString());
		} else if (req.method() == HttpMethod.POST) {
			// post的请求参数
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(req);
			decoder.offer(req);
			try {
				List<InterfaceHttpData> postList = decoder.getBodyHttpDatas();
				// 读取从客户端传过来的参数
				for (InterfaceHttpData data : postList) {
					String name = data.getName();
					// logger.info(data.toString());
					String value = null;
					if (InterfaceHttpData.HttpDataType.Attribute == data.getHttpDataType()) {
						MixedAttribute attribute = (MixedAttribute) data;
						attribute.setCharset(CharsetUtil.UTF_8);
						value = attribute.getValue();
						logger.info("name:" + name + ",value:" + value);
					}
				}
				ret.put("ret", "true");
				ret.put("msg", "来自post请求的返回");
				responseJson(ctx, req, ret.toString());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

	}

	/**
	 * 响应HTTP的请�?
	 * 
	 * @param ctx
	 * @param req
	 * @param jsonStr
	 */
	private void responseJson(ChannelHandlerContext ctx, FullHttpRequest req, String jsonStr) {
		boolean keepAlive = HttpUtil.isKeepAlive(req);
		byte[] jsonByteByte = jsonStr.getBytes();
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(jsonByteByte));
		response.headers().set(CONTENT_TYPE, "text/json;charset=utf-8");
		response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
		response.headers().set("Access-Control-Allow-Origin", "*");// 跨域访问

		if (!keepAlive) {
			ctx.write(response).addListener(ChannelFutureListener.CLOSE);
			ctx.flush();
		} else {
			response.headers().set(CONNECTION, KEEP_ALIVE);
			ctx.write(response);
			ctx.flush();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error(ctx.channel().remoteAddress() + ":关闭了连接");
		ctx.close();
	}

}
