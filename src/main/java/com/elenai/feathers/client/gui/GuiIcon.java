package com.elenai.feathers.client.gui;

public record GuiIcon(int x, int y, int width, int height) {

    public static GuiIcon featherIcon(int x, int y) {
        return new GuiIcon((x * 9) - 1, (y * 9) - 1, 9, 9);
    }

}
