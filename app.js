const PHRASEBOOK = Object.freeze({
  "hello": "ሰላም",
  "hi": "ሰላም",
  "good morning": "እንደምን አደርክ",
  "good afternoon": "እንደምን አለህ",
  "good evening": "እንደምን አመሸህ",
  "good night": "መልካም ምሽት",
  "thank you": "አመሰግናለሁ",
  "please": "እባክህ",
  "yes": "አዎ",
  "no": "አይ",
  "how are you": "እንዴት ነህ",
  "what is your name": "ስምህ ማን ነው",
  "my name is": "ስሜ",
  "welcome": "እንኳን ደህና መጣህ",
  "sorry": "ይቅርታ",
  "i love you": "እወድሃለሁ",
  "water": "ውሃ",
  "food": "ምግብ",
  "coffee": "ቡና",
  "tea": "ሻይ",
  "bread": "ዳቦ",
  "home": "ቤት",
  "school": "ትምህርት ቤት",
  "help": "እርዳታ",
  "where is the market": "ገበያው የት ነው",
  "today": "ዛሬ",
  "tomorrow": "ነገ",
  "yesterday": "ትናንት",
  "i am happy": "ደስ ብሎኛል"
});

const WORD_HINTS = Object.freeze({
  "amharic": "አማርኛ",
  "ethiopia": "ኢትዮጵያ",
  "ethiopian": "ኢትዮጵያዊ",
  "addis": "አዲስ",
  "ababa": "አበባ",
  "selam": "ሰላም",
  "coffee": "ቡና",
  "water": "ውሃ",
  "bread": "ዳቦ",
  "food": "ምግብ",
  "school": "ትምህርት ቤት",
  "market": "ገበያ",
  "home": "ቤት",
  "today": "ዛሬ",
  "tomorrow": "ነገ",
  "yesterday": "ትናንት",
  "help": "እርዳታ",
  "friend": "ጓደኛ",
  "city": "ከተማ",
  "name": "ስም",
  "i": "እኔ",
  "you": "አንተ",
  "we": "እኛ",
  "they": "እነርሱ",
  "and": "እና",
  "or": "ወይም",
  "with": "ጋር",
  "for": "ለ",
  "in": "ውስጥ",
  "to": "ወደ",
  "is": "ነው",
  "are": "ናቸው",
  "my": "የኔ",
  "your": "የአንተ"
});

