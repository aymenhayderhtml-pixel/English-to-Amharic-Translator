package dev.amharictranslator.ime

import android.inputmethodservice.InputMethodService
import android.view.inputmethod.InputMethodManager
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import dev.amharictranslator.data.DictionaryRepository
import dev.amharictranslator.keyboard.CommitTrigger

class AmharicImeService : InputMethodService(), AmharicKeyboardView.Listener {
    private val controller by lazy {
        ImeCompositionController(DictionaryRepository.load(applicationContext))
    }

    private var keyboardView: AmharicKeyboardView? = null
    private var transliterationEnabled: Boolean = true
    private var amharicModeRequested: Boolean = true

    override fun onCreateInputView(): View {
        return AmharicKeyboardView(this).also { view ->
            view.listener = this
            keyboardView = view
            view.setStatus("")
            view.setPreview("")
            view.setTypingMode(amharicEnabled = amharicModeRequested, transliterationEnabled = transliterationEnabled)
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        transliterationEnabled = amharicModeRequested && !isSecureInputType(attribute)
        controller.reset()
        keyboardView?.setPreview("")
        keyboardView?.setStatus(buildStatusLabel(attribute))
        keyboardView?.setTypingMode(amharicEnabled = amharicModeRequested, transliterationEnabled = transliterationEnabled)
    }

    override fun onFinishInput() {
        controller.reset()
        keyboardView?.setPreview("")
        super.onFinishInput()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return super.onKeyDown(keyCode, event)
        }

        return when (keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                handleBackspace()
                true
            }
            KeyEvent.KEYCODE_SPACE -> {
                handleTrigger(CommitTrigger.Space)
                true
            }
            KeyEvent.KEYCODE_ENTER -> {
                handleTrigger(CommitTrigger.Newline)
                true
            }
            else -> {
                val char = event.unicodeChar.takeIf { it != 0 }?.toChar()
                if (char != null) {
                    handleCharacter(char)
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
        }
    }

    override fun onLatinKey(char: Char) {
        handleCharacter(char)
    }

    override fun onCommitTrigger(trigger: CommitTrigger) {
        handleTrigger(trigger)
    }

    override fun onBackspace() {
        handleBackspace()
    }

    override fun onHideKeyboard() {
        requestHideSelf(0)
    }

    override fun onShowKeyboardPicker() {
        val imm = getSystemService(InputMethodManager::class.java) ?: return
        imm.showInputMethodPicker()
    }

    override fun onToggleTypingMode() {
        amharicModeRequested = !amharicModeRequested
        transliterationEnabled = amharicModeRequested && !isSecureInputType(currentInputEditorInfo)
        controller.reset()
        currentInputConnection?.finishComposingText()
        keyboardView?.setPreview("")
        keyboardView?.setTypingMode(amharicEnabled = amharicModeRequested, transliterationEnabled = transliterationEnabled)
        keyboardView?.setStatus("")
    }

    private fun handleCharacter(char: Char) {
        val inputConnection = currentInputConnection ?: return

        if (!transliterationEnabled) {
            inputConnection.commitText(char.toString(), 1)
            keyboardView?.setPreview("")
            return
        }

        when {
            char.isLetter() || char == '\'' -> {
                val preview = controller.inputLatin(char)
                inputConnection.setComposingText(preview, 1)
                keyboardView?.setPreview(preview)
            }
            CommitTrigger.fromChar(char) != null -> {
                handleTrigger(CommitTrigger.fromChar(char)!!)
            }
            char == ' ' -> handleTrigger(CommitTrigger.Space)
            char == '\n' -> handleTrigger(CommitTrigger.Newline)
            else -> {
                commitCurrentComposition(CommitTrigger.Space)
                inputConnection.commitText(char.toString(), 1)
            }
        }
    }

    private fun handleTrigger(trigger: CommitTrigger) {
        val inputConnection = currentInputConnection ?: return

        if (!transliterationEnabled) {
            commitMarker(inputConnection, trigger)
            keyboardView?.setPreview("")
            return
        }

        val committedText = commitCurrentComposition(trigger)
        val marker = trigger.marker.toString()
        inputConnection.finishComposingText()
        inputConnection.commitText(committedText + marker, 1)
        keyboardView?.setPreview("")
    }

    private fun handleBackspace() {
        val inputConnection = currentInputConnection ?: return
        if (controller.hasComposition()) {
            val preview = controller.deleteBackward()
            if (preview.isBlank()) {
                inputConnection.finishComposingText()
            } else {
                inputConnection.setComposingText(preview, 1)
            }
            keyboardView?.setPreview(preview)
            return
        }

        inputConnection.deleteSurroundingText(1, 0)
    }

    private fun commitCurrentComposition(trigger: CommitTrigger): String {
        if (!controller.hasComposition()) return ""

        return controller.commit(trigger)?.amharicText.orEmpty()
    }

    private fun commitMarker(inputConnection: InputConnection, trigger: CommitTrigger) {
        val marker = trigger.marker.toString()
        val beforeCursor = inputConnection.getTextBeforeCursor(marker.length, 0)?.toString().orEmpty()
        if (!beforeCursor.endsWith(marker)) {
            inputConnection.commitText(marker, 1)
        }
    }

    private fun isSecureInputType(attribute: EditorInfo?): Boolean {
        val inputType = attribute?.inputType ?: 0
        val inputClass = inputType and EditorInfo.TYPE_MASK_CLASS
        val variation = inputType and EditorInfo.TYPE_MASK_VARIATION

        return when (inputClass) {
            EditorInfo.TYPE_CLASS_TEXT -> variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD
            EditorInfo.TYPE_CLASS_NUMBER -> variation == EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
    }

    private fun buildStatusLabel(attribute: EditorInfo?): String {
        if (amharicModeRequested && !isSecureInputType(attribute)) {
            return ""
        }

        val inputClass = when (attribute?.inputType?.and(EditorInfo.TYPE_MASK_CLASS)) {
            EditorInfo.TYPE_CLASS_TEXT -> "text"
            EditorInfo.TYPE_CLASS_NUMBER -> "number"
            EditorInfo.TYPE_CLASS_PHONE -> "phone"
            else -> "input"
        }

        val secure = isSecureInputType(attribute)

        return if (secure) {
            "Secure field detected, Amharic keyboard limited"
        } else {
            "English typing in $inputClass field"
        }
    }
}
