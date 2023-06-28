package eu.oakroot

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
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
                val importBlock = extractBlockContent(document.text)
                if (importBlock != "") {
                    val modifiedInput = document.text.replace(importBlock, parsedFile.fileContent)
                    val cmd = Runnable {
                        document.setReadOnly(false)
                        document.setText(modifiedInput)
                    }
                    WriteCommandAction.runWriteCommandAction(project, cmd)
                }
            }
        } catch (err : IOException) {
            err.printStackTrace()
        }
    }

    fun extractBlockContent(input: String): String {
        val importStart = input.indexOf("import (")
        if (importStart == -1) {
            return ""
        }

        val importEnd = input.indexOf(")\n", startIndex = importStart)
        if (importEnd == -1) {
            return ""
        }

        val blockContent = input.substring(importStart, importEnd + 1)
        val lines = blockContent.lines().drop(1).dropLast(1)

        return lines.joinToString("\n")
    }


    fun parseFile(importsBlock: java.util.ArrayList<String>, local: String): ParsedFile {
        val contents = extractImports(importsBlock)
        if (contents.size == 0) {
            return ParsedFile("", false)
        }
        val imports: ArrayList<String> = formatImports(contents, local)
        val parsed: String = imports.joinToString("\n")
        return ParsedFile(parsed, true)
    }

    private fun formatImports(imports: java.util.ArrayList<String>, local: String): ArrayList<String> {
        val results = ArrayList<String>()
        var needEmptyLine = false
        val groups: MutableMap<Int, ArrayList<String>> = HashMap()
        val stdLib = ArrayList<String>()
        val locLib = ArrayList<String>()
        val extLib = ArrayList<String>()
        var comments = ArrayList<String>()
        val hasComments = HashMap<String, ArrayList<String>>()

        for (imp: String in imports) {
            if (imp.trim { it <= ' ' } == "") {
                continue
            }

            if (imp.trim().startsWith("//")) {
                comments.add(imp)
                continue
            }
            when (group(imp, local)) {
                STD_LIB -> {
                    stdLib.add(imp)
                    hasComments[imp.trim()] = comments
                }
                EXTERNAL_LIB -> {
                    extLib.add(imp)
                    hasComments[imp.trim()] = comments
                }
                LOCAL_LIB -> {
                    locLib.add(imp)
                    hasComments[imp.trim()] = comments
                }
            }
            comments = ArrayList()
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
        val resultsWithComments = ArrayList<String>()
        for (l in results) {
            if (hasComments.contains(l.trim())) {
                val cms = hasComments[l.trim()]
                if (cms != null) {
                    resultsWithComments.addAll(cms)
                }
            }
            resultsWithComments.add(l)
        }
        return resultsWithComments
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
             if ((!line.contains("\"") && !line.contains("//")) || line.isEmpty()) {
                continue
            }
            importSec.add(line)
        }
        return importSec
    }

    fun findImports(document: String?): String {
        val lines = document?.lines() ?: return ""
        var inImportBlock = false
        val importStatements = mutableListOf<String>()
        for (line in lines) {
            if (line.contains("import (")) {
                inImportBlock = true
                continue
            }
            if (line.contains(")") && !line.trim().startsWith("//")) {
                break
            }
            if (inImportBlock) {
                importStatements.add(line)
            }
        }
        return importStatements.joinToString("\n")
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
    }
}