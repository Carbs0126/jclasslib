/*
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public
 License as published by the Free Software Foundation; either
 version 2 of the license, or (at your option) any later version.
 */

package org.gjt.jclasslib.browser

import org.gjt.jclasslib.browser.config.classpath.FindResult
import org.gjt.jclasslib.browser.config.window.BrowserPath
import org.gjt.jclasslib.browser.config.window.WindowState
import org.gjt.jclasslib.io.ClassFileReader
import org.gjt.jclasslib.structures.ClassFile
import org.gjt.jclasslib.util.GUIHelper
import java.awt.BorderLayout
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.jar.JarFile
import javax.swing.Action
import javax.swing.JOptionPane
import javax.swing.JPanel

class BrowserTab(private val tabbedPane: BrowserTabbedPane, val fileName: String, private val browserPath: BrowserPath?) : JPanel(), BrowserServices {

    private val frameContent: FrameContent
        get() = tabbedPane.container

    private val parentFrame: BrowserMDIFrame
        get() = frameContent.frame

    override val classFile: ClassFile = readClassFile()
    override val browserComponent: BrowserComponent = BrowserComponent(this)

    override fun activate() {
        tabbedPane.focus()
        // force sync of toolbar state with this tab
    }

    override val backwardAction: Action
        get() = parentFrame.backwardAction

    override val forwardAction: Action
        get() = parentFrame.forwardAction

    override fun openClassFile(className: String, browserPath: BrowserPath?) {
        var findResult: FindResult? = parentFrame.config.findClass(className)
        while (findResult == null) {
            if (GUIHelper.showOptionDialog(parentFrame,
                    "The class $className could not be found.\nYou can check your classpath configuration and try again.",
                    arrayOf("Setup classpath", "Cancel"),
                    JOptionPane.WARNING_MESSAGE) == 0) {
                parentFrame.setupClasspathAction()
                findResult = parentFrame.config.findClass(className)
            } else {
                return
            }
        }

        val openTab = frameContent.findTab(findResult.fileName)
        if (openTab != null) {
            openTab.apply {
                select()
                browserComponent.browserPath = browserPath
            }
        } else {
            try {
                tabbedPane.addTab(findResult.fileName, browserPath)
            } catch (e: IOException) {
                GUIHelper.showMessage(parentFrame, e.message, JOptionPane.ERROR_MESSAGE)
            }

        }
    }

    init {
        readClassFile()
        if (browserPath != null) {
            browserComponent.browserPath = browserPath
        }
        layout = BorderLayout()
        add(browserComponent, BorderLayout.CENTER)
    }

    fun reload() {
        readClassFile()
        browserComponent.rebuild()
    }

    fun createWindowState(): WindowState = WindowState(fileName, browserComponent.browserPath)

    private fun select() {
        tabbedPane.selectedComponent = this
    }

    override fun canOpenClassFiles(): Boolean = true

    override fun showURL(urlSpec: String) {
        GUIHelper.showURL(urlSpec)
    }

    private fun readClassFile(): ClassFile {
        try {
            val index = fileName.indexOf('!')
            if (index > -1) {
                val jarFileName = fileName.substring(0, index)
                val classFileName = fileName.substring(index + 1)
                val jarFile = JarFile(jarFileName)
                val jarEntry = jarFile.getJarEntry(classFileName)
                if (jarEntry != null) {
                    return ClassFileReader.readFromInputStream(jarFile.getInputStream(jarEntry))
                } else {
                    throw IOException("The jar entry $classFileName was not found")
                }
            } else {
                return ClassFileReader.readFromFile(File(fileName))
            }
        } catch (ex: FileNotFoundException) {
            throw IOException("The file $fileName was not found")
        } catch (ex: IOException) {
            throw IOException("An error occurred while reading " + fileName)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw IOException("The file $fileName does not seem to contain a class file")
        }
    }
}