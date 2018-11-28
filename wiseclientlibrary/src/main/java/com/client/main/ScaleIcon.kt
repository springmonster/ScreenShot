package com.client.main

import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints

import javax.swing.Icon

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

class ScaleIcon internal constructor(private val icon: Icon) : Icon {

    override fun getIconHeight(): Int {
        return icon.iconHeight
    }

    override fun getIconWidth(): Int {
        return icon.iconWidth
    }

    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        val wid = c.width.toFloat()
        val hei = c.height.toFloat()
        val iconWid = icon.iconWidth
        val iconHei = icon.iconHeight

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.scale((wid / iconWid).toDouble(), (hei / iconHei).toDouble())
        icon.paintIcon(c, g2d, 0, 0)
    }
}