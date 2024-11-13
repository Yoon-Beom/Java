package ChatEx01;

import javax.swing.*;
import java.awt.*;

public class ChatGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    public ChatGUI() {
        setTitle("1대1 채팅");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 채팅 내용을 표시할 영역
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // 메시지 입력 필드
        messageField = new JTextField();

        // 전송 버튼
        sendButton = new JButton("전송");

        // 레이아웃 설정
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatGUI chatGUI = new ChatGUI();
            chatGUI.setVisible(true);
        });
    }
}