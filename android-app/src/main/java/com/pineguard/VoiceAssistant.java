package com.pineguard;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class VoiceAssistant {
    private TextToSpeech tts;

    public VoiceAssistant(Context context) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.CHINESE);
            }
        });
    }

    public void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void shutdown() { if (tts != null) tts.shutdown(); }
}
