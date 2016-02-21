package edu.cwru.eecs395_s16.core.actions;

import edu.cwru.eecs395_s16.interfaces.objects.GameAction;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.networking.requests.gameactions.MoveGameActionData;

import java.util.List;

/**
 * Created by james on 2/21/16.
 */
public class MoveGameAction implements GameAction {

    private MoveGameActionData data;

    public MoveGameAction(MoveGameActionData data){
        this.data = data;
    }


    @Override
    public boolean canDoAction(GameMap map, List<GameObject> boardObjects) {
        return false;
    }

    @Override
    public void doGameAction(GameMap map, List<GameObject> boardObjects) {

    }
}