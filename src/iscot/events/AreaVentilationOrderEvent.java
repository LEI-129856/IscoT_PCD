package iscot.events;

import iscot.enums.Areas;

/** Ordem para a ventilação de uma divisão. Só precisa de saber se liga ou desliga. */
public class AreaVentilationOrderEvent extends HighLevelOrderEvent {
    private static final long serialVersionUID = 1L;

    public AreaVentilationOrderEvent(String senderId, Areas area, boolean on) {
        super(senderId, area, on);
    }
}
