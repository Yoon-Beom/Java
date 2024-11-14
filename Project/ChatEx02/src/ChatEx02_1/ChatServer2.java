package ChatEx02_1;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;



public class ChatServer2 {
	private static final int PORT = 8010;
    private static Map<String, Set<ClientHandler>> chatRooms = new ConcurrentHashMap<>();

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("서버가 포트 " + PORT + "에서 시작되었습니다.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class ClientHandler implements Runnable {
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;
		private String nickname;
		private String currentRoom;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // 닉네임 설정
                out.println("닉네임을 입력하세요:");
                nickname = in.readLine();
                
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("/join ")) {
                        joinRoom(inputLine.substring(6));
                    } else if (inputLine.startsWith("/create ")) {
                        createRoom(inputLine.substring(8));
                    } else {
                        broadcast(nickname + ": " + inputLine);
                    }
                }
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (currentRoom != null) {
                    chatRooms.get(currentRoom).remove(this);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
			}
		}
		
		private void joinRoom(String roomName) {
			if (currentRoom != null) {
                chatRooms.get(currentRoom).remove(this);
            }
            chatRooms.computeIfAbsent(roomName, k -> new CopyOnWriteArraySet<>()).add(this);
            currentRoom = roomName;
            out.println("방 '" + roomName + "'에 입장했습니다.");
		}
		
		private void createRoom(String roomName) {
            if (!chatRooms.containsKey(roomName)) {
                chatRooms.put(roomName, new CopyOnWriteArraySet<>());
                out.println("방 '" + roomName + "'이 생성되었습니다.");
            } else {
                out.println("이미 존재하는 방 이름입니다.");
            }
		}
		
		private void broadcast(String message) {
			if (currentRoom != null) {
                for (ClientHandler client : chatRooms.get(currentRoom)) {
                    client.out.println(message);
                }
            }
		}
	}
}
