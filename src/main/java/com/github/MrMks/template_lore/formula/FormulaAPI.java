package com.github.MrMks.template_lore.formula;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FormulaAPI {
    private static Pattern patternE = Pattern.compile("(?:(-?\\d+(?:\\.\\d+)?)([+\\-*/\\\\]))*(-?\\d+(?:\\.\\d+)?)");
    private static Pattern pattern = Pattern.compile("(round)?\\(([^()]+)\\)");

    public static Number cal(String str) {
        String tar = str.replaceAll("\\s", "");
        Matcher matcher;
        boolean dealt;
        try {
            do {
                dealt = false;
                matcher = pattern.matcher(tar);
                while (matcher.find()) {
                    Number number = calNonGroup(matcher.group(2));
                    if (matcher.group(1) != null && matcher.group(1).equals("round")) {
                        number = Math.round(number.doubleValue());
                    }
                    tar = matcher.replaceFirst(number.toString());
                    dealt = true;
                }
            } while (dealt);
            return calNonGroup(tar);
        } catch (Throwable tr) {
            tr.printStackTrace();
            return 0;
        }
    }

    private static Number calNonGroup(String str) {
        Matcher matcher = patternE.matcher(str);
        if (matcher.find()) {
            String[] list = str.split("(?<=\\d)[+\\-*/\\\\]");
            OperateSymbol[] oList = new OperateSymbol[list.length - 1];
            int last = 0;
            for (int i = 0; i < list.length - 1; i++) {
                last += list[i].length() + 1;
                oList[i] = getSym(str.charAt(last - 1));
            }
            List<Double> numbers = Arrays.stream(list).map(FormulaAPI::strToDouble).collect(Collectors.toList());
            List<OperateSymbol> opers = new ArrayList<>(Arrays.asList(oList));
            ListIterator<OperateSymbol> iterator = opers.listIterator();
            while (iterator.hasNext()) {
                switch (iterator.next()) {
                    case MULTIPLIER:
                        numbers.set(iterator.nextIndex() - 1, numbers.get(iterator.nextIndex() - 1) * numbers.remove(iterator.nextIndex()));
                        iterator.remove();
                        break;
                    case DIVIDE:
                        numbers.set(iterator.nextIndex() - 1, numbers.get(iterator.nextIndex() - 1) / numbers.remove(iterator.nextIndex()));
                        iterator.remove();
                        break;
                }
            }
            iterator = opers.listIterator();
            while (iterator.hasNext()) {
                if (iterator.next().equals(OperateSymbol.MOD)) {
                    numbers.set(iterator.nextIndex() - 1, numbers.get(iterator.nextIndex() - 1) % numbers.remove(iterator.nextIndex()));
                    iterator.remove();
                }
            }
            iterator = opers.listIterator();
            while (iterator.hasNext()) {
                switch (iterator.next()) {
                    case ADD:
                        numbers.set(iterator.nextIndex() - 1, numbers.get(iterator.nextIndex() - 1) + numbers.remove(iterator.nextIndex()));
                        iterator.remove();
                        break;
                    case SUBTRACT:
                        numbers.set(iterator.nextIndex() - 1, numbers.get(iterator.nextIndex() - 1) - numbers.remove(iterator.nextIndex()));
                        iterator.remove();
                        break;
                }
            }
            return numbers.get(0);
        }
        return 0;
    }

    private static double strToDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e){
            return 0;
        }
    }

    private static OperateSymbol getSym(char c) {
        switch (c) {
            default:
            case '+':
                return OperateSymbol.ADD;
            case '-':
                return OperateSymbol.SUBTRACT;
            case '*':
                return OperateSymbol.MULTIPLIER;
            case '/':
                return OperateSymbol.DIVIDE;
            case '\\':
                return OperateSymbol.MOD;
        }
    }

    private enum OperateSymbol {
        ADD, SUBTRACT, DIVIDE, MULTIPLIER, MOD
    }

}
