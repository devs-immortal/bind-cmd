package net.immortaldevs.bindcmd;

import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class CommandBinding {
    private static final KeyBinding.Category CATEGORY = new KeyBinding.Category(
            Identifier.of("bindcmd", "category")
    );

    public boolean wasPressed = false;
    public String command;

    private BindSource source = BindSource.CLIENT;
    private KeyBinding key;

    public CommandBinding(String command, KeyBinding key) {
        this.command = command;
        this.key = key;
    }

    public CommandBinding(String command) {
        this(command, new KeyBinding("key.keyboard.unknown", -1, CATEGORY));
    }

    public CommandBinding(String command, String translationKey, BindSource source) {
        this(command);
        this.command = command;
        this.source = source;
        InputUtil.Key keyFromTranslation = InputUtil.fromTranslationKey(translationKey);
        int keyCode = keyFromTranslation.getCode();
        InputUtil.Type type = translationKey.startsWith("key.mouse") ? InputUtil.Type.MOUSE : InputUtil.Type.KEYSYM;
        this.key = new KeyBinding(translationKey, type, keyCode, CATEGORY);
    }

    public CommandBinding(String command, String translationKey) {
        this(command, translationKey, BindSource.CLIENT);
    }

    public KeyBinding getKey() {
        return this.key;
    }

    public BindSource getSource() {
        return this.source;
    }

    public String getTranslationKey() {
        return this.key.getBoundKeyTranslationKey();
    }

    public boolean isUnknown() {
        return getTranslationKey().endsWith("unknown");
    }

    public boolean isPressed() {
        return this.key.isPressed();
    }

    public void setBoundKey(KeyInput keyInput) {
        unbind();
        InputUtil.Key key = keyInput.isEscape() ? InputUtil.UNKNOWN_KEY : InputUtil.fromKeyCode(keyInput);
        this.key = new KeyBinding(key.getTranslationKey(), key.getCode(), CATEGORY);
    }

    public void setBoundMouse(int button) {
        unbind();
        InputUtil.Key key = InputUtil.Type.MOUSE.createFromCode(button);
        this.key = new KeyBinding(key.getTranslationKey(), InputUtil.Type.MOUSE, key.getCode(), CATEGORY);
    }

    public void unbind() {
        KeyBinding.KEYS_BY_ID.remove(getTranslationKey());
        KeyBinding.updateKeysByCode();
    }
}
