package net.novucs.ftop.gui;

import com.google.common.collect.ImmutableList;
import net.novucs.ftop.gui.element.GuiElement;

public class GuiLayout {

    private final ImmutableList<GuiElement> elements;
    private final int entriesPerPage;

    public GuiLayout(ImmutableList<GuiElement> elements, int entriesPerPage) {
        this.elements = elements;
        this.entriesPerPage = entriesPerPage;
    }

    public int getEntriesPerPage() {
        return entriesPerPage;
    }

    public void render(GuiContext context) {
        elements.forEach(element -> element.render(context));
    }
}
