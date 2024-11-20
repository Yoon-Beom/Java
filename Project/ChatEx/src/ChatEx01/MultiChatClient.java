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
	private JButton leaveButton; // 나가기 버튼 추가

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

		// 나가기 버튼 생성
		leaveButton = new JButton("나가기");

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
		topPanel.add(nicknameField);		// 닉네임 입력창
		topPanel.add(changeNicknameButton);	// 닉네임 변경 버튼
		add(topPanel, BorderLayout.NORTH);

		// 왼쪽 패널 (방 목록)
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.add(roomScrollPane, BorderLayout.CENTER);  // 채팅창 목록
		westPanel.add(createRoomButton, BorderLayout.SOUTH); // 방 만들기 버튼

		// 오른쪽 패널 (참여자 목록)
		JPanel eastPanel = new JPanel(new BorderLayout());
		eastPanel.add(new JLabel("참여자 목록"), BorderLayout.NORTH); // 참여자 목록 라벨
		eastPanel.add(participantScrollPane, BorderLayout.CENTER);  // 참여자 목록
		eastPanel.add(leaveButton, BorderLayout.SOUTH); // 나가기 버튼

		// 가운데 패널 (채팅창)
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(chatScrollPane, BorderLayout.CENTER);  // 채팅창

		// 아래 패널 (채팅창 입력)
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(messageField, BorderLayout.CENTER);		// 메세지 입력창
		bottomPanel.add(sendButton, BorderLayout.EAST);			// 전송 버튼
		centerPanel.add(bottomPanel, BorderLayout.SOUTH);		// 가운데 패널에 바닥 패널 밑에 붙이기

		// JSplitPane으로 화면 분할
		// 1. 왼쪽 (방 목록)과 가운데+오른쪽 (채팅창 + 참여자 목록) 분할
		JSplitPane leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPanel, centerPanel);
		leftRightSplitPane.setDividerLocation(200); // 방 목록의 초기 크기 설정

		// 2. 가운데 (채팅창)과 오른쪽 (참여자 목록) 분할
		JSplitPane centerEastSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftRightSplitPane, eastPanel);
		centerEastSplitPane.setDividerLocation(600); // 채팅창과 참여자 목록의 초기 크기 설정

		add(centerEastSplitPane, BorderLayout.CENTER); // 전체 레이아웃에 추가
	}

	// 서버 연결
	private void connectToServer() {

	}

	// 메세지 전송
	private void sendMessage() {

	}

	// 방 만들기
	private void createRoom() {

	}

	// 닉네임 변경하기
	private void changeNickname() {

	}

	// 방 나가기
	private void leaveRoom() {

	}

	// 방 참여하기
	private void joinRoom(String selectedRoom) {

	}

	// 메세지 받기
	private void receiveMessages() {

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