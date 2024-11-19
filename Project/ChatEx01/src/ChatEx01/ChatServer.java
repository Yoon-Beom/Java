// ChatServer.java
package ChatEx01;

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
        // 수정된 부분: 서버 소켓 생성 및 클라이언트 연결 대기
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("서버가 포트 " + PORT + "에서 시작되었습니다.");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("새로운 클라이언트가 연결되었습니다: " + clientSocket);
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
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
            try {
            	// 게스트 닉네임 자동 생성
            	nickname = generateUniqueGuestNickname();
            	synchronized (usedNicknames) {
                    usedNicknames.add(nickname);
                }
            	out.println("NICKNAME_ASSIGNED " + nickname);
            	
                System.out.println("새로운 사용자 연결: " + nickname);
                allClients.add(this);
                broadcastToAll(nickname + "님이 입장하셨습니다.");
                
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                	System.out.println("inputLine : " + inputLine);	// 메세지 확인용
                	
                	if (inputLine.startsWith("/nick ")) {	// 닉네임 변경 처리
                		String newNickname = inputLine.substring(6).trim();
                		if (!newNickname.isEmpty()) { // 비어있지 않으면,
                			synchronized (usedNicknames) {
                				if (!usedNicknames.contains(newNickname)) {
                					String oldNickname = this.nickname;
                                    usedNicknames.remove(oldNickname);
                                    usedNicknames.add(newNickname);
                                    this.nickname = newNickname;
                                    out.println("NICKNAME_CHANGED " + newNickname);
                                    
                                    System.out.println("'" + oldNickname + "'의 닉네임이 '" + newNickname + "'으로 변경되었습니다.");
                                    broadcastToAll("'" + oldNickname + "'의 닉네임이 '" + newNickname + "'으로 변경되었습니다.");
                				} else {
                                    out.println("NICKNAME_TAKEN");  // 닉네임 중복
                                }
                			}
                		} else {
                            out.println("INVALID_NICKNAME"); // Guest 거나, 닉네임이 없을 경우 
                        }
                	} else if (inputLine.startsWith("/rooms")) {
                		
                	} else if (inputLine.startsWith("/join ")) {
                		String roomName = inputLine.substring(6).trim();
                        joinRoom(roomName);
                	} else {
                		System.out.println("받은 메시지: " + inputLine);
                		broadcastToAll(nickname + ": " + inputLine);
                	}
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                	allClients.remove(this); // 참여자 목록에서 제거
                	usedNicknames.remove(nickname);
                    broadcastToAll(nickname + "님이 퇴장하셨습니다.");
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    	
    	// 고유한 게스트 닉네임 생성 메소드
    	private String generateUniqueGuestNickname() {
    		int number = 1;
    		String nickname = "";
    		
    		do {
    			nickname = "Guest" + number;
    			number++;
    		} while (usedNicknames.contains(nickname));
    		
    		return nickname;
    	}
    	
    	// 모두에게 채팅 날리는 메소드
    	private void broadcastToAll(String message) {
    		System.out.println("allClients : " + allClients);
            System.out.println("broadcastToAll : " + message);
            for (ClientHandler client : allClients) {
                client.out.println(message);
            }
        }
    	
    	private void joinRoom(String roomName) {
    		
    		
    		this.currentRoom = roomName;
    		
    		updateParticipantList();
    	}
    	
    	private void createRoom(String roomName) {
    		
    	}
    	
    	private void broadcast(String message) {
    		
    	}
    	
    	private void updateParticipantList() {
            // 모든 클라이언트에게 현재 참여자 목록 전송
            StringBuilder participants = new StringBuilder("PARTICIPANTS ");
            for (ClientHandler client : allClients) {
                participants.append(client.nickname).append(" ");
                
            }
            broadcastToAll(participants.toString());
        }
    }
}
