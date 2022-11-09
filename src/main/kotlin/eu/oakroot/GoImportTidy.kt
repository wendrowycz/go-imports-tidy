package eu.oakroot

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.StringUtils
import java.io.IOException

@Suppress("MissingActionUpdateThread")
open class GoImportTidy : AnAction() {
    private var lexicalComparator = Comparator.comparing { s: String -> importPath(s) }

    override fun update(e: AnActionEvent) {
        val project: Project? = e.project
        (project != null).also { e.presentation.isEnabledAndVisible = it }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val editor: Editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val document: Document = editor.document

        try {
            val importsBlockStr:String = findImports(document.text)
            val importsBlock: ArrayList<String> = ArrayList(listOf(*importsBlockStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
            val local: String =
                TidyImportsSettingsConfigurable.getOptionTextString(project, TidyImportsOptionsForm.LOCAL_PREFIX)
            val parsedFile: ParsedFile = parseFile(importsBlock, local)
            if (parsedFile.isParsed) {
                val newContent: String = document.text.replace(importsBlockStr.toRegex(), parsedFile.fileContent)
                val cmd = Runnable {
                    document.setReadOnly(false)
                    document.setText(newContent)
                }
                WriteCommandAction.runWriteCommandAction(project, cmd)
            }
        } catch (err : IOException) {
            err.printStackTrace()
        }
    }

    fun parseFile(importsBlock: java.util.ArrayList<String>, local: String): ParsedFile {
        LOG.warn("GO TIDY: $local, imports: $importsBlock")
        val contents = extractImports(importsBlock)
        if (contents.size == 0) {
            return ParsedFile("", false)
        }
        val imports: ArrayList<String> = formatImports(contents, local)
        val parsed:String = imports.joinToString("\n")
        return ParsedFile(parsed, true)
    }

    private fun formatImports(imports: java.util.ArrayList<String>, local: String): ArrayList<String> {
        val results = ArrayList<String>()
        var needEmptyLine = false
        val groups: MutableMap<Int, ArrayList<String>> = HashMap()
        val stdLib = ArrayList<String>()
        val locLib = ArrayList<String>()
        val extLib = ArrayList<String>()

        for (imp: String in imports) {
            if (imp.trim { it <= ' ' } == "") {
                continue
            }
            when (group(imp, local)) {
                STD_LIB -> stdLib.add(imp)
                EXTERNAL_LIB -> extLib.add(imp)
                LOCAL_LIB -> locLib.add(imp)
            }
        }
        groups[STD_LIB] = stdLib
        groups[EXTERNAL_LIB] = extLib
        groups[LOCAL_LIB] = locLib
        for ((_: Int, groupImports: ArrayList<String>) in groups) {
            if (groupImports.isEmpty()) {
                continue
            }
            groupImports.sortWith(lexicalComparator)
            if (needEmptyLine) {
                results.add("")
            }
            results.addAll(groupImports)
            needEmptyLine = true
        }
        return results
    }

    private fun group(s: String, local: String): Int {
        val path: String = importPath(s)
        if (!s.contains(".")) {
            return STD_LIB
        }
        return if (path != "" && path.contains(local, true)) {
            LOCAL_LIB
        } else EXTERNAL_LIB
    }

    private fun extractImports(importsBlock: java.util.ArrayList<String>): ArrayList<String> {
        val importSec = ArrayList<String>()
        for (line in importsBlock) {
            if (!line.contains("\"") || line.isEmpty()) {
                continue
            }
            importSec.add(line)
        }
        return importSec
    }

    fun findImports(document: String?): String {
        return StringUtils.substringBetween(document, "import (", ")") ?: return ""
    }

    private fun importPath(str: String): String {
        var s = str
        s = s.trim { it <= ' '}
        val groups = s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val path = groups[groups.size -1]
        return unquote(path)
    }

    companion object {
        const val STD_LIB = 0
        const val EXTERNAL_LIB = 1
        const val LOCAL_LIB = 2

        private fun unquote(v: String): String {
            return if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'")))
            {
                v.substring(1, v.length - 1)
            } else v
        }

        private val LOG = Logger.getInstance(
            TidySaveFieListener::class.java
        )
    }
}