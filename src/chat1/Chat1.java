package chat1;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chat1 {
    private static HashMap<String, PrintWriter> usuarios = new HashMap<>();
    public static void main(String[] args) throws Exception {

        System.out.println("The chat server is running... ");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        } catch (Exception e) {
        }

    }
    private static class Handler implements Runnable {

        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
          public Handler(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                 while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null || name.toLowerCase().startsWith("quit") || name.length() < 4) {
                        continue;
                    }
                    synchronized (usuarios) {
                        if (!usuarios.containsKey(name)) {
                            out.println("NAMEACCEPTED " + name);
                            for (PrintWriter writer : usuarios.values()) {
                                writer.println("MESSAGE " + name + " has joined");
                            }
                            usuarios.put(name, out);
                            break;
                        }
                    }
                }
                while (true) {
                    String input = in.nextLine();
                    if (input.startsWith("/")) {
                        if (input.toLowerCase().startsWith("/quit")) {
                            return;
                        } else {
                            try {
                                int separador = input.substring(1).indexOf(" ");
                                String address = input.substring(1, separador + 1);
                                String mess = input.substring(1).substring(separador + 1);

                                usuarios.get(address).println("MESSAGE " + name + " a " + address + ": " + mess);
                                usuarios.get(name).println("MESSAGE " + name + " a " + address + ": " + mess);
                            } catch (Exception e) {
                                System.err.println(e);
                            }
                        }
                    } else {
                        for (PrintWriter writer : usuarios.values()) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
            } finally {
                if (out != null && name != null) {
                    System.err.println(name + " is leaving");
                    usuarios.remove(name, out);
                    for (PrintWriter writer : usuarios.values()) {
                        writer.println("MESSAGE " + name + " has left");
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
