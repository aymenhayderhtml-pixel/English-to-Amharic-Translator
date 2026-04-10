package dev.amharictranslator.ime

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import dev.amharictranslator.keyboard.CommitTrigger

class AmharicKeyboardView(context: Context) : LinearLayout(context) {
    interface Listener {
        fun onLatinKey(char: Char)
        fun onCommitTrigger(trigger: CommitTrigger)
        fun onBackspace()
        fun onHideKeyboard()
        fun onShowKeyboardPicker()
        fun onToggleTypingMode()
    }

    var listener: Listener? = null

    private lateinit var previewTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var modeButton: Button
    private lateinit var pickerButton: Button
    private lateinit var hideButton: Button
    private lateinit var shiftButton: Button

    private var shiftEnabled = false
    private var amharicModeEnabled = true
    private var transliterationActive = true

    init {
        orientation = VERTICAL
        setPadding(dp(10), dp(8), dp(10), dp(10))
        background = roundedBackground(fill = 0xFF0F1115.toInt(), stroke = 0xFF202632.toInt(), radius = 24f)

        addView(makeHeader())
        addView(makePreviewCard())
        addView(makeKeyRow("1234567890".toCharArray(), keyHeight = dp(42), keyWeight = 1f))
        addView(makeKeyRow("qwertyuiop".toCharArray(), keyHeight = dp(48), keyWeight = 1f))
        addView(makeKeyRow("asdfghjkl".toCharArray(), keyHeight = dp(48), keyWeight = 1f, offsetStart = dp(18)))
        addView(makeShiftRow())
        addView(makeBottomRow())
    }

    fun setPreview(text: String) {
        previewTextView.text = text
    }

    fun setStatus(text: String) {
        statusTextView.text = text
        statusTextView.visibility = if (text.isBlank()) View.GONE else View.VISIBLE
    }

    fun setTypingMode(amharicEnabled: Boolean, transliterationEnabled: Boolean) {
        amharicModeEnabled = amharicEnabled
        transliterationActive = transliterationEnabled
        updateModeLabel()
    }

    private fun makeHeader(): View {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
        }

        val title = TextView(context).apply {
            text = "Amharic"
            setTextColor(0xFFB8C0CC.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTypeface(typeface, Typeface.BOLD)
        }

        modeButton = makeActionButton(
            label = "አ",
            onClick = { listener?.onToggleTypingMode() },
            keyWidth = LayoutParams.WRAP_CONTENT,
            keyHeight = dp(32),
            keyWeight = 0f,
            compact = true,
            accent = true
        )

        pickerButton = makeActionButton(
            label = "🌐",
            onClick = { listener?.onShowKeyboardPicker() },
            keyWidth = LayoutParams.WRAP_CONTENT,
            keyHeight = dp(32),
            keyWeight = 0f,
            compact = true
        )

        hideButton = makeActionButton(
            label = "⌄",
            onClick = { listener?.onHideKeyboard() },
            keyWidth = LayoutParams.WRAP_CONTENT,
            keyHeight = dp(32),
            keyWeight = 0f,
            compact = true
        )

        statusTextView = TextView(context).apply {
            text = ""
            setTextColor(0xFF8A93A3.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            gravity = Gravity.END
            visibility = View.GONE
        }

        row.addView(title, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        row.addView(modeButton)
        row.addView(pickerButton)
        row.addView(hideButton)
        return row
    }

    private fun makePreviewCard(): View {
        val card = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = roundedBackground(fill = 0xFF171B22.toInt(), stroke = 0xFF2A313D.toInt(), radius = 20f)
        }

        previewTextView = TextView(context).apply {
            text = ""
            setTextColor(0xFFF3F5F8.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.START
            minHeight = dp(28)
        }

        card.addView(previewTextView)
        return card
    }

    private fun makeKeyRow(
        chars: CharArray,
        keyHeight: Int,
        keyWeight: Float,
        offsetStart: Int = 0
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
                if (offsetStart > 0) {
                    setPadding(offsetStart, 0, 0, 0)
                }
            }

            chars.forEach { char ->
                addView(
                    makeLetterButton(
                        label = char.toString(),
                        onClick = { listener?.onLatinKey(applyShift(char)) },
                        keyHeight = keyHeight,
                        keyWeight = keyWeight
                    )
                )
            }
        }
    }

