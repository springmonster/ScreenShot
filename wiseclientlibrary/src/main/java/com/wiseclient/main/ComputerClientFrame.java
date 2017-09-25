package com.wiseclient.main;

import com.wiseclient.script.SaveScript;
import com.wiseclient.script.UserAction;
import com.wiseclient.script.UserActionInterface;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

public class ComputerClientFrame extends JFrame {
    private JLabel mImageLabel;
    private JPanel mMainPanel;
    private JButton mJButtonSaveFile;
    private JTextArea mJTextAreaShowScript;
    private JButton mConnectBtn;
    private JScrollPane mJScrollPane;
    private JLabel mJLabelBottomMenu;
    private JLabel mJLabelBottomHome;
    private JLabel mJLabelBottomBack;

    private boolean isMove = false;
    private BufferedWriter writer;
    private int mDisplayX;
    private int mDisplayY;
    private int mDisplayOldX;
    private int mDisplayOldY;

    private UserActionInterface mUserActionInterface;

    private int mMoveOldX;
    private int mMoveOldY;
    private int mMoveNewX;
    private int mMoveNewY;

    private boolean isScriptShow = false;
    private boolean isGVertical = true;

    public ComputerClientFrame() throws IOException {
        createComputerClientFrame();

        createMainPanel();

        createConnectBtn();

        createImageLabel();

        createBottomBar();

        createSaveButton();

        createScriptPanel();

        setScriptViewsVisible(false);

        this.add(mMainPanel);

        mUserActionInterface = new UserAction(mJTextAreaShowScript);

        mMainPanel.addKeyListener(new LabelMouseKeyListener());
        mainPanelRequestFocus();
    }

    private void setScriptViewsVisible(boolean b) {
        if (isGVertical) {
            if (b) {
                this.setBounds(360, 20, 1000, 1000);
            } else {
                this.setBounds(360, 20, 500, 1000);
            }
        } else {
            if (b) {
                this.setBounds(360, 20, 1000, 1000);
            } else {
                this.setBounds(360, 20, 1000, 500);
            }
        }
        isScriptShow = b;
        mMainPanel.updateUI();
    }

