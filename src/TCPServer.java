import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author Lucia Kokulova
 */
public class TCPServer implements Runnable {
    private int id;
    private File subor;
    private int port;
    private Socket socket;
    private int chunkSize = 3 * 4096;
    private int[] offset;
    private int pocetTCPSpojeni;

    public TCPServer(int pocetTCPSpojeni, int[] offset, int id, File subor, int port) throws FileNotFoundException {
        this.pocetTCPSpojeni = pocetTCPSpojeni;
        this.offset = offset;
        this.id = id;
        this.subor = subor;
        this.port = port;

    }

    @Override
    public void run() {
        ServerSocket serverovskySoket = null;

        try {
            serverovskySoket = new ServerSocket(port);

            Socket socket = serverovskySoket.accept();
            System.out.println("Server počúva na porte: " + port);
            RandomAccessFile raf = new RandomAccessFile(subor, "r");

            OutputStream os = socket.getOutputStream();
            int dlzkaCasti = (int) Math.ceil((double) subor.length() / pocetTCPSpojeni);

            int pocetPaketov = (int) Math.ceil((double) (dlzkaCasti - offset[id]) / chunkSize);

            byte[] data = new byte[chunkSize];
            raf.seek(offset[id] + id * dlzkaCasti);
            for (int i = 0; i < pocetPaketov; i++) {
                int read = raf.read(data, 0, data.length);
                if (read <= 0) {
                    break;
                }
                os.write(data, 0, read);
            }
            System.out.println("Vlákno " + id + " dokončilo sťahovanie. ");
            os.flush();

        } catch (SocketException se) {
            System.err.println("Vlakno bolo prerusene " + id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverovskySoket != null) {
                try {
                    System.out.println("zatvaram serverovsky " + port);
                    serverovskySoket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
