package progetto_2;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GUI {
    final int BUFFER = 8;
    private JTabbedPane tabbedPane1;
    private ButtonGroup buttonGroup1;
    private JComboBox codificaMSelect;
    private JComboBox hashSelect;
    private JComboBox macSelect;
    private JComboBox DSASelect;
    private JComboBox modiSelect;
    private JComboBox paddingRSASelect;
    private JComboBox dimRSASelect;
    private JRadioButton firmaRadio;
    private JTextField mittenteText;
    private JTextField destinatarioText;
    private JButton fileButton;
    private JButton codificaButton;
    private JComboBox dimDSASelect;
    private JRadioButton MACRadio;
    private JRadioButton HashRadio;
    private JPanel panelMain;

    private File codificaFile = null;

    public GUI() {


        buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(firmaRadio);
        firmaRadio.setSelected(true);
        buttonGroup1.add(MACRadio);
        buttonGroup1.add(HashRadio);


        codificaMSelect.setMaximumRowCount(3);
        codificaMSelect.setModel(new DefaultComboBoxModel<>(new String[]{"AES", "DES", "DESede"}));

        modiSelect.setMaximumRowCount(3);
        modiSelect.setModel(new DefaultComboBoxModel<>(new String[]{"ECB", "CBC", "CFB"}));

        dimRSASelect.setMaximumRowCount(2);
        dimRSASelect.setModel(new DefaultComboBoxModel<>(new String[]{"1024", "2048"}));

        paddingRSASelect.setMaximumRowCount(2);
        paddingRSASelect.setModel(new DefaultComboBoxModel<>(new String[]{"PKCS1", "OAEP"}));

        hashSelect.setMaximumRowCount(5);
        hashSelect.setModel(new DefaultComboBoxModel<>(new String[]{"SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512"}));
        hashSelect.setEnabled(false);

        macSelect.setMaximumRowCount(3);
        macSelect.setModel(new DefaultComboBoxModel<>(new String[]{"MD5", "SHA-256", "SHA-384"}));
        macSelect.setEnabled(false);

        dimDSASelect.setMaximumRowCount(2);
        dimDSASelect.setModel(new DefaultComboBoxModel<>(new String[]{"1024", "2048"}));

        DSASelect.setMaximumRowCount(3);
        DSASelect.setModel(new DefaultComboBoxModel<>(new String[]{"SHA1withDSA", "SHA224withDSA", "SHA256withDSA"}));


        firmaRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableFirma();

            }
        });
        HashRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableHash();

            }
        });
        MACRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableMac();

            }
        });
        codificaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codificaActionPerformed(e);
            }
        });
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();


                int returnValue = fc.showOpenDialog(null);
                // int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    codificaFile = fc.getSelectedFile();
                    System.out.println(codificaFile.getAbsolutePath());
                }
            }
        });
    }

    private void enableFirma() {
        DSASelect.setEnabled(true);
        dimDSASelect.setEnabled(true);
        hashSelect.setEnabled(false);
        macSelect.setEnabled(false);
    }

    private void enableMac() {
        DSASelect.setEnabled(false);
        dimDSASelect.setEnabled(false);
        hashSelect.setEnabled(false);
        macSelect.setEnabled(true);
    }

    private void enableHash() {
        DSASelect.setEnabled(false);
        dimDSASelect.setEnabled(false);
        hashSelect.setEnabled(true);
        macSelect.setEnabled(false);
    }

    private void codificaActionPerformed(ActionEvent evt) {
        if (codificaFile == null) {
            return;
        }
        String mittente = mittenteText.getText();
        String destinatario = destinatarioText.getText();
        byte cifrario_m = (byte) codificaMSelect.getSelectedIndex();
        byte cifrario_k_dim = (byte) dimRSASelect.getSelectedIndex();
        byte padding = (byte) paddingRSASelect.getSelectedIndex();
        byte integrita;
        byte hash = 0x08;
        byte mac = 0x08;
        byte firma = 0x08;
        byte dimFirma = 0x08;
        if (firmaRadio.isSelected()) {
            integrita = 0x00;
            hash = (byte) DSASelect.getSelectedIndex();
            hash = (byte) dimDSASelect.getSelectedIndex();

        } else if (MACRadio.isSelected()) {
            integrita = 0x01;
            hash = (byte) macSelect.getSelectedIndex();
        } else {
            integrita = 0x02;
            hash = (byte) hashSelect.getSelectedIndex();
        }
        byte modi_operativi = (byte) modiSelect.getSelectedIndex();


        System.out.println(mittente + " " + destinatario + " " + cifrario_m);
        NewFile f = new NewFile(mittente, destinatario, cifrario_m,
                cifrario_k_dim, padding, integrita, null, modi_operativi, null, hash, mac, firma, dimFirma, null);
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(1024);
            KeyPair k = gen.generateKeyPair();
            Key publickey = k.getPublic();



            f.codifica(publickey, codificaFile);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame app = new JFrame("App");
                app.setContentPane(new GUI().panelMain);
                app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                app.pack();
                app.setVisible(true);
            }
        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelMain = new JPanel();
        panelMain.setLayout(new CardLayout(0, 0));
        tabbedPane1 = new JTabbedPane();
        panelMain.add(tabbedPane1, "Card1");
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        tabbedPane1.addTab("Untitled", panel1);
        final JLabel label1 = new JLabel();
        label1.setText("Codifica Messaggio");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel1.add(label1, gbc);
        codificaMSelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(codificaMSelect, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Dimensione RSA");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label2, gbc);
        dimRSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(dimRSASelect, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Hash");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label3, gbc);
        hashSelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(hashSelect, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("MAC");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Firma");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label5, gbc);
        macSelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(macSelect, gbc);
        DSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(DSASelect, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Modo Operativo");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label6, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Padding RSA");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label7, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("Dimensione Firma");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label8, gbc);
        modiSelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(modiSelect, gbc);
        paddingRSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(paddingRSASelect, gbc);
        dimDSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(dimDSASelect, gbc);
        firmaRadio = new JRadioButton();
        firmaRadio.setText("Firma Digitale");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(firmaRadio, gbc);
        MACRadio = new JRadioButton();
        MACRadio.setText("MAC");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(MACRadio, gbc);
        HashRadio = new JRadioButton();
        HashRadio.setText("Hash");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(HashRadio, gbc);
        mittenteText = new JTextField();
        mittenteText.setText("Mittente");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(mittenteText, gbc);
        destinatarioText = new JTextField();
        destinatarioText.setText("Destinatario");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(destinatarioText, gbc);
        fileButton = new JButton();
        fileButton.setText("File");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(fileButton, gbc);
        codificaButton = new JButton();
        codificaButton.setText("Codifica");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(codificaButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
