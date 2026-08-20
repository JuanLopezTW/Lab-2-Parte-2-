package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;

    private int health;

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private HighlanderPauseController pauseController;


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }

    public void setPauseController(HighlanderPauseController pauseController) {
        this.pauseController = pauseController;
    }

    public void run() {

        while (true) {
            Immortal im;

            try {
                pauseController.checkpoint();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            try {
                if (this.health <= 0) {
                    Thread.sleep(1);
                    continue;
                }

                int size = immortalsPopulation.size();
                if (size <= 1) {
                    Thread.sleep(1);
                    continue;
                }

                int myIndex = immortalsPopulation.indexOf(this);
                int nextFighterIndex = r.nextInt(size);

                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % size);
                }

                im = immortalsPopulation.get(nextFighterIndex);
                this.fight(im);

            } catch (IndexOutOfBoundsException ex) {

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }


            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

        }

    }

    public void fight(Immortal i2) {

        Immortal first = this;
        Immortal second = i2;

        if (this.name.compareTo(i2.name) > 0) {
            first = i2;
            second = this;
        }

        synchronized (first) {
            synchronized (second) {
                if (this.health <= 0){
                   return;
                }
                if (i2.getHealth() > 0) {
                    int newHealth = i2.getHealth() - defaultDamageValue;
                    i2.changeHealth(newHealth);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");

                    if (newHealth <= 0) {
                        immortalsPopulation.remove(i2);
                        updateCallback.processReport(i2 + " has died and was removed.\n");
                    }
                } else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                }
            }
        }

    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}