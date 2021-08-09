package com.github.mrmks.mc.template;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class WordStock {

    private static final Random rg = new Random();
    private static final int[] emptyInt = new int[0];
    private static final String[] emptyStr = new String[0];

    private HashMap<String, Word> map = new HashMap<>();
    private LinkedList<Word> list;
    private String[] strAry;
    private int[] intAry;
    private int size = -1, weightSum = -1;

    public String getWord() {
        if (strAry == null || intAry == null || size < 0 || weightSum < 0) bake();
        if (size == 0) return "";
        else if (size == 1) return strAry[0];
        int weight = rg.nextInt(weightSum);
        int b = 0, e = intAry.length - 1;
        if (weight < intAry[b]) return strAry[b];
        if (weight >= intAry[e]) return strAry[e + 1];
        while ((e - b) > 1) {
            int index = (b + e) / 2;
            if (weight >= intAry[index]) {
                b = index;
            } else {
                e = index;
            }
        }
        return strAry[b + 1];
    }

    public void addWord(int weight, String word) {
        if (word == null || weight <= 0) return;
        if (list == null) list = new LinkedList<>();
        Word w = new Word();
        w.word = word;
        w.weight = weight;
        this.list.add(w);
        weightSum = -1;
    }

    private void bake() {
        size = 0;
        weightSum = 0;
        if (list == null || list.isEmpty()) {
            strAry = emptyStr;
            intAry = emptyInt;
        }
        else {
            list.removeIf(w->w == null || w.word == null || w.weight <= 0);

            Iterator<Word> iterator = list.iterator();
            while (iterator.hasNext()) {
                Word w = iterator.next();
                if (map.containsKey(w.word)) {
                    map.get(w.word).weight += w.weight;
                    iterator.remove();
                } else map.put(w.word, w);
            }
            map.clear();

            size = list.size();
            strAry = new String[size];
            intAry = new int[size - 1];
            for (int i = 0; i < size; i++) {
                Word w = list.get(i);
                weightSum += w.weight;
                strAry[i] = w.word;
                if (i > 0) intAry[i - 1] = weightSum;
            }
        }
    }

    private static class Word {
        int weight = 1;
        String word;
    }
}
