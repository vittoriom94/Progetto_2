package progetto_2;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
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
    private JButton decFileChooseButton;
    private JButton decodificaButton;
    private JButton keyFileButton;
    private JButton keyFileDecodeButton;
    private JButton generaPrimoButton;
    private JButton generaRSAButton;
    private JComboBox sharesBox;
    private JComboBox sharesMinBox;

    private File codificaFile = null;
    private File decodificaFile = null;

    private File keyFileEncode = null;
    private File keyFileDecode = null;

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
        decFileChooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                fc.setSelectedFile(new File("codificato.txt"));


                int returnValue = fc.showOpenDialog(null);
                // int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    decodificaFile = fc.getSelectedFile();
                    System.out.println(decodificaFile.getAbsolutePath());
                }
            }
        });
        decodificaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (decodificaFile != null) {
                    // decodifica

                    JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                    fc.setSelectedFile(new File("decodificato.txt"));


                    int returnValue = fc.showSaveDialog(null);

                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File destinationFile = fc.getSelectedFile();
                        NewFile nf = new NewFile();
                        try {
                            nf.decodifica(decodificaFile, destinationFile);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchAlgorithmException e1) {
                            e1.printStackTrace();
                        } catch (InvalidKeyException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchPaddingException e1) {
                            e1.printStackTrace();
                        } catch (BadPaddingException e1) {
                            e1.printStackTrace();
                        } catch (InvalidKeySpecException e1) {
                            e1.printStackTrace();
                        } catch (IllegalBlockSizeException e1) {
                            e1.printStackTrace();
                        } catch (InvalidAlgorithmParameterException e1) {
                            e1.printStackTrace();
                        }
                    }


                }
            }
        });
        keyFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                fc.setSelectedFile(new File("chiavi.txt"));


                int returnValue = fc.showOpenDialog(null);
                // int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    keyFileEncode = fc.getSelectedFile();
                }
            }
        });
        keyFileDecodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
                fc.setSelectedFile(new File("chiaviPrivate.txt"));


                int returnValue = fc.showOpenDialog(null);
                // int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    keyFileDecode = fc.getSelectedFile();
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

                KeyRing kr = new KeyRing(Const.ANYMESSAGEID);
                KeyPairGenerator gen = null;
                try {
                    gen = KeyPairGenerator.getInstance("RSA");
                } catch (NoSuchAlgorithmException e1) {
                    throw new RuntimeException(e1);
                }

                gen.initialize(Match.dimensione.get((byte) dimRSASelect.getSelectedIndex()));
                KeyPair k = gen.generateKeyPair();
                Key publickey = k.getPublic();
                Key privatekey = k.getPrivate();


                kr.saveKey(publickey.getEncoded(), "RSA" + "Public");
                kr.saveKey(privatekey.getEncoded(), "RSA" + "Private");

                kr.saveShamir(sharesMinBox.getSelectedIndex() + 1, sharesBox.getSelectedIndex() + 1);

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
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setSelectedFile(new File("codificato.txt"));


        int returnValue = fc.showSaveDialog(null);

        if (codificaFile == null || returnValue != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destinationFile = fc.getSelectedFile();
        String mittente = mittenteText.getText();
        String destinatario = destinatarioText.getText();
        byte cifrario_m = (byte) codificaMSelect.getSelectedIndex();
        byte cifrario_k_dim = (byte) dimRSASelect.getSelectedIndex();

        byte padding = (byte) paddingRSASelect.getSelectedIndex();
        byte integrita;
        byte type;
        byte hash = 0x08;
        byte mac = 0x08;
        byte firma = 0x08;
        byte dimFirma = 0x08;
        if (firmaRadio.isSelected()) {
            integrita = 0x00;
            type = (byte) (DSASelect.getSelectedIndex() + 8);
            dimFirma = (byte) dimDSASelect.getSelectedIndex();

        } else if (MACRadio.isSelected()) {
            integrita = 0x01;
            type = (byte) (macSelect.getSelectedIndex() + 5);
        } else {
            integrita = 0x02;
            type = (byte) hashSelect.getSelectedIndex();
        }
        byte modi_operativi = (byte) modiSelect.getSelectedIndex();

        System.out.println(mittente + " " + destinatario + " " + cifrario_m);
        NewFile f = new NewFile(mittente.replace("#", ""), destinatario.replace("#", ""), cifrario_m,
                cifrario_k_dim, padding, integrita, null, modi_operativi, null, type, dimFirma, null);
        try {


            f.codifica(codificaFile, destinationFile);
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
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) throws IOException {
        Utils.createServers();

        //SharesRing.distribute();
        //System.out.println();



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
                JFrame app = new JFrame("App");
                app.setContentPane(new GUI().panelMain);
                app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                app.pack();
                app.setVisible(true);
            }
        });
        try {


            File f = new File("prova.txt");

            FileOutputStream fos = new FileOutputStream(f);
            if (!f.exists()) {
                f.createNewFile();
            }
            byte[] bytesArray = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x11, 0x12, 0x13, 0x14};

            fos.write(bytesArray);
            fos.flush();
            fos.close();


            int BUFFER = 4;
            FileInputStream fis = new FileInputStream(f);
            byte[] buffer = new byte[BUFFER]; //array contentente i byte letti a ogni iterazione
            int read = 0; //ignorare
            while ((read = fis.read(buffer)) > 0) {
                //effettua update
                String s = "";
                for (byte b : buffer) {
                    s = s + (char) b;
                }
                System.out.println(s);
            }
            //qui dovrebbe esserci il dofinal??
            fis.close();
        } catch (Exception e) {
        }
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
        final JLabel label4 = new JLabel();
        label4.setText("Firma");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label4, gbc);
        macSelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(macSelect, gbc);
        DSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(DSASelect, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Modo Operativo");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label5, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Dimensione Firma");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label6, gbc);
        modiSelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(modiSelect, gbc);
        dimDSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
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
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(mittenteText, gbc);
        destinatarioText = new JTextField();
        destinatarioText.setText("Destinatario");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(destinatarioText, gbc);
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
        keyFileButton = new JButton();
        keyFileButton.setText("File chiavi");
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(keyFileButton, gbc);
        paddingRSASelect = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(paddingRSASelect, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Padding RSA");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label7, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        tabbedPane1.addTab("Decodifica", panel2);
        decFileChooseButton = new JButton();
        decFileChooseButton.setText("Scegli File");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(decFileChooseButton, gbc);
        decodificaButton = new JButton();
        decodificaButton.setText("Decodifica");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(decodificaButton, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 2.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(spacer6, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel2.add(spacer7, gbc);
        keyFileDecodeButton = new JButton();
        keyFileDecodeButton.setText("File chiavi");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(keyFileDecodeButton, gbc);
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
        final JLabel label8 = new JLabel();
        label8.setText("Dimensione RSA");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label8, gbc);
        generaRSAButton = new JButton();
        generaRSAButton.setText("Genera RSA");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(generaRSAButton, gbc);
        final JLabel label9 = new JLabel();
        label9.setText("Numero di share");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label9, gbc);
        final JLabel label10 = new JLabel();
        label10.setText("Numero di share minime");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label10, gbc);
        sharesBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(sharesBox, gbc);
        sharesMinBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(sharesMinBox, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panelMain.add(panel4, BorderLayout.SOUTH);
        final JLabel label11 = new JLabel();
        label11.setText("GUI Pronta");
        panel4.add(label11, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
