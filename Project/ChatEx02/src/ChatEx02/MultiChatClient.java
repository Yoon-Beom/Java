package ChatEx02;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class MultiChatClient extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private JButton createRoomButton;
    private JTextField nicknameField;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public MultiChatClient() {
    	
    	setTitle("다중 채팅방");
        setSize(600, 400);
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

        // 닉네임 입력 필드
        nicknameField = new JTextField("닉네임");

        // 레이아웃 설정
        setLayout(new BorderLayout());

        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.add(roomScrollPane, BorderLayout.CENTER);
        westPanel.add(createRoomButton, BorderLayout.SOUTH);
        add(westPanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("닉네임: "));
        topPanel.add(nicknameField);
        add(topPanel, BorderLayout.NORTH);

        // 서버 연결
        connectToServer();

        // 이벤트 리스너 추가
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        createRoomButton.addActionListener(e -> createRoom());
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
            roomListModel.addElement(roomName);
        }
    }

    private void joinRoom(String roomName) {
        out.println("/join " + roomName);
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String finalMessage = message;
                SwingUtilities.invokeLater(() -> chatArea.append(finalMessage + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MultiChatClient client = new MultiChatClient();
            client.setVisible(true);
        });
    }
}