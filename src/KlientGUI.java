import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * @author Lucia Kokulova
 */
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

        ipAdresaTextField.setText("127.0.0.1");

        pocetSoketovSpinner.setValue(3);

        //progressBar.setValue(nastavProgressbar());

        prerusitButton.setEnabled(false);
        zrusitButton.setEnabled(false);

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {

                System.out.println("Začínam s progress barom.");
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
                System.out.println("Koniec sťahovania.");
            }
        };


        spustitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    prerusitButton.setEnabled(true);
                    zrusitButton.setEnabled(true);
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
                spustitButton.setEnabled(true);
                prerusitButton.setEnabled(false);
                pokracovatButton.setEnabled(false);
                Klient.zrusit();
            }
        });

        prerusitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spustitButton.setEnabled(false);
                Klient.prerus();
            }
        });

        pokracovatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    spustitButton.setEnabled(false);
                    zrusitButton.setEnabled(true);
                    progressBar.setValue(nastavProgressbar());
                    Klient.pokracuj((int) pocetSoketovSpinner.getValue());
                    worker.execute();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we)
            {
                String ObjButtons[] = {"Yes","No"};
                int PromptResult = JOptionPane.showOptionDialog(null,"Are you sure you want to exit?","Online Examination System",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,ObjButtons,ObjButtons[1]);
                if(PromptResult==JOptionPane.YES_OPTION)
                {
                    System.exit(0);
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
        if (!file.exists()) {
            return 0;
        }
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextInt()) {
                sucet += scanner.nextInt();
            }
        } catch (FileNotFoundException e) {
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return sucet;
    }


}
