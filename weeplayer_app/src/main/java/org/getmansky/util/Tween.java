/*
 * Copyright (C) 2015 Oleg Getmansky
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.getmansky.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author getmansky
 */
public class Tween {

    private Timer timer = new Timer();
    private Double currentValue;
    private boolean closed = false;

    public Tween(Double value) {
        currentValue = value;
        timer = new Timer();
    }

    public void tweenToZero(Double delta, Long interval, EventHandler<Double> valueHandler, EventHandler<Double> zeroHandler) {
        if (closed) {
            throw new IllegalStateException("This Tween is already closed. Create another instance");
        }
        closed = true;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Double nextValue = currentValue - delta;
                if (nextValue <= 0d) {
                    timer.cancel();
                    zeroHandler.handle(0d);
                } else {
                    currentValue = nextValue;
                    valueHandler.handle(currentValue);
                }
            }
        }, 0, interval);
    }

    public void tweenToValue(Double value, Double delta, Long interval, EventHandler<Double> valueHandler, EventHandler<Double> finalHandler) {
        if (closed) {
            throw new IllegalStateException("This Tween is already closed. Create another instance");
        }
        closed = true;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Double nextValue = currentValue + delta;
                if (nextValue >= value) {
                    timer.cancel();
                    finalHandler.handle(value);
                } else {
                    currentValue = nextValue;
                    valueHandler.handle(currentValue);
                }
            }
        }, 0, interval);
    }
}
