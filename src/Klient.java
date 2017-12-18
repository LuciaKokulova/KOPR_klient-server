import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Klient {
    public static ExecutorService executor;
    public static int[] offsety;
    public static DataOutputStream dosKomunikacia;
    public static Socket socket;
    public static CountDownLatch pocitadlo;
    public static int[] prijateByty;

    public static String cesta;
    public static String nazov;

    public static void main(String[] args) throws IOException {

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Klient");
            frame.setContentPane(new KlientGUI().panel);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Font font = new Font("Corbel", 0, 20);
            KlientGUI.changeFont(frame, font);
            frame.pack();
            frame.setVisible(true);

        });

        socket = new Socket(Server.SERVER_IP, Server.SERVER_PORT);
        dosKomunikacia = new DataOutputStream(socket.getOutputStream());
        System.out.println("Klient pripojeny na IP: " + socket.getInetAddress() + " ,porte: " + socket.getPort());
    }

    public static void spusti(int[] offsetyZGUI) throws IOException {
        System.out.println("Zacina sa posielat subor od zaciatku ....");
        offsety = offsetyZGUI;
        int pocetTCPSpojeni = offsety.length;

        System.out.println("Posielam serveru spravu " + 0 + ", pocetTCP " + pocetTCPSpojeni + " ....");
        dosKomunikacia.writeInt(0);
        dosKomunikacia.writeInt(pocetTCPSpojeni);
        dosKomunikacia.flush();

        DataInputStream dis = new DataInputStream(socket.getInputStream());

        int velkostSuboru = dis.readInt();
        int port = dis.readInt();
        int prvyPort = dis.readInt();
        int velkostNazvu = dis.readInt();

        System.out.println("Subor ma velkost " + velkostSuboru + ", posiela sa na porte " + port + ", prvy port je " + prvyPort + " ....");

        nazov = dis.readUTF();
        cesta = "C:\\Users\\lucka\\Desktop\\Nový priečinok\\" + nazov;

        System.out.println("Nazov suboru je " + nazov + ", cesta k nemu je " + cesta + " ....");

        pocitadlo = new CountDownLatch(pocetTCPSpojeni);
        prijateByty = new int[pocetTCPSpojeni];

        try {
            File file = new File(cesta);
            int velkostCasti = (int) Math.ceil((double) velkostSuboru/pocetTCPSpojeni);
            executor = Executors.newFixedThreadPool(pocetTCPSpojeni);

            System.out.println("Velkost casti je " + velkostCasti);

            for (int i = 0; i < pocetTCPSpojeni; i++) {
                executor.execute(new TCPKlient(i, file, velkostCasti, prvyPort + i , offsety, prijateByty));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        finally {
//            socket.close();
//        }
    }


    public static void prerus() {
        executor.shutdownNow();
        File file = new File("subor.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
            System.out.println("V preruseni zapisujem do suboru offsety " + Arrays.toString(offsety));
            for (int i = 0; i < offsety.length; i++) {
                pw.println(offsety[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pw.close();
        }
    }

    public static void pokracuj(int pocet) throws IOException {
        if (offsety == null) {
            offsety = new int[pocet];
        }

        System.out.println("Spustila sa metoda pokracuj s offsetmmi " + Arrays.toString(offsety));

        Scanner scanner = null;
        File file = new File("subor.txt");
        try {
            scanner = new Scanner(file);
            for (int i = 0; i < offsety.length; i++) {
                offsety[i] = scanner.nextInt();
            }
            System.out.println("Offsety su " + Arrays.toString(offsety));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e1) {
            e1.printStackTrace();
        } finally {
            scanner.close();
        }


        System.out.println("Posielam spravu " + 1 + ", pocet spojeni je " + pocet + " ....");
        dosKomunikacia.writeInt(1);
        dosKomunikacia.writeInt(pocet);
        for (int i = 0; i < offsety.length; i++) {
            dosKomunikacia.writeInt(offsety[i]);
        }
         System.out.println("posielam z klienta offsety " + Arrays.toString(offsety) + " a spustam metodu spusti....");
        dosKomunikacia.flush();

        spusti(offsety);
    }

    public static void zrusit() {
        executor.shutdownNow();
        System.out.println("Exekutor je vypnuty a mazem subor " + cesta);
        File file = new File(cesta);
        System.out.println(cesta);
        file.delete();
    }

}
