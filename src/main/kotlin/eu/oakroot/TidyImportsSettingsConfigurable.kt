package eu.oakroot

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import java.util.*
import javax.swing.JComponent

class TidyImportsSettingsConfigurable(project: Project) : Configurable {
    private var tidyOptionsFrom: TidyImportsOptionsForm? = null
    var propertiesComponent: PropertiesComponent = PropertiesComponent.getInstance(project)

    init {
        instance = this
    }

    override fun getDisplayName(): String {
        return "GO Tidy Imports"
    }

    override fun getHelpTopic(): String? {
        return null
    }

    override fun createComponent(): JComponent? {
        TidyImportsOptionsForm().also { tidyOptionsFrom = it }
        return tidyOptionsFrom!!.contentPane
    }

    override fun isModified(): Boolean {
        val textInForm = tidyOptionsFrom!!.getOptionText(TidyImportsOptionsForm.LOCAL_PREFIX)
        val savedOptionText = propertiesComponent.getValue(TidyImportsOptionsForm.LOCAL_PREFIX)
        return textInForm != savedOptionText
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        propertiesComponent.setValue(
            TidyImportsOptionsForm.LOCAL_PREFIX,
            tidyOptionsFrom!!.getOptionText(TidyImportsOptionsForm.LOCAL_PREFIX)
        )
    }

    override fun reset() {
        var optionId = TidyImportsOptionsForm.LOCAL_PREFIX
        val savedOptionText = propertiesComponent.getValue(optionId)
        tidyOptionsFrom!!.setOptionText(optionId, Objects.requireNonNullElse(savedOptionText, ""))
    }

    override fun disposeUIResources() {
        tidyOptionsFrom = null
    }

    companion object {
        lateinit var instance: TidyImportsSettingsConfigurable

        @JvmStatic
        fun getOptionTextString(
            project: Project?,
            optionId: String?
        ): String {
            return PropertiesComponent.getInstance(project!!).getValue(optionId!!) ?: return ""
        }
    }
}