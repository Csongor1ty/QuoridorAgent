//4gent47,tarjanyi.csongor@stud.u-szeged.hu

import game.quoridor.MoveAction;
import game.quoridor.QuoridorGame;
import game.quoridor.QuoridorPlayer;
import game.quoridor.WallAction;
import game.quoridor.players.DummyPlayer;
import game.quoridor.utils.PlaceObject;
import game.quoridor.utils.QuoridorAction;
import game.quoridor.utils.WallObject;

import java.util.*;

public class Agent extends QuoridorPlayer {

    private final List<WallObject> walls = new LinkedList<> ();
    private final QuoridorPlayer[] players = new QuoridorPlayer[2];
    private int numWalls;

    public Agent(int i, int j, int color, Random random) {
        super(i, j, color, random);
        players[color] = this; 									//color: 0 OR 1
        players[1-color] = new DummyPlayer ((1-color) * (QuoridorGame.HEIGHT - 1), j, 1-color, null);
        // ^ ellenfel
        numWalls = 0; // falak szama, lokalis tarolasra
    }
    /**
     * A* algoritmus, ahol:
     * Halmaz - open -> nyilt halmaz
     * Halmaz - closed -> zart halmaz
     * fv. - assume -> heurisztikahoz szukseges fuggveny, a vegpontok az ellenfel kezdovonalan talalhatok
     * Kulcs-ertek par - relation -> eppen vizsgalt node es szulojenek tarolasa
     * Kulcs-ertek par - cost -> eppen vizsgalt node koltsegenek tarolasa
     * Kulcs-ertek par - sum -> node ertekenek tarolasa (f(n) = g(n) + h(n))
     * algoritmus flowchart:
     *
     * kezdopont meghatarozasa, nyilt halmazba tetel
     *                  |
     *                  v
     * koltseg szamitasa f(n) = g(n) + h(n)
     *                  |
     *                  v
     * node kivetele a nyilt halmazbol, szomszedok kozul a legkisebb ertekut taroljuk
     *                ^  |
     *               |   v
     *              |    HA vegallapot - - - - -> [Terminate, alg. kesz, megfelelo node-ra ugras]
     *             |     |
     *            |      v
     *           |       ELSE nem zart halmazban levo szomszedok vizsgalata
     *          |        |
     *         |         v
     *        L - - - -  emlitett szomszedok koltsegszamitasa
     */
    private int goals(){
        return (1-color) * (QuoridorGame.HEIGHT - 1);
    }
    private int assume(PlaceObject playerPos){
        return Math.abs((goals ()) - playerPos.i);
    }

    private int[] aStarSearch(int currentPos_i, int currentPos_j){

        Set<PlaceObject> open = new HashSet<> ();
        Set <PlaceObject> closed = new HashSet<> ();

        Map<PlaceObject, PlaceObject> relation = new HashMap<> ();
        Map<PlaceObject, Integer> cost = new HashMap<> ();
        Map<PlaceObject, Integer> sum = new HashMap<> ();
        PlaceObject currentNode = new PlaceObject (currentPos_i, currentPos_j);

        /** 1. lepes - kezdoallapot nyilt halmazba tetele */
        open.add(currentNode);

        /** 2. lepes - ertek szamitasa, f(n) = g(n) + h(n)*/
        relation.put (currentNode, null);
        cost.put (currentNode, 0);
        sum.put (currentNode, cost.get (currentNode) + assume (currentNode));

        /** 3. lepes - legkisebb erteku node megkeresese...*/
        while (open.size () > 0) {

            int minimize = Integer.MAX_VALUE;

            for(Map.Entry<PlaceObject, Integer> entry: sum.entrySet ()){
                if(open.contains (entry.getKey ())){
                    minimize = Math.min (minimize, entry.getValue ());
                }
            }

        /** ... es eltarolasa*/
        PlaceObject opt = null;
        for(Map.Entry<PlaceObject, Integer> entry : sum.entrySet ()) {
            if (entry.getValue () == minimize && open.contains (entry.getKey ())){
                opt = entry.getKey ();
                break;
            }
        }

        /** 4. lepes - ha a kereses vegallapothoz er, megszamoljuk a lepeseket es visszavezetjuk a lepeseket az elso,
         * legjobb lepesre, amivel visszaterunk*/
            if (opt != null && goals () == opt.i) {
                while (relation.get (opt) != currentNode) {
                    opt = relation.get (opt);
                }
                return new int[]{opt.i, opt.j};
            }

            /** 5. lepes - node kivetele a nyilt halmazbol es hozzaadasa a zart halmazhoz*/
            closed.add(opt);
            open.remove (opt);

            List<PlaceObject> adjNodes = null;
            if (opt != null) {
                adjNodes = opt.getNeighbors (walls, players);
            }

            /** max melyseg miatt felvesszuk azokat a node-okat is, amik egyik halmaznak sem tagjai*/

            if (adjNodes != null) {
                for(PlaceObject unDecided : adjNodes){
                    if (!closed.contains (unDecided) && !open.contains (unDecided)){
                        open.add(unDecided);
                        relation.put(unDecided, opt);
                        cost.put(unDecided, cost.get(opt) + 1);
                        sum.put(unDecided, cost.get (unDecided) + assume (unDecided));
                    }
                }
            }
        }
        return null;
    }