const SYLLABLE_RULES = [
  ["hue", "ኃ"],
  ["hui", "ኅ"],
  ["hua", "ኋ"],
  ["hee", "ሄ"],
  ["shee", "ሼ"],
  ["chee", "ቼ"],
  ["qee", "ቄ"],
  ["gee", "ጌ"],
  ["kee", "ኬ"],
  ["mee", "ሜ"],
  ["nee", "ኔ"],
  ["ree", "ሬ"],
  ["see", "ሴ"],
  ["tee", "ቴ"],
  ["dee", "ዴ"],
  ["lee", "ሌ"],
  ["bee", "ቤ"],
  ["fee", "ፌ"],
  ["pee", "ፔ"],
  ["vee", "ቬ"],
  ["wee", "ዌ"],
  ["yee", "ዬ"],
  ["zee", "ዜ"],
  ["jee", "ጄ"],
  ["xee", "ኼ"],
  ["he", "ሀ"],
  ["hu", "ሁ"],
  ["hi", "ሂ"],
  ["ha", "ሃ"],
  ["ho", "ሆ"],
  ["h", "ህ"],
  ["she", "ሸ"],
  ["shu", "ሹ"],
  ["shi", "ሺ"],
  ["sha", "ሻ"],
  ["sh", "ሽ"],
  ["sho", "ሾ"],
  ["che", "ቸ"],
  ["chu", "ቹ"],
  ["chi", "ቺ"],
  ["cha", "ቻ"],
  ["ch", "ች"],
  ["cho", "ቾ"],
  ["qe", "ቀ"],
  ["qu", "ቁ"],
  ["qi", "ቂ"],
  ["qa", "ቃ"],
  ["q", "ቅ"],
  ["qo", "ቆ"],
  ["ge", "ገ"],
  ["gu", "ጉ"],
  ["gi", "ጊ"],
  ["ga", "ጋ"],
  ["g", "ግ"],
  ["go", "ጎ"],
  ["ke", "ከ"],
  ["ku", "ኩ"],
  ["ki", "ኪ"],
  ["ka", "ካ"],
  ["k", "ክ"],
  ["ko", "ኮ"],
  ["me", "መ"],
  ["mu", "ሙ"],
  ["mi", "ሚ"],
  ["ma", "ማ"],
  ["m", "ም"],
  ["mo", "ሞ"],
  ["ne", "ነ"],
  ["nu", "ኑ"],
  ["ni", "ኒ"],
  ["na", "ና"],
  ["n", "ን"],
  ["no", "ኖ"],
  ["re", "ረ"],
  ["ru", "ሩ"],
  ["ri", "ሪ"],
  ["ra", "ራ"],
  ["r", "ር"],
  ["ro", "ሮ"],
  ["se", "ሰ"],
  ["su", "ሱ"],
  ["si", "ሲ"],
  ["sa", "ሳ"],
  ["s", "ስ"],
  ["so", "ሶ"],
  ["te", "ተ"],
  ["tu", "ቱ"],
  ["ti", "ቲ"],
  ["ta", "ታ"],
  ["t", "ት"],
  ["to", "ቶ"],
  ["de", "ደ"],
  ["du", "ዱ"],
  ["di", "ዲ"],
  ["da", "ዳ"],
  ["d", "ድ"],
  ["do", "ዶ"],
  ["le", "ለ"],
  ["lu", "ሉ"],
  ["li", "ሊ"],
  ["la", "ላ"],
  ["l", "ል"],
  ["lo", "ሎ"],
  ["be", "በ"],
  ["bu", "ቡ"],
  ["bi", "ቢ"],
  ["ba", "ባ"],
  ["bua", "ቧ"],
  ["b", "ብ"],
  ["bo", "ቦ"],
  ["fe", "ፈ"],
  ["fu", "ፉ"],
  ["fi", "ፊ"],
  ["fa", "ፋ"],
  ["f", "ፍ"],
  ["fo", "ፎ"],
  ["pe", "ፐ"],
  ["pu", "ፑ"],
  ["pi", "ፒ"],
  ["pa", "ፓ"],
  ["p", "ፕ"],
  ["po", "ፖ"],
  ["ve", "ቨ"],
  ["vu", "ቩ"],
  ["vi", "ቪ"],
  ["va", "ቫ"],
  ["v", "ቭ"],
  ["vo", "ቮ"],
  ["we", "ወ"],
  ["wu", "ዉ"],
  ["wi", "ዊ"],
  ["wa", "ዋ"],
  ["w", "ው"],
  ["wo", "ዎ"],
  ["ye", "የ"],
  ["yu", "ዩ"],
  ["yi", "ዪ"],
  ["ya", "ያ"],
  ["y", "ይ"],
  ["yo", "ዮ"],
  ["ze", "ዘ"],
  ["zu", "ዙ"],
  ["zi", "ዚ"],
  ["za", "ዛ"],
  ["z", "ዝ"],
  ["zo", "ዞ"],
  ["je", "ጀ"],
  ["ju", "ጁ"],
  ["ji", "ጂ"],
  ["ja", "ጃ"],
  ["j", "ጅ"],
  ["jo", "ጆ"],
  ["xe", "ኸ"],
  ["xu", "ኹ"],
  ["xi", "ኺ"],
  ["xa", "ኻ"],
  ["x", "ኽ"],
  ["xo", "ኾ"],
  ["aee", "ኤ"],
  ["ae", "አ"],
  ["au", "ኡ"],
  ["ai", "ኢ"],
  ["aa", "ኣ"],
  ["ao", "ኦ"],
  ["a", "እ"]
];

const EXAMPLES = [
  "hello",
  "good morning",
  "thank you",
  "where is the market",
  "i am happy",
  "coffee",
  "please"
];

const KEYBOARD_EXAMPLES = [
  "he",
  "hu",
  "hi",
  "ha",
  "hee",
  "h",
  "ho",
  "hua",
  "ae",
  "au",
  "ai",
  "aa"
];

