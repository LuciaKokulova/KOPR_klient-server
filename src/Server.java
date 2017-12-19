import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Lucia Kokulova
 */
public class Server {
    public final static int SERVER_PORT = 5555;
    public final static int INITIAL_PORT = 7000;
    public static final String SERVER_IP = "127.0.0.1";
    public static final String
            SUBOR_NA_ODOSLANIE = "C:\\Users\\lucka\\Desktop\\film.avi";

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

        System.out.println("Odoslané dáta klientovi: veľkosť súboru: " + velkostSuboru + ", server port: " + SERVER_PORT +
                ", prvý port: " + INITIAL_PORT + ", súbor: " + SUBOR_NA_ODOSLANIE);

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
        System.out.println("Čakam klienta na porte: " + SERVER_PORT);
        while (true) {
            Socket accept = serverSocket.accept();
            System.out.println("Pripojil sa klient na IP: " + accept.getInetAddress() + " a porte: " + accept.getPort() + ".");

            dos = new DataOutputStream(accept.getOutputStream());
            DataInputStream disKomunikacia = new DataInputStream(accept.getInputStream());
            System.out.println("Začínam komunikáciu.");

            while (accept.isConnected()) {
                try {

                    int sprava = disKomunikacia.readInt();
                    System.out.println("Správa od klienta: \"" + sprava + "\"");

                    if (sprava == 0) {
                        System.out.println("Začínam posielať súbor od začiatku.");
                        int pocetTCPspojeni = disKomunikacia.readInt();
                        System.out.println("Budem posielať súbor cez: " + pocetTCPspojeni + " TCP spojení.");

                        sendFile(new int[pocetTCPspojeni]);
                    }

                    if (sprava == 1) {
                        System.out.println("Pokračujem v posielaní súboru.");
                        int pocetTCPspojeni = disKomunikacia.readInt();
                        System.out.println("Znovu posielam cez: " + pocetTCPspojeni + " TCP spojení.");
                        int[] offsety = new int[pocetTCPspojeni];
                        for (int i = 0; i < pocetTCPspojeni; i++) {
                            offsety[i] = disKomunikacia.readInt();
                        }
                        System.out.println("Posielam s offsetmi: " + Arrays.toString(offsety));
                        sendFile(offsety);
                    }

                } catch (SocketException e) {

                }
            }
            //System.out.println("server closed");
            // serverSocket.close();
        }
    }

}
