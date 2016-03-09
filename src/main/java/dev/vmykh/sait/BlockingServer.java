package dev.vmykh.sait;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingServer {
	private static final int PORT = 20000;

	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(PORT);
		while (true) {
			final Socket clientSocket = server.accept();
			handleRequestInNewThread(clientSocket);
		}
	}

	private static final void handleRequestInNewThread(final Socket clientSocket) throws IOException {
		final PrintWriter out =
				new PrintWriter(clientSocket.getOutputStream(), true);
		final BufferedReader in = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String input = in.readLine();
					out.println("Hello, " + input);
					clientSocket.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}
}
