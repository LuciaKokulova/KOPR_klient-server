import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * @author Lucia Kokulova
 */
public class TCPKlient implements Runnable {
    private int port;
    private File file;
    private int velkostCasti;
    private int i;
    private int offsety[];
    private int[] prijateByty;


    public TCPKlient( int i, File file, int velkostCasti, int port, int[] offsety, int[] prijateByty) {
        this.i = i;
        this.file = file;
        this.velkostCasti = velkostCasti;
        this.port = port;
        this.offsety = offsety;
        this.prijateByty = prijateByty;
    }

    @Override
    public void run() {
        int precitaneByty = 0;
        try {
            Socket socket = new Socket(Server.SERVER_IP, port);
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            System.out.println("Klient sa pripája na port: " + port);

            InputStream is = socket.getInputStream();

            int velkostChunku = 3*4096;
            byte[] poleBytov = new byte[velkostChunku];

            raf.seek(offsety[i] + i * velkostCasti);

            while ((precitaneByty = is.read(poleBytov, 0, poleBytov.length)) > 0) {

                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                raf.write(poleBytov, 0, precitaneByty);

                offsety[i] += precitaneByty;
                prijateByty[i] = offsety[i];
            }

            Klient.pocitadlo.countDown();

            if (Klient.pocitadlo.getCount() == 0) {
                System.out.println("Koniec vsetkych posielajucich tcp spojeni.");
            }
            raf.close();
            socket.close();

        } catch (SocketException s) {

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
