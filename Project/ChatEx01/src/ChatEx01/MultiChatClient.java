// MultiChatClient.java
package ChatEx01;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MultiChatClient extends JFrame {
	private JTextArea chatArea; // 채팅 영역
	private JTextField messageField; // 메세지 입력창
	private JButton sendButton; // 전송 버튼
	private JList<String> roomList; // 채팅방 리스트
	private DefaultListModel<String> roomListModel; // 채팅방 리스트 모델
	private JButton createRoomButton; // 방 만들기 버튼
	private JTextField nicknameField; // 닉네임 입력창
	private JButton changeNicknameButton; // 닉네임 변경 버튼
	private JList<String> participantList; // 참여자 리스트
	private DefaultListModel<String> participantListModel; // 참여자 리스트 모델

	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	
	public MultiChatClient() {
		setTitle("다중 채팅방");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 채팅창 영역
		chatArea = new JTextArea();
		chatArea.setEditable(false);
		JScrollPane chatScrollPane = new JScrollPane(chatArea);

		// 메세지 입력 창과 전송 버튼
		messageField = new JTextField();
		sendButton = new JButton("전송");

		// 채팅창 목록
		roomListModel = new DefaultListModel<>();
		roomList = new JList<>(roomListModel);
		JScrollPane roomScrollPane = new JScrollPane(roomList);

		// 채팅방 생성 버튼
		createRoomButton = new JButton("방 만들기");

		// 닉네임 입력 필드 및 변경 버튼
		nicknameField = new JTextField(10);
		changeNicknameButton = new JButton("닉네임 변경");

		// 참여자 목록
		participantListModel = new DefaultListModel<>();
		participantList = new JList<>(participantListModel);
		JScrollPane participantScrollPane = new JScrollPane(participantList);

		// 레이아웃 설정
		setLayout(new BorderLayout());

		// 상단 패널 (닉네임 입력 및 변경 버튼)
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(new JLabel("닉네임: "));
		topPanel.add(nicknameField);
		topPanel.add(changeNicknameButton);
		add(topPanel, BorderLayout.NORTH);

		// 왼쪽 패널 (방 목록)
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.add(roomScrollPane, BorderLayout.CENTER);
		westPanel.add(createRoomButton, BorderLayout.SOUTH);

		// 오른쪽 패널 (참여자 목록)
		JPanel eastPanel = new JPanel(new BorderLayout());
		eastPanel.add(new JLabel("참여자 목록"), BorderLayout.NORTH);
		eastPanel.add(participantScrollPane, BorderLayout.CENTER);

		// 가운데 패널 (채팅창)
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(chatScrollPane, BorderLayout.CENTER);

		// 아래 패널 (채팅창 입력)
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(messageField, BorderLayout.CENTER);
		bottomPanel.add(sendButton, BorderLayout.EAST);
		centerPanel.add(bottomPanel, BorderLayout.SOUTH);

		// JSplitPane으로 화면 분할
		// 1. 왼쪽 (방 목록)과 가운데+오른쪽 (채팅창 + 참여자 목록) 분할
		JSplitPane leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPanel, centerPanel);
		leftRightSplitPane.setDividerLocation(200); // 방 목록의 초기 크기 설정

		// 2. 가운데 (채팅창)과 오른쪽 (참여자 목록) 분할
		JSplitPane centerEastSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftRightSplitPane, eastPanel);
		centerEastSplitPane.setDividerLocation(600); // 채팅창과 참여자 목록의 초기 크기 설정

		add(centerEastSplitPane, BorderLayout.CENTER); // 전체 레이아웃에 추가

		// 서버 연결
		connectToServer();

		// 이벤트 리스너 추가
		sendButton.addActionListener(e -> sendMessage());				// 전송 버튼
		messageField.addActionListener(e -> sendMessage());				// 채팅창 전송
		createRoomButton.addActionListener(e -> createRoom());			// 방 생성 버튼
		changeNicknameButton.addActionListener(e -> changeNickname());	// 닉네임 변경 버튼
		roomList.addMouseListener(new MouseAdapter() {					// 방 리스트 클릭
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String selectedRoom = roomList.getSelectedValue();
					if (selectedRoom != null) {
						joinRoom(selectedRoom);
					}
				}
			};
		});
	}

	private void connectToServer() {
		try {
			socket = new Socket("localhost", 8010);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


			// 서버로부터 자동 할당된 닉네임 받기
            String response = in.readLine();
            if (response.startsWith("NICKNAME_ASSIGNED")) {
            	String assignedNickname = response.split(" ")[1];
            	nicknameField.setText(assignedNickname);
            	chatArea.append("서버에 연결되었습니다. 할당된 닉네임: " + assignedNickname + "\n");
            }
			
            // 서버로부터 메시지를 받는 쓰레드 시작
            new Thread(this::receiveMessages).start();
			
            // 기존 방 목록 요청
            out.println("/rooms");
		} catch (IOException e) {
			e.printStackTrace();
			chatArea.append("서버 연결 실패: " + e.getMessage() + "\n");
		}
	}

	// 메세지 전송
	private void sendMessage() {

	}

	// 방 만들기
	private void createRoom() {

	}

	// 방 참여하기
	private void joinRoom(String roomName) {

	}

	// 닉네임 변경하기
	private void changeNickname() {
		String newNickname = nicknameField.getText().trim();
        if (!newNickname.isEmpty()) {
            out.println("/nick " + newNickname);
        } else {
            JOptionPane.showMessageDialog
            	(this, "닉네임을 입력해주세요.",
            	"오류", JOptionPane.ERROR_MESSAGE);
        }
	}

	// 메세지 받기
	private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
            	// 닉네임 변경 메시지 처리
            	final String finalMessage = message;
                SwingUtilities.invokeLater(() -> {
                	if (finalMessage.startsWith("NICKNAME_CHANGED")) { // 닉네임 변경 성공
                		String newNickname = finalMessage.split(" ")[1];
                		nicknameField.setText(newNickname);
                	} else if (finalMessage.equals("NICKNAME_TAKEN")) { // 닉네임 중복
                		JOptionPane.showMessageDialog
                			(this, "닉네임이 이미 사용 중입니다. 다른 닉네임을 선택해주세요.",
                			"닉네임 중복", JOptionPane.WARNING_MESSAGE);
                	} else if (finalMessage.equals("INVALID_NICKNAME")) { // 유효하지 않은 닉네임
                		JOptionPane.showMessageDialog
                			(this, "유효하지 않은 닉네임입니다. Guest로 시작하지 않는 닉네임을 입력해주세요.",
                			"오류", JOptionPane.ERROR_MESSAGE);
                	} else if(finalMessage.contains("의 닉네임이") && finalMessage.contains("으로 변경되었습니다.")) { // 다른 사용자의 닉네임 변경 메시지 처리
                		String[] parts = finalMessage.split("의 닉네임이");
                        String oldNick = parts[0];
                        String newNick = parts[1].split("으로 변경되었습니다.")[0].trim();
                        if (nicknameField.getText().equals(oldNick)) {
                            nicknameField.setText(newNick);
                        }
                        chatArea.append(finalMessage + "\n");
                	} else if (finalMessage.startsWith("PARTICIPANTS")) {
                		
                	} else {
                        chatArea.append(finalMessage + "\n");
                	}
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> chatArea.append("서버와의 연결이 끊어졌습니다.\n"));
        }
    }

	// 방 리스트 업데이트
	private void updateRoomList(String[] rooms) {

	}

	// 참여자 리스트 업데이트
	private void updateParticipantList(String[] participants) {

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			MultiChatClient client = new MultiChatClient();
			client.setVisible(true);
		});
	}
}
