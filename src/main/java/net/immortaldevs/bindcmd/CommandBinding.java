package net.immortaldevs.bindcmd;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;

public class CommandBinding {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath("bindcmd", "category")
    );

    public boolean wasPressed = false;
    public String command;

    private BindSource source = BindSource.CLIENT;
    private KeyMapping key;

    public CommandBinding(String command, KeyMapping key) {
        this.command = command;
        this.key = key;
    }

    public CommandBinding(String command) {
        this(command, new KeyMapping("key.keyboard.unknown", -1, CATEGORY));
    }

    public CommandBinding(String command, String translationKey, BindSource source) {
        this(command);
        this.command = command;
        this.source = source;
        InputConstants.Key keyFromTranslation = InputConstants.getKey(translationKey);
        int keyCode = keyFromTranslation.getValue();
        InputConstants.Type type = translationKey.startsWith("key.mouse") ? InputConstants.Type.MOUSE : InputConstants.Type.KEYSYM;
        this.key = new KeyMapping(translationKey, type, keyCode, CATEGORY);
    }

    public KeyMapping getKey() {
        return this.key;
    }

    public BindSource getSource() {
        return this.source;
    }

    public String getTranslationKey() {
        return this.key.saveString();
    }

    public boolean isUnknown() {
        return getTranslationKey().endsWith("unknown");
    }

    public boolean isPressed() {
        return key.isDown();
    }

    public void setBoundKey(KeyEvent keyInput) {
        unbind();
        InputConstants.Key inpuKey = keyInput.isEscape() ? InputConstants.UNKNOWN : InputConstants.getKey(keyInput);
        key = new KeyMapping(inpuKey.getName(), inpuKey.getValue(), CATEGORY);
    }

    public void setBoundMouse(int button) {
        unbind();
        InputConstants.Key inpuKey = InputConstants.Type.MOUSE.getOrCreate(button);
        key = new KeyMapping(inpuKey.getName(), InputConstants.Type.MOUSE, inpuKey.getValue(), CATEGORY);
    }

    public void unbind() {
        KeyMapping.ALL.remove(getTranslationKey());
        KeyMapping.resetMapping();
    }
}
