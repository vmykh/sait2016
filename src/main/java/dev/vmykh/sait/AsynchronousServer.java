package dev.vmykh.sait;

import io.datakernel.bytebuf.ByteBuf;
import io.datakernel.eventloop.NioEventloop;
import io.datakernel.eventloop.SimpleNioServer;
import io.datakernel.eventloop.SocketConnection;
import io.datakernel.eventloop.TcpSocketConnection;

import java.nio.channels.SocketChannel;

import static io.datakernel.util.ByteBufStrings.decodeAscii;
import static io.datakernel.util.ByteBufStrings.encodeAscii;

public class AsynchronousServer extends SimpleNioServer {

	private static final int PORT = 21000;

	private static class HelloConnection extends TcpSocketConnection {
		protected HelloConnection(NioEventloop eventloop, SocketChannel socketChannel) {
			super(eventloop, socketChannel);
		}

		@Override
		protected void onRead() {
			while (!readQueue.isEmpty()) {
				String input = decodeAscii(readQueue.take());
				input = input.substring(0, input.length() - 1);
				String response = Utils.processRequest(input);
				ByteBuf outputBuffer = ByteBuf.wrap(encodeAscii(response));
				write(outputBuffer);
			}
		}
	}

	public AsynchronousServer(NioEventloop eventloop) {
		super(eventloop);
	}

	@Override
	protected SocketConnection createConnection(SocketChannel socketChannel) {
		HelloConnection connection = new HelloConnection(eventloop, socketChannel);
		return connection;
	}

	public static void main(String[] args) throws Exception {
		final NioEventloop eventloop = new NioEventloop();

		AsynchronousServer server = new AsynchronousServer(eventloop);
		server.setListenPort(PORT);
		server.listen();

		eventloop.run();
	}
}