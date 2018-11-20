package progetto_2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GUI {
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
    private JButton decodificaButton;
    private JButton generaPrimoButton;
    private JButton generaRSAButton;
    private JComboBox sharesBox;
    private JComboBox sharesMinBox;
    private JButton pulisciKeyringButton;
    private JList filesList;
    private JButton generaFirmaButton;
    private JOptionPane text;

    private File codificaFile = null;

    private ArrayList<Identifier> identifiers = new ArrayList<>();


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

        dimRSASelect.setMaximumRowCount(2);
        dimRSASelect.setModel(new DefaultComboBoxModel<>(new String[]{"1024", "2048"}));

        DSASelect.setMaximumRowCount(3);
        DSASelect.setModel(new DefaultComboBoxModel<>(new String[]{"SHA1withDSA", "SHA224withDSA", "SHA256withDSA"}));

        sharesBox.setMaximumRowCount(7);
        sharesBox.setModel(new DefaultComboBoxModel<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8",}));
        sharesBox.setSelectedIndex(4);

        sharesMinBox.setMaximumRowCount(7);
        sharesMinBox.setModel(new DefaultComboBoxModel<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8",}));
        sharesMinBox.setSelectedIndex(2);

        identifiers.addAll(MessageShare.getInstance().getIdentifiers());
        Collections.sort(identifiers);
        Collections.reverse(identifiers);
        filesList.setListData(identifiers.toArray());

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
                JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                fc.setSelectedFile(new File("helloworld.txt"));


                int returnValue = fc.showOpenDialog(null);
                // int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    codificaFile = fc.getSelectedFile();
                    System.out.println(codificaFile.getAbsolutePath());
                }
            }
        });
        decodificaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                File decodificaFile = new File("tempDecodifica");
                if (filesList.getSelectedIndex() == -1) {
                    return;
                }
                Identifier id = identifiers.get(filesList.getSelectedIndex());
                MessageShare.getInstance().rebuildFile(id, decodificaFile);
                // decodifica

                JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                fc.setSelectedFile(new File("decodificato.txt"));


                int returnValue = fc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File destinationFile = fc.getSelectedFile();
                    boolean result = NewFile.decodifica(id.getNome(), decodificaFile, destinationFile);
                    if (result == true)
                        JOptionPane.showMessageDialog(null, "Messaggio corretto");
                    else {
                        destinationFile.delete();
                        JOptionPane.showMessageDialog(null, "Messaggio non corretto");
                    }
                }


            }
        });

        generaPrimoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // genera primo
                SharesRing.getInstance();

            }
        });
        generaRSAButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //genera rsa
                KeyRing kr = NewFile.createKeyPair((byte) dimRSASelect.getSelectedIndex(), "RSA", null);
                kr.saveShamir(mittenteText.getText(), destinatarioText.getText(), sharesMinBox.getSelectedIndex() + 1, sharesBox.getSelectedIndex() + 1);

            }
        });
        sharesBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int n = sharesBox.getSelectedIndex();
                String[] s = new String[n + 1];
                for (int i = 0; i <= n; i++) {
                    s[i] = "" + (i + 1);
                }
                sharesMinBox.setMaximumRowCount(n + 1);
                sharesMinBox.setModel(new DefaultComboBoxModel<>(s));
                sharesMinBox.setSelectedIndex(n);

            }
        });
        mittenteText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '#') {
                    e.consume();
                }
                super.keyTyped(e);
            }
        });
        destinatarioText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '#') {
                    e.consume();
                }
                super.keyTyped(e);
            }
        });
        dimDSASelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dimDSASelect.getSelectedIndex() == 1) {
                    DSASelect.setMaximumRowCount(3);
                    DSASelect.setModel(new DefaultComboBoxModel<>(new String[]{"SHA224withDSA", "SHA256withDSA"}));
                } else {

                    DSASelect.setMaximumRowCount(3);
                    DSASelect.setModel(new DefaultComboBoxModel<>(new String[]{"SHA1withDSA", "SHA224withDSA", "SHA256withDSA"}));
                }
            }
        });
        pulisciKeyringButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.deleteData();
            }
        });
        generaFirmaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyRing kr = NewFile.createKeyPair((byte) dimDSASelect.getSelectedIndex(), "DSA", null);
                kr.saveShamir(mittenteText.getText(), destinatarioText.getText(), sharesMinBox.getSelectedIndex() + 1, sharesBox.getSelectedIndex() + 1);
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
        File destinationFile = new File("tempCodifica");
        String mittente = mittenteText.getText();
        String destinatario = destinatarioText.getText();
        byte cifrario_m = (byte) codificaMSelect.getSelectedIndex();
        byte cifrario_k_dim = (byte) dimRSASelect.getSelectedIndex();

        byte padding = (byte) paddingRSASelect.getSelectedIndex();
        byte integrita;
        byte type;
        byte dimFirma = 0x08;
        byte modi_operativi = (byte) modiSelect.getSelectedIndex();
        if (firmaRadio.isSelected()) {
            integrita = 0x00;
            type = (byte) (DSASelect.getSelectedIndex() + 8 + dimDSASelect.getSelectedIndex());
            dimFirma = (byte) dimDSASelect.getSelectedIndex();
            CodificaSign f = new CodificaSign(mittente.replace(Const.MESSAGESEPARATOR, ""), destinatario.replace(Const.MESSAGESEPARATOR, ""), cifrario_m,
                    cifrario_k_dim, padding, integrita, modi_operativi, type, dimFirma, codificaFile, destinationFile, sharesMinBox.getSelectedIndex() + 1, sharesBox.getSelectedIndex() + 1);


        } else if (MACRadio.isSelected()) {
            integrita = 0x01;
            type = (byte) (macSelect.getSelectedIndex() + 5);
            CodificaMAC f = new CodificaMAC(mittente.replace(Const.MESSAGESEPARATOR, ""), destinatario.replace(Const.MESSAGESEPARATOR, ""), cifrario_m,
                    cifrario_k_dim, padding, integrita, modi_operativi, type, dimFirma, codificaFile, destinationFile, sharesMinBox.getSelectedIndex() + 1, sharesBox.getSelectedIndex() + 1);

        } else {
            integrita = 0x02;
            type = (byte) hashSelect.getSelectedIndex();
            CodificaHash f = new CodificaHash(mittente.replace(Const.MESSAGESEPARATOR, ""), destinatario.replace(Const.MESSAGESEPARATOR, ""), cifrario_m,
                    cifrario_k_dim, padding, integrita, modi_operativi, type, dimFirma, codificaFile, destinationFile, sharesMinBox.getSelectedIndex() + 1, sharesBox.getSelectedIndex() + 1);

        }
        identifiers.clear();
        identifiers.addAll(MessageShare.getInstance().getIdentifiers());
        Collections.sort(identifiers);
        Collections.reverse(identifiers);
        filesList.setListData(identifiers.toArray());
    }


    public static void main(String args[]) throws IOException {

        Utils.createServers();
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            /*for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }*/
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
                JFrame app = new JFrame("Codifica");
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
        panelMain.setLayout(new BorderLayout(0, 0));
        tabbedPane1 = new JTabbedPane();
        panelMain.add(tabbedPane1, BorderLayout.CENTER);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        tabbedPane1.addTab("Codifica", panel1);
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
        label2.setText("Hash");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label2, gbc);
        hashSelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(hashSelect, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("MAC");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label3, gbc);
        macSelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(macSelect, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Modo Operativo");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label4, gbc);
        modiSelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(modiSelect, gbc);
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
        fileButton = new JButton();
        fileButton.setText("Scegli File");
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(fileButton, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer5, gbc);
        codificaButton = new JButton();
        codificaButton.setText("Codifica");
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(codificaButton, gbc);
        paddingRSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(paddingRSASelect, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Padding RSA");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label5, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Firma");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label6, gbc);
        DSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(DSASelect, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        tabbedPane1.addTab("Decodifica", panel2);
        decodificaButton = new JButton();
        decodificaButton.setText("Decodifica");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(decodificaButton, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel2.add(spacer6, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 2.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(spacer7, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(scrollPane1, gbc);
        filesList = new JList();
        scrollPane1.setViewportView(filesList);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        tabbedPane1.addTab("Configurazione", panel3);
        generaPrimoButton = new JButton();
        generaPrimoButton.setText("Genera primo");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(generaPrimoButton, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.01;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(spacer8, gbc);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel3.add(spacer9, gbc);
        dimRSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(dimRSASelect, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Dimensione RSA");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label7, gbc);
        generaRSAButton = new JButton();
        generaRSAButton.setText("Genera RSA");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(generaRSAButton, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("Dimensione Firma");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label8, gbc);
        dimDSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(dimDSASelect, gbc);
        generaFirmaButton = new JButton();
        generaFirmaButton.setText("Genera Firma");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(generaFirmaButton, gbc);
        pulisciKeyringButton = new JButton();
        pulisciKeyringButton.setText("Svuota Keyring");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(pulisciKeyringButton, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        panelMain.add(panel4, BorderLayout.SOUTH);
        final JLabel label9 = new JLabel();
        label9.setText("Numero di share");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(label9, gbc);
        final JPanel spacer10 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(spacer10, gbc);
        final JPanel spacer11 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel4.add(spacer11, gbc);
        sharesBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(sharesBox, gbc);
        final JLabel label10 = new JLabel();
        label10.setText("Numero di share minime");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(label10, gbc);
        sharesMinBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(sharesMinBox, gbc);
        destinatarioText = new JTextField();
        destinatarioText.setText("Destinatario");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(destinatarioText, gbc);
        mittenteText = new JTextField();
        mittenteText.setText("Mittente");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(mittenteText, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
