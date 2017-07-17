package com.wiseclient.main;

import com.wiseclient.script.UserAction;
import com.wiseclient.script.UserActionInterface;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

public class ComputerClientFrame extends JFrame {
    private JLabel mImageLabel;
    private boolean isMove = false;
    private BufferedWriter writer;
    private int mDisplayX;
    private int mDisplayY;
    private UserActionInterface mUserActionInterface;
    private int mMoveOldX;
    private int mMoveOldY;
    private int mMoveNewX;
    private int mMoveNewY;

    public ComputerClientFrame() throws IOException {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setBounds(360, 20, 1000, 1000);
        this.setTitle("屏幕共享");

        JPanel mainPanel = new JPanel();
        mainPanel.setBounds(0, 0, 1000, 1000);
        mainPanel.setLayout(null);

        JButton connectBtn = new JButton("连接手机");
        connectBtn.setBounds(0, 10, 500, 20);
        mainPanel.add(connectBtn);

        mImageLabel = new JLabel();
        mImageLabel.setBounds(0, 30, 500, 910);
        mImageLabel.setBackground(Color.BLUE);
        mainPanel.add(mImageLabel);

        mainPanel.add(createBottomBar());

        JTextArea jTextAreaShowScript = new JTextArea();
        jTextAreaShowScript.setBounds(500, 10, 500, 950);
        jTextAreaShowScript.setLineWrap(true);
        jTextAreaShowScript.setWrapStyleWord(true);
        mainPanel.add(jTextAreaShowScript);

        this.add(mainPanel);

        mUserActionInterface = new UserAction(jTextAreaShowScript);

        connectBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    read("127.0.0.1", "9999");
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

    private int calcXInDisplay(int input) {
        float result = mDisplayX * (input * 1.0f / mImageLabel.getWidth());
        return (int) result;
    }

    private int calcYInDisplay(int input) {
        float result = mDisplayY * (input * 1.0f / mImageLabel.getHeight());
        return (int) result;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3));
        bar.setBounds(0, 950, 500, 20);

        JButton menu = new JButton("MENU");
        JButton home = new JButton("HOME");
        JButton back = new JButton("BACK");

        bar.add(menu);
        bar.add(home);
        bar.add(back);

        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                try {
                    writer.write("MENU");
                    writer.newLine();
                    writer.flush();
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
        return bar;
    }

    private void read(final String ip, final String port) throws IOException {
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

                        int length = readInt(inputStream);
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

    private int readInt(InputStream inputStream) throws IOException {
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        int b3 = inputStream.read();
        int b4 = inputStream.read();

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    public static void main(String[] args) throws IOException {
        new ComputerClientFrame().setVisible(true);
    }
}