    private void createComputerClientFrame() {
        createJMenu();

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setBounds(360, 20, 1000, 1000);
        this.setTitle("屏幕共享");
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("window closing");
                CommandInstall.execExitAppProcessCommand();
                super.windowClosing(e);
            }
        });
    }

    private void createJMenu() {
        JMenuBar jMenuBar = new JMenuBar();
        final JMenu jMenu = new JMenu("脚本");
        final JMenuItem jMenuItem = new JMenuItem("显示");
        this.setJMenuBar(jMenuBar);
        jMenuBar.add(jMenu);
        jMenu.add(jMenuItem);
        jMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (mJScrollPane != null) {
                    if (jMenuItem.getText().equals("显示")) {
                        jMenuItem.setText("隐藏");
                        setScriptViewsVisible(true);
                    } else if (jMenuItem.getText().equals("隐藏")) {
                        jMenuItem.setText("显示");
                        setScriptViewsVisible(false);
                    }
                }
            }
        });
    }

    private void createScriptPanel() {
        mJTextAreaShowScript = new JTextArea();
        mJTextAreaShowScript.setBounds(510, 20, 490, 960);
        mJTextAreaShowScript.setLineWrap(true);
        mJTextAreaShowScript.setWrapStyleWord(true);

        mJScrollPane = new JScrollPane(mJTextAreaShowScript);
        mJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mJScrollPane.setBounds(510, 20, 490, 960);

        mMainPanel.add(mJScrollPane);
    }

    private void createMainPanel() {
        mMainPanel = new JPanel();
        mMainPanel.setBounds(0, 0, 1000, 1000);
        mMainPanel.setLayout(null);
        mMainPanel.setBackground(new Color(220, 240, 250));
    }

    private void createImageLabel() {
        mImageLabel = new JLabel();
        mImageLabel.setBackground(Color.BLACK);
        mImageLabel.setOpaque(true);
        mImageLabel.setBounds(0, 20, 510, 920);
        mImageLabel.addMouseListener(new LabelMouseClickListener());
        mImageLabel.addMouseMotionListener(new LabelMouseMotionListener());
        mMainPanel.add(mImageLabel);
    }

    private void createConnectBtn() {
        mConnectBtn = new JButton("连接手机");
        mConnectBtn.setBounds(0, 0, 510, 20);
        mConnectBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    startSocket("127.0.0.1", "9999");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        mMainPanel.add(mConnectBtn);
    }

    private void createSaveButton() {
        mJButtonSaveFile = new JButton();
        mJButtonSaveFile.setText("保存脚本");
        mJButtonSaveFile.setBounds(510, 0, 490, 20);
        mJButtonSaveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showSaveDialog(null);
                if (JFileChooser.APPROVE_OPTION == result) {
                    File file = fileChooser.getSelectedFile();
                    System.out.println(file.getAbsolutePath());
                    String scriptContent = mJTextAreaShowScript.getText();
                    SaveScript.saveFile(file, scriptContent);
                }
            }
        });
        mMainPanel.add(mJButtonSaveFile);
    }

    private int calcXInDisplay(int input) {
        float result = mDisplayX * (input * 1.0f / mImageLabel.getWidth());
        return (int) result;
    }

    private int calcYInDisplay(int input) {
        float result = mDisplayY * (input * 1.0f / mImageLabel.getHeight());
        return (int) result;
    }

    private void createBottomBar() throws IOException {
        File file = new File("");
        String path = file.getAbsolutePath();

        System.out.println("current path is " + path);

        ImageIcon menuImageIcon = new ImageIcon(ImageIO.read(new File(path + "/wiseclientlibrary/images/menu.png")));
        ImageIcon homeImageIcon = new ImageIcon(ImageIO.read(new File(path + "/wiseclientlibrary/images/home.png")));
        ImageIcon backImageIcon = new ImageIcon(ImageIO.read(new File(path + "/wiseclientlibrary/images/back.png")));

        mJLabelBottomMenu = new JLabel(menuImageIcon);
        mJLabelBottomMenu.setBackground(Color.BLACK);
        mJLabelBottomMenu.setOpaque(true);
        mJLabelBottomMenu.setBounds(0, 940, 170, 40);

        mJLabelBottomHome = new JLabel(homeImageIcon);
        mJLabelBottomHome.setBackground(Color.BLACK);
        mJLabelBottomHome.setOpaque(true);
        mJLabelBottomHome.setBounds(170, 940, 170, 40);

        mJLabelBottomBack = new JLabel(backImageIcon);
        mJLabelBottomBack.setBackground(Color.BLACK);
        mJLabelBottomBack.setOpaque(true);
        mJLabelBottomBack.setBounds(340, 940, 170, 40);

        mMainPanel.add(mJLabelBottomMenu);
        mMainPanel.add(mJLabelBottomHome);
        mMainPanel.add(mJLabelBottomBack);

        mJLabelBottomMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("MENU");
                    writer.newLine();
                    writer.flush();
                    if (mUserActionInterface != null) {
                        mUserActionInterface.actionMenuPress();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mJLabelBottomHome.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("HOME");
                    writer.newLine();
                    writer.flush();
                    if (mUserActionInterface != null) {
                        mUserActionInterface.actionHomePress();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mJLabelBottomBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("BACK");
                    writer.newLine();
                    writer.flush();
                    if (mUserActionInterface != null) {
                        mUserActionInterface.actionBackPress();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startSocket(final String ip, final String port) throws IOException {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Socket socket = new Socket(ip, Integer.parseInt(port));
                    BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
                    DataInputStream dataInputStream = new DataInputStream(inputStream);
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    byte[] bytes = null;
                    while (true) {
                        mDisplayX = dataInputStream.readInt();
                        mDisplayY = dataInputStream.readInt();
                        if (mDisplayOldX != mDisplayX && mDisplayOldY != mDisplayY) {
                            // 如果为机顶盒的应用，则mDisplayX是大于mDisplayY的，需要改变成为横屏状态
                            if (mDisplayOldX == 0 && mDisplayOldY == 0) {
                                if (mDisplayX > mDisplayY) {
                                    changeDisplayOrientation(false);
                                }
                            }
                            if (mDisplayOldX < mDisplayOldY && mDisplayX > mDisplayY) {
                                //此时由竖屏状态改变为横屏状态
                                changeDisplayOrientation(false);
                            } else if (mDisplayOldX > mDisplayOldY && mDisplayX < mDisplayY) {
                                //此时由横屏状态改变为竖屏状态
                                changeDisplayOrientation(true);
                            }
                            mDisplayOldX = mDisplayX;
                            mDisplayOldY = mDisplayY;
                        }

                        int length = dataInputStream.readInt();

                        if (bytes == null) {
                            bytes = new byte[length];
                        }
                        if (bytes.length < length) {
                            bytes = new byte[length];
                        }
                        int read = 0;
                        while ((read < length)) {
                            read += inputStream.read(bytes, read, length - read);
                        }
                        InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                        Image image = ImageIO.read(byteArrayInputStream);
                        mImageLabel.setIcon(new ScaleIcon(new ImageIcon(image)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void changeDisplayOrientation(boolean isVertical) {
        isGVertical = isVertical;

        if (isVertical) {
            System.out.println("screen change to vertical");
            mConnectBtn.setBounds(0, 0, 510, 20);
            mImageLabel.setBounds(0, 20, 510, 920);
            mJLabelBottomMenu.setBounds(0, 940, 170, 40);
            mJLabelBottomHome.setBounds(170, 940, 170, 40);
            mJLabelBottomBack.setBounds(340, 940, 170, 40);

            mJButtonSaveFile.setBounds(510, 0, 490, 20);
            mJScrollPane.setBounds(510, 20, 490, 960);

            if (isScriptShow) {
                this.setBounds(360, 20, 1000, 1000);
            } else {
                this.setBounds(360, 20, 500, 1000);
            }
        } else {
            System.out.println("screen change to landscape");
            mConnectBtn.setBounds(0, 0, 1000, 20);
            mImageLabel.setBounds(0, 20, 1000, 450);
            mJLabelBottomMenu.setBounds(0, 470, 333, 40);
            mJLabelBottomHome.setBounds(333, 470, 333, 40);
            mJLabelBottomBack.setBounds(666, 470, 334, 40);

            mJButtonSaveFile.setBounds(0, 510, 1000, 20);
            mJScrollPane.setBounds(0, 530, 1000, 450);

            if (isScriptShow) {
                this.setBounds(360, 20, 1000, 1000);
            } else {
                this.setBounds(360, 20, 1000, 500);
            }
        }
        mMainPanel.validate();
        mMainPanel.repaint();
    }

    public static void main(String[] args) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CommandInstall.installDex();
            }
        }).start();

        new ComputerClientFrame().setVisible(true);
    }

    class LabelMouseClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            mainPanelRequestFocus();

            super.mouseClicked(mouseEvent);
            try {
                int x = mouseEvent.getX();
                int y = mouseEvent.getY();
                int calcX = calcXInDisplay(x);
                int calcY = calcYInDisplay(y);

                writer.write("DOWN" + calcX + "#" + calcY);
                writer.newLine();
                writer.write("UP" + calcX + "#" + calcY);
                writer.newLine();
                writer.flush();
                if (mUserActionInterface != null) {
                    mUserActionInterface.actionViewClick(calcX, calcY);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            mainPanelRequestFocus();

            super.mouseReleased(mouseEvent);
            try {
                int x = mouseEvent.getX();
                int y = mouseEvent.getY();
                writer.write("UP" + (calcXInDisplay(x)) + "#" + (calcYInDisplay(y)));
                writer.newLine();
                writer.flush();
                if (isMove) {
                    mMoveNewX = calcXInDisplay(x);
                    mMoveNewY = calcYInDisplay(y);
                    if (mUserActionInterface != null) {
                        mUserActionInterface.actionViewMove(mMoveOldX, mMoveOldY, mMoveNewX, mMoveNewY);
                    }
                }
                isMove = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class LabelMouseMotionListener extends MouseAdapter {
        @Override
        public void mouseDragged(MouseEvent mouseEvent) {
            mainPanelRequestFocus();

            super.mouseDragged(mouseEvent);
            try {
                int x = mouseEvent.getX();
                int y = mouseEvent.getY();
                if (!isMove) {
                    isMove = true;
                    writer.write("DOWN" + (calcXInDisplay(x)) + "#" + (calcYInDisplay(y)));
                    mMoveOldX = calcXInDisplay(x);
                    mMoveOldY = calcYInDisplay(y);
                    System.out.println("move down x " + calcXInDisplay(x));
                } else {
                    writer.write("MOVE" + (calcXInDisplay(x)) + "#" + (calcYInDisplay(y)));
                    System.out.println("move move x " + calcXInDisplay(x));
                }
                writer.newLine();
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void mainPanelRequestFocus() {
        mMainPanel.setFocusable(true);
        mMainPanel.requestFocus();
    }

    class LabelMouseKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            try {
                System.out.println("key pressed " + e.getKeyCode());
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_UP) {
                    writer.write("KEY_UP");
                    if (mUserActionInterface != null) {
                        mUserActionInterface.actionKeyUpPress();
                    }
                } else if (code == KeyEvent.VK_DOWN) {
                    writer.write("KEY_DOWN");
                    if (mUserActionInterface != null) {
                        mUserActionInterface.actionKeyDownPress();
                    }
                } else if (code == KeyEvent.VK_LEFT) {
                    writer.write("KEY_LEFT");
                    if (mUserActionInterface != null) {
                        mUserActionInterface.actionKeyLeftPress();
                    }
                } else if (code == KeyEvent.VK_RIGHT) {
                    writer.write("KEY_RIGHT");
                    if (mUserActionInterface != null) {
                        mUserActionInterface.actionKeyRightPress();
                    }
                } else if (code == KeyEvent.VK_ENTER) {
                    writer.write("KEY_ENTER");
                    if (mUserActionInterface != null) {
                        mUserActionInterface.actionKeyEnterPress();
                    }
                } else if (code == KeyEvent.VK_ESCAPE) {
                    writer.write("KEY_ESC");
                    if (mUserActionInterface != null) {
                        mUserActionInterface.actionKeyBackPress();
                    }
                }

                writer.newLine();
                writer.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            System.out.println("key pressed " + e.getKeyCode());
        }
    }
}

