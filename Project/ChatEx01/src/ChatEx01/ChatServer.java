package ChatEx01;

import java.io.*;
import java.net.*;

public class ChatServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("서버가 시작되었습니다. 클라이언트 연결 대기 중...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("클라이언트가 연결되었습니다.");

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("클라이언트: " + inputLine);
                out.println("서버: " + inputLine);
            }
        } catch (IOException e) {
            System.out.println("서버 오류: " + e.getMessage());
        }
    }
}