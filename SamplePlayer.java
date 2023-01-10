import java.util.Random;

import game.quoridor.QuoridorPlayer;
import game.quoridor.utils.QuoridorAction;

public class SamplePlayer extends QuoridorPlayer {
    public SamplePlayer(int i, int j, int color, Random random) {
        super(i, j, color, random);
    }

    @Override
    public QuoridorAction getAction(QuoridorAction prevAction, long[] remainingTimes) {
        return null;
    }
}
