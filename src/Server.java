import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public final static int SERVER_PORT = 5555;
    public final static int INITIAL_PORT = 7000;
    public static final String SERVER_IP = "127.0.0.1";
    public static final String
            SUBOR_NA_ODOSLANIE = "C:\\Users\\lucka\\Desktop\\film.avi";
    public static ServerSocket[] serverovskeSokety;

    public static DataOutputStream dos;
    public static ExecutorService executor = null;

    public static void sendFile(int[] offsety) throws IOException {
        int pocetTCPspojeni = offsety.length;
        int velkostSuboru = (int) new File(SUBOR_NA_ODOSLANIE).length();
        dos.writeInt(velkostSuboru);
        dos.writeInt(SERVER_PORT);
        dos.writeInt(INITIAL_PORT);
        String subor = new File(SUBOR_NA_ODOSLANIE).getName();
        dos.writeInt(subor.length());
        dos.writeUTF(subor);

        dos.flush();

        System.out.println("Odoslane data: { Velkost suboru: " + velkostSuboru + " ,Server port: " + SERVER_PORT +
                " , Initial port: " + INITIAL_PORT + " nazov suboru: " + subor + " }");

        try {
            executor = Executors.newFixedThreadPool(pocetTCPspojeni);

            for (int i = 0; i < pocetTCPspojeni; i++) {
                executor.execute(new TCPServer(offsety.length, offsety, i, new File(SUBOR_NA_ODOSLANIE), Server.INITIAL_PORT + i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("Cakam klienta na porte: " + SERVER_PORT + " ....");
        while (true) {
            Socket accept = serverSocket.accept();
            System.out.println("Pripojil sa klient na ipecke: " + accept.getInetAddress() + " a porte: " + accept.getPort() + " ....");

            dos = new DataOutputStream(accept.getOutputStream());
            DataInputStream disKomunikacia = new DataInputStream(accept.getInputStream());
            System.out.println("Otvaram komunikaciu ....");

            while (accept.isConnected()) {
                try {

                    int sprava = disKomunikacia.readInt();
                    System.out.println("Sprava od klienta: \"" + sprava + "\"");

                    if (sprava == 0) {
                        System.out.println("Zacinam posielat subor od zaciatku ....");
                        int pocetTCPspojeni = disKomunikacia.readInt();
                        System.out.println("Budem posielat subor cez: " + pocetTCPspojeni + " tcp spojeni");

                        sendFile(new int[pocetTCPspojeni]);
                    }

                    if (sprava == 1) {
                        System.out.println("Pokracujem v posielani suboru");
                        int pocetTCPspojeni = disKomunikacia.readInt();
                        System.out.println("Budem pokracovat subor cez: " + pocetTCPspojeni + " tcp spojeni");
                        int[] offsety = new int[pocetTCPspojeni];
                        for (int i = 0; i < pocetTCPspojeni; i++) {
                            offsety[i] = disKomunikacia.readInt();
                        }
                        System.out.println("Odoslane offsety: " + Arrays.toString(offsety));
                        sendFile(offsety);
                    }

                } catch (SocketException e) {
                    //accept.close();
                    //executor.shutdownNow();
                }
            }
            //System.out.println("server closed");
            // serverSocket.close();
        }
    }

}
