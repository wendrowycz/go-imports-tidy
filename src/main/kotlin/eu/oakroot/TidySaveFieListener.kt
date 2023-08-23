package eu.oakroot

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.guessProjectForFile
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.util.*


class TidySaveFieListener : FileDocumentManagerListener {

    private val myDocumentsToStripLater: MutableSet<Document> = HashSet()

    override fun beforeAllDocumentsSaving() {
        val documentsToStrip: Set<Document> = HashSet(myDocumentsToStripLater)
        myDocumentsToStripLater.clear()
        for (document in documentsToStrip) {
            tidy(document)
        }
    }

    override fun beforeDocumentSaving(document: Document) {
        tidy(document)
    }

    private fun tidy(document: Document) {
        val file: VirtualFile = FileDocumentManager.getInstance().getFile(document) ?: return
        val project = guessProjectForFile(file)?: return
        val goImportTidy = GoImportTidy()
        val importsBlockStr = goImportTidy.findImports(document.text)
        val importsBlock =
            ArrayList(Arrays.asList(*importsBlockStr.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()))
        val locals: List<String> = TidyImportsSettingsConfigurable.getOptionTextString(project, TidyImportsOptionsForm.LOCAL_PREFIX).split(",").map { it.trim() }.filter { it.isNotEmpty() }
        try {
            val parsedFile = goImportTidy.parseFile(importsBlock, locals)
            val importBlock = goImportTidy.extractBlockContent(document.text)
            if (parsedFile.isParsed) {
                CommandProcessor.getInstance().runUndoTransparentAction {
                    document.setText(
                        document.text.replace(
                            importBlock,
                            parsedFile.fileContent
                        )
                    )
                }
            }
        } catch (e: IOException) {
            LOG.debug(e.message)
        }
    }

    override fun unsavedDocumentsDropped() {
        myDocumentsToStripLater.clear()
    }

    companion object {
        private val LOG = Logger.getInstance(
            TidySaveFieListener::class.java
        )
    }
}