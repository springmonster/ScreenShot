package com.client.main

import com.client.script.UserAction
import com.client.script.UserActionInterface
import com.client.script.saveFile
import java.awt.Color
import java.awt.Toolkit
import java.awt.event.*
import java.io.*
import java.net.Socket
import javax.imageio.ImageIO
import javax.swing.*

/**
 * Created by kuanghaochuan on 2017/7/13.
 */
internal class ComputerClientFrame @Throws(IOException::class) constructor() : JFrame() {
    private val mScreenSize = Toolkit.getDefaultToolkit().screenSize
    private val mFrameHeight = (mScreenSize.getHeight() * 9 / 10).toInt()
    private val mFrameWidth = (mScreenSize.getHeight() * 9 / 10).toInt()

    private lateinit var mJMenuBar: JMenuBar
    private lateinit var mImageLabel: JLabel
    private lateinit var mMainPanel: JPanel
    private lateinit var mJTextAreaShowScript: JTextArea
    private lateinit var mJScrollPane: JScrollPane
    private lateinit var mJLabelBottomMenu: JLabel
    private lateinit var mJLabelBottomHome: JLabel
    private lateinit var mJLabelBottomBack: JLabel

    private var isMove = false
    private lateinit var writer: BufferedWriter
    private var mDisplayX: Int = 0
    private var mDisplayY: Int = 0
    private var mDisplayOldX: Int = 0
    private var mDisplayOldY: Int = 0

    private var mUserActionInterface: UserActionInterface

    private var mMoveOldX: Int = 0
    private var mMoveOldY: Int = 0
    private var mMoveNewX: Int = 0
    private var mMoveNewY: Int = 0

    private var isScriptShow = false
    private var isGVertical = true

    init {
        createComputerClientFrame()

        createMainPanel()

        createImageLabel()

        createBottomBar()

        createScriptPanel()

        setScriptViewsVisible(false)

        this.add(mMainPanel)

        mUserActionInterface = UserAction(mJTextAreaShowScript)

        mMainPanel.addKeyListener(LabelMouseKeyListener())
        mainPanelRequestFocus()
    }

    private fun setScriptViewsVisible(scriptViewsVisible: Boolean) {
        if (isGVertical) {
            if (scriptViewsVisible) {
                this.setBounds(450, 0, mFrameWidth, mFrameHeight)
            } else {
                this.setBounds(450, 0, mFrameWidth / 2, mFrameHeight)
            }
        } else {
            if (scriptViewsVisible) {
                this.setBounds(450, 0, mFrameWidth, mFrameHeight)
            } else {
                this.setBounds(450, 0, mFrameWidth, mFrameHeight / 2)
            }
        }
        isScriptShow = scriptViewsVisible
        mMainPanel.updateUI()
    }