const ui = {
  offlineStatus: document.getElementById("offlineStatus"),
  translatorInput: document.getElementById("translatorInput"),
  translatorOutput: document.getElementById("translatorOutput"),
  translationMode: document.getElementById("translationMode"),
  translationConfidence: document.getElementById("translationConfidence"),
  copyTranslatorOutput: document.getElementById("copyTranslatorOutput"),
  clearTranslatorInput: document.getElementById("clearTranslatorInput"),
  translatorExamples: document.getElementById("translatorExamples"),
  keyboardInput: document.getElementById("keyboardInput"),
  typingOutput: document.getElementById("typingOutput"),
  typingTokenLabel: document.getElementById("typingTokenLabel"),
  keyboardExamples: document.getElementById("keyboardExamples"),
  typingSuggestions: document.getElementById("typingSuggestions")
};

function normalizePhrase(value) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^\p{L}\p{N}\s']/gu, "")
    .replace(/\s+/g, " ");
}

function tokenize(value) {
  return value.match(/[A-Za-z']+|[^A-Za-z']+/g) ?? [];
}

function transliterateWord(word) {
  const lower = word.toLowerCase();
  let result = "";
  let index = 0;

  while (index < lower.length) {
    let matched = false;

    for (const [latin, amharic] of SYLLABLE_RULES) {
      if (lower.startsWith(latin, index)) {
        result += amharic;
        index += latin.length;
        matched = true;
        break;
      }
    }

    if (!matched) {
      const current = lower[index];

      if (current === "'") {
        index += 1;
        continue;
      }

      if (current === "-") {
        result += " ";
      } else {
        result += current;
      }

      index += 1;
    }
  }

  return result;
}

