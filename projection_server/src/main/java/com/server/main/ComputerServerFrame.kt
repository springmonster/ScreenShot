package com.server.main

import com.server.script.UserActionManager
import com.server.script.saveFile
import java.awt.Color
import java.awt.Toolkit
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.*
import java.net.Socket
import javax.imageio.ImageIO
import javax.swing.*

/**
 * Created by kuanghaochuan on 2017/7/13.
 */
internal class ComputerServerFrame @Throws(IOException::class) constructor() : JFrame() {
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

    private var userActionManager: UserActionManager

    private var mMoveOldX: Int = 0
    private var mMoveOldY: Int = 0
    private var mMoveNewX: Int = 0
    private var mMoveNewY: Int = 0

    private var isScriptShow = false
    private var isGVertical = true

    init {
        initComputerServerFrame()

        initMainPanel()

        initImageLabel()

        initBottomBar()

        initScriptPanel()

        this.add(mMainPanel)

        userActionManager = UserActionManager(mJTextAreaShowScript)

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

    private fun initComputerServerFrame() {
        initJMenu()
        initDefaultSettings()
    }

    private fun initJMenu() {
        mJMenuBar = JMenuBar()

        val phoneJMenu = JMenu("Phone")
        val prepareJMenuItem = JMenuItem("Prepare")
        val connectJMenuItem = JMenuItem("Connect phone")

        val scriptJMenu = JMenu("Script")
        val disJMenuItem = JMenuItem("Show script")
        val saveJMenuItem = JMenuItem("Save script")
        val replayJMenuItem = JMenuItem("Replay script")

        this.jMenuBar = mJMenuBar
        mJMenuBar.add(phoneJMenu)
        mJMenuBar.add(scriptJMenu)

        phoneJMenu.add(prepareJMenuItem)
        phoneJMenu.add(connectJMenuItem)

        scriptJMenu.add(disJMenuItem)
        scriptJMenu.add(saveJMenuItem)
        scriptJMenu.add(replayJMenuItem)

        prepareJMenuItem.addActionListener {
            Thread {
                installDex()
            }.start()
        }
        connectJMenuItem.addActionListener { startConnecting() }

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

        replayJMenuItem.addActionListener {
            Thread(Runnable {
                val tempList = ArrayList<String>()
                tempList.addAll(userActionManager.strList)

                println("server replay ${tempList.size}")

                for (s in tempList) {
                    writer.write(s)
                    writer.flush()
                    Thread.sleep(500)
                    println("server replay $s")
                }
            }).start()
        }
    }

    private fun startConnecting() {
        startSocket("127.0.0.1", "3000")
    }

    private fun initDefaultSettings() {
        this.defaultCloseOperation = EXIT_ON_CLOSE
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

    private fun initScriptPanel() {
        mJTextAreaShowScript = JTextArea()
        mJTextAreaShowScript.setBounds(mFrameWidth / 2, 0, mFrameWidth / 2, mFrameHeight)
        mJTextAreaShowScript.lineWrap = true
        mJTextAreaShowScript.wrapStyleWord = true

        mJScrollPane = JScrollPane(mJTextAreaShowScript)
        mJScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        mJScrollPane.setBounds(mFrameWidth / 2, 0, mFrameWidth / 2, mFrameHeight)

        mMainPanel.add(mJScrollPane)
    }

    private fun initMainPanel() {
        mMainPanel = JPanel()
        mMainPanel.setBounds(0, 0, mFrameWidth, mFrameHeight)
        mMainPanel.layout = null
        mMainPanel.background = Color(220, 240, 250)
    }

    private fun initImageLabel() {
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
    private fun initBottomBar() {
        val file = File("")
        val path = file.absolutePath

        println("current path is $path")

        val menuImageIcon = ImageIcon(ImageIO.read(File("$path/projection_server/images/menu.png")))
        val homeImageIcon = ImageIcon(ImageIO.read(File("$path/projection_server/images/home.png")))
        val backImageIcon = ImageIcon(ImageIO.read(File("$path/projection_server/images/back.png")))

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

            userActionManager.saveAction(action)
            userActionManager.saveAction("\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startSocket(ip: String, port: String) {
        Thread(Runnable {
            try {
                val socket = Socket(ip, Integer.parseInt(port))
                val bufferedInputStream = BufferedInputStream(socket.getInputStream())
                val dataInputStream = DataInputStream(bufferedInputStream)
                writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

                var bytes: ByteArray? = null
                while (true) {
                    // 获取手机的宽和高
                    mDisplayX = dataInputStream.readInt()
                    mDisplayY = dataInputStream.readInt()

                    // 判断是否有屏幕旋转
                    getWidthAndHeight()

//                    println("server x is $mDisplayX")
//                    println("server x is $mDisplayY")

                    // 获取图片的大小
                    val size = dataInputStream.readInt()

//                    println("server data size is $size")

                    // 获取图片的字节数组并展示
                    if (bytes == null) {
                        bytes = ByteArray(size)
                    }
                    if (bytes.size < size) {
                        bytes = ByteArray(size)
                    }
                    var read = 0
                    while (read < size) {
                        read += bufferedInputStream.read(bytes, read, size - read)
                    }

                    val byteArrayInputStream = ByteArrayInputStream(bytes)
                    val image = ImageIO.read(byteArrayInputStream)
                    val scaleIcon = ScaleIcon(ImageIcon(image))
                    mImageLabel.icon = scaleIcon

//                    println("server image width is ${scaleIcon.iconWidth}")
//                    println("server image height is ${scaleIcon.iconHeight}")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun getWidthAndHeight() {
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

    /**
     * 鼠标点击事件，对应手机端的点击
     */
    internal inner class LabelMouseClickListener : MouseAdapter() {
        override fun mouseClicked(mouseEvent: MouseEvent) {
            mainPanelRequestFocus()

            super.mouseClicked(mouseEvent)
            try {
                val x = mouseEvent.x
                val y = mouseEvent.y
                val calcX = calcXInDisplay(x)
                val calcY = calcYInDisplay(y)

//                println("server calc x is $calcX")
//                println("server calc y is $calcY")

                writer.write("DOWN$calcX#$calcY")
                writer.newLine()
                writer.write("UP$calcX#$calcY")
                writer.newLine()
                writer.flush()

                userActionManager.saveAction("DOWN$calcX#$calcY")
                userActionManager.saveAction("\n")
                userActionManager.saveAction("UP$calcX#$calcY")
                userActionManager.saveAction("\n")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        override fun mouseReleased(mouseEvent: MouseEvent) {
            mainPanelRequestFocus()

            super.mouseReleased(mouseEvent)
//            try {
//                val x = mouseEvent.x
//                val y = mouseEvent.y
//                writer.write("UP" + calcXInDisplay(x) + "#" + calcYInDisplay(y))
//                writer.newLine()
//                writer.flush()
//
//                userActionManager.saveAction("UP" + calcXInDisplay(x) + "#" + calcYInDisplay(y))
//                userActionManager.saveAction("\n")
//
//                if (isMove) {
//                    mMoveNewX = calcXInDisplay(x)
//                    mMoveNewY = calcYInDisplay(y)
//                }
//                isMove = false
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }

        }
    }

    /**
     * 鼠标移动，对应手机端的滑动
     */
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

                    userActionManager.saveAction("DOWN" + calcXInDisplay(x) + "#" + calcYInDisplay(y))
                } else {
                    writer.write("MOVE" + calcXInDisplay(x) + "#" + calcYInDisplay(y))
                    println("move move x " + calcXInDisplay(x))

                    userActionManager.saveAction("MOVE" + calcXInDisplay(x) + "#" + calcYInDisplay(y))
                }
                writer.newLine()
                writer.flush()

                userActionManager.saveAction("\n")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun mainPanelRequestFocus() {
        mMainPanel.isFocusable = true
        mMainPanel.requestFocus()
    }

    companion object {

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            ComputerServerFrame().isVisible = true
        }
    }
}

