package ChatEx03;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient3 {
   
   private Socket socket;
   private BufferedReader br;
   private PrintWriter pw;
   private Scanner sc;
   
   
   public void go() throws Exception {
      try {
         socket = new Socket("localhost", 9002);
         br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         pw = new PrintWriter(socket.getOutputStream(), true);
         sc = new Scanner(System.in);
         
         ReceiverWorker rw = new ReceiverWorker();
         Thread th = new Thread(rw);
         th.setDaemon(true);
         th.start();
         System.out.println("** ChatClient 가 서버에 접속 **");
         while(true) {
            String message = sc.nextLine();
            pw.println(message);
            
            if(message.trim().equals("종료")) {
               System.out.println("** ChatClient 종료합니다.");
               break;
            }
               
         }
         
      }catch(Exception e){
         
      }finally {
         clossAll();
      }
   }
   
   
   class ReceiverWorker implements Runnable{

      @Override
      public void run() {
         try {
            receiveMessage();
         }catch(Exception e) {
            
         }
         
      }
      
      
      public void receiveMessage() throws Exception{
         while(true) {
            String message = br.readLine();
            if(message == null) {
               break;
            }
            System.out.println(message);
            System.out.println("서버에 보낼 메세지 >> ");
         }
      }
      
   }

   public static void main(String[] args) {
      ChatClient3 client = new ChatClient3();
      try {
         client.go();
      }catch(Exception e) {
         
      }

   }
   
   public void clossAll() throws Exception{
      if(pw != null)
         pw.close();
   }

}
