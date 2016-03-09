//package dev.vmykh.sait;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.util.Scanner;
//
//public class Client {
//
//	public static void main(String[] args) throws IOException {
//		System.out.println("Enter name:");
//		Scanner scanner = new Scanner(System.in);
//		String input = scanner.nextLine();
//		System.out.println("connecting...");
//		Socket clientSocket = new Socket("localhost", 21000);
//		System.out.println("connected");
//		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//		BufferedReader in = new BufferedReader(
//				new InputStreamReader(clientSocket.getInputStream()));
//
//		out.println(input);
//		System.out.println("printed");
//
//		String response = in.readLine();
//		System.out.println("Response: " + response);
//	}
//}
