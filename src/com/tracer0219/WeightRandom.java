package com.tracer0219;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class WeightRandom<T> {

    private final List<T> items = new ArrayList<>();
    private double[] weights;

    public WeightRandom(List<ItemWithWeight<T>> itemsWithWeight) {
        this.calWeights(itemsWithWeight);
    }


    public void calWeights(List<ItemWithWeight<T>> itemsWithWeight) {
        items.clear();


        double originWeightSum = 0;
        for (ItemWithWeight<T> itemWithWeight : itemsWithWeight) {
            double weight = itemWithWeight.getWeight();
            if (weight <= 0) {
                continue;
            }

            items.add(itemWithWeight.getItem());
            if (Double.isInfinite(weight)) {
                weight = 10000.0D;
            }
            if (Double.isNaN(weight)) {
                weight = 1.0D;
            }
            originWeightSum += weight;
        }


        double[] actualWeightRatios = new double[items.size()];
        int index = 0;
        for (ItemWithWeight<T> itemWithWeight : itemsWithWeight) {
            double weight = itemWithWeight.getWeight();
            if (weight <= 0) {
                continue;
            }
            actualWeightRatios[index++] = weight / originWeightSum;
        }


        weights = new double[items.size()];
        double weightRangeStartPos = 0;
        for (int i = 0; i < index; i++) {
            weights[i] = weightRangeStartPos + actualWeightRatios[i];
            weightRangeStartPos += actualWeightRatios[i];
        }
    }


    public T choose() {
        double random = ThreadLocalRandom.current().nextDouble();
        int index = Arrays.binarySearch(weights, random);
        if (index < 0) {
            index = -index - 1;
        } else {
            return items.get(index);
        }

        if (index < weights.length && random < weights[index]) {
            return items.get(index);
        }


        return items.get(0);
    }

    public static class ItemWithWeight<T> {
        T item;
        double weight;

        public ItemWithWeight() {
        }

        public ItemWithWeight(T item, double weight) {
            this.item = item;
            this.weight = weight;
        }

        public T getItem() {
            return item;
        }

        public void setItem(T item) {
            this.item = item;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }

}