    private fun makeShiftRow(): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
            }
        }

        shiftButton = makeActionButton(
            label = "⇧",
            onClick = {
                shiftEnabled = !shiftEnabled
                refreshShiftLabel()
            },
            keyWidth = 0,
            keyHeight = dp(48),
            keyWeight = 1.15f
        )

        row.addView(shiftButton)
        row.addView(makeLetterButton("z", { listener?.onLatinKey(applyShift('z')) }, dp(48), 1f))
        row.addView(makeLetterButton("x", { listener?.onLatinKey(applyShift('x')) }, dp(48), 1f))
        row.addView(makeLetterButton("c", { listener?.onLatinKey(applyShift('c')) }, dp(48), 1f))
        row.addView(makeLetterButton("v", { listener?.onLatinKey(applyShift('v')) }, dp(48), 1f))
        row.addView(makeLetterButton("b", { listener?.onLatinKey(applyShift('b')) }, dp(48), 1f))
        row.addView(makeLetterButton("n", { listener?.onLatinKey(applyShift('n')) }, dp(48), 1f))
        row.addView(makeLetterButton("m", { listener?.onLatinKey(applyShift('m')) }, dp(48), 1f))
        row.addView(
            makeActionButton(
                label = "⌫",
                onClick = { listener?.onBackspace() },
                keyWidth = 0,
                keyHeight = dp(48),
                keyWeight = 1.2f
            )
        )

        return row
    }

    private fun makeBottomRow(): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        row.addView(
            makeActionButton(
                label = ",",
                onClick = { listener?.onCommitTrigger(CommitTrigger.Comma) },
                keyWidth = 0,
                keyHeight = dp(50),
                keyWeight = 1.0f
            )
        )
        row.addView(
            makeActionButton(
                label = "space",
                onClick = { listener?.onCommitTrigger(CommitTrigger.Space) },
                keyWidth = 0,
                keyHeight = dp(50),
                keyWeight = 3.2f
            )
        )
        row.addView(
            makeActionButton(
                label = ".",
                onClick = { listener?.onCommitTrigger(CommitTrigger.Period) },
                keyWidth = 0,
                keyHeight = dp(50),
                keyWeight = 0.9f
            )
        )
        row.addView(
            makeActionButton(
                label = "↵",
                onClick = { listener?.onCommitTrigger(CommitTrigger.Newline) },
                keyWidth = 0,
                keyHeight = dp(50),
                keyWeight = 1.0f
            )
        )

        return row
    }

    private fun makeLetterButton(label: String, onClick: () -> Unit, keyHeight: Int, keyWeight: Float): Button {
        return makeActionButton(
            label = label,
            onClick = onClick,
            keyWidth = 0,
            keyHeight = keyHeight,
            keyWeight = keyWeight
        )
    }

    private fun makeActionButton(
        label: String,
        onClick: () -> Unit,
        keyWidth: Int,
        keyHeight: Int,
        keyWeight: Float,
        compact: Boolean = false,
        accent: Boolean = false
    ): Button {
        return Button(context).apply {
            text = label
            textSize = if (compact) 13f else 15f
            setTextColor(if (accent) 0xFFF4D48A.toInt() else 0xFFF3F5F8.toInt())
            isAllCaps = false
            setOnClickListener { onClick() }
            background = roundedBackground(
                fill = if (accent) 0xFF2E3644.toInt() else 0xFF232831.toInt(),
                stroke = if (accent) 0xFF4B5568.toInt() else 0xFF323A47.toInt(),
                radius = if (compact) 12f else 16f
            )
            layoutParams = LayoutParams(keyWidth, keyHeight, keyWeight).apply {
                marginStart = dp(3)
                marginEnd = dp(3)
            }
        }
    }

    private fun applyShift(char: Char): Char {
        val output = if (shiftEnabled) char.uppercaseChar() else char
        if (shiftEnabled) {
            shiftEnabled = false
            refreshShiftLabel()
        }
        return output
    }

    private fun refreshShiftLabel() {
        shiftButton.alpha = if (shiftEnabled) 1.0f else 0.75f
        shiftButton.text = "⇧"
    }

    private fun updateModeLabel() {
        modeButton.text = when {
            !amharicModeEnabled -> "E"
            transliterationActive -> "አ"
            else -> "E"
        }
        modeButton.alpha = if (transliterationActive || !amharicModeEnabled) 1.0f else 0.75f
    }

    private fun roundedBackground(fill: Int, stroke: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radius).toFloat()
            setColor(fill)
            setStroke(dp(1), stroke)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun dp(value: Float): Int = (value * resources.displayMetrics.density).toInt()
}
