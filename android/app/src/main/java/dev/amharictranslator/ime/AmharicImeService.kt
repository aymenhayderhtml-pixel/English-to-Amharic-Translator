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

    override fun onCreateInputView(): View {
        return AmharicKeyboardView(this).also { view ->
            view.listener = this
            keyboardView = view
            view.setStatus("Amharic Keyboard ready")
            view.setPreview("")
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        transliterationEnabled = !isSecureInputType(attribute)
        controller.reset()
        keyboardView?.setPreview("")
        keyboardView?.setStatus(buildStatusLabel(attribute))
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
                commitCurrentComposition(inputConnection, CommitTrigger.Space)
                inputConnection.commitText(char.toString(), 1)
            }
        }
    }

    private fun handleTrigger(trigger: CommitTrigger) {
        val inputConnection = currentInputConnection ?: return

        if (!transliterationEnabled) {
            inputConnection.commitText(trigger.marker.toString(), 1)
            keyboardView?.setPreview("")
            return
        }

        commitCurrentComposition(inputConnection, trigger)
        inputConnection.commitText(trigger.marker.toString(), 1)
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

    private fun commitCurrentComposition(inputConnection: InputConnection, trigger: CommitTrigger) {
        if (!controller.hasComposition()) return

        val committed = controller.commit(trigger) ?: return

        inputConnection.finishComposingText()
        if (committed.amharicText.isNotBlank()) {
            inputConnection.commitText(committed.amharicText, 1)
        }
        keyboardView?.setPreview("")
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
            "Typing in $inputClass field"
        }
    }
}