    @Override
    public QuoridorAction getAction(QuoridorAction prevAction, long[] remainingTimes) {

    /** AKCIOK:
     * 1. - ellenfel lepesenek rogzitese */
        if (prevAction instanceof WallAction) {
            WallAction a = (WallAction) prevAction;
            walls.add(new WallObject(a.i, a.j, a.horizontal));
        } else if (prevAction instanceof MoveAction) {
            MoveAction a = (MoveAction) prevAction;
            players[1 - color].i = a.to_i;
            players[1 - color].j = a.to_j;
        }

    //Kovetkezo akcio eldontese
    int di = (color * (QuoridorGame.HEIGHT - 1)) - players[1-color].i < 0 ? -1 : 0;

        List<WallObject> wallObjects = new LinkedList<WallObject>();
        wallObjects.add(new WallObject(players[1-color].i + di, players[1-color].j - color, true));
        wallObjects.add(new WallObject(players[1-color].i + di, players[1-color].j - 1 + color, true));
        wallObjects.add(new WallObject(players[1-color].i + di, players[1-color].j - color, false));
        wallObjects.add(new WallObject(players[1-color].i + di, players[1-color].j - 1 + color, false));
        for (WallObject wall : wallObjects) {
            if (numWalls < QuoridorGame.MAX_WALLS && QuoridorGame.checkWall(wall, walls, players)) {
                numWalls ++;
                walls.add(wall);
                return wall.toWallAction();
            }
        }

    /** 2. - A* algoritmus alapjan szamitott lepes*/
       int[] optLepes = aStarSearch (players[color].i, players[color].j);
        return new MoveAction(this.i, this.j, optLepes != null ? optLepes[0] : 0, optLepes != null ? optLepes[1] : 0);
    }
}

/**
 * Felhasznalt irodalmak, melyek a munkat inspiraltak:
 *
 * http://www.inf.u-szeged.hu/~ihegedus/teach/mi.html.pdf
 * http://www.inf.u-szeged.hu/~dobo/MI1_gyak_jegyzet_2013-2014_I._felev.pdf
 * https://towardsdatascience.com/a-star-a-search-algorithm-eb495fb156bb
 * https://gist.github.com/ryancollingwood/32446307e976a11a1185a5394d6657bc
 * https://medium.com/@nicholas.w.swift/easy-a-star-pathfinding-7e6689c7f7b2
 * https://www.youtube.com/watch?v=mZfyt03LDH4&list=PLFt_AvWsXl0cq5Umv3pMC9SPnKjfp9eGW&index=3
 * https://docplayer.hu/203316842-Zeroosszegu-ketszemelyes-jatekok.html
 * stackoverflow.com
 * */