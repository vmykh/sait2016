package dev.vmykh.sait;

import io.datakernel.bytebuf.ByteBuf;
import io.datakernel.eventloop.*;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import static io.datakernel.net.SocketSettings.defaultSocketSettings;
import static io.datakernel.util.ByteBufStrings.decodeAscii;
import static io.datakernel.util.ByteBufStrings.decodeUTF8;
import static io.datakernel.util.ByteBufStrings.encodeAscii;

public class ClientBenchmark {
	static long totalElapsed = 0;

	private static class SimpleConnection extends TcpSocketConnection {
		public SimpleConnection(NioEventloop eventloop, SocketChannel socketChannel, int requests) {
			super(eventloop, socketChannel);
			this.requests = requests;
		}

		private int requests;
		private long timeStart;
		private int requestsSentAlready = 0;

		@Override
		public void onRegistered() {
			timeStart = System.currentTimeMillis();
			sendRequest();
		}

		private void sendRequest() {
//			System.out.println("into");
			requestsSentAlready++;
			String requestMsg = "request#" + requestsSentAlready;
			final ByteBuf buf = ByteBuf.wrap(encodeAscii(requestMsg + "\n"));
			eventloop.postConcurrently(new Runnable() {
				@Override
				public void run() {
					write(buf);
				}
			});
		}

		@Override
		protected void onRead() {
			while (!readQueue.isEmpty()) {
				ByteBuf buffer = readQueue.take();
				String response = decodeAscii(buffer);
//				System.out.println("Response: " + response);
				checkResponse(response);
			}
			if (requestsSentAlready < requests) {
				sendRequest();
			} else {
				long timeElapsedForRequestHandling = System.currentTimeMillis() - timeStart;
				totalElapsed += (timeElapsedForRequestHandling);
				close();
			}
		}

		private void checkResponse(String response) {
			boolean responseIsCorrect = response.startsWith("response#" + requestsSentAlready);
			if (!responseIsCorrect) {
				System.out.println("Incorrect response: " + response);
			}
		}
	}

	/* Run TCP client in an event loop. */
	public static void main(String[] args) throws Exception {
		final NioEventloop eventloop = new NioEventloop();

		int connections = 500;
		final int requestsPerConnection = 500;
		for (int i = 0; i < connections; i++) {
			eventloop.connect(
					new InetSocketAddress("localhost", 20000), defaultSocketSettings(), new ConnectCallback() {
						@Override
						public void onConnect(SocketChannel socketChannel) {
							SocketConnection socketConnection =
									new SimpleConnection(eventloop, socketChannel, requestsPerConnection);
							socketConnection.register();
						}

						@Override
						public void onException(Exception exception) {
							exception.printStackTrace();
							System.err.println("Could not connect to server, make sure it is started: \n" + exception);
						}
					}
			);
		}

//		eventloop.keepAlive(true);
		eventloop.run();

		System.out.println("avg: " + (0.001 * totalElapsed / ((double) (connections))));
	}
}
