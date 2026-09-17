package me.redst.worldcenter.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.EntityType;

import java.util.Locale;

public final class Text {
    public static final TextColor ACCENT = NamedTextColor.AQUA;
    public static final TextColor LABEL = NamedTextColor.GRAY;
    public static final TextColor VALUE = NamedTextColor.WHITE;
    public static final TextColor ON = NamedTextColor.GREEN;
    public static final TextColor OFF = NamedTextColor.RED;
    public static final TextColor PROBLEM = NamedTextColor.RED;
    public static final TextColor BRANCH = NamedTextColor.DARK_GRAY;

    public static final String TEE = "\u251c\u2500 ";
    public static final String END = "\u2514\u2500 ";
    public static final String BAR = "\u2502  ";
    public static final String GAP = "   ";
    public static final String ARROW = " \u2192 ";

    private static final Component PREFIX = Component.text("[", BRANCH)
            .append(Component.text("WorldCenter", ACCENT))
            .append(Component.text("] ", BRANCH));

    private Text() {
    }

    public static Component info(String message) {
        return PREFIX.append(Component.text(message, VALUE));
    }

    public static Component problem(String message) {
        return PREFIX.append(Component.text(message, PROBLEM));
    }

    public static Component detail(String message) {
        return Component.text("  " + message, LABEL);
    }

    public static Component header(String title) {
        return Component.text(title, ACCENT);
    }

    public static Component branch(String indent, boolean last, String label, Component value) {
        Component line = Component.text(indent + (last ? END : TEE), BRANCH)
                .append(Component.text(label, LABEL));
        return value == null ? line : line.append(Component.text(": ", LABEL)).append(value);
    }

    public static Component onOff(boolean on) {
        return on ? Component.text("ON", ON) : Component.text("OFF", OFF);
    }

    public static Component value(String text) {
        return Component.text(text, VALUE);
    }

    public static String pretty(EntityType type) {
        String name = type.name().toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(name.length());
        boolean upper = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                out.append(' ');
                upper = true;
            } else {
                out.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return out.toString();
    }
}
