package com.darkona.feathers.client.gui;

public class Icons {

    static final Set NORMAL = new Set(GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(0, 1),
            GuiIcon.featherIcon(0, 2));
    static final Set COLD = new Set(GuiIcon.featherIcon(1, 0),
            GuiIcon.featherIcon(1, 1),
            GuiIcon.featherIcon(1, 2));
    static final Set HOT = new Set(GuiIcon.featherIcon(2, 0),
            GuiIcon.featherIcon(2, 1),
            GuiIcon.featherIcon(2, 2));
    static final Set GREEN = new Set(GuiIcon.featherIcon(3, 0),
            GuiIcon.featherIcon(3, 1),
            GuiIcon.featherIcon(3, 2));
    static final Set OVERFLOW = new Set(GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(4, 1),
            GuiIcon.featherIcon(4, 2));
    static final Set ENERGY = new Set(GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(5, 1),
            GuiIcon.featherIcon(5, 2));
    static final Set MOMENTUM = new Set(GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(6, 1),
            GuiIcon.featherIcon(6, 2));
    static final Set REGEN = new Set(GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(7, 1),
            GuiIcon.featherIcon(7, 2));
    static final Set STRAINED = new Set(GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(9, 1),
            GuiIcon.featherIcon(9, 2));
    static final Set ENDURANCE = new Set(GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(10, 1),
            GuiIcon.featherIcon(10, 2));
    static final Set ARMOR = new Set(GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(3, 4),
            GuiIcon.featherIcon(3, 3));
    static final GuiIcon REGEN_OVERLAY = GuiIcon.featherIcon(0, 3);
    static final GuiIcon COLD_OVERLAY = GuiIcon.featherIcon(1, 3);
    static final GuiIcon HOT_OVERLAY = GuiIcon.featherIcon(2, 3);

    record Set(GuiIcon background, GuiIcon half, GuiIcon full) {
    }
}
