package ChatEx03;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer3 {
   
   private ArrayList<ServerWorker> list = new ArrayList<ServerWorker>();
   
   
   class ServerWorker implements Runnable{
      
      private Socket socket;
      private BufferedReader br;
      private PrintWriter pw;
      private String user;
      
      
      public ServerWorker(Socket socket) {
         this.socket = socket;
         user = socket.getInetAddress().toString();
      }
      
      public void chatting() throws Exception {
         br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         pw = new PrintWriter(socket.getOutputStream(), true);
         sendMessage(user + " 닙이 입장하셨습니다.");
         
         try {
            while(true) {
               String message = br.readLine();
               if(message.trim().equals("종료") || message.equals("null") || message == null){
                  break;
               }
               sendMessage(user + "님 : " + message);
            }
         }catch(Exception e) {
            
         }
         
         
      }
      

      @Override
      public void run() {
         try {
            chatting();
         }catch(Exception e) {
            
         }finally {
            try {
               closeAll();
            }catch(Exception e) {
               
            }
            sendMessage(user + "님이 나가셨습니다.");
            list.remove(this);
         }
      }
      
      
      public void closeAll() throws Exception{
         if(pw != null)
            pw.close();
         if(br != null)
            br.close();
         if(socket != null) {
            socket.close();
         }
      }
      
   }
   
   
   public void go() throws Exception {
      ServerSocket serverSocket = null;
      
      try {
         // 채팅 서버 시작
         serverSocket = new ServerSocket(9002);
         System.out.println("다중 채팅 준비");
         
         // 다수의 클라이언트에게 지속적으로 서비스를 하기 위해 while 문 사용
         while(true) {
            Socket socket = serverSocket.accept();
            ServerWorker sw = new ServerWorker(socket);
            list.add(sw);
            Thread th = new Thread(sw);
            th.start();
         }
         
         
      }catch(Exception e) {
         
      }finally {
         if(serverSocket != null)
            serverSocket.close();
         System.out.println("## chat을 종료 합니다");
      }
      
      
   }
   
   
   public void sendMessage(String message) {
      System.err.println(message);
      for(int i=0; i<list.size(); i++) {
         list.get(i).pw.println(message);
      }
   }
   

   public static void main(String[] args) throws Exception {
      new ChatServer3().go();
   }

}
