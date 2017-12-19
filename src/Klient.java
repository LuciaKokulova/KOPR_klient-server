import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Lucia Kokulova
 */
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
        System.out.println("Klient sa pripája na IP: " + socket.getInetAddress() + ", port: " + socket.getPort());
    }

    public static void spusti(int[] offsetyZGUI) throws IOException {
        System.out.println("Začína sa posielať súbor od offsetov: " + Arrays.toString(offsetyZGUI));
        offsety = offsetyZGUI;
        int pocetTCPSpojeni = offsety.length;

        System.out.println("Posielam serveru správu " + 0 + ", počet vlákien je " + pocetTCPSpojeni);
        dosKomunikacia.writeInt(0);
        dosKomunikacia.writeInt(pocetTCPSpojeni);
        dosKomunikacia.flush();

        DataInputStream dis = new DataInputStream(socket.getInputStream());

        int velkostSuboru = dis.readInt();
        int port = dis.readInt();
        int prvyPort = dis.readInt();
        int velkostNazvu = dis.readInt();

        System.out.println("Súbor ma veľkosť " + velkostSuboru + ", komunikuje sa na porte " + port + ", prvý port na prenos je " + prvyPort);

        nazov = dis.readUTF();
        cesta = "C:\\Users\\lucka\\Desktop\\Nový priečinok\\" + nazov;

        System.out.println("Názov súboru je " + nazov + ", cesta k nemu je " + cesta);

        pocitadlo = new CountDownLatch(pocetTCPSpojeni);
        prijateByty = new int[pocetTCPSpojeni];

        try {
            File file = new File(cesta);
            int velkostCasti = (int) Math.ceil((double) velkostSuboru/pocetTCPSpojeni);
            executor = Executors.newFixedThreadPool(pocetTCPSpojeni);

            System.out.println("Veľkosť jednej posielanej časti je " + velkostCasti);

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
            System.out.println("V prerušení zapisujem do súboru offsety " + Arrays.toString(offsety));
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

        System.out.println("Pokračujem posielať s offsetmi " + Arrays.toString(offsety));

        Scanner scanner = null;
        File file = new File("subor.txt");

        if (!file.exists()) {
            file.createNewFile();
        }

        try {
            scanner = new Scanner(file);
            for (int i = 0; i < offsety.length; i++) {
                offsety[i] = scanner.nextInt();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
//        } catch (NoSuchElementException e2) {
//            System.err.println("Ak chceš pokračovať v sťahovaní musíš najprv nejaké začať :-)");

        }
        finally {
            scanner.close();
        }

        System.out.println("Posielam správu " + 1 + ", počet vlákien je " + pocet);
        dosKomunikacia.writeInt(1);
        dosKomunikacia.writeInt(pocet);
        for (int i = 0; i < offsety.length; i++) {
            dosKomunikacia.writeInt(offsety[i]);
        }
         System.out.println("Posielam z klienta offsety " + Arrays.toString(offsety) + " a spúšťam prijímanie súboru.");
        dosKomunikacia.flush();

        spusti(offsety);
    }

    public static void zrusit() {
        executor.shutdownNow();
//        try {
            System.out.println("Mažem súbor " + cesta + " a " + "subor.txt");

            File file = new File(cesta);
            //System.out.println(cesta);
            file.delete();

            File file2 = new File("subor.txt");
            file2.delete();

        for (int i = 0; i < Klient.prijateByty.length; i++) {
            Klient.prijateByty[i] = 0;
        }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

}
