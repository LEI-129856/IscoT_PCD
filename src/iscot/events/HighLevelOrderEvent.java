package iscot.events;

import iscot.enums.Areas;

/** Uma ordem para os atuadores. Publicada pelos decisores, consumida pelos atuadores. */
public abstract class HighLevelOrderEvent extends Event {
    private static final long serialVersionUID = 1L;

    private final boolean on; // ligar (true) ou desligar (false) - única coisa em comum

    protected HighLevelOrderEvent(String senderId, Areas area, boolean on) {
        super(senderId, area);
        this.on = on;
    }

    public boolean isOn() {
        return on;
    }

    @Override
    public String toString() {
        return super.toString() + " | " + (on ? "ligar" : "desligar");
    }
}
