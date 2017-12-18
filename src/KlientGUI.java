import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class KlientGUI extends JFrame {
    private JProgressBar progressBar;
    private JButton pokracovatButton;
    private JLabel popisProgressBar;
    private JLabel pocetSoketovLabel;
    private JButton zrusitButton;
    private JSpinner pocetSoketovSpinner;
    private JTextField ipAdresaTextField;
    private JButton spustitButton;
    private JButton prerusitButton;
    public JPanel panel;


    public KlientGUI() {

        progressBar.setMinimum(0);
        progressBar.setMaximum((int) new File(Server.SUBOR_NA_ODOSLANIE).length());
        progressBar.setValue(0);

        pocetSoketovSpinner.setValue(3);

        progressBar.setValue(nastavProgressbar());

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {

                System.out.println("Zacinam s progress barom");
                while (Klient.pocitadlo.getCount() >= 0) {
                    int[] prijateByty = Klient.prijateByty;
                    if(prijateByty == null) continue;

                    int sum = 0;
                    for (int i = 0; i < prijateByty.length; i++) {
                        sum += prijateByty[i];
                    }
                    publish(sum);
                }
                return null;
            }

            protected void process(List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                System.out.println("Done.");
            }
        };


        spustitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Klient.spusti(new int[(int) pocetSoketovSpinner.getValue()]);

                    worker.execute();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        zrusitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Klient.zrusit();
            }
        });

        prerusitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Klient.prerus();
            }
        });

        pokracovatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Klient.pokracuj((int) pocetSoketovSpinner.getValue());

                    worker.execute();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void changeFont(Component component, Font font) {
        component.setFont(font);
        if (component instanceof Container) {
            for (Component child : ((Container)component).getComponents()) {
                changeFont(child, font);
            }
        }
    }

    public static int nastavProgressbar() {
        Scanner scanner = null;
        int sucet = 0;
        File file = new File("subor.txt");
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextInt()) {
                sucet += scanner.nextInt();
            }} catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            scanner.close();
        }

        return sucet;
    }


}
