// ChatServer.java
package ChatEx02;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
	private static final int PORT = 8010;
    private static Map<String, Set<ClientHandler>> chatRooms = new ConcurrentHashMap<>();	// 방 리스트
    private static Set<ClientHandler> allClients = ConcurrentHashMap.newKeySet();			// 클라이언트 리스트
    private static Set<String> usedNicknames = ConcurrentHashMap.newKeySet();				// 닉네임 리스트

    public static void main(String[] args) {
    	// 서버 소켓 생성 및 클라이언트 연결 대기
    	try (ServerSocket serverSocket = new ServerSocket(PORT)) {
    		System.out.println("서버가 포트 " + PORT + "에서 시작되었습니다.");
    		
    		while(true) {
    			Socket clientSocket = serverSocket.accept();
                System.out.println("새로운 클라이언트가 연결되었습니다: " + clientSocket);
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
		}
	}
    
    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nickname;	// 현재 닉네임
        private String currentRoom; // 현재 방 이름 추가
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    	@Override
    	public void run() {

    	}
    }
}