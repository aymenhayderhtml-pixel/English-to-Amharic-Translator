package dev.amharictranslator.keyman

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun KeymanKeyboardHost(
    modifier: Modifier = Modifier,
    showKeyboard: Boolean,
    onTextChanged: (String) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    if (!showKeyboard) {
        Box(modifier = modifier)
        return
    }

    var error by remember { mutableStateOf<String?>(null) }

    if (error != null) {
        Box(
            modifier = modifier
                .background(Color.Transparent)
        )
        return
    }

    val reportError: (String) -> Unit = { message ->
        error = message
        onError(message)
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            tryCreateKeyboardInput(viewContext, reportError, onTextChanged)
        },
        update = { view ->
            view.requestFocus()
        }
    )
}

private fun tryCreateKeyboardInput(
    context: Context,
    onError: (String) -> Unit,
    onTextChanged: (String) -> Unit
): View {
    return try {
        val managerClass = Class.forName("com.keyman.engine.KMManager")
        val textViewClass = Class.forName("com.keyman.engine.KMTextView")
        val keyboardTypeClass = Class.forName("com.keyman.engine.KMManager\$KeyboardType")
        @Suppress("UNCHECKED_CAST")
        val inAppKeyboardType = java.lang.Enum.valueOf(
            keyboardTypeClass as Class<out Enum<*>>,
            "KEYBOARD_TYPE_INAPP"
        )

        val initializeMethod = managerClass.methods.firstOrNull {
            it.name == "initialize" &&
                it.parameterTypes.size == 2 &&
                it.parameterTypes[0] == Context::class.java
        } ?: throw IllegalStateException("Keyman initialize method unavailable.")
        initializeMethod.invoke(null, context, inAppKeyboardType)

        val getKeyboardsMethod = managerClass.methods.firstOrNull {
            it.name == "getKeyboardsList" && it.parameterTypes.size == 1
        } ?: throw IllegalStateException("Keyman keyboard list unavailable.")

        val installedKeyboards = (getKeyboardsMethod.invoke(null, context) as? List<*>) ?: emptyList<Any>()
        val keyboardInfo = installedKeyboards.firstOrNull()
            ?: throw IllegalStateException("No Keyman keyboard package is installed yet.")

        val setDefaultKeyboard = managerClass.methods.firstOrNull {
            it.name == "setDefaultKeyboard" && it.parameterTypes.size == 1
        } ?: throw IllegalStateException("Keyman default keyboard method unavailable.")
        setDefaultKeyboard.invoke(null, keyboardInfo)

        val setKeyboard = managerClass.methods.firstOrNull {
            it.name == "setKeyboard" && it.parameterTypes.size == 1
        } ?: throw IllegalStateException("Keyman setKeyboard method unavailable.")
        setKeyboard.invoke(null, keyboardInfo)

        textViewClass.getConstructor(Context::class.java).newInstance(context).let { view ->
            view as View
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            (view as? android.widget.TextView)?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    onTextChanged(s?.toString().orEmpty())
                }

                override fun afterTextChanged(s: Editable?) = Unit
            })
            (view as? android.widget.TextView)?.hint = "Tap here to type with the Keyman keyboard"

            view
        }
    } catch (error: Throwable) {
        onError(error.message ?: "Keyman keyboard failed to initialize.")
        FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
