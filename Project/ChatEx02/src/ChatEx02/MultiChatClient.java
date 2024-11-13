package ChatEx02;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MultiChatClient extends JFrame {
	private JTextArea chatArea;
	private JTextField messageField;
	private JButton sendButton;
	private JList<String> roomList;
	private DefaultListModel<String> roomListModel;
	private JButton createRoomButton;
	private JTextField nicknameField;
	private JButton changeNicknameButton; // 새로 추가: 닉네임 변경 버튼
	private JList<String> participantList; // 새로 추가: 참여자 목록
	private DefaultListModel<String> participantListModel; // 새로 추가: 참여자 목록 모델

	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;

	public MultiChatClient() {
		setTitle("다중 채팅방");
		setSize(800, 600); // 변경: 창 크기 증가
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 채팅 영역
		chatArea = new JTextArea();
		chatArea.setEditable(false);
		JScrollPane chatScrollPane = new JScrollPane(chatArea);

		// 메시지 입력 필드와 전송 버튼
		messageField = new JTextField();
		sendButton = new JButton("전송");

		// 채팅방 목록
		roomListModel = new DefaultListModel<>();
		roomList = new JList<>(roomListModel);
		JScrollPane roomScrollPane = new JScrollPane(roomList);

		// 채팅방 생성 버튼
		createRoomButton = new JButton("방 만들기");

		// 닉네임 입력 필드 및 변경 버튼
		nicknameField = new JTextField(10); // 변경: 필드 크기 증가
		changeNicknameButton = new JButton("닉네임 변경"); // 새로 추가

		// 참여자 목록
		participantListModel = new DefaultListModel<>(); // 새로 추가
		participantList = new JList<>(participantListModel); // 새로 추가
		JScrollPane participantScrollPane = new JScrollPane(participantList); // 새로 추가

		// 레이아웃 설정
		setLayout(new BorderLayout());

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

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(messageField, BorderLayout.CENTER);
		bottomPanel.add(sendButton, BorderLayout.EAST);
		centerPanel.add(bottomPanel, BorderLayout.SOUTH);

		// 상단 패널 (닉네임 입력 및 변경 버튼)
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(new JLabel("닉네임: "));
		topPanel.add(nicknameField);
		topPanel.add(changeNicknameButton);

		add(topPanel, BorderLayout.NORTH);
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
		sendButton.addActionListener(e -> sendMessage());
		messageField.addActionListener(e -> sendMessage());
		createRoomButton.addActionListener(e -> createRoom());
		changeNicknameButton.addActionListener(e -> changeNickname()); // 새로 추가
		roomList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					String selectedRoom = roomList.getSelectedValue();
					if (selectedRoom != null) {
						joinRoom(selectedRoom);
					}
				}
			}
		});
	}

	private void connectToServer() {
		try {
			socket = new Socket("localhost", 5000);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// 닉네임 전송
			String nickname = nicknameField.getText();
			out.println(nickname);

			// 서버로부터 메시지를 받는 쓰레드 시작
			new Thread(this::receiveMessages).start();

			// 기존 방 목록 요청
			out.println("/rooms"); // 새로 추가: 방 목록 요청
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage() {
		String message = messageField.getText();
		if (!message.isEmpty()) {
			out.println(message);
			messageField.setText("");
		}
	}

	private void createRoom() {
		String roomName = JOptionPane.showInputDialog(this, "방 이름을 입력하세요:");
		if (roomName != null && !roomName.trim().isEmpty()) {
			out.println("/create " + roomName);
		}
	}

	private void joinRoom(String roomName) {
		out.println("/join " + roomName);
	}

	// 새로 추가: 닉네임 변경 메서드
	private void changeNickname() {
		String newNickname = nicknameField.getText().trim();
		if (!newNickname.isEmpty()) {
			out.println("/nick " + newNickname);
		}
	}

	private void receiveMessages() {
		try {
			String message;
			while ((message = in.readLine()) != null) {
				if (message.startsWith("/roomlist ")) {
					// 방 목록 업데이트
					updateRoomList(message.substring(10).split(","));
				} else if (message.startsWith("/participants ")) {
					// 참여자 목록 업데이트
					updateParticipantList(message.substring(14).split(","));
				} else {
					// 일반 메시지
					final String finalMessage = message;
					SwingUtilities.invokeLater(() -> chatArea.append(finalMessage + "\n"));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 새로 추가: 방 목록 업데이트 메서드
	private void updateRoomList(String[] rooms) {
		SwingUtilities.invokeLater(() -> {
			roomListModel.clear();
			for (String room : rooms) {
				if (!room.trim().isEmpty()) {
					roomListModel.addElement(room.trim());
				}
			}
		});
	}

	// 새로 추가: 참여자 목록 업데이트 메서드
	private void updateParticipantList(String[] participants) {
		SwingUtilities.invokeLater(() -> {
			participantListModel.clear();
			for (String participant : participants) {
				if (!participant.trim().isEmpty()) {
					participantListModel.addElement(participant.trim());
				}
			}
		});
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			MultiChatClient client = new MultiChatClient();
			client.setVisible(true);
		});
	}
}