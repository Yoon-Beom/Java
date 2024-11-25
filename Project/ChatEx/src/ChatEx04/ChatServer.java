// ChatServer.java
package ChatEx04;

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
    		try {
    			// 게스트 닉네임 자동 생성
            	nickname = generateUniqueGuestNickname();
            	synchronized (usedNicknames) {
                    usedNicknames.add(nickname);
                }
            	out.println("NICKNAME_ASSIGNED " + nickname); // 닉네임 할당됨 + 닉네임
            	
            	System.out.println("새로운 사용자 연결: " + nickname);
                allClients.add(this);		// 클라이언트를 전체 리스트에 추가
                joinRoom("Lobby"); // 기본 로비에 입장
                broadcastToAll(nickname + "님이 입장하셨습니다.");
            	updateParticipantList(); 	// 대기실 참여자 목록 전송
            	broadcastRoomList();		// 방 목록 전송
            	
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                	System.out.println("inputLine : " + inputLine);	// 메세지 확인용
                	
                	if (inputLine.startsWith("/nick ")) {	// 닉네임 변경 처리
                		String newNickname = inputLine.substring(6).trim();
                		
                		if (!newNickname.isEmpty()) { // 비어있지 않으면,
                			synchronized (usedNicknames) {
                				if (!usedNicknames.contains(newNickname)) {	// 중복 확인
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
                            out.println("INVALID_NICKNAME"); // 닉네임이 없을 경우 
                        }
                	} else if (inputLine.equals("/participants")) { // 참여자 목록 요청
                	    updateParticipantList();
                	} else if (inputLine.startsWith("/create ")) { // 방 생성 처리
                        String roomName = inputLine.substring(8).trim();
                        createRoom(roomName);
                    } else if (inputLine.startsWith("/join ")) { // 방 입장 처리
                        String roomName = inputLine.substring(6).trim();
                        joinRoom(roomName);
                    }
                }
			} catch (Exception e) {
                e.printStackTrace();
			} finally {
                try {
                	allClients.remove(this); // 참여자 목록에서 제거
                	usedNicknames.remove(nickname);
                    broadcastToAll(nickname + "님이 퇴장하셨습니다.");
                    socket.close();
                    out.close();
                    in.close();
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
    	
    	// 방 인원에게 채팅 날리는 메소드
    	private void broadcastToRoom(String roomName, String message) {
            for (ClientHandler client : chatRooms.get(roomName)) {
                client.out.println(message);
            }
        }

    	// 방 생성
    	private void createRoom(String roomName) {
    		 if (!chatRooms.containsKey(roomName)) {
    		        chatRooms.put(roomName, ConcurrentHashMap.newKeySet());
    		        out.println("ROOM_CREATED " + roomName);
    		        broadcastToAll("New room created: " + roomName);
    		        broadcastRoomList();
    		    } else {
    		        out.println("ROOM_EXISTS");
    		    }
    	}
    	
    	// 방 입장
    	private void joinRoom(String roomName) {
    		if (currentRoom != null) {
                leaveRoom(); // 이전 방에서 나가기
            }
    	}
    	
    	private void leaveRoom() {
    		if (currentRoom != null) {
                chatRooms.get(currentRoom).remove(this);
                broadcastToRoom(currentRoom, nickname + " has left the room.");
                
                // 방이 비어있으면 삭제
                if (chatRooms.get(currentRoom).isEmpty()) {
                	chatRooms.remove(currentRoom);
                }
                
                currentRoom = null; // 현재 방 초기화
    		}
    	}
    	
    	private void broadcastRoomList() {
    		// 모든 클라이언트에게 업데이트된 방 목록 전송
    	    StringBuilder roomList = new StringBuilder("ROOMS ");
    	    for (String room : chatRooms.keySet()) {
    	        roomList.append(room).append(", ");
    	    }
    	    broadcastToAll(roomList.toString());
    	}
    	    	
    	private void updateParticipantList() {
    		// 같은 방 현재 참여자 목록 전송
    		if (currentRoom != null) {
        		Set<ClientHandler> roomParticipants = chatRooms.get(currentRoom);
        		if (roomParticipants != null) {
        			StringBuilder participants = new StringBuilder("PARTICIPANTS ");
            	    for (ClientHandler participant  : roomParticipants) {
            	        participants.append(participant .nickname).append(", ");
            	    }
            	    broadcastToRoom(currentRoom, participants.toString());
        		}
    		}
    	}
    }
}