    private fun createComputerClientFrame() {
        createJMenu()

        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        this.setBounds(450, 0, mFrameWidth, mFrameHeight)
        this.title = "Screen Share"
        this.isResizable = false
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                println("window closing")
                execExitAppProcessCommand()
                super.windowClosing(e)
            }
        })
    }

    private fun createJMenu() {
        mJMenuBar = JMenuBar()

        val phoneJMenu = JMenu("Phone")
        val prepareJMenuItem = JMenuItem("Prepare")
        val connectJMenuItem = JMenuItem("Connect phone")

        val scriptJMenu = JMenu("Script")
        val disJMenuItem = JMenuItem("Show script")
        val saveJMenuItem = JMenuItem("Save script")

        this.jMenuBar = mJMenuBar
        mJMenuBar.add(phoneJMenu)
        mJMenuBar.add(scriptJMenu)

        phoneJMenu.add(prepareJMenuItem)
        phoneJMenu.add(connectJMenuItem)

        scriptJMenu.add(disJMenuItem)
        scriptJMenu.add(saveJMenuItem)

        prepareJMenuItem.addActionListener {
            Thread {
                installDex()
            }.start()
        }
        connectJMenuItem.addActionListener { startSocket("127.0.0.1", "3000") }

        disJMenuItem.addActionListener {
            if (disJMenuItem.text == "Show script") {
                disJMenuItem.text = "Hide script"
                setScriptViewsVisible(true)
            } else if (disJMenuItem.text == "Hide script") {
                disJMenuItem.text = "Show script"
                setScriptViewsVisible(false)
            }
        }

        saveJMenuItem.addActionListener {
            val fileChooser = JFileChooser()
            val result = fileChooser.showSaveDialog(null)
            if (JFileChooser.APPROVE_OPTION == result) {
                val file = fileChooser.selectedFile
                println(file.absolutePath)
                val scriptContent = mJTextAreaShowScript.text
                saveFile(file, scriptContent)
            }
        }
    }

    private fun createScriptPanel() {
        mJTextAreaShowScript = JTextArea()
        mJTextAreaShowScript.setBounds(mFrameWidth / 2, 0, mFrameWidth / 2, mFrameHeight)
        mJTextAreaShowScript.lineWrap = true
        mJTextAreaShowScript.wrapStyleWord = true

        mJScrollPane = JScrollPane(mJTextAreaShowScript)
        mJScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        mJScrollPane.setBounds(mFrameWidth / 2, 0, mFrameWidth / 2, mFrameHeight)

        mMainPanel.add(mJScrollPane)
    }

    private fun createMainPanel() {
        mMainPanel = JPanel()
        mMainPanel.setBounds(0, 0, mFrameWidth, mFrameHeight)
        mMainPanel.layout = null
        mMainPanel.background = Color(220, 240, 250)
    }

    private fun createImageLabel() {
        mImageLabel = JLabel()
        mImageLabel.background = Color.BLACK
        mImageLabel.isOpaque = true
        mImageLabel.setBounds(0, 0, mFrameWidth / 2, mFrameHeight - 80)
        mImageLabel.addMouseListener(LabelMouseClickListener())
        mImageLabel.addMouseMotionListener(LabelMouseMotionListener())
        mMainPanel.add(mImageLabel)
    }

    private fun calcXInDisplay(input: Int): Int {
        val result = mDisplayX * (input * 1.0f / mImageLabel.width)
        return result.toInt()
    }

    private fun calcYInDisplay(input: Int): Int {
        val result = mDisplayY * (input * 1.0f / mImageLabel.height)
        return result.toInt()
    }

    @Throws(IOException::class)
    private fun createBottomBar() {
        val file = File("")
        val path = file.absolutePath

        println("current path is $path")

        val menuImageIcon = ImageIcon(ImageIO.read(File("$path/wiseclientlibrary/images/menu.png")))
        val homeImageIcon = ImageIcon(ImageIO.read(File("$path/wiseclientlibrary/images/home.png")))
        val backImageIcon = ImageIcon(ImageIO.read(File("$path/wiseclientlibrary/images/back.png")))

        mJLabelBottomMenu = JLabel(menuImageIcon)
        mJLabelBottomMenu.background = Color.BLACK
        mJLabelBottomMenu.isOpaque = true
        mJLabelBottomMenu.setBounds(0, mImageLabel.height, mFrameWidth / 6, 40)

        mJLabelBottomHome = JLabel(homeImageIcon)
        mJLabelBottomHome.background = Color.BLACK
        mJLabelBottomHome.isOpaque = true
        mJLabelBottomHome.setBounds(mFrameWidth / 6, mImageLabel.height, mFrameWidth / 6, 40)

        mJLabelBottomBack = JLabel(backImageIcon)
        mJLabelBottomBack.background = Color.BLACK
        mJLabelBottomBack.isOpaque = true
        mJLabelBottomBack.setBounds(mFrameWidth / 6 * 2, mImageLabel.height, mFrameWidth / 6, 40)

        mMainPanel.add(mJLabelBottomMenu)
        mMainPanel.add(mJLabelBottomHome)
        mMainPanel.add(mJLabelBottomBack)

        mJLabelBottomMenu.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(mouseEvent: MouseEvent) {
                super.mouseClicked(mouseEvent)
                writeMouseAction("MENU")
            }
        })
        mJLabelBottomHome.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(mouseEvent: MouseEvent) {
                super.mouseClicked(mouseEvent)
                writeMouseAction("HOME")

            }
        })
        mJLabelBottomBack.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(mouseEvent: MouseEvent) {
                super.mouseClicked(mouseEvent)
                writeMouseAction("BACK")
            }
        })
    }

    private fun writeMouseAction(action: String) {
        try {
            writer.write(action)
            writer.newLine()
            writer.flush()
            mUserActionInterface.actionHomePress()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startSocket(ip: String, port: String) {
        object : Thread() {
            override fun run() {
                super.run()
                try {
                    val socket = Socket(ip, Integer.parseInt(port))
                    val inputStream = BufferedInputStream(socket.getInputStream())
                    val dataInputStream = DataInputStream(inputStream)
                    writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                    var bytes: ByteArray? = null
                    while (true) {
                        mDisplayX = dataInputStream.readInt()
                        mDisplayY = dataInputStream.readInt()
                        if (mDisplayOldX != mDisplayX && mDisplayOldY != mDisplayY) {
                            if (mDisplayOldX == 0 && mDisplayOldY == 0) {
                                if (mDisplayX > mDisplayY) {
                                    changeDisplayOrientation(false)
                                }
                            }
                            if (mDisplayOldX < mDisplayOldY && mDisplayX > mDisplayY) {
                                changeDisplayOrientation(false)
                            } else if (mDisplayOldX > mDisplayOldY && mDisplayX < mDisplayY) {
                                changeDisplayOrientation(true)
                            }
                            mDisplayOldX = mDisplayX
                            mDisplayOldY = mDisplayY
                        }

                        val length = dataInputStream.readInt()

                        if (bytes == null) {
                            bytes = ByteArray(length)
                        }
                        if (bytes.size < length) {
                            bytes = ByteArray(length)
                        }
                        var read = 0
                        while (read < length) {
                            read += inputStream.read(bytes, read, length - read)
                        }
                        val byteArrayInputStream = ByteArrayInputStream(bytes)
                        val image = ImageIO.read(byteArrayInputStream)
                        mImageLabel.icon = ScaleIcon(ImageIcon(image))
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }.start()
    }

    private fun changeDisplayOrientation(isVertical: Boolean) {
        isGVertical = isVertical

        if (isVertical) {
            println("screen change to vertical")
            mImageLabel.setBounds(0, 0, mFrameWidth / 2, mFrameHeight - 80)
            mJLabelBottomMenu.setBounds(0, mImageLabel.height, mFrameWidth / 6, 40)
            mJLabelBottomHome.setBounds(mFrameWidth / 6, mImageLabel.height, mFrameWidth / 6, 40)
            mJLabelBottomBack.setBounds(mFrameWidth / 6 * 2, mImageLabel.height, mFrameWidth / 6, 40)

            mJScrollPane.setBounds(mFrameWidth / 2, 0, mFrameWidth / 2, mFrameHeight)

            if (isScriptShow) {
                this.setBounds(450, 0, mFrameWidth, mFrameHeight)
            } else {
                this.setBounds(450, 0, mFrameWidth / 2, mFrameHeight)
            }
        } else {
            println("screen change to landscape")
            mImageLabel.setBounds(0, 0, mFrameWidth, mFrameHeight / 2 - 40)
            mJLabelBottomMenu.setBounds(0, mImageLabel.height, mFrameWidth / 3, 40)
            mJLabelBottomHome.setBounds(mFrameWidth / 3, mImageLabel.height, mFrameWidth / 3, 40)
            mJLabelBottomBack.setBounds(mFrameWidth / 3 * 2, mImageLabel.height, mFrameWidth / 3, 40)

            mJScrollPane.setBounds(0, mFrameHeight / 2, mFrameWidth, mFrameHeight / 2)

            if (isScriptShow) {
                this.setBounds(450, 0, mFrameWidth, mFrameHeight)
            } else {
                this.setBounds(450, 0, mFrameWidth, mFrameHeight / 2)
            }
        }
        mMainPanel.validate()
        mMainPanel.repaint()
    }

    internal inner class LabelMouseClickListener : MouseAdapter() {
        override fun mouseClicked(mouseEvent: MouseEvent) {
            mainPanelRequestFocus()

            super.mouseClicked(mouseEvent)
            try {
                val x = mouseEvent.x
                val y = mouseEvent.y
                val calcX = calcXInDisplay(x)
                val calcY = calcYInDisplay(y)

                writer.write("DOWN$calcX#$calcY")
                writer.newLine()
                writer.write("UP$calcX#$calcY")
                writer.newLine()
                writer.flush()
                mUserActionInterface.actionViewClick(calcX, calcY)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        override fun mouseReleased(mouseEvent: MouseEvent) {
            mainPanelRequestFocus()

            super.mouseReleased(mouseEvent)
            try {
                val x = mouseEvent.x
                val y = mouseEvent.y
                writer.write("UP" + calcXInDisplay(x) + "#" + calcYInDisplay(y))
                writer.newLine()
                writer.flush()
                if (isMove) {
                    mMoveNewX = calcXInDisplay(x)
                    mMoveNewY = calcYInDisplay(y)
                    mUserActionInterface.actionViewMove(mMoveOldX, mMoveOldY, mMoveNewX, mMoveNewY)
                }
                isMove = false
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    internal inner class LabelMouseMotionListener : MouseAdapter() {
        override fun mouseDragged(mouseEvent: MouseEvent) {
            mainPanelRequestFocus()

            super.mouseDragged(mouseEvent)
            try {
                val x = mouseEvent.x
                val y = mouseEvent.y
                if (!isMove) {
                    isMove = true
                    writer.write("DOWN" + calcXInDisplay(x) + "#" + calcYInDisplay(y))
                    mMoveOldX = calcXInDisplay(x)
                    mMoveOldY = calcYInDisplay(y)
                    println("move down x " + calcXInDisplay(x))
                } else {
                    writer.write("MOVE" + calcXInDisplay(x) + "#" + calcYInDisplay(y))
                    println("move move x " + calcXInDisplay(x))
                }
                writer.newLine()
                writer.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun mainPanelRequestFocus() {
        mMainPanel.isFocusable = true
        mMainPanel.requestFocus()
    }

    internal inner class LabelMouseKeyListener : KeyListener {
        override fun keyTyped(e: KeyEvent) {}

        override fun keyPressed(e: KeyEvent) {
            try {
                println("key pressed " + e.keyCode)
                val code = e.keyCode

                if (code == KeyEvent.VK_UP) {
                    writer.write("KEY_UP")
                    mUserActionInterface.actionKeyUpPress()
                } else if (code == KeyEvent.VK_DOWN) {
                    writer.write("KEY_DOWN")
                    mUserActionInterface.actionKeyDownPress()
                } else if (code == KeyEvent.VK_LEFT) {
                    writer.write("KEY_LEFT")
                    mUserActionInterface.actionKeyLeftPress()
                } else if (code == KeyEvent.VK_RIGHT) {
                    writer.write("KEY_RIGHT")
                    mUserActionInterface.actionKeyRightPress()
                } else if (code == KeyEvent.VK_ENTER) {
                    writer.write("KEY_ENTER")
                    mUserActionInterface.actionKeyEnterPress()
                } else if (code == KeyEvent.VK_ESCAPE) {
                    writer.write("KEY_ESC")
                    mUserActionInterface.actionKeyBackPress()
                }

                writer.newLine()
                writer.flush()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }

        }

        override fun keyReleased(e: KeyEvent) {
            println("key pressed " + e.keyCode)
        }
    }

    companion object {

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            ComputerClientFrame().isVisible = true
        }
    }
}

