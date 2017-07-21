package com.wiseclient.main;

import com.wiseclient.script.SaveScript;
import com.wiseclient.script.UserAction;
import com.wiseclient.script.UserActionInterface;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

public class ComputerClientFrame extends JFrame {
    private JLabel mImageLabel;
    private JPanel mMainPanel;
    private JButton jButtonSaveFile;
    private JTextArea jTextAreaShowScript;
    private JButton mConnectBtn;
    private JScrollPane mJScrollPane;
    private JPanel mJpanelBottomBar;

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

    public ComputerClientFrame() throws IOException {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setBounds(360, 20, 1000, 1000);
        this.setTitle("屏幕共享");

        mMainPanel = new JPanel();
        mMainPanel.setBounds(0, 0, 1000, 1000);
        mMainPanel.setLayout(null);

        mConnectBtn = new JButton("连接手机");
        mConnectBtn.setBounds(10, 10, 490, 20);
        mMainPanel.add(mConnectBtn);

        mImageLabel = new JLabel();
        mImageLabel.setBounds(10, 40, 490, 900);
        mImageLabel.setBackground(Color.BLUE);
        mMainPanel.add(mImageLabel);

        mMainPanel.add(createBottomBar());

        initSaveButton(mMainPanel);

        jTextAreaShowScript = new JTextArea();
        jTextAreaShowScript.setBounds(510, 40, 480, 930);
        jTextAreaShowScript.setLineWrap(true);
        jTextAreaShowScript.setWrapStyleWord(true);

        mJScrollPane = new JScrollPane(jTextAreaShowScript);
        mJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mJScrollPane.setBounds(510, 40, 480, 930);

        mMainPanel.add(mJScrollPane);

        this.add(mMainPanel);

        mUserActionInterface = new UserAction(jTextAreaShowScript);

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


        mImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                int x = mouseEvent.getX();
                int y = mouseEvent.getY();
                try {
                    int calcX = calcXInDisplay(x);
                    int calcY = calcYInDisplay(y);

                    writer.write("DOWN" + calcX + "#" + calcY);
                    writer.newLine();
                    writer.write("UP" + calcX + "#" + calcY);
                    writer.newLine();
                    writer.flush();
                    mUserActionInterface.actionViewClick(calcX, calcY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
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
                        mUserActionInterface.actionViewMove(mMoveOldX, mMoveOldY, mMoveNewX, mMoveNewY);
                    }
                    isMove = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mImageLabel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
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
        });
    }

    private void initSaveButton(JPanel mainPanel) {
        jButtonSaveFile = new JButton();
        jButtonSaveFile.setText("保存脚本");
        jButtonSaveFile.setBounds(510, 10, 480, 20);
        jButtonSaveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showSaveDialog(null);
                if (JFileChooser.APPROVE_OPTION == result) {
                    File file = fileChooser.getSelectedFile();
                    System.out.println(file.getAbsolutePath());
                    String scriptContent = jTextAreaShowScript.getText();
                    SaveScript.saveFile(file, scriptContent);
                }
            }
        });
        mainPanel.add(jButtonSaveFile);
    }

    private int calcXInDisplay(int input) {
        float result = mDisplayX * (input * 1.0f / mImageLabel.getWidth());
        return (int) result;
    }

    private int calcYInDisplay(int input) {
        float result = mDisplayY * (input * 1.0f / mImageLabel.getHeight());
        return (int) result;
    }

    private JPanel createBottomBar() {
        mJpanelBottomBar = new JPanel(new GridLayout(1, 3));
        mJpanelBottomBar.setBounds(10, 950, 490, 20);

        JButton menu = new JButton("MENU");
        JButton home = new JButton("HOME");
        JButton back = new JButton("BACK");

        mJpanelBottomBar.add(menu);
        mJpanelBottomBar.add(home);
        mJpanelBottomBar.add(back);

        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("MENU");
                    writer.newLine();
                    writer.flush();
                    mUserActionInterface.actionMenuPress();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        home.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("HOME");
                    writer.newLine();
                    writer.flush();
                    mUserActionInterface.actionHomePress();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("BACK");
                    writer.newLine();
                    writer.flush();
                    mUserActionInterface.actionBackPress();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return mJpanelBottomBar;
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
        if (isVertical) {
            System.out.println("screen change to vertical");
            mConnectBtn.setBounds(10, 10, 490, 20);
            mImageLabel.setBounds(10, 40, 490, 900);
            mJpanelBottomBar.setBounds(10, 950, 490, 20);
            jButtonSaveFile.setBounds(510, 10, 480, 20);
            mJScrollPane.setBounds(510, 40, 480, 930);
        } else {
            System.out.println("screen change to landscape");
            mConnectBtn.setBounds(10, 10, 980, 20);
            mImageLabel.setBounds(10, 40, 980, 450);
            mJpanelBottomBar.setBounds(10, 500, 980, 20);
            jButtonSaveFile.setBounds(10, 530, 980, 20);
            mJScrollPane.setBounds(10, 560, 980, 400);
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
}

