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
    }

    private lateinit var previewTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var shiftButton: Button
    private var shiftEnabled: Boolean = false
    var listener: Listener? = null

    init {
        orientation = VERTICAL
        setPadding(dp(10), dp(8), dp(10), dp(10))
        background = roundedBackground(fill = 0xFFF5F6F8.toInt(), stroke = 0xFFD3D8E0.toInt(), radius = 24f)

        val header = makeHeader()
        val previewCard = makePreviewCard()
        val numberRow = makeKeyRow("1234567890".toCharArray(), keyHeight = dp(42), keyWeight = 1f)
        val qwertyRow = makeKeyRow("qwertyuiop".toCharArray(), keyHeight = dp(48), keyWeight = 1f)
        val asdfRow = makeKeyRow("asdfghjkl".toCharArray(), keyHeight = dp(48), keyWeight = 1f, offsetStart = dp(18))
        val zxcvRow = makeShiftRow()
        val bottomRow = makeBottomRow()

        addView(header)
        addView(previewCard)
        addView(numberRow)
        addView(qwertyRow)
        addView(asdfRow)
        addView(zxcvRow)
        addView(bottomRow)
    }

    fun setPreview(text: String) {
        previewTextView.text = if (text.isBlank()) {
            "Type English letters. Space commits Amharic."
        } else {
            text
        }
    }

    fun setStatus(text: String) {
        statusTextView.text = text
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
            text = "Amharic Keyboard"
            setTextColor(0xFF4A5568.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTypeface(typeface, Typeface.BOLD)
        }

        statusTextView = TextView(context).apply {
            text = "Ready"
            setTextColor(0xFF718096.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            gravity = Gravity.END
        }

        row.addView(title, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        row.addView(statusTextView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        return row
    }

    private fun makePreviewCard(): View {
        val card = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = roundedBackground(fill = 0xFFFFFFFF.toInt(), stroke = 0xFFE2E8F0.toInt(), radius = 20f)
        }

        previewTextView = TextView(context).apply {
            text = "Type English letters. Space commits Amharic."
            setTextColor(0xFF1A202C.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.START
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
        row.addView(
            makeLetterButton(
                label = "z",
                onClick = { listener?.onLatinKey(applyShift('z')) },
                keyHeight = dp(48),
                keyWeight = 1f
            )
        )
        row.addView(makeLetterButton(label = "x", onClick = { listener?.onLatinKey(applyShift('x')) }, keyHeight = dp(48), keyWeight = 1f))
        row.addView(makeLetterButton(label = "c", onClick = { listener?.onLatinKey(applyShift('c')) }, keyHeight = dp(48), keyWeight = 1f))
        row.addView(makeLetterButton(label = "v", onClick = { listener?.onLatinKey(applyShift('v')) }, keyHeight = dp(48), keyWeight = 1f))
        row.addView(makeLetterButton(label = "b", onClick = { listener?.onLatinKey(applyShift('b')) }, keyHeight = dp(48), keyWeight = 1f))
        row.addView(makeLetterButton(label = "n", onClick = { listener?.onLatinKey(applyShift('n')) }, keyHeight = dp(48), keyWeight = 1f))
        row.addView(makeLetterButton(label = "m", onClick = { listener?.onLatinKey(applyShift('m')) }, keyHeight = dp(48), keyWeight = 1f))

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
                label = "🌐",
                onClick = { listener?.onShowKeyboardPicker() },
                keyWidth = 0,
                keyHeight = dp(50),
                keyWeight = 1.1f
            )
        )

        row.addView(
            makeActionButton(
                label = ",",
                onClick = { listener?.onCommitTrigger(CommitTrigger.Comma) },
                keyWidth = 0,
                keyHeight = dp(50),
                keyWeight = 0.9f
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
                keyWeight = 1.1f
            )
        )

        row.addView(
            makeActionButton(
                label = "Hide",
                onClick = { listener?.onHideKeyboard() },
                keyWidth = 0,
                keyHeight = dp(50),
                keyWeight = 1.1f
            )
        )

        return row
    }

    private fun makeLetterButton(
        label: String,
        onClick: () -> Unit,
        keyHeight: Int,
        keyWeight: Float
    ): Button {
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
        keyWeight: Float
    ): Button {
        return Button(context).apply {
            text = label
            textSize = 15f
            setTextColor(0xFF1A202C.toInt())
            isAllCaps = false
            setOnClickListener { onClick() }
            background = roundedBackground(fill = 0xFFFFFFFF.toInt(), stroke = 0xFFCBD5E0.toInt(), radius = 16f)
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
        shiftButton.text = if (shiftEnabled) "⇧" else "⇧"
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