function translateText(input) {
  const normalized = normalizePhrase(input);

  if (!normalized) {
    return {
      output: "",
      mode: "Waiting for input",
      confidence: "Prototype",
      source: "Local data"
    };
  }

  if (PHRASEBOOK[normalized]) {
    return {
      output: PHRASEBOOK[normalized],
      mode: "Phrasebook match",
      confidence: "High confidence",
      source: "Local phrasebook"
    };
  }

  const parts = tokenize(input);
  const output = parts
    .map((part) => {
      if (!/^[A-Za-z']+$/.test(part)) {
        return part;
      }

      const word = part.toLowerCase();

      if (WORD_HINTS[word]) {
        return WORD_HINTS[word];
      }

      return transliterateWord(word);
    })
    .join("")
    .trim();

  return {
    output,
    mode: "Transliteration fallback",
    confidence: "Prototype preview",
    source: "Local fallback"
  };
}

function getCurrentToken(value) {
  const matches = value.toLowerCase().match(/[A-Za-z']+/g);
  return matches?.at(-1) ?? "";
}

function buildTypingSuggestions(prefix) {
  const normalized = prefix.toLowerCase().trim();

  if (!normalized) {
    return SYLLABLE_RULES.slice(0, 10).map(([latin, amharic]) => ({
      latin,
      amharic,
      label: "Try",
      kind: "starter"
    }));
  }

  const phraseMatches = Object.entries(PHRASEBOOK)
    .filter(([phrase]) => phrase.startsWith(normalized))
    .slice(0, 4)
    .map(([latin, amharic]) => ({
      latin,
      amharic,
      label: "Phrase",
      kind: "phrase"
    }));

  const wordMatches = Object.entries(WORD_HINTS)
    .filter(([word]) => word.startsWith(normalized))
    .slice(0, 4)
    .map(([latin, amharic]) => ({
      latin,
      amharic,
      label: "Word",
      kind: "word"
    }));

  const syllableMatches = SYLLABLE_RULES.filter(([latin]) => latin.startsWith(normalized))
    .slice(0, 6)
    .map(([latin, amharic]) => ({
      latin,
      amharic,
      label: "Syllable",
      kind: "syllable"
    }));

  const combined = [...phraseMatches, ...wordMatches, ...syllableMatches];
  const seen = new Set();

  return combined.filter((item) => {
    const key = `${item.latin}:${item.amharic}`;
    if (seen.has(key)) {
      return false;
    }

    seen.add(key);
    return true;
  });
}

function renderChips(container, items, onPick) {
  container.innerHTML = "";

  if (!items.length) {
    const empty = document.createElement("span");
    empty.className = "muted";
    empty.textContent = "No suggestions yet.";
    container.appendChild(empty);
    return;
  }

  for (const item of items) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "chip";
    button.textContent = `${item.latin} → ${item.amharic}`;
    button.title = `${item.label}: ${item.latin} to ${item.amharic}`;
    button.addEventListener("click", () => onPick(item));
    container.appendChild(button);
  }
}

function renderTranslatorExamples() {
  renderChips(ui.translatorExamples, EXAMPLES.map((text) => ({
    latin: text,
    amharic: PHRASEBOOK[normalizePhrase(text)] ?? translateText(text).output
  })), (item) => {
    ui.translatorInput.value = item.latin;
    updateTranslator();
    ui.translatorInput.focus();
  });
}

function renderKeyboardExamples() {
  renderChips(ui.keyboardExamples, KEYBOARD_EXAMPLES.map((text) => ({
    latin: text,
    amharic: transliterateWord(text)
  })), (item) => {
    ui.keyboardInput.value = item.latin;
    updateKeyboardLab();
    ui.keyboardInput.focus();
  });
}

function updateTranslator() {
  const { output, mode, confidence, source } = translateText(ui.translatorInput.value);
  ui.translatorOutput.textContent = output || "Your Amharic output will appear here.";
  ui.translatorOutput.classList.toggle("muted", !output);
  ui.translationMode.textContent = mode;
  ui.translationConfidence.textContent = confidence;
  ui.copyTranslatorOutput.dataset.copyText = output;
  ui.copyTranslatorOutput.disabled = !output;

  if (!output) {
    ui.translationConfidence.textContent = "Prototype";
    return;
  }

  ui.translationConfidence.title = source;
}

function updateKeyboardLab() {
  const value = ui.keyboardInput.value;
  const currentToken = getCurrentToken(value);
  const transliterated = value ? transliterateWord(value.replace(/\s+/g, " ").trim()) : "";

  ui.typingOutput.textContent = transliterated || "Live transliteration preview will appear here.";
  ui.typingOutput.classList.toggle("muted", !transliterated);
  ui.typingTokenLabel.textContent = currentToken ? `Token: ${currentToken}` : "No token yet";

  const suggestions = buildTypingSuggestions(value);
  renderChips(ui.typingSuggestions, suggestions, (item) => {
    ui.keyboardInput.value = item.latin;
    updateKeyboardLab();
    ui.keyboardInput.focus();
  });
}

async function copyToClipboard(text) {
  if (!text) {
    return;
  }

  try {
    await navigator.clipboard.writeText(text);
    ui.copyTranslatorOutput.textContent = "Copied";
    setTimeout(() => {
      ui.copyTranslatorOutput.textContent = "Copy Amharic";
    }, 1200);
  } catch {
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.setAttribute("readonly", "");
    textarea.style.position = "absolute";
    textarea.style.left = "-9999px";
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    textarea.remove();
  }
}

function updateOfflineStatus() {
  const online = navigator.onLine;
  ui.offlineStatus.textContent = online ? "Ready offline and online" : "Running offline";
}

function registerServiceWorker() {
  if (!("serviceWorker" in navigator)) {
    return;
  }

  window.addEventListener("load", async () => {
    try {
      await navigator.serviceWorker.register("sw.js");
    } catch {
      // The prototype still works without the service worker.
    }
  });
}

ui.translatorInput.addEventListener("input", updateTranslator);
ui.keyboardInput.addEventListener("input", updateKeyboardLab);
ui.copyTranslatorOutput.addEventListener("click", () => copyToClipboard(ui.copyTranslatorOutput.dataset.copyText ?? ""));
ui.clearTranslatorInput.addEventListener("click", () => {
  ui.translatorInput.value = "";
  updateTranslator();
  ui.translatorInput.focus();
});

window.addEventListener("online", updateOfflineStatus);
window.addEventListener("offline", updateOfflineStatus);

renderTranslatorExamples();
renderKeyboardExamples();
updateTranslator();
updateKeyboardLab();
updateOfflineStatus();
registerServiceWorker();
