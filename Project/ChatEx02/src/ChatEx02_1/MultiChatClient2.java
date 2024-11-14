package ChatEx02_1;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MultiChatClient2 extends JFrame {
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

	public MultiChatClient2() {
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
		sendButton.addActionListener(e -> sendMessage());
		messageField.addActionListener(e -> sendMessage());
		createRoomButton.addActionListener(e -> createRoom());
		changeNicknameButton.addActionListener(e -> changeNickname());
		roomList.addMouseListener(new MouseAdapter() {
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

	// 메세지 전송
	private void sendMessage() {
		String message = messageField.getText();
		if (!message.isEmpty()) {
			out.println(message);
			messageField.setText("");
		}
	}

	// 방 만들기
	private void createRoom() {
		String roomName = JOptionPane.showInputDialog(this, "방 이름을 입력하세요:");
		if (roomName != null && !roomName.trim().isEmpty()) {
			out.println("/create " + roomName);
		}
	}

	// 방 참여하기
	private void joinRoom(String roomName) {
		out.println("/join " + roomName);
	}

	// 닉네임 변경하기
	private void changeNickname() {
		String newNickname = nicknameField.getText().trim();
		if (!newNickname.isEmpty()) {
			out.println("/nick " + newNickname);
		}
	}

	// 메세지 받기
	private void receiveMessages() {
		try {
			String message;
			while ((message = in.readLine()) != null) {
				if (message.startsWith("/roomlist")) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 방 리스트 업데이트
	private void updateRoomList(String[] rooms) {
		SwingUtilities.invokeLater(() -> {
			roomListModel.clear();
			for(String room : rooms) {
				if(!room.trim().isEmpty()) {
					roomListModel.addElement(room.trim());
				}
			}
		});
	}

	// 참여자 리스트 업데이트
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
			MultiChatClient2 client = new MultiChatClient2();
			client.setVisible(true);
		});
	}
}
