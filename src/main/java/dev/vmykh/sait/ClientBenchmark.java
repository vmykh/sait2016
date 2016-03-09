package dev.vmykh.sait;

import io.datakernel.bytebuf.ByteBuf;
import io.datakernel.eventloop.*;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import static io.datakernel.net.SocketSettings.defaultSocketSettings;
import static io.datakernel.util.ByteBufStrings.decodeUTF8;
import static io.datakernel.util.ByteBufStrings.encodeAscii;

public class ClientBenchmark {
	static long totalElapsed = 0;

	private static class SimpleConnection extends TcpSocketConnection {
		public SimpleConnection(NioEventloop eventloop, SocketChannel socketChannel) {
			super(eventloop, socketChannel);
		}

		long start;

		@Override
		public void onRegistered() {
			String line = "line of data";
			final ByteBuf buf = ByteBuf.wrap(encodeAscii(line + "\n"));
			eventloop.postConcurrently(new Runnable() {
				@Override
				public void run() {
					start = System.currentTimeMillis();
					write(buf);
				}
			});


		}

		@Override
		protected void onRead() {
			while (!readQueue.isEmpty()) {
				ByteBuf buffer = readQueue.take();
				totalElapsed += (System.currentTimeMillis() - start);
			}
		}
	}

	/* Run TCP client in an event loop. */
	public static void main(String[] args) throws Exception {
		final NioEventloop eventloop = new NioEventloop();

		for (int i = 0; i < 100; i++) {
//			final int currentIter = i;
			eventloop.connect(new InetSocketAddress("localhost", 21000), defaultSocketSettings(), new ConnectCallback() {
						@Override
						public void onConnect(SocketChannel socketChannel) {
							SocketConnection socketConnection = new SimpleConnection(eventloop,
									socketChannel);
							socketConnection.register();
						}

						@Override
						public void onException(Exception exception) {
							System.err.println("Could not connect to server, make sure it is started: \n" + exception);
						}
					}
			);
		}

		eventloop.run();

		System.out.println("avg: " + (totalElapsed / 1000.0));
	}
